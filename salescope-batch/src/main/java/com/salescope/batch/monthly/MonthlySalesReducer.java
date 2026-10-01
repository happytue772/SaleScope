package com.salescope.batch.monthly;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.salescope.batch.parser.TransactionType;

/**
 * 월별 정상 매출, 취소 금액 및
 * 순매출을 집계한다.
 *
 * 출력 TSV 컬럼 순서:
 *
 * 1. sales_month
 * 2. sales_order_count
 * 3. sold_quantity
 * 4. gross_sales
 * 5. cancel_order_count
 * 6. cancel_quantity
 * 7. cancel_amount
 * 8. net_sales
 */
public class MonthlySalesReducer
        extends Reducer<
                Text,
                Text,
                Text,
                NullWritable> {

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
         * 주문 건수는 행 개수가 아니라
         * 서로 다른 InvoiceNo 개수이다.
         */
        Set<String> salesInvoices =
                new HashSet<String>();

        Set<String> cancelInvoices =
                new HashSet<String>();

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

        BigDecimal netSales =
                grossSales.subtract(
                        cancelAmount
                );

        String outputLine =
                key.toString()
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
                + scale2(netSales);

        context.write(
                new Text(outputLine),
                NullWritable.get()
        );
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

        private MapperValue(
                String invoiceNo,
                TransactionType type,
                long quantity,
                BigDecimal amount) {

            this.invoiceNo = invoiceNo;
            this.type = type;
            this.quantity = quantity;
            this.amount = amount;
        }

        private static MapperValue parse(
                String value)
                throws IOException {

            /*
             * limit=5를 사용하므로 Description에
             * | 문자가 있어도 앞의 4개 필드는 유지된다.
             */
            String[] fields =
                    value.split(
                            VALUE_SEPARATOR_REGEX,
                            VALUE_FIELD_COUNT
                    );

            if (fields.length
                    != VALUE_FIELD_COUNT) {

                throw new IOException(
                        "잘못된 Mapper Value "
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
                        )
                );

            } catch (
                    IllegalArgumentException e) {

                throw new IOException(
                        "Mapper Value 변환에 "
                        + "실패했습니다: "
                        + value,
                        e
                );
            }
        }
    }
}