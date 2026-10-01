package com.salescope.dto;

import java.math.BigDecimal;

public class MonthlySalesStatDTO {

    private final long jobId;
    private final String salesMonth;
    private final long salesOrderCount;
    private final long soldQuantity;
    private final BigDecimal grossSales;
    private final long cancelOrderCount;
    private final long cancelQuantity;
    private final BigDecimal cancelAmount;
    private final BigDecimal netSales;

    public MonthlySalesStatDTO(
            long jobId,
            String salesMonth,
            long salesOrderCount,
            long soldQuantity,
            BigDecimal grossSales,
            long cancelOrderCount,
            long cancelQuantity,
            BigDecimal cancelAmount,
            BigDecimal netSales) {

        this.jobId = jobId;
        this.salesMonth = salesMonth;
        this.salesOrderCount = salesOrderCount;
        this.soldQuantity = soldQuantity;
        this.grossSales = grossSales;
        this.cancelOrderCount = cancelOrderCount;
        this.cancelQuantity = cancelQuantity;
        this.cancelAmount = cancelAmount;
        this.netSales = netSales;
    }

    public long getJobId() {
        return jobId;
    }

    public String getSalesMonth() {
        return salesMonth;
    }

    public long getSalesOrderCount() {
        return salesOrderCount;
    }

    public long getSoldQuantity() {
        return soldQuantity;
    }

    public BigDecimal getGrossSales() {
        return grossSales;
    }

    public long getCancelOrderCount() {
        return cancelOrderCount;
    }

    public long getCancelQuantity() {
        return cancelQuantity;
    }

    public BigDecimal getCancelAmount() {
        return cancelAmount;
    }

    public BigDecimal getNetSales() {
        return netSales;
    }
}