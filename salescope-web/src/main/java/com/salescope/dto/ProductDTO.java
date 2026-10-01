package com.salescope.dto;

public class ProductDTO {

    private String stockCode;
    private String productName;

    public ProductDTO() {
    }

    public ProductDTO(
            String stockCode,
            String productName) {

        this.stockCode = stockCode;
        this.productName = productName;
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

    @Override
    public String toString() {
        return "ProductDTO{" +
                "stockCode='" + stockCode + '\'' +
                ", productName='" + productName + '\'' +
                '}';
    }
}