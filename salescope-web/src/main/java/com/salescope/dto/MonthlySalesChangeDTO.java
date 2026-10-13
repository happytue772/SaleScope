package com.salescope.dto;

import java.math.BigDecimal;

/** 직전 달과 비교한 순매출 차이. 비교 기준이 0 이하이면 비율은 제공하지 않는다. */
public class MonthlySalesChangeDTO {

    private final BigDecimal changeAmount;
    private final BigDecimal changePercent;

    public MonthlySalesChangeDTO(BigDecimal changeAmount,
            BigDecimal changePercent) {
        this.changeAmount = changeAmount;
        this.changePercent = changePercent;
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public BigDecimal getChangePercent() {
        return changePercent;
    }
}
