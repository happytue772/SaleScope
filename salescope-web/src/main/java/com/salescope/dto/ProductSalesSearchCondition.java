package com.salescope.dto;

import java.math.BigDecimal;

/**
 * 상품별 조회조건을 전달하는 DTO
 */
public class ProductSalesSearchCondition {

    private final long jobId;
    private final String keyword;
    private final String sortBy;
    private final String sortDirection;
    private final int rowLimit;
    private final BigDecimal minNetSales;
    private final Long minSoldQuantity;
    private final BigDecimal minCancelAmount;

    public ProductSalesSearchCondition(
            long jobId,
            String keyword,
            String sortBy,
            String sortDirection,
            int rowLimit,
            BigDecimal minNetSales,
            Long minSoldQuantity,
            BigDecimal minCancelAmount) {

        this.jobId = jobId;
        this.keyword = keyword;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
        this.rowLimit = rowLimit;
        this.minNetSales = minNetSales;
        this.minSoldQuantity = minSoldQuantity;
        this.minCancelAmount = minCancelAmount;
    }

    public long getJobId() {
        return jobId;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getSortBy() {
        return sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public int getRowLimit() {
        return rowLimit;
    }

    public BigDecimal getMinNetSales() {
        return minNetSales;
    }

    public Long getMinSoldQuantity() {
        return minSoldQuantity;
    }

    public BigDecimal getMinCancelAmount() {
        return minCancelAmount;
    }
}