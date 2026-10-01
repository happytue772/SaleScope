package com.salescope.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import com.salescope.dao.AnalysisJobDAO;
import com.salescope.dao.DataQualityDAO;
import com.salescope.dao.DatasetDAO;
import com.salescope.dao.MonthlySalesDAO;
import com.salescope.dao.ProductSalesDAO;
import com.salescope.dto.AnalysisJobDTO;
import com.salescope.dto.DashboardSummaryDTO;
import com.salescope.dto.DataQualityStatDTO;
import com.salescope.dto.DatasetDTO;
import com.salescope.dto.MonthlySalesStatDTO;
import com.salescope.dto.ProductSalesSearchCondition;
import com.salescope.dto.ProductSalesStatDTO;

public class DashboardService {

    private final DatasetDAO datasetDAO;
    private final AnalysisJobDAO analysisJobDAO;
    private final MonthlySalesDAO monthlySalesDAO;
    private final DataQualityDAO dataQualityDAO;
    private final ProductSalesDAO productSalesDAO;

    public DashboardService() {
        this.datasetDAO = new DatasetDAO();
        this.analysisJobDAO = new AnalysisJobDAO();
        this.monthlySalesDAO = new MonthlySalesDAO();
        this.dataQualityDAO = new DataQualityDAO();
        this.productSalesDAO = new ProductSalesDAO();
    }

    public List<DatasetDTO> getDatasets() throws SQLException {
        return datasetDAO.findAll();
    }

    public List<AnalysisJobDTO> getJobs(long datasetId)
            throws SQLException {

        return analysisJobDAO.findByDatasetIdForDashboard(datasetId);
    }

    public List<MonthlySalesStatDTO> getMonthlyStats(long jobId)
            throws SQLException {

        return monthlySalesDAO.findByJobId(jobId);
    }

    public List<DataQualityStatDTO> getQualityStats(long jobId)
            throws SQLException {

        return dataQualityDAO.findByJobId(jobId);
    }

    /**
     * 월별·상품별·품질 결과를 결합해 대시보드 KPI를 만든다.
     */
    public DashboardSummaryDTO createSummary(
            long jobId,
            List<MonthlySalesStatDTO> monthlyStats,
            List<DataQualityStatDTO> qualityStats)
            throws SQLException {

        BigDecimal totalGrossSales = calculateGrossSales(monthlyStats);
        BigDecimal totalNetSales = calculateNetSales(monthlyStats);
        BigDecimal totalCancelAmount = calculateCancelAmount(monthlyStats);
        long totalSalesOrderCount =
            calculateSalesOrderCount(monthlyStats);
        long totalSoldQuantity = calculateSoldQuantity(monthlyStats);

        MonthlySalesStatDTO bestMonth = findBestMonth(monthlyStats);

        List<ProductSalesStatDTO> topProducts =
            productSalesDAO.findByCondition(
                new ProductSalesSearchCondition(
                    jobId,
                    null,
                    "netSales",
                    "desc",
                    1,
                    null,
                    null,
                    null
                )
            );

        ProductSalesStatDTO topProduct = topProducts.isEmpty()
            ? null
            : topProducts.get(0);

        return new DashboardSummaryDTO(
            totalGrossSales,
            totalNetSales,
            totalCancelAmount,
            totalSalesOrderCount,
            totalSoldQuantity,
            bestMonth == null ? null : bestMonth.getSalesMonth(),
            bestMonth == null
                ? BigDecimal.ZERO
                : bestMonth.getNetSales(),
            topProduct == null ? null : topProduct.getStockCode(),
            topProduct == null ? null : topProduct.getProductName(),
            topProduct == null
                ? BigDecimal.ZERO
                : topProduct.getNetSales(),
            productSalesDAO.countByJobId(jobId),
            qualityStats == null ? 0 : qualityStats.size()
        );
    }

    public BigDecimal calculateGrossSales(
            List<MonthlySalesStatDTO> stats) {

        BigDecimal total = BigDecimal.ZERO;
        for (MonthlySalesStatDTO stat : safeMonthlyList(stats)) {
            if (stat.getGrossSales() != null) {
                total = total.add(stat.getGrossSales());
            }
        }
        return total;
    }

    public BigDecimal calculateNetSales(
            List<MonthlySalesStatDTO> stats) {

        BigDecimal total = BigDecimal.ZERO;
        for (MonthlySalesStatDTO stat : safeMonthlyList(stats)) {
            if (stat.getNetSales() != null) {
                total = total.add(stat.getNetSales());
            }
        }
        return total;
    }

    public BigDecimal calculateCancelAmount(
            List<MonthlySalesStatDTO> stats) {

        BigDecimal total = BigDecimal.ZERO;
        for (MonthlySalesStatDTO stat : safeMonthlyList(stats)) {
            if (stat.getCancelAmount() != null) {
                total = total.add(stat.getCancelAmount());
            }
        }
        return total;
    }

    public long calculateSalesOrderCount(
            List<MonthlySalesStatDTO> stats) {

        long total = 0L;
        for (MonthlySalesStatDTO stat : safeMonthlyList(stats)) {
            total += stat.getSalesOrderCount();
        }
        return total;
    }

    public long calculateSoldQuantity(
            List<MonthlySalesStatDTO> stats) {

        long total = 0L;
        for (MonthlySalesStatDTO stat : safeMonthlyList(stats)) {
            total += stat.getSoldQuantity();
        }
        return total;
    }

    private MonthlySalesStatDTO findBestMonth(
            List<MonthlySalesStatDTO> stats) {

        MonthlySalesStatDTO best = null;

        for (MonthlySalesStatDTO stat : safeMonthlyList(stats)) {
            BigDecimal current = stat.getNetSales() == null
                ? BigDecimal.ZERO
                : stat.getNetSales();

            BigDecimal bestValue = best == null
                || best.getNetSales() == null
                ? BigDecimal.ZERO
                : best.getNetSales();

            if (best == null || current.compareTo(bestValue) > 0) {
                best = stat;
            }
        }

        return best;
    }

    private List<MonthlySalesStatDTO> safeMonthlyList(
            List<MonthlySalesStatDTO> stats) {

        return stats == null
            ? Collections.<MonthlySalesStatDTO>emptyList()
            : stats;
    }
}