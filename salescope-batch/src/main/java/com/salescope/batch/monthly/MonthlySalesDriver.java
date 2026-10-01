package com.salescope.batch.monthly;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.salescope.batch.util.AbstractRetailMapper;
import com.salescope.batch.util.CounterWriter;

/**
 * 월별 매출 MapReduce Job을 설정하고 실행한다.
 *
 * 실행 인자:
 * MonthlySalesDriver <jobId> <input> <output> [expectedRows]
 */
public class MonthlySalesDriver
        extends Configured
        implements Tool {

    private static final String CONF_JOB_ID =
            "salescope.job.id";

    @Override
    public int run(String[] args)
            throws Exception {

        if (args.length < 3
                || args.length > 4) {

            System.err.println(
                    "Usage: MonthlySalesDriver "
                    + "<jobId> <input> "
                    + "<output> [expectedRows]"
            );

            return 2;
        }

        /*
         * CounterWriter가 long 타입의 jobId를 요구하므로
         * 문자열 실행 인자를 long으로 변환한다.
         */
        long jobId =
                parseJobId(args[0]);

        String input = args[1];
        String output = args[2];

        long expectedRows =
                args.length == 4
                ? parseExpectedRows(args[3])
                : 0L;

        Configuration configuration =
                getConf();

        configuration.setLong(
                CONF_JOB_ID,
                jobId
        );

        if (configuration.get(
                AbstractRetailMapper.CONF_DATE_MODE
        ) == null) {

            configuration.set(
                    AbstractRetailMapper.CONF_DATE_MODE,
                    "AUTO"
            );
        }

        Job job = Job.getInstance(
                configuration,
                "SaleScope Monthly Sales - job "
                        + jobId
        );

        job.setJarByClass(
                MonthlySalesDriver.class
        );

        job.setMapperClass(
                MonthlySalesMapper.class
        );

        job.setReducerClass(
                MonthlySalesReducer.class
        );

        /*
         * Mapper 출력 자료형
         */
        job.setMapOutputKeyClass(
                Text.class
        );

        job.setMapOutputValueClass(
                Text.class
        );

        /*
         * Reducer 최종 출력 자료형
         */
        job.setOutputKeyClass(
                Text.class
        );

        job.setOutputValueClass(
                NullWritable.class
        );

        /*
         * 서로 다른 InvoiceNo 개수를 계산하므로
         * Combiner는 사용하지 않는다.
         *
         * 하나의 결과 TSV를 만들기 위해
         * Reducer를 1개로 설정한다.
         */
        job.setNumReduceTasks(1);

        FileInputFormat.addInputPath(
                job,
                new Path(input)
        );

        FileOutputFormat.setOutputPath(
                job,
                new Path(output)
        );

        long startedAt =
                System.currentTimeMillis();

        boolean success =
                job.waitForCompletion(true);

        long processingTimeMs =
                System.currentTimeMillis()
                        - startedAt;

        System.out.println(
                "[SaleScope] processing_time_ms = "
                + processingTimeMs
        );

        /*
         * Hadoop Job 성공 후 Counter를
         * 품질 통계 TSV 파일로 저장한다.
         */
        if (success) {

            Path qualityFile =
                    CounterWriter.write(
                            configuration,
                            job.getCounters(),
                            jobId,
                            new Path(output),
                            expectedRows
                    );

            System.out.println(
                    "[SaleScope] 품질 통계 저장: "
                    + qualityFile.toString()
            );
        }

        return success ? 0 : 1;
    }

    /**
     * 실행 인자로 받은 jobId를 long으로 변환한다.
     */
    private long parseJobId(
            String value) {

        try {

            long jobId =
                    Long.parseLong(value);

            if (jobId <= 0L) {

                throw new IllegalArgumentException(
                        "jobId는 1 이상이어야 합니다."
                );
            }

            return jobId;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "jobId는 정수여야 합니다: "
                    + value,
                    e
            );
        }
    }

    /**
     * 예상 데이터 행 수를 long으로 변환한다.
     */
    private long parseExpectedRows(
            String value) {

        try {

            long expectedRows =
                    Long.parseLong(value);

            if (expectedRows < 0L) {

                throw new IllegalArgumentException(
                        "expectedRows는 "
                        + "0 이상이어야 합니다."
                );
            }

            return expectedRows;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "expectedRows는 "
                    + "정수여야 합니다: "
                    + value,
                    e
            );
        }
    }

    public static void main(
            String[] args)
            throws Exception {

        int exitCode =
                ToolRunner.run(
                        new Configuration(),
                        new MonthlySalesDriver(),
                        args
                );

        System.exit(exitCode);
    }
}