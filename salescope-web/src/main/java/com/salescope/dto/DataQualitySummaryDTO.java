package com.salescope.dto;

import java.math.BigDecimal;

/** 배타적 거래 분류를 바탕으로 만든 Job별 품질 요약. */
public class DataQualitySummaryDTO {

    private final long totalRecords;
    private final long includedRecords;
    private final long excludedRecords;
    private final BigDecimal excludedPercent;

    public DataQualitySummaryDTO(long totalRecords,
            long includedRecords, long excludedRecords,
            BigDecimal excludedPercent) {
        this.totalRecords = totalRecords;
        this.includedRecords = includedRecords;
        this.excludedRecords = excludedRecords;
        this.excludedPercent = excludedPercent;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public long getIncludedRecords() {
        return includedRecords;
    }

    public long getExcludedRecords() {
        return excludedRecords;
    }

    public BigDecimal getExcludedPercent() {
        return excludedPercent;
    }
}
