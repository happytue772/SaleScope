package com.salescope.batch.parser;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * Online Retail CSV 한 행을 RetailTransaction 객체로 변환한다.
 *
 * CSV 컬럼 순서:
 *
 * 0. InvoiceNo
 * 1. StockCode
 * 2. Description
 * 3. Quantity
 * 4. InvoiceDate
 * 5. UnitPrice
 * 6. CustomerID
 * 7. Country
 */
public final class RetailTransactionParser {

    /*
     * CSV 변환 방법에 따라 날짜 형식이 달라질 수 있으므로
     * 현재 예상되는 날짜 형식들을 등록한다.
     *
     * 기존 날짜 형식을 그대로 유지한다.
     */
    private static final DateTimeFormatter[] DATE_FORMATTERS = {

            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd H:mm:ss"
            ),

            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd H:mm"
            ),

            DateTimeFormatter.ofPattern(
                    "M/d/yyyy H:mm:ss"
            ),

            DateTimeFormatter.ofPattern(
                    "M/d/yyyy H:mm"
            )
    };

    /**
     * 정적 유틸리티 클래스이므로 외부 객체 생성을 막는다.
     */
    private RetailTransactionParser() {
    }

    /**
     * CSV 한 줄을 RetailTransaction 객체로 변환한다.
     *
     * @param csvLine CSV 한 줄
     * @return 파싱 및 거래 분류 결과
     */
    public static RetailTransaction parse(String csvLine) {

        RetailTransaction tx =
                new RetailTransaction();

        /*
         * 1. 빈 줄 검사
         *
         * 빈 줄은 오류 데이터가 아니므로 INVALID가 아니라
         * SKIP으로 반환한다.
         */
        if (csvLine == null
                || csvLine.trim().isEmpty()) {

            return skip(
                    tx,
                    "EMPTY_LINE"
            );
        }

        /*
         * UTF-8 BOM이 있는 CSV 헤더에 대비한다.
         */
        String normalizedLine =
                csvLine.trim();

        if (normalizedLine.startsWith("\uFEFF")) {

            normalizedLine =
                    normalizedLine.substring(1);
        }

        /*
         * 2. Apache Commons CSV를 이용한 CSV 분리
         *
         * Description에 쉼표가 들어 있어도
         * 하나의 컬럼으로 처리한다.
         */
        try (
            CSVParser csvParser =
                    CSVParser.parse(
                            normalizedLine,
                            CSVFormat.DEFAULT
                    )
        ) {

            /*
             * 기존 getRecords() 방식 대신 Iterator를 사용한다.
             *
             * CSV 한 줄에서 레코드가 여러 개 생성되는 비정상
             * 상황도 확인할 수 있다.
             */
            Iterator<CSVRecord> iterator =
                    csvParser.iterator();

            if (!iterator.hasNext()) {

                return skip(
                        tx,
                        "EMPTY_CSV_RECORD"
                );
            }

            CSVRecord record =
                    iterator.next();

            /*
             * CSV 한 줄에서 두 개 이상의 레코드가 만들어지면
             * 정상적인 단일 거래 행으로 처리하지 않는다.
             */
            if (iterator.hasNext()) {

                return invalid(
                        tx,
                        "PARSE_ERROR_MULTIPLE_RECORDS"
                );
            }

            /*
             * CSV 컬럼 개수는 정확히 8개여야 한다.
             */
            if (record.size() != 8) {

                return invalid(
                        tx,
                        "INVALID_COLUMN_COUNT"
                );
            }

            String invoiceNo =
                    getValue(record, 0);

            /*
             * 3. 헤더 검사
             *
             * InvoiceNo로 시작하는 헤더는 분석하지 않는다.
             */
            if ("InvoiceNo".equalsIgnoreCase(invoiceNo)) {

                return skip(
                        tx,
                        "HEADER_ROW"
                );
            }

            String stockCode =
                    getValue(record, 1);

            String description =
                    getValue(record, 2);

            String quantityValue =
                    getValue(record, 3);

            String invoiceDateValue =
                    getValue(record, 4);

            String unitPriceValue =
                    getValue(record, 5);

            String customerId =
                    getValue(record, 6);

            String country =
                    getValue(record, 7);

            /*
             * 4. 문자열 필드를 먼저 객체에 저장한다.
             *
             * 파싱에 실패하더라도 읽을 수 있었던 문자열 정보는
             * RetailTransaction 객체에 남긴다.
             */
            tx.setInvoiceNo(invoiceNo);
            tx.setStockCode(stockCode);
            tx.setDescription(description);
            tx.setCustomerId(customerId);
            tx.setCountry(country);

            /*
             * 5. 필수값 검사
             *
             * 기존 필드별 오류 메시지를 그대로 유지한다.
             *
             * Description과 CustomerID는 비어 있어도 매출 집계에
             * 사용할 수 있으므로 INVALID로 처리하지 않는다.
             */
            if (invoiceNo.isEmpty()) {

                return invalid(
                        tx,
                        "MISSING_INVOICE_NO"
                );
            }

            if (stockCode.isEmpty()) {

                return invalid(
                        tx,
                        "MISSING_STOCK_CODE"
                );
            }

            if (quantityValue.isEmpty()) {

                return invalid(
                        tx,
                        "MISSING_QUANTITY"
                );
            }

            if (invoiceDateValue.isEmpty()) {

                return invalid(
                        tx,
                        "MISSING_INVOICE_DATE"
                );
            }

            if (unitPriceValue.isEmpty()) {

                return invalid(
                        tx,
                        "MISSING_UNIT_PRICE"
                );
            }

            /*
             * 6. 숫자와 날짜 변환
             */
            long quantity =
                    Long.parseLong(quantityValue);

            BigDecimal unitPrice =
                    new BigDecimal(unitPriceValue);

            LocalDateTime invoiceDate =
                    parseInvoiceDate(
                            invoiceDateValue
                    );

            tx.setQuantity(quantity);
            tx.setUnitPrice(unitPrice);
            tx.setInvoiceDate(invoiceDate);

            /*
             * 기존 가격 오류 메시지를 그대로 유지한다.
             */
            if (unitPrice.compareTo(
                    BigDecimal.ZERO) < 0) {

                return invalid(
                        tx,
                        "NEGATIVE_UNIT_PRICE"
                );
            }

            if (unitPrice.compareTo(
                    BigDecimal.ZERO) == 0) {

                return invalid(
                        tx,
                        "ZERO_UNIT_PRICE"
                );
            }

            /*
             * 7. 거래 유형 분류
             *
             * A로 시작하는 InvoiceNo는 Adjust bad debt 등의
             * 조정 거래이므로 NORMAL로 처리하지 않는다.
             */
            String upperInvoiceNo =
                    invoiceNo
                            .trim()
                            .toUpperCase(Locale.ROOT);

            boolean isCancelledInvoice =
                    upperInvoiceNo.startsWith("C");

            boolean isAdjustmentInvoice =
                    upperInvoiceNo.startsWith("A");

            boolean isNormalInvoice =
                    upperInvoiceNo.matches("\\d+");

            /*
             * A 거래를 NORMAL보다 먼저 검사해야 한다.
             */
            if (isAdjustmentInvoice) {

                tx.setType(
                        TransactionType.ADJUSTMENT
                );

                tx.setErrorMessage(null);

            } else if (isCancelledInvoice
                    && quantity < 0) {

                tx.setType(
                        TransactionType.CANCELLED
                );

                tx.setErrorMessage(null);

            } else if (isNormalInvoice
                    && quantity > 0) {

                tx.setType(
                        TransactionType.NORMAL
                );

                tx.setErrorMessage(null);

            } else if (isNormalInvoice
                    && quantity < 0) {

                tx.setType(
                        TransactionType.ADJUSTMENT
                );

                tx.setErrorMessage(null);

            } else {

                return invalid(
                        tx,
                        "UNMATCHED_BUSINESS_RULE"
                );
            }

        } catch (NumberFormatException e) {

            return invalid(
                    tx,
                    "PARSE_ERROR_NUMBER_FORMAT"
            );

        } catch (DateTimeParseException e) {

            return invalid(
                    tx,
                    "PARSE_ERROR_DATE_FORMAT"
            );

        } catch (IOException e) {

            return invalid(
                    tx,
                    "PARSE_ERROR_CSV"
            );
        }

        return tx;
    }

    /**
     * CSVRecord에서 지정한 인덱스의 값을 안전하게 가져온다.
     *
     * record가 null이거나 인덱스 범위를 벗어나면
     * 예외를 발생시키지 않고 빈 문자열을 반환한다.
     */
    private static String getValue(
            CSVRecord record,
            int index) {

        if (record == null
                || index < 0
                || index >= record.size()) {

            return "";
        }

        String value =
                record.get(index);

        if (value == null) {

            return "";
        }

        return value.trim();
    }

    /**
     * 여러 날짜 형식 중 일치하는 형식으로 날짜를 변환한다.
     */
    private static LocalDateTime parseInvoiceDate(
            String invoiceDateValue) {

        for (DateTimeFormatter formatter
                : DATE_FORMATTERS) {

            try {

                return LocalDateTime.parse(
                        invoiceDateValue,
                        formatter
                );

            } catch (DateTimeParseException ignored) {

                /*
                 * 현재 형식으로 변환하지 못한 경우
                 * 다음 날짜 형식을 시도한다.
                 */
            }
        }

        throw new DateTimeParseException(
                "지원하지 않는 날짜 형식입니다.",
                invoiceDateValue,
                0
        );
    }

    /**
     * 유효하지 않은 데이터 처리.
     */
    private static RetailTransaction invalid(
            RetailTransaction tx,
            String errorMessage) {

        tx.setType(
                TransactionType.INVALID
        );

        tx.setErrorMessage(
                errorMessage
        );

        return tx;
    }

    /**
     * 헤더 또는 빈 줄 처리.
     */
    private static RetailTransaction skip(
            RetailTransaction tx,
            String message) {

        tx.setType(
                TransactionType.SKIP
        );

        tx.setErrorMessage(
                message
        );

        return tx;
    }
}