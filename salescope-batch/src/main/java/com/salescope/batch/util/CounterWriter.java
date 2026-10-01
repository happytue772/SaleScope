package com.salescope.batch.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Counters;

/**
 * Hadoop Counter 결과를 HDFS의 TSV 파일로 저장한다.
 *
 * 생성 파일:
 * 분석 결과 경로/data_quality.tsv
 *
 * TSV 컬럼:
 * job_id
 * quality_type
 * record_count
 * sample_message
 */
public final class CounterWriter {

    private static final String OUTPUT_FILE_NAME = "data_quality.tsv";

    /**
     * 객체 생성을 막기 위한 private 생성자.
     */
    private CounterWriter() {
    }

    /**
     * Hadoop Counter를 TSV 파일로 저장한다.
     *
     * @param configuration Hadoop 설정
     * @param counters Hadoop Job 실행 결과 Counter
     * @param jobId Oracle ANALYSIS_JOB의 JOB_ID
     * @param outputDirectory Hadoop Job 출력 디렉터리
     * @param expectedRows 예상 데이터 행 수.
     *                     검증하지 않을 경우 -1을 전달한다.
     * @return 생성된 TSV 파일 경로
     * @throws IOException 파일 저장 실패
     */
    public static Path write(
            Configuration configuration,
            Counters counters,
            long jobId,
            Path outputDirectory,
            long expectedRows) throws IOException {

        validateArguments(
                configuration,
                counters,
                jobId,
                outputDirectory
        );

        long exclusiveTotal = calculateExclusiveTotal(counters);

        validateExpectedRows(
                exclusiveTotal,
                expectedRows
        );

        FileSystem fileSystem =
                outputDirectory.getFileSystem(configuration);

        if (!fileSystem.exists(outputDirectory)) {

            fileSystem.mkdirs(outputDirectory);
        }

        Path outputFile =
                new Path(outputDirectory, OUTPUT_FILE_NAME);

        /*
         * true를 전달하므로 동일한 Job 출력 폴더에서
         * 다시 실행할 경우 기존 data_quality.tsv를 덮어쓴다.
         *
         * 실제 MapReduce 출력 경로는 jobId별로 분리해야 한다.
         */
        try (
            FSDataOutputStream outputStream =
                    fileSystem.create(outputFile, true);

            BufferedWriter writer =
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    outputStream,
                                    StandardCharsets.UTF_8
                            )
                    )
        ) {

            writeHeader(writer);

            for (RetailCounters counter : RetailCounters.values()) {

                long count = counters
                        .findCounter(counter)
                        .getValue();

                writeCounterRow(
                        writer,
                        jobId,
                        counter,
                        count
                );
            }
        }

        printValidationResult(
                outputFile,
                exclusiveTotal,
                expectedRows
        );

        return outputFile;
    }

    /**
     * Counter 결과 중 배타적 분류 6종의 합계를 계산한다.
     */
    public static long calculateExclusiveTotal(Counters counters) {

        if (counters == null) {

            throw new IllegalArgumentException(
                    "Counters는 null일 수 없습니다."
            );
        }

        long total = 0L;

        for (RetailCounters counter
                : RetailCounters.exclusiveCounters()) {

            total += counters
                    .findCounter(counter)
                    .getValue();
        }

        return total;
    }

    /**
     * 필수 인자를 검증한다.
     */
    private static void validateArguments(
            Configuration configuration,
            Counters counters,
            long jobId,
            Path outputDirectory) {

        if (configuration == null) {

            throw new IllegalArgumentException(
                    "Hadoop Configuration은 null일 수 없습니다."
            );
        }

        if (counters == null) {

            throw new IllegalArgumentException(
                    "Hadoop Counters는 null일 수 없습니다."
            );
        }

        if (jobId <= 0) {

            throw new IllegalArgumentException(
                    "jobId는 1 이상의 값이어야 합니다."
            );
        }

        if (outputDirectory == null) {

            throw new IllegalArgumentException(
                    "출력 디렉터리는 null일 수 없습니다."
            );
        }
    }

    /**
     * 예상 행 수와 실제 분류 합계를 비교한다.
     */
    private static void validateExpectedRows(
            long exclusiveTotal,
            long expectedRows) {

        /*
         * expectedRows가 음수이면 행 수 검증을 생략한다.
         */
        if (expectedRows < 0) {

            return;
        }

        if (exclusiveTotal != expectedRows) {

            throw new IllegalStateException(
                    "배타적 Counter 합계가 예상 행 수와 일치하지 않습니다."
                    + " expectedRows=" + expectedRows
                    + ", exclusiveTotal=" + exclusiveTotal
            );
        }
    }

    /**
     * TSV 헤더를 작성한다.
     */
    private static void writeHeader(
            BufferedWriter writer) throws IOException {

        writer.write(
                "job_id"
                + "\tquality_type"
                + "\trecord_count"
                + "\tsample_message"
        );

        writer.newLine();
    }

    /**
     * Counter 한 행을 TSV 형식으로 작성한다.
     */
    private static void writeCounterRow(
            BufferedWriter writer,
            long jobId,
            RetailCounters counter,
            long count) throws IOException {

        writer.write(Long.toString(jobId));
        writer.write('\t');

        writer.write(counter.name());
        writer.write('\t');

        writer.write(Long.toString(count));
        writer.write('\t');

        /*
         * 현재 Counter에는 오류 샘플 메시지가 없으므로
         * sample_message는 빈 값으로 저장한다.
         */
        writer.write("");

        writer.newLine();
    }

    /**
     * 저장 및 정합성 검증 결과를 콘솔에 출력한다.
     */
    private static void printValidationResult(
            Path outputFile,
            long exclusiveTotal,
            long expectedRows) {

        System.out.println();
        System.out.println(
                "===== Hadoop Counter 저장 완료 ====="
        );

        System.out.println(
                "저장 경로: " + outputFile
        );

        System.out.println(
                "배타적 분류 합계: " + exclusiveTotal
        );

        if (expectedRows >= 0) {

            System.out.println(
                    "예상 데이터 행 수: " + expectedRows
            );

            System.out.println(
                    "행 수 검증 결과: 통과"
            );

        } else {

            System.out.println(
                    "행 수 검증 결과: 검증 생략"
            );
        }
    }
}