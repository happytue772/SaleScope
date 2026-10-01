package com.salescope.dto;

import java.math.BigDecimal;

public class ProductSalesStatDTO {

    private long jobId;
    private String stockCode;
    private String productName;

    private long salesOrderCount;
    private long soldQuantity;
    private BigDecimal grossSales;

    private long cancelOrderCount;
    private long cancelQuantity;
    private BigDecimal cancelAmount;

    private long netQuantity;
    private BigDecimal netSales;

    public ProductSalesStatDTO() {
    }

    public ProductSalesStatDTO(
            long jobId,
            String stockCode,
            String productName,
            long salesOrderCount,
            long soldQuantity,
            BigDecimal grossSales,
            long cancelOrderCount,
            long cancelQuantity,
            BigDecimal cancelAmount,
            long netQuantity,
            BigDecimal netSales) {

        this.jobId = jobId;
        this.stockCode = stockCode;
        this.productName = productName;
        this.salesOrderCount = salesOrderCount;
        this.soldQuantity = soldQuantity;
        this.grossSales = grossSales;
        this.cancelOrderCount = cancelOrderCount;
        this.cancelQuantity = cancelQuantity;
        this.cancelAmount = cancelAmount;
        this.netQuantity = netQuantity;
        this.netSales = netSales;
    }

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public long getSalesOrderCount() {
        return salesOrderCount;
    }

    public void setSalesOrderCount(long salesOrderCount) {
        this.salesOrderCount = salesOrderCount;
    }

    public long getSoldQuantity() {
        return soldQuantity;
    }

    public void setSoldQuantity(long soldQuantity) {
        this.soldQuantity = soldQuantity;
    }

    public BigDecimal getGrossSales() {
        return grossSales;
    }

    public void setGrossSales(BigDecimal grossSales) {
        this.grossSales = grossSales;
    }

    public long getCancelOrderCount() {
        return cancelOrderCount;
    }

    public void setCancelOrderCount(long cancelOrderCount) {
        this.cancelOrderCount = cancelOrderCount;
    }

    public long getCancelQuantity() {
        return cancelQuantity;
    }

    public void setCancelQuantity(long cancelQuantity) {
        this.cancelQuantity = cancelQuantity;
    }

    public BigDecimal getCancelAmount() {
        return cancelAmount;
    }

    public void setCancelAmount(BigDecimal cancelAmount) {
        this.cancelAmount = cancelAmount;
    }

    public long getNetQuantity() {
        return netQuantity;
    }

    public void setNetQuantity(long netQuantity) {
        this.netQuantity = netQuantity;
    }

    public BigDecimal getNetSales() {
        return netSales;
    }

    public void setNetSales(BigDecimal netSales) {
        this.netSales = netSales;
    }

    @Override
    public String toString() {
        return "ProductSalesStatDTO{" +
                "jobId=" + jobId +
                ", stockCode='" + stockCode + '\'' +
                ", productName='" + productName + '\'' +
                ", salesOrderCount=" + salesOrderCount +
                ", soldQuantity=" + soldQuantity +
                ", grossSales=" + grossSales +
                ", cancelOrderCount=" + cancelOrderCount +
                ", cancelQuantity=" + cancelQuantity +
                ", cancelAmount=" + cancelAmount +
                ", netQuantity=" + netQuantity +
                ", netSales=" + netSales +
                '}';
    }
}