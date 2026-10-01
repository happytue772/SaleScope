package com.salescope.batch.util;

import java.util.EnumSet;
import java.util.Locale;

import com.salescope.batch.parser.RetailTransaction;
import com.salescope.batch.parser.TransactionType;

/**
 * SaleScope 데이터 분류 및 품질 검증용 Hadoop Counter.
 *
 * 분류 카운터 6개는 한 데이터 행당 정확히 하나만 증가한다.
 *
 * NORMAL
 * CANCELLED
 * ADJUSTMENT
 * ZERO_OR_NEG_PRICE
 * UNCLASSIFIED
 * PARSE_ERROR
 *
 * 속성 카운터는 분류 카운터와 중복 증가할 수 있다.
 *
 * MISSING_DESCRIPTION
 * MISSING_CUSTOMER_ID
 *
 * HEADER_SKIPPED는 CSV 헤더 또는 빈 줄을 건너뛴 건수다.
 */
public enum RetailCounters {

    // 배타적 분류 6종
    NORMAL,
    CANCELLED,
    ADJUSTMENT,
    ZERO_OR_NEG_PRICE,
    UNCLASSIFIED,
    PARSE_ERROR,

    // 속성 카운터 2종
    MISSING_DESCRIPTION,
    MISSING_CUSTOMER_ID,

    // 참고 카운터
    HEADER_SKIPPED;

    /**
     * 전체 데이터 행 수와 비교할 배타적 분류 카운터 목록.
     */
    private static final EnumSet<RetailCounters> EXCLUSIVE_COUNTERS =
            EnumSet.of(
                    NORMAL,
                    CANCELLED,
                    ADJUSTMENT,
                    ZERO_OR_NEG_PRICE,
                    UNCLASSIFIED,
                    PARSE_ERROR
            );

    /**
     * 배타적 분류 카운터 목록의 복사본을 반환한다.
     */
    public static EnumSet<RetailCounters> exclusiveCounters() {

        return EnumSet.copyOf(EXCLUSIVE_COUNTERS);
    }

    /**
     * 현재 Counter가 배타적 분류 6종에 해당하는지 확인한다.
     */
    public boolean isExclusiveCounter() {

        return EXCLUSIVE_COUNTERS.contains(this);
    }

    /**
     * Parser가 반환한 RetailTransaction을 Hadoop Counter로 변환한다.
     *
     * @param tx Parser가 반환한 거래 객체
     * @return 해당 거래가 증가시킬 기본 Counter
     */
    public static RetailCounters fromTransaction(RetailTransaction tx) {

        if (tx == null || tx.getType() == null) {

            return UNCLASSIFIED;
        }

        TransactionType type = tx.getType();

        switch (type) {

        case NORMAL:
            return NORMAL;

        case CANCELLED:
            return CANCELLED;

        case ADJUSTMENT:
            return ADJUSTMENT;

        case SKIP:
            return HEADER_SKIPPED;

        case INVALID:
            return fromErrorMessage(tx.getErrorMessage());

        default:
            return UNCLASSIFIED;
        }
    }

    /**
     * Parser의 오류 메시지를 데이터 품질 Counter로 변환한다.
     */
    private static RetailCounters fromErrorMessage(String errorMessage) {

        if (errorMessage == null || errorMessage.trim().isEmpty()) {

            return UNCLASSIFIED;
        }

        String error = errorMessage
                .trim()
                .toUpperCase(Locale.ROOT);

        /*
         * 가격이 0이거나 음수인 경우.
         *
         * 기존 Parser에서 사용했을 가능성이 있는 여러 오류 메시지를
         * 모두 동일한 Counter로 처리한다.
         */
        if (error.contains("ZERO_OR_NEG_PRICE")
                || error.contains("ZERO_UNIT_PRICE")
                || error.contains("NEGATIVE_UNIT_PRICE")
                || error.contains("NON_POSITIVE_UNIT_PRICE")) {

            return ZERO_OR_NEG_PRICE;
        }

        /*
         * CSV 구조, 컬럼 수, 숫자 및 날짜 변환 실패는
         * PARSE_ERROR로 통합한다.
         */
        if (error.startsWith("PARSE_ERROR")
                || error.contains("INVALID_COLUMN_COUNT")
                || error.contains("MISSING_COLUMNS")
                || error.contains("MISSING_REQUIRED_FIELD")
                || error.contains("EMPTY_REQUIRED_FIELD")
                || error.contains("INVALID_DATE_FORMAT")
        	    || error.startsWith("MISSING_")){

            return PARSE_ERROR;
        }

        /*
         * UNMATCHED_BUSINESS_RULE을 비롯하여
         * 위 조건에 포함되지 않는 오류는 UNCLASSIFIED로 처리한다.
         */
        return UNCLASSIFIED;
    }

    /**
     * Description 누락 여부를 확인한다.
     */
    public static boolean hasMissingDescription(RetailTransaction tx) {

        return tx != null && isBlank(tx.getDescription());
    }

    /**
     * CustomerID 누락 여부를 확인한다.
     */
    public static boolean hasMissingCustomerId(RetailTransaction tx) {

        return tx != null && isBlank(tx.getCustomerId());
    }

    /**
     * null, 빈 문자열 또는 공백 문자열인지 확인한다.
     */
    private static boolean isBlank(String value) {

        return value == null || value.trim().isEmpty();
    }
}