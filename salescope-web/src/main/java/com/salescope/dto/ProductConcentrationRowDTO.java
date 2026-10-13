package com.salescope.dto;

import java.math.BigDecimal;

public class ProductConcentrationRowDTO {
    private final String stockCode;
    private final String productName;
    private final BigDecimal netSales;
    private final BigDecimal sharePercent;
    private final BigDecimal cumulativePercent;
    private final String grade;

    public ProductConcentrationRowDTO(String stockCode, String productName,
            BigDecimal netSales, BigDecimal sharePercent,
            BigDecimal cumulativePercent, String grade) {
        this.stockCode = stockCode;
        this.productName = productName;
        this.netSales = netSales;
        this.sharePercent = sharePercent;
        this.cumulativePercent = cumulativePercent;
        this.grade = grade;
    }

    public String getStockCode() { return stockCode; }
    public String getProductName() { return productName; }
    public BigDecimal getNetSales() { return netSales; }
    public BigDecimal getSharePercent() { return sharePercent; }
    public BigDecimal getCumulativePercent() { return cumulativePercent; }
    public String getGrade() { return grade; }
}
