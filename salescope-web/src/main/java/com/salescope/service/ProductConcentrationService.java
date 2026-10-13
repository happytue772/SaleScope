package com.salescope.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.salescope.dto.ProductConcentrationDTO;
import com.salescope.dto.ProductConcentrationRowDTO;
import com.salescope.dto.ProductSalesStatDTO;

/** 양수 순매출 상품 전체를 분모로 한 누적 기여도 분석. */
public class ProductConcentrationService {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public ProductConcentrationDTO summarize(List<ProductSalesStatDTO> source) {
        List<ProductSalesStatDTO> products = new ArrayList<>();
        if (source != null) {
            for (ProductSalesStatDTO row : source) {
                if (row != null && row.getNetSales() != null
                        && row.getNetSales().signum() > 0) {
                    products.add(row);
                }
            }
        }
        products.sort(Comparator.comparing(ProductSalesStatDTO::getNetSales)
            .reversed().thenComparing(ProductSalesStatDTO::getStockCode));

        BigDecimal total = BigDecimal.ZERO;
        for (ProductSalesStatDTO row : products) {
            total = total.add(row.getNetSales());
        }

        BigDecimal cumulative = BigDecimal.ZERO;
        BigDecimal topFive = BigDecimal.ZERO;
        int a = 0, b = 0, c = 0;
        List<ProductConcentrationRowDTO> topRows = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            ProductSalesStatDTO row = products.get(i);
            String grade;
            if (cumulative.multiply(HUNDRED).compareTo(total.multiply(BigDecimal.valueOf(80))) < 0) {
                grade = "A";
                a++;
            } else if (cumulative.multiply(HUNDRED).compareTo(total.multiply(BigDecimal.valueOf(95))) < 0) {
                grade = "B";
                b++;
            } else {
                grade = "C";
                c++;
            }
            if (i < 5) {
                topFive = topFive.add(row.getNetSales());
            }
            cumulative = cumulative.add(row.getNetSales());
            if (i < 20) {
                topRows.add(new ProductConcentrationRowDTO(
                    row.getStockCode(), row.getProductName(), row.getNetSales(),
                    percent(row.getNetSales(), total), percent(cumulative, total), grade));
            }
        }
        return new ProductConcentrationDTO(products.size(), a, b, c, total,
            percent(topFive, total), topRows);
    }

    private BigDecimal percent(BigDecimal amount, BigDecimal total) {
        return total.signum() == 0 ? BigDecimal.ZERO
            : amount.multiply(HUNDRED).divide(total, 2, RoundingMode.HALF_UP);
    }
}
