package com.salescope.batch.product;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.salescope.batch.parser.TransactionType;

/**
 * 상품코드별 정상 매출, 취소 금액,
 * 순판매 수량 및 순매출을 집계한다.
 *
 * 출력 TSV 컬럼 순서:
 *
 * 1. stock_code
 * 2. product_name
 * 3. sales_order_count
 * 4. sold_quantity
 * 5. gross_sales
 * 6. cancel_order_count
 * 7. cancel_quantity
 * 8. cancel_amount
 * 9. net_quantity
 * 10. net_sales
 */
public class ProductSalesReducer
        extends Reducer<
                Text,
                Text,
                Text,
                NullWritable> {

    /*
     * AbstractRetailMapper의 내부 Value 구분자다.
     *
     * 실제 전달 형식:
     * InvoiceNo|TransactionType|Quantity
     * |AbsoluteAmount|Description
     */
    private static final String
            VALUE_SEPARATOR_REGEX = "\\|";

    private static final int
            VALUE_FIELD_COUNT = 5;

    @Override
    protected void reduce(
            Text key,
            Iterable<Text> values,
            Context context)
            throws IOException, InterruptedException {

        /*
         * 주문 수는 거래 행 수가 아니라
         * 서로 다른 InvoiceNo 개수로 계산한다.
         */
        Set<String> salesInvoices =
                new HashSet<String>();

        Set<String> cancelInvoices =
                new HashSet<String>();

        /*
         * 동일 StockCode에 여러 Description이
         * 존재할 수 있으므로 상품명별 등장 횟수를 센다.
         */
        Map<String, Integer>
                productNameFrequency =
                new HashMap<String, Integer>();

        long soldQuantity = 0L;
        long cancelQuantity = 0L;

        BigDecimal grossSales =
                BigDecimal.ZERO;

        BigDecimal cancelAmount =
                BigDecimal.ZERO;

        for (Text value : values) {

            MapperValue record =
                    MapperValue.parse(
                            value.toString()
                    );

            countProductName(
                    productNameFrequency,
                    record.description
            );

            if (record.type
                    == TransactionType.NORMAL) {

                salesInvoices.add(
                        record.invoiceNo
                );

                soldQuantity +=
                        record.quantity;

                grossSales =
                        grossSales.add(
                                record.amount
                        );

            } else if (record.type
                    == TransactionType.CANCELLED) {

                cancelInvoices.add(
                        record.invoiceNo
                );

                cancelQuantity +=
                        Math.abs(
                                record.quantity
                        );

                cancelAmount =
                        cancelAmount.add(
                                record.amount.abs()
                        );
            }
        }

        long netQuantity =
                soldQuantity - cancelQuantity;

        BigDecimal netSales =
                grossSales.subtract(
                        cancelAmount
                );

        String representativeProductName =
                selectRepresentativeProductName(
                        productNameFrequency
                );

        String outputLine =
                key.toString()
                + "\t"
                + representativeProductName
                + "\t"
                + salesInvoices.size()
                + "\t"
                + soldQuantity
                + "\t"
                + scale2(grossSales)
                + "\t"
                + cancelInvoices.size()
                + "\t"
                + cancelQuantity
                + "\t"
                + scale2(cancelAmount)
                + "\t"
                + netQuantity
                + "\t"
                + scale2(netSales);

        context.write(
                new Text(outputLine),
                NullWritable.get()
        );
    }

    /**
     * 비어 있지 않은 상품명의 등장 횟수를 센다.
     *
     * 탭과 개행은 최종 TSV 컬럼을 깨뜨리므로
     * 집계 전에 공백으로 치환한다.
     */
    private void countProductName(
            Map<String, Integer> frequency,
            String description) {

        String productName =
                sanitizeForTsv(description);

        if (productName.isEmpty()) {
            return;
        }

        Integer currentCount =
                frequency.get(productName);

        if (currentCount == null) {

            frequency.put(
                    productName,
                    Integer.valueOf(1)
            );

        } else {

            frequency.put(
                    productName,
                    Integer.valueOf(
                            currentCount.intValue() + 1
                    )
            );
        }
    }

    /**
     * 대표 상품명 선정 규칙:
     *
     * 1. 가장 많이 등장한 상품명
     * 2. 등장 횟수가 같으면 사전순으로 앞선 상품명
     *
     * HashMap 순회 순서에 영향을 받지 않으므로
     * 같은 데이터는 항상 같은 상품명을 선택한다.
     */
    private String selectRepresentativeProductName(
            Map<String, Integer> frequency) {

        String bestName = "";
        int bestCount = 0;

        for (
            Map.Entry<String, Integer> entry
                : frequency.entrySet()
        ) {

            String candidateName =
                    entry.getKey();

            int candidateCount =
                    entry.getValue().intValue();

            boolean higherFrequency =
                    candidateCount > bestCount;

            boolean sameFrequencyAndEarlierName =
                    candidateCount == bestCount
                    && (
                        bestName.isEmpty()
                        || candidateName.compareTo(
                                bestName
                           ) < 0
                    );

            if (higherFrequency
                    || sameFrequencyAndEarlierName) {

                bestName = candidateName;
                bestCount = candidateCount;
            }
        }

        return bestName;
    }

    /**
     * 상품명이 TSV 컬럼 구조를 깨뜨리지 않도록
     * 탭과 개행을 공백으로 변환한다.
     *
     * 이 부분은 새로운 분석 기준이 아니라
     * 결과 파일 손상을 방지하는 보호 로직이다.
     */
    private String sanitizeForTsv(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace('\t', ' ')
                .replace('\r', ' ')
                .replace('\n', ' ')
                .trim();
    }

    /**
     * Oracle NUMBER(19,2)에 맞춰
     * 소수점 둘째 자리까지 출력한다.
     */
    private String scale2(
            BigDecimal value) {

        return value
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                )
                .toPlainString();
    }

    /**
     * AbstractRetailMapper가 출력한
     * 내부 Value를 해석한다.
     */
    private static final class MapperValue {

        private final String invoiceNo;
        private final TransactionType type;
        private final long quantity;
        private final BigDecimal amount;
        private final String description;

        private MapperValue(
                String invoiceNo,
                TransactionType type,
                long quantity,
                BigDecimal amount,
                String description) {

            this.invoiceNo = invoiceNo;
            this.type = type;
            this.quantity = quantity;
            this.amount = amount;
            this.description = description;
        }

        private static MapperValue parse(
                String value)
                throws IOException {

            /*
             * limit=5를 사용하므로 Description에
             * | 문자가 있더라도 앞의 네 필드는
             * 정상적으로 유지된다.
             */
            String[] fields =
                    value.split(
                            VALUE_SEPARATOR_REGEX,
                            VALUE_FIELD_COUNT
                    );

            if (fields.length
                    != VALUE_FIELD_COUNT) {

                throw new IOException(
                        "잘못된 상품별 Mapper Value "
                        + "형식입니다: "
                        + value
                );
            }

            try {

                return new MapperValue(
                        fields[0],
                        TransactionType.valueOf(
                                fields[1]
                        ),
                        Long.parseLong(
                                fields[2]
                        ),
                        new BigDecimal(
                                fields[3]
                        ),
                        fields[4]
                );

            } catch (
                    IllegalArgumentException e) {

                throw new IOException(
                        "상품별 Mapper Value 변환에 "
                        + "실패했습니다: "
                        + value,
                        e
                );
            }
        }
    }
}