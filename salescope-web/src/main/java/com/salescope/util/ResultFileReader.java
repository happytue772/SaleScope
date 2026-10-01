package com.salescope.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.salescope.dto.DataQualityStatDTO;
import com.salescope.dto.MonthlySalesStatDTO;
import com.salescope.dto.ProductSalesStatDTO;

public class ResultFileReader {

    // 기존 월별 Hadoop 결과를 읽는다.
    public List<MonthlySalesStatDTO> readMonthlyResults(
            Path resultDirectory,
            long jobId) throws IOException {

        validateResultDirectory(resultDirectory);

        List<Path> partFiles =
            findPartFiles(resultDirectory);

        if (partFiles.isEmpty()) {
            throw new IOException(
                "월별 part-r-* 파일이 없습니다: "
                    + resultDirectory
            );
        }

        List<MonthlySalesStatDTO> results =
            new ArrayList<>();

        for (Path partFile : partFiles) {
            readMonthlyPartFile(
                partFile,
                jobId,
                results
            );
        }

        return results;
    }

    // 상품별 Hadoop 10컬럼 결과를 읽는다.
    public List<ProductSalesStatDTO> readProductResults(
            Path resultDirectory,
            long jobId) throws IOException {

        validateResultDirectory(resultDirectory);

        List<Path> partFiles =
            findPartFiles(resultDirectory);

        if (partFiles.isEmpty()) {
            throw new IOException(
                "상품별 part-r-* 파일이 없습니다: "
                    + resultDirectory
            );
        }

        List<ProductSalesStatDTO> results =
            new ArrayList<>();

        for (Path partFile : partFiles) {
            readProductPartFile(
                partFile,
                jobId,
                results
            );
        }

        return results;
    }

    // 기존 데이터 품질 결과를 읽는다.
    public List<DataQualityStatDTO> readQualityResults(
            Path resultDirectory,
            long expectedJobId) throws IOException {

        validateResultDirectory(resultDirectory);

        Path qualityFile =
            resultDirectory.resolve(
                "data_quality.tsv"
            );

        if (!Files.isRegularFile(qualityFile)) {
            throw new IOException(
                "data_quality.tsv 파일이 없습니다: "
                    + qualityFile
            );
        }

        List<DataQualityStatDTO> results =
            new ArrayList<>();

        try (
            BufferedReader reader =
                Files.newBufferedReader(
                    qualityFile,
                    StandardCharsets.UTF_8
                )
        ) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] fields =
                    line.split("\t", -1);

                String firstColumn =
                    removeBom(fields[0]).trim();

                if (lineNumber == 1
                        && "job_id".equalsIgnoreCase(
                            firstColumn
                        )) {
                    continue;
                }

                if (fields.length != 4) {
                    throw new IOException(
                        "품질 TSV 컬럼 수 오류: file="
                            + qualityFile
                            + ", line="
                            + lineNumber
                            + ", actual="
                            + fields.length
                    );
                }

                long fileJobId =
                    parseLong(
                        firstColumn,
                        qualityFile,
                        lineNumber,
                        "job_id"
                    );

                if (fileJobId != expectedJobId) {
                    throw new IOException(
                        "품질 TSV의 JOB_ID가 다릅니다: "
                            + "expected="
                            + expectedJobId
                            + ", actual="
                            + fileJobId
                            + ", line="
                            + lineNumber
                    );
                }

                String qualityType =
                    fields[1].trim();

                long recordCount =
                    parseLong(
                        fields[2],
                        qualityFile,
                        lineNumber,
                        "record_count"
                    );

                String sampleMessage =
                    fields[3].trim();

                if (sampleMessage.isEmpty()) {
                    sampleMessage = null;
                }

                results.add(
                    new DataQualityStatDTO(
                        fileJobId,
                        qualityType,
                        recordCount,
                        sampleMessage
                    )
                );
            }
        }

        return results;
    }

    // 월별 TSV 한 파일을 DTO로 변환한다.
    private void readMonthlyPartFile(
            Path partFile,
            long jobId,
            List<MonthlySalesStatDTO> results)
            throws IOException {

        try (
            BufferedReader reader =
                Files.newBufferedReader(
                    partFile,
                    StandardCharsets.UTF_8
                )
        ) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] fields =
                    line.split("\t", -1);

                if (fields.length != 8) {
                    throw new IOException(
                        "월별 TSV 컬럼 수 오류: file="
                            + partFile
                            + ", line="
                            + lineNumber
                            + ", actual="
                            + fields.length
                    );
                }

                String salesMonth =
                    removeBom(fields[0]).trim();

                if (!salesMonth.matches(
                        "\\d{4}-\\d{2}")) {

                    throw new IOException(
                        "SALES_MONTH 형식 오류: "
                            + salesMonth
                            + ", line="
                            + lineNumber
                    );
                }

                results.add(
                    new MonthlySalesStatDTO(
                        jobId,
                        salesMonth,
                        parseLong(
                            fields[1],
                            partFile,
                            lineNumber,
                            "sales_order_count"
                        ),
                        parseLong(
                            fields[2],
                            partFile,
                            lineNumber,
                            "sold_quantity"
                        ),
                        parseBigDecimal(
                            fields[3],
                            partFile,
                            lineNumber,
                            "gross_sales"
                        ),
                        parseLong(
                            fields[4],
                            partFile,
                            lineNumber,
                            "cancel_order_count"
                        ),
                        parseLong(
                            fields[5],
                            partFile,
                            lineNumber,
                            "cancel_quantity"
                        ),
                        parseBigDecimal(
                            fields[6],
                            partFile,
                            lineNumber,
                            "cancel_amount"
                        ),
                        parseBigDecimal(
                            fields[7],
                            partFile,
                            lineNumber,
                            "net_sales"
                        )
                    )
                );
            }
        }
    }

    // 상품별 TSV 한 파일을 10컬럼 DTO로 변환한다.
    private void readProductPartFile(
            Path partFile,
            long jobId,
            List<ProductSalesStatDTO> results)
            throws IOException {

        try (
            BufferedReader reader =
                Files.newBufferedReader(
                    partFile,
                    StandardCharsets.UTF_8
                )
        ) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] fields =
                    line.split("\t", -1);

                if (fields.length != 10) {
                    throw new IOException(
                        "상품별 TSV 컬럼 수 오류: file="
                            + partFile
                            + ", line="
                            + lineNumber
                            + ", actual="
                            + fields.length
                    );
                }

                String stockCode =
                    removeBom(fields[0]).trim();

                if (stockCode.isEmpty()) {
                    throw new IOException(
                        "STOCK_CODE가 비어 있습니다: "
                            + "file="
                            + partFile
                            + ", line="
                            + lineNumber
                    );
                }

                if (stockCode.length() > 35) {
                    throw new IOException(
                        "STOCK_CODE 길이가 "
                            + "35자를 초과했습니다: "
                            + "value="
                            + stockCode
                            + ", file="
                            + partFile
                            + ", line="
                            + lineNumber
                    );
                }

                String productName =
                    fields[1].trim();

                if (productName.isEmpty()) {
                    productName = null;

                } else if (
                    productName.length() > 500
                ) {
                    throw new IOException(
                        "PRODUCT_NAME 길이가 "
                            + "500자를 초과했습니다: "
                            + "file="
                            + partFile
                            + ", line="
                            + lineNumber
                    );
                }

                results.add(
                    new ProductSalesStatDTO(
                        jobId,
                        stockCode,
                        productName,
                        parseLong(
                            fields[2],
                            partFile,
                            lineNumber,
                            "sales_order_count"
                        ),
                        parseLong(
                            fields[3],
                            partFile,
                            lineNumber,
                            "sold_quantity"
                        ),
                        parseBigDecimal(
                            fields[4],
                            partFile,
                            lineNumber,
                            "gross_sales"
                        ),
                        parseLong(
                            fields[5],
                            partFile,
                            lineNumber,
                            "cancel_order_count"
                        ),
                        parseLong(
                            fields[6],
                            partFile,
                            lineNumber,
                            "cancel_quantity"
                        ),
                        parseBigDecimal(
                            fields[7],
                            partFile,
                            lineNumber,
                            "cancel_amount"
                        ),
                        parseLong(
                            fields[8],
                            partFile,
                            lineNumber,
                            "net_quantity"
                        ),
                        parseBigDecimal(
                            fields[9],
                            partFile,
                            lineNumber,
                            "net_sales"
                        )
                    )
                );
            }
        }
    }

    // 모든 part-r-* 파일을 이름순으로 찾는다.
    private List<Path> findPartFiles(
            Path resultDirectory)
            throws IOException {

        try (
            Stream<Path> paths =
                Files.list(resultDirectory)
        ) {
            return paths
                .filter(Files::isRegularFile)
                .filter(path ->
                    path.getFileName()
                        .toString()
                        .startsWith("part-r-")
                )
                .sorted(
                    Comparator.comparing(
                        path ->
                            path.getFileName()
                                .toString()
                    )
                )
                .collect(Collectors.toList());
        }
    }

    // 디렉터리와 Hadoop 성공 파일을 확인한다.
    private void validateResultDirectory(
            Path resultDirectory)
            throws IOException {

        if (resultDirectory == null) {
            throw new IOException(
                "결과 디렉터리가 null입니다."
            );
        }

        if (!Files.isDirectory(
                resultDirectory)) {

            throw new IOException(
                "결과 디렉터리가 없습니다: "
                    + resultDirectory
            );
        }

        Path successFile =
            resultDirectory.resolve("_SUCCESS");

        if (!Files.isRegularFile(successFile)) {
            throw new IOException(
                "Hadoop _SUCCESS 파일이 없습니다: "
                    + successFile
            );
        }
    }

    private long parseLong(
            String value,
            Path file,
            int lineNumber,
            String fieldName)
            throws IOException {

        try {
            return Long.parseLong(value.trim());

        } catch (NumberFormatException e) {
            throw new IOException(
                "정수 변환 오류: field="
                    + fieldName
                    + ", value="
                    + value
                    + ", file="
                    + file
                    + ", line="
                    + lineNumber,
                e
            );
        }
    }

    private BigDecimal parseBigDecimal(
            String value,
            Path file,
            int lineNumber,
            String fieldName)
            throws IOException {

        try {
            return new BigDecimal(value.trim());

        } catch (NumberFormatException e) {
            throw new IOException(
                "금액 변환 오류: field="
                    + fieldName
                    + ", value="
                    + value
                    + ", file="
                    + file
                    + ", line="
                    + lineNumber,
                e
            );
        }
    }

    private String removeBom(String value) {

        if (value != null
                && value.startsWith("\uFEFF")) {

            return value.substring(1);
        }

        return value;
    }
}