package com.salescope.service;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.salescope.dao.AnalysisJobDAO;
import com.salescope.dao.DataQualityDAO;
import com.salescope.dao.MonthlySalesDAO;
import com.salescope.dao.ProductDAO;
import com.salescope.dao.ProductSalesDAO;
import com.salescope.dto.DataQualityStatDTO;
import com.salescope.dto.MonthlySalesStatDTO;
import com.salescope.dto.ProductDTO;
import com.salescope.dto.ProductSalesStatDTO;
import com.salescope.util.DBConnectionUtil;
import com.salescope.util.ResultFileReader;

public class ResultImportService {

    private final ResultFileReader fileReader;
    private final MonthlySalesDAO monthlySalesDAO;
    private final DataQualityDAO dataQualityDAO;
    private final AnalysisJobDAO analysisJobDAO;
    private final ProductDAO productDAO;
    private final ProductSalesDAO productSalesDAO;

    public ResultImportService() {

        this.fileReader =
            new ResultFileReader();

        this.monthlySalesDAO =
            new MonthlySalesDAO();

        this.dataQualityDAO =
            new DataQualityDAO();

        this.analysisJobDAO =
            new AnalysisJobDAO();

        this.productDAO =
            new ProductDAO();

        this.productSalesDAO =
            new ProductSalesDAO();
    }

    // 기존 월별 및 품질 결과 적재 기능
    public ImportResult importMonthlyResult(
            long jobId,
            Path resultDirectory)
            throws Exception {

        if (jobId <= 0) {
            throw new IllegalArgumentException(
                "jobId는 1 이상이어야 합니다."
            );
        }

        List<MonthlySalesStatDTO> monthlyList =
            fileReader.readMonthlyResults(
                resultDirectory,
                jobId
            );

        List<DataQualityStatDTO> qualityList =
            fileReader.readQualityResults(
                resultDirectory,
                jobId
            );

        if (monthlyList.isEmpty()) {
            throw new IllegalStateException(
                "월별 적재 데이터가 없습니다."
            );
        }

        try (
            Connection connection =
                DBConnectionUtil.getConnection()
        ) {
            boolean originalAutoCommit =
                connection.getAutoCommit();

            connection.setAutoCommit(false);

            try {
                String status =
                    analysisJobDAO.findStatus(
                        connection,
                        jobId
                    );

                if (status == null) {
                    throw new IllegalStateException(
                        "ANALYSIS_JOB이 없습니다: "
                            + jobId
                    );
                }

                if (!"SUCCESS".equals(status)) {
                    throw new IllegalStateException(
                        "SUCCESS 상태의 Job만 "
                            + "적재할 수 있습니다: "
                            + status
                    );
                }

                if (analysisJobDAO
                        .isResultImported(
                            connection,
                            jobId
                        )) {

                    throw new IllegalStateException(
                        "이미 적재가 완료된 Job입니다: "
                            + jobId
                    );
                }

                dataQualityDAO.deleteByJobId(
                    connection,
                    jobId
                );

                monthlySalesDAO.deleteByJobId(
                    connection,
                    jobId
                );

                monthlySalesDAO.insertBatch(
                    connection,
                    monthlyList
                );

                dataQualityDAO.insertBatch(
                    connection,
                    qualityList
                );

                int updated =
                    analysisJobDAO.updateImportSuccess(
                        connection,
                        jobId,
                        resultDirectory
                            .toAbsolutePath()
                            .normalize()
                            .toString()
                    );

                if (updated != 1) {
                    throw new IllegalStateException(
                        "ANALYSIS_JOB 적재 상태 변경 실패"
                    );
                }

                connection.commit();

                return new ImportResult(
                    monthlyList.size(),
                    qualityList.size()
                );

            } catch (Exception e) {
                connection.rollback();

                try {
                    analysisJobDAO.updateImportFailure(
                        connection,
                        jobId,
                        e.getMessage()
                    );

                    connection.commit();

                } catch (Exception ignored) {
                    connection.rollback();
                }

                throw e;

            } finally {
                connection.setAutoCommit(
                    originalAutoCommit
                );
            }
        }
    }

    // 상품 마스터와 상품별 통계를 하나의 트랜잭션으로 적재한다.
    public ProductImportResult importProductResult(
            long jobId,
            Path resultDirectory)
            throws Exception {

        if (jobId <= 0) {
            throw new IllegalArgumentException(
                "jobId는 1 이상이어야 합니다."
            );
        }

        List<ProductSalesStatDTO> productSalesList =
            fileReader.readProductResults(
                resultDirectory,
                jobId
            );

        if (productSalesList.isEmpty()) {
            throw new IllegalStateException(
                "상품별 적재 데이터가 없습니다."
            );
        }

        List<ProductDTO> productList =
            new ArrayList<>(
                productSalesList.size()
            );

        for (
            ProductSalesStatDTO stat :
                productSalesList
        ) {
            productList.add(
                new ProductDTO(
                    stat.getStockCode(),
                    stat.getProductName()
                )
            );
        }

        try (
            Connection connection =
                DBConnectionUtil.getConnection()
        ) {
            boolean originalAutoCommit =
                connection.getAutoCommit();

            connection.setAutoCommit(false);

            try {
                String status =
                    analysisJobDAO.findStatus(
                        connection,
                        jobId
                    );

                if (status == null) {
                    throw new IllegalStateException(
                        "ANALYSIS_JOB이 없습니다: "
                            + jobId
                    );
                }

                if (!"SUCCESS".equals(status)) {
                    throw new IllegalStateException(
                        "SUCCESS 상태의 Job만 "
                            + "적재할 수 있습니다: "
                            + status
                    );
                }

                /*
                 * 외래키 때문에 PRODUCT를 먼저 MERGE한다.
                 * 동일 상품코드는 INSERT하지 않고 갱신한다.
                 */
                productDAO.mergeBatch(
                    connection,
                    productList
                );

                /*
                 * 같은 JOB_ID 상품 결과를 다시 적재할 수 있도록
                 * 기존 통계를 삭제한 후 새 결과를 삽입한다.
                 */
                productSalesDAO.deleteByJobId(
                    connection,
                    jobId
                );

                productSalesDAO.insertBatch(
                    connection,
                    productSalesList
                );

                connection.commit();

                return new ProductImportResult(
                    productList.size(),
                    productSalesList.size()
                );

            } catch (Exception e) {
                /*
                 * PRODUCT MERGE와 상품 통계 INSERT 중
                 * 하나라도 실패하면 전체를 원상복구한다.
                 */
                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(
                    originalAutoCommit
                );
            }
        }
    }

    // 기존 월별 적재 결과
    public static class ImportResult {

        private final int monthlyCount;
        private final int qualityCount;

        public ImportResult(
                int monthlyCount,
                int qualityCount) {

            this.monthlyCount = monthlyCount;
            this.qualityCount = qualityCount;
        }

        public int getMonthlyCount() {
            return monthlyCount;
        }

        public int getQualityCount() {
            return qualityCount;
        }
    }

    // 새로 추가된 상품 적재 결과
    public static class ProductImportResult {

        private final int productMasterCount;
        private final int productSalesCount;

        public ProductImportResult(
                int productMasterCount,
                int productSalesCount) {

            this.productMasterCount =
                productMasterCount;

            this.productSalesCount =
                productSalesCount;
        }

        public int getProductMasterCount() {
            return productMasterCount;
        }

        public int getProductSalesCount() {
            return productSalesCount;
        }
    }
}