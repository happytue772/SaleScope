package com.salescope.batch.parser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

public class RetailTransaction {
    private String invoiceNo;
    private String stockCode;
    private String description;
    private long quantity;
    private LocalDateTime invoiceDate;
    private BigDecimal unitPrice;
    private String customerId;
    private String country;
    
    private TransactionType type;
    private String errorMessage;

    // Getter & Setter (단축키 Alt+Shift+S -> Generate Getters and Setters로 자동 생성 가능)
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public String getStockCode() { return stockCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public long getQuantity() { return quantity; }
    public void setQuantity(long quantity) { this.quantity = quantity; }
    public LocalDateTime getInvoiceDate() {  return invoiceDate; }
    public void setInvoiceDate(LocalDateTime invoiceDate) { this.invoiceDate = invoiceDate; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getCustomerId() {return customerId; }
    public void setCustomerId(String customerId) {     this.customerId = customerId; }
    public String getCountry() {      return country; }
    public void setCountry(String country) {      this.country = country;  }
    public TransactionType getType() { return type; } 
    public void setType(TransactionType type) { this.type = type; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getSalesMonth() {

        if (invoiceDate == null) {
            return null;
        }

        return YearMonth.from(invoiceDate).toString();
    }

    // 절대값 매출액 계산 (수량 * 단가)
    public BigDecimal calculateAbsoluteAmount() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(Math.abs(quantity)));
    }
}