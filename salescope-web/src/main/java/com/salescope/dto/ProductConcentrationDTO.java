package com.salescope.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProductConcentrationDTO {
    private final int productCount;
    private final int gradeACount;
    private final int gradeBCount;
    private final int gradeCCount;
    private final BigDecimal positiveNetTotal;
    private final BigDecimal topFivePercent;
    private final List<ProductConcentrationRowDTO> topRows;

    public ProductConcentrationDTO(int productCount, int gradeACount,
            int gradeBCount, int gradeCCount, BigDecimal positiveNetTotal,
            BigDecimal topFivePercent, List<ProductConcentrationRowDTO> topRows) {
        this.productCount = productCount;
        this.gradeACount = gradeACount;
        this.gradeBCount = gradeBCount;
        this.gradeCCount = gradeCCount;
        this.positiveNetTotal = positiveNetTotal;
        this.topFivePercent = topFivePercent;
        this.topRows = topRows;
    }

    public int getProductCount() { return productCount; }
    public int getGradeACount() { return gradeACount; }
    public int getGradeBCount() { return gradeBCount; }
    public int getGradeCCount() { return gradeCCount; }
    public BigDecimal getPositiveNetTotal() { return positiveNetTotal; }
    public BigDecimal getTopFivePercent() { return topFivePercent; }
    public List<ProductConcentrationRowDTO> getTopRows() { return topRows; }
}
