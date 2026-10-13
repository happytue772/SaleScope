package com.salescope.dto;

import java.math.BigDecimal;

public class JobComparisonRowDTO {

    private final String salesMonth;
    private final BigDecimal firstNetSales;
    private final BigDecimal secondNetSales;
    private final BigDecimal difference;
    private final BigDecimal differencePercent;

    public JobComparisonRowDTO(String salesMonth, BigDecimal firstNetSales,
            BigDecimal secondNetSales, BigDecimal difference,
            BigDecimal differencePercent) {
        this.salesMonth = salesMonth;
        this.firstNetSales = firstNetSales;
        this.secondNetSales = secondNetSales;
        this.difference = difference;
        this.differencePercent = differencePercent;
    }

    public String getSalesMonth() { return salesMonth; }
    public BigDecimal getFirstNetSales() { return firstNetSales; }
    public BigDecimal getSecondNetSales() { return secondNetSales; }
    public BigDecimal getDifference() { return difference; }
    public BigDecimal getDifferencePercent() { return differencePercent; }
}
