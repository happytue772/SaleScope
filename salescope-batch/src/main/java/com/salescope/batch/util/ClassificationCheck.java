package com.salescope.batch.util;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;

import com.salescope.batch.parser.RetailTransaction;
import com.salescope.batch.parser.RetailTransactionParser;
import com.salescope.batch.parser.TransactionType;

/**
 * 실제 CSV 전체를 로컬에서 읽어 Parser 분류 결과를 검증한다.
 *
 * Hadoop이나 HDFS 없이 Java Application으로 실행할 수 있다.
 *
 * 실행 인자:
 * args[0] = CSV 파일 경로
 * args[1] = 예상 데이터 행 수(선택)
 *
 * 예시:
 * "C:\SaleScope\data\raw\online_retail.csv" 541909
 */
public class ClassificationCheck {

    public static void main(String[] args) throws Exception {

        validateArguments(args);

        Path csvPath = Paths.get(args[0]);

        long expectedRows = parseExpectedRows(args);

        validateInputFile(csvPath);

        Map<RetailCounters, Long> counts =
                initializeCounts();

        long physicalLineCount = 0L;

        System.out.println(
                "===== SaleScope 전체 CSV 분류 검증 시작 ====="
        );

        System.out.println(
                "CSV 경로: " + csvPath.toAbsolutePath()
        );

        if (expectedRows >= 0) {

            System.out.println(
                    "예상 데이터 행 수: " + expectedRows
            );

        } else {

            System.out.println(
                    "예상 데이터 행 수: 지정하지 않음"
            );
        }

        try (
            BufferedReader reader =
                    Files.newBufferedReader(
                            csvPath,
                            StandardCharsets.UTF_8
                    )
        ) {

            String csvLine;

            while ((csvLine = reader.readLine()) != null) {

                physicalLineCount++;

                RetailTransaction transaction =
                        RetailTransactionParser.parse(csvLine);

                /*
                 * A로 시작하는 양수 가격 거래가 정상 매출로
                 * 잘못 분류되지 않았는지 검증한다.
                 */
                validateAdjustmentInvoice(
                        transaction,
                        physicalLineCount
                );

                RetailCounters primaryCounter =
                        RetailCounters.fromTransaction(transaction);

                increment(
                        counts,
                        primaryCounter
                );

                /*
                 * 헤더나 빈 줄은 속성 누락 검사의 대상이 아니다.
                 *
                 * InvoiceNo가 존재한다는 것은 Parser가 최소한
                 * 실제 데이터 행의 기본 필드를 읽었다는 의미다.
                 */
                if (primaryCounter != RetailCounters.HEADER_SKIPPED
                        && transaction != null
                        && transaction.getInvoiceNo() != null) {

                    if (RetailCounters
                            .hasMissingDescription(transaction)) {

                        increment(
                                counts,
                                RetailCounters.MISSING_DESCRIPTION
                        );
                    }

                    if (RetailCounters
                            .hasMissingCustomerId(transaction)) {

                        increment(
                                counts,
                                RetailCounters.MISSING_CUSTOMER_ID
                        );
                    }
                }
            }
        }

        long exclusiveTotal =
                calculateExclusiveTotal(counts);

        long skippedTotal =
                getCount(
                        counts,
                        RetailCounters.HEADER_SKIPPED
                );

        printResult(
                counts,
                physicalLineCount,
                exclusiveTotal,
                skippedTotal,
                expectedRows
        );

        validateTotals(
                physicalLineCount,
                exclusiveTotal,
                skippedTotal,
                expectedRows
        );

        System.out.println();
        System.out.println(
                "모든 CSV 분류 검증 통과"
        );
    }

    /**
     * 프로그램 실행 인자를 검증한다.
     */
    private static void validateArguments(String[] args) {

        if (args == null || args.length < 1) {

            System.out.println(
                    "사용 방법:"
            );

            System.out.println(
                    "ClassificationCheck "
                    + "<CSV 파일 경로> "
                    + "[예상 데이터 행 수]"
            );

            System.out.println();
            System.out.println(
                    "Eclipse Program arguments 예시:"
            );

            System.out.println(
                    "\"C:\\SaleScope\\data\\raw"
                    + "\\online_retail.csv\" 541909"
            );

            throw new IllegalArgumentException(
                    "CSV 파일 경로가 필요합니다."
            );
        }

        if (args.length > 2) {

            throw new IllegalArgumentException(
                    "실행 인자는 CSV 경로와 예상 행 수만 사용할 수 있습니다."
            );
        }
    }

    /**
     * 선택 인자인 expectedRows를 long으로 변환한다.
     */
    private static long parseExpectedRows(String[] args) {

        if (args.length < 2) {

            return -1L;
        }

        try {

            long expectedRows =
                    Long.parseLong(args[1]);

            if (expectedRows < 0) {

                throw new IllegalArgumentException(
                        "예상 데이터 행 수는 0 이상이어야 합니다."
                );
            }

            return expectedRows;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "예상 데이터 행 수는 숫자로 입력해야 합니다.",
                    e
            );
        }
    }

    /**
     * 입력 CSV 파일이 존재하고 일반 파일인지 확인한다.
     */
    private static void validateInputFile(Path csvPath) {

        if (!Files.exists(csvPath)) {

            throw new IllegalArgumentException(
                    "CSV 파일을 찾을 수 없습니다: " + csvPath
            );
        }

        if (!Files.isRegularFile(csvPath)) {

            throw new IllegalArgumentException(
                    "일반 파일이 아닙니다: " + csvPath
            );
        }
    }

    /**
     * 모든 Counter를 0으로 초기화한다.
     */
    private static Map<RetailCounters, Long>
            initializeCounts() {

        Map<RetailCounters, Long> counts =
                new EnumMap<>(RetailCounters.class);

        for (RetailCounters counter
                : RetailCounters.values()) {

            counts.put(counter, 0L);
        }

        return counts;
    }

    /**
     * 지정한 Counter를 1 증가시킨다.
     */
    private static void increment(
            Map<RetailCounters, Long> counts,
            RetailCounters counter) {

        counts.put(
                counter,
                counts.get(counter) + 1L
        );
    }

    /**
     * 배타적 분류 6종의 합계를 계산한다.
     */
    private static long calculateExclusiveTotal(
            Map<RetailCounters, Long> counts) {

        long total = 0L;

        for (RetailCounters counter
                : RetailCounters.exclusiveCounters()) {

            total += getCount(counts, counter);
        }

        return total;
    }

    /**
     * 지정한 Counter의 현재 값을 반환한다.
     */
    private static long getCount(
            Map<RetailCounters, Long> counts,
            RetailCounters counter) {

        Long value = counts.get(counter);

        return value == null ? 0L : value;
    }

    /**
     * A로 시작하는 양수 가격 거래가 ADJUSTMENT로
     * 분류되었는지 확인한다.
     */
    private static void validateAdjustmentInvoice(
            RetailTransaction transaction,
            long lineNumber) {

        if (transaction == null
                || transaction.getInvoiceNo() == null) {

            return;
        }

        String invoiceNo =
                transaction.getInvoiceNo()
                        .trim()
                        .toUpperCase();

        BigDecimal unitPrice =
                transaction.getUnitPrice();

        if (invoiceNo.startsWith("A")
                && unitPrice != null
                && unitPrice.compareTo(BigDecimal.ZERO) > 0
                && transaction.getType()
                        != TransactionType.ADJUSTMENT) {

            throw new IllegalStateException(
                    "A InvoiceNo가 ADJUSTMENT로 분류되지 않았습니다."
                    + " line=" + lineNumber
                    + ", invoiceNo=" + invoiceNo
                    + ", actualType=" + transaction.getType()
            );
        }
    }

    /**
     * 전체 분류 결과를 콘솔에 출력한다.
     */
    private static void printResult(
            Map<RetailCounters, Long> counts,
            long physicalLineCount,
            long exclusiveTotal,
            long skippedTotal,
            long expectedRows) {

        System.out.println();
        System.out.println(
                "===== 배타적 분류 6종 ====="
        );

        for (RetailCounters counter
                : RetailCounters.exclusiveCounters()) {

            System.out.printf(
                    "%-28s %,12d%n",
                    counter.name(),
                    getCount(counts, counter)
            );
        }

        System.out.println();
        System.out.println(
                "===== 속성 카운터 ====="
        );

        System.out.printf(
                "%-28s %,12d%n",
                RetailCounters.MISSING_DESCRIPTION.name(),
                getCount(
                        counts,
                        RetailCounters.MISSING_DESCRIPTION
                )
        );

        System.out.printf(
                "%-28s %,12d%n",
                RetailCounters.MISSING_CUSTOMER_ID.name(),
                getCount(
                        counts,
                        RetailCounters.MISSING_CUSTOMER_ID
                )
        );

        System.out.println();
        System.out.println(
                "===== 참고 카운터 ====="
        );

        System.out.printf(
                "%-28s %,12d%n",
                RetailCounters.HEADER_SKIPPED.name(),
                skippedTotal
        );

        System.out.println();
        System.out.println(
                "===== 정합성 검증 ====="
        );

        System.out.printf(
                "%-28s %,12d%n",
                "실제 읽은 줄 수",
                physicalLineCount
        );

        System.out.printf(
                "%-28s %,12d%n",
                "배타적 분류 합계",
                exclusiveTotal
        );

        System.out.printf(
                "%-28s %,12d%n",
                "헤더/빈 줄 제외 건수",
                skippedTotal
        );

        System.out.printf(
                "%-28s %,12d%n",
                "분류 합계 + 제외 건수",
                exclusiveTotal + skippedTotal
        );

        if (expectedRows >= 0) {

            System.out.printf(
                    "%-28s %,12d%n",
                    "예상 데이터 행 수",
                    expectedRows
            );
        }
    }

    /**
     * 전체 줄 수 및 예상 데이터 행 수와 분류 결과를 비교한다.
     */
    private static void validateTotals(
            long physicalLineCount,
            long exclusiveTotal,
            long skippedTotal,
            long expectedRows) {

        long accountedLines =
                exclusiveTotal + skippedTotal;

        if (accountedLines != physicalLineCount) {

            throw new IllegalStateException(
                    "실제 읽은 줄 수와 전체 분류 건수가 일치하지 않습니다."
                    + " physicalLineCount=" + physicalLineCount
                    + ", accountedLines=" + accountedLines
            );
        }

        if (expectedRows >= 0
                && exclusiveTotal != expectedRows) {

            throw new IllegalStateException(
                    "배타적 분류 합계와 예상 데이터 행 수가 일치하지 않습니다."
                    + " expectedRows=" + expectedRows
                    + ", exclusiveTotal=" + exclusiveTotal
            );
        }
    }
}