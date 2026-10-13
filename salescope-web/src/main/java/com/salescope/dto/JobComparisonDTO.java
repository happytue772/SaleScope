package com.salescope.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class JobComparisonDTO {

    private final List<JobComparisonRowDTO> rows;
    private final BigDecimal firstTotal;
    private final BigDecimal secondTotal;
    private final BigDecimal totalDifference;
    private final BigDecimal totalDifferencePercent;

    public JobComparisonDTO(List<JobComparisonRowDTO> rows,
            BigDecimal firstTotal, BigDecimal secondTotal,
            BigDecimal totalDifference, BigDecimal totalDifferencePercent) {
        this.rows = Collections.unmodifiableList(rows);
        this.firstTotal = firstTotal;
        this.secondTotal = secondTotal;
        this.totalDifference = totalDifference;
        this.totalDifferencePercent = totalDifferencePercent;
    }

    public List<JobComparisonRowDTO> getRows() { return rows; }
    public BigDecimal getFirstTotal() { return firstTotal; }
    public BigDecimal getSecondTotal() { return secondTotal; }
    public BigDecimal getTotalDifference() { return totalDifference; }
    public BigDecimal getTotalDifferencePercent() { return totalDifferencePercent; }
}
