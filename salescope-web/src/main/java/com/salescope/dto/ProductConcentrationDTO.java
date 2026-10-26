package com.salescope.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class ProductConcentrationDTO {
    private final int productCount;
    private final int gradeACount;
    private final int gradeBCount;
    private final int gradeCCount;
    private final BigDecimal positiveNetTotal;
    private final BigDecimal topFivePercent;
    private final List<ProductConcentrationRowDTO> allRows;

    public ProductConcentrationDTO(int productCount, int gradeACount,
            int gradeBCount, int gradeCCount, BigDecimal positiveNetTotal,
            BigDecimal topFivePercent, List<ProductConcentrationRowDTO> allRows) {
        this.productCount = productCount;
        this.gradeACount = gradeACount;
        this.gradeBCount = gradeBCount;
        this.gradeCCount = gradeCCount;
        this.positiveNetTotal = positiveNetTotal;
        this.topFivePercent = topFivePercent;
        this.allRows = Collections.unmodifiableList(new ArrayList<>(allRows));
    }

    public int getProductCount() { return productCount; }
    public int getGradeACount() { return gradeACount; }
    public int getGradeBCount() { return gradeBCount; }
    public int getGradeCCount() { return gradeCCount; }
    public BigDecimal getPositiveNetTotal() { return positiveNetTotal; }
    public BigDecimal getTopFivePercent() { return topFivePercent; }
    public List<ProductConcentrationRowDTO> getTopRows() {
        return allRows.subList(0, Math.min(20, allRows.size()));
    }
    public List<ProductConcentrationRowDTO> getAllRows() { return allRows; }
}
