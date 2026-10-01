package com.salescope.dto;

import java.math.BigDecimal;

/**
 * 대시보드 KPI 카드에 표시할 요약값을 전달한다.
 */
public class DashboardSummaryDTO {

    private final BigDecimal totalGrossSales;
    private final BigDecimal totalNetSales;
    private final BigDecimal totalCancelAmount;
    private final long totalSalesOrderCount;
    private final long totalSoldQuantity;
    private final String bestSalesMonth;
    private final BigDecimal bestMonthNetSales;
    private final String topProductCode;
    private final String topProductName;
    private final BigDecimal topProductNetSales;
    private final long productCount;
    private final int qualityTypeCount;

    public DashboardSummaryDTO(
            BigDecimal totalGrossSales,
            BigDecimal totalNetSales,
            BigDecimal totalCancelAmount,
            long totalSalesOrderCount,
            long totalSoldQuantity,
            String bestSalesMonth,
            BigDecimal bestMonthNetSales,
            String topProductCode,
            String topProductName,
            BigDecimal topProductNetSales,
            long productCount,
            int qualityTypeCount) {

        this.totalGrossSales = zeroIfNull(totalGrossSales);
        this.totalNetSales = zeroIfNull(totalNetSales);
        this.totalCancelAmount = zeroIfNull(totalCancelAmount);
        this.totalSalesOrderCount = totalSalesOrderCount;
        this.totalSoldQuantity = totalSoldQuantity;
        this.bestSalesMonth = bestSalesMonth;
        this.bestMonthNetSales = zeroIfNull(bestMonthNetSales);
        this.topProductCode = topProductCode;
        this.topProductName = topProductName;
        this.topProductNetSales = zeroIfNull(topProductNetSales);
        this.productCount = productCount;
        this.qualityTypeCount = qualityTypeCount;
    }

    public static DashboardSummaryDTO empty() {
        return new DashboardSummaryDTO(
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            0L,
            0L,
            null,
            BigDecimal.ZERO,
            null,
            null,
            BigDecimal.ZERO,
            0L,
            0
        );
    }

    private static BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public BigDecimal getTotalGrossSales() {
        return totalGrossSales;
    }

    public BigDecimal getTotalNetSales() {
        return totalNetSales;
    }

    public BigDecimal getTotalCancelAmount() {
        return totalCancelAmount;
    }

    public long getTotalSalesOrderCount() {
        return totalSalesOrderCount;
    }

    public long getTotalSoldQuantity() {
        return totalSoldQuantity;
    }

    public String getBestSalesMonth() {
        return bestSalesMonth;
    }

    public BigDecimal getBestMonthNetSales() {
        return bestMonthNetSales;
    }

    public String getTopProductCode() {
        return topProductCode;
    }

    public String getTopProductName() {
        return topProductName;
    }

    public BigDecimal getTopProductNetSales() {
        return topProductNetSales;
    }

    public long getProductCount() {
        return productCount;
    }

    public int getQualityTypeCount() {
        return qualityTypeCount;
    }
}