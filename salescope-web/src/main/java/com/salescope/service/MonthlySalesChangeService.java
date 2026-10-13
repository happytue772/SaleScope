package com.salescope.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.salescope.dto.MonthlySalesChangeDTO;
import com.salescope.dto.MonthlySalesStatDTO;

/** 조회 결과에 직전 달이 있을 때만 순매출의 전월 대비 변화를 계산한다. */
public class MonthlySalesChangeService {

    public Map<String, MonthlySalesChangeDTO> calculate(
            List<MonthlySalesStatDTO> rows) {
        Map<String, MonthlySalesChangeDTO> changes = new LinkedHashMap<>();
        if (rows == null) {
            return changes;
        }

        MonthlySalesStatDTO previous = null;
        for (MonthlySalesStatDTO current : rows) {
            if (previous != null && previous.getNetSales() != null
                    && current.getNetSales() != null
                    && YearMonth.parse(previous.getSalesMonth()).plusMonths(1)
                        .equals(YearMonth.parse(current.getSalesMonth()))) {
                BigDecimal amount = current.getNetSales()
                    .subtract(previous.getNetSales());
                BigDecimal percent = null;
                if (previous.getNetSales().signum() > 0) {
                    percent = amount.multiply(BigDecimal.valueOf(100))
                        .divide(previous.getNetSales(), 2, RoundingMode.HALF_UP);
                }
                changes.put(current.getSalesMonth(),
                    new MonthlySalesChangeDTO(amount, percent));
            }
            previous = current;
        }
        return changes;
    }
}
