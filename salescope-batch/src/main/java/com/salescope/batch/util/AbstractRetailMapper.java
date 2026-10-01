package com.salescope.batch.util;

import java.io.IOException;
import java.math.BigDecimal;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.salescope.batch.parser.RetailTransaction;
import com.salescope.batch.parser.RetailTransactionParser;
import com.salescope.batch.parser.TransactionType;

/**
 * 월별/상품별 Mapper가 공통으로 사용하는
 * 파싱, 분류, Counter 처리 클래스.
 *
 * 정상 거래와 취소 거래만 Reducer로 전달하고
 * 나머지 거래는 Hadoop Counter에만 기록한다.
 */
public abstract class AbstractRetailMapper
        extends Mapper<LongWritable, Text, Text, Text> {

    public static final String CONF_DATE_MODE =
            "salescope.date.mode";

    private static final String VALUE_SEPARATOR = "|";

    /**
     * 월별 Mapper는 YYYY-MM,
     * 상품별 Mapper는 StockCode를 반환한다.
     */
    protected abstract String mapKey(
            RetailTransaction transaction
    );

    @Override
    protected void map(
            LongWritable key,
            Text value,
            Context context)
            throws IOException, InterruptedException {

        RetailTransaction transaction =
                RetailTransactionParser.parse(
                        value.toString()
                );

        /*
         * Parser가 비정상적으로 null을 반환했거나
         * 거래 유형이 설정되지 않은 경우
         */
        if (transaction == null
                || transaction.getType() == null) {

            context.getCounter(
                    RetailCounters.PARSE_ERROR
            ).increment(1L);

            return;
        }

        TransactionType type =
                transaction.getType();

        /*
         * 헤더 또는 빈 줄은 실제 데이터가 아니므로
         * 배타적 분류 합계에서 제외한다.
         */
        if (type == TransactionType.SKIP) {

            context.getCounter(
                    RetailCounters.HEADER_SKIPPED
            ).increment(1L);

            return;
        }

        /*
         * Description과 CustomerID 누락은
         * 거래 유형과 별도로 중복 집계할 수 있다.
         */
        countMissingAttributes(
                transaction,
                context
        );

        /*
         * 정상·취소 거래만 Reducer로 전달한다.
         */
        if (type == TransactionType.NORMAL
                || type == TransactionType.CANCELLED) {

            String outputKey =
                    mapKey(transaction);

            if (isBlank(outputKey)) {

                context.getCounter(
                        RetailCounters.UNCLASSIFIED
                ).increment(1L);

                return;
            }

            incrementExclusiveCounter(
                    type,
                    null,
                    context
            );

            context.write(
                    new Text(outputKey),
                    new Text(
                            toMapperValue(transaction)
                    )
            );

            return;
        }

        /*
         * 조정·가격 오류·미분류·파싱 오류는
         * Reducer로 보내지 않고 Counter에만 기록한다.
         */
        incrementExclusiveCounter(
                type,
                transaction.getErrorMessage(),
                context
        );
    }

    /**
     * 속성 카운터 처리
     */
    private void countMissingAttributes(
            RetailTransaction transaction,
            Context context) {

        if (isBlank(
                transaction.getDescription())) {

            context.getCounter(
                    RetailCounters.MISSING_DESCRIPTION
            ).increment(1L);
        }

        if (isBlank(
                transaction.getCustomerId())) {

            context.getCounter(
                    RetailCounters.MISSING_CUSTOMER_ID
            ).increment(1L);
        }
    }

    /**
     * Parser 결과를 배타적 Counter 6종으로 변환한다.
     */
    private void incrementExclusiveCounter(
            TransactionType type,
            String errorMessage,
            Context context) {

        String typeName = type.name();

        if ("NORMAL".equals(typeName)) {

            context.getCounter(
                    RetailCounters.NORMAL
            ).increment(1L);

            return;
        }

        if ("CANCELLED".equals(typeName)) {

            context.getCounter(
                    RetailCounters.CANCELLED
            ).increment(1L);

            return;
        }

        if ("ADJUSTMENT".equals(typeName)) {

            context.getCounter(
                    RetailCounters.ADJUSTMENT
            ).increment(1L);

            return;
        }

        if ("ZERO_OR_NEG_PRICE".equals(typeName)
                || isPriceError(errorMessage)) {

            context.getCounter(
                    RetailCounters.ZERO_OR_NEG_PRICE
            ).increment(1L);

            return;
        }

        if ("UNCLASSIFIED".equals(typeName)
                || isUnclassifiedError(
                        errorMessage)) {

            context.getCounter(
                    RetailCounters.UNCLASSIFIED
            ).increment(1L);

            return;
        }

        context.getCounter(
                RetailCounters.PARSE_ERROR
        ).increment(1L);
    }

    /**
     * 기존 Parser의 가격 오류 메시지와
     * 통합 가격 오류 메시지를 모두 지원한다.
     */
    private boolean isPriceError(
            String errorMessage) {

        return "ZERO_OR_NEG_PRICE"
                .equals(errorMessage)
                || "ZERO_UNIT_PRICE"
                .equals(errorMessage)
                || "NEGATIVE_UNIT_PRICE"
                .equals(errorMessage);
    }

    /**
     * 미분류 오류 판별
     */
    private boolean isUnclassifiedError(
            String errorMessage) {

        return "UNMATCHED_BUSINESS_RULE"
                .equals(errorMessage)
                || "UNCLASSIFIED"
                .equals(errorMessage);
    }

    /**
     * Reducer 전달 형식:
     *
     * InvoiceNo|TransactionType|Quantity
     * |AbsoluteAmount|Description
     */
    private String toMapperValue(
            RetailTransaction transaction) {

        BigDecimal amount =
                transaction
                        .calculateAbsoluteAmount();

        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        return safe(
                transaction.getInvoiceNo())
                + VALUE_SEPARATOR
                + transaction.getType().name()
                + VALUE_SEPARATOR
                + transaction.getQuantity()
                + VALUE_SEPARATOR
                + amount.toPlainString()
                + VALUE_SEPARATOR
                + safe(
                        transaction.getDescription());
    }

    private boolean isBlank(String value) {

        return value == null
                || value.trim().isEmpty();
    }

    private String safe(String value) {

        return value == null
                ? ""
                : value;
    }
}