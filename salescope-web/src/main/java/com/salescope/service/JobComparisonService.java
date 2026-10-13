package com.salescope.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.salescope.dto.JobComparisonDTO;
import com.salescope.dto.JobComparisonRowDTO;
import com.salescope.dto.MonthlySalesStatDTO;

/** 두 Job에 모두 존재하는 월의 순매출만 비교한다. */
public class JobComparisonService {

    public JobComparisonDTO compare(List<MonthlySalesStatDTO> first,
            List<MonthlySalesStatDTO> second) {
        Map<String, BigDecimal> secondByMonth = new HashMap<>();
        for (MonthlySalesStatDTO row : second) {
            if (row.getNetSales() != null) {
                secondByMonth.put(row.getSalesMonth(), row.getNetSales());
            }
        }

        List<JobComparisonRowDTO> rows = new ArrayList<>();
        BigDecimal firstTotal = BigDecimal.ZERO;
        BigDecimal secondTotal = BigDecimal.ZERO;
        for (MonthlySalesStatDTO row : first) {
            BigDecimal secondNet = secondByMonth.get(row.getSalesMonth());
            BigDecimal firstNet = row.getNetSales();
            if (firstNet == null || secondNet == null) {
                continue;
            }

            BigDecimal difference = secondNet.subtract(firstNet);
            rows.add(new JobComparisonRowDTO(row.getSalesMonth(), firstNet,
                secondNet, difference, percent(difference, firstNet)));
            firstTotal = firstTotal.add(firstNet);
            secondTotal = secondTotal.add(secondNet);
        }

        BigDecimal totalDifference = secondTotal.subtract(firstTotal);
        return new JobComparisonDTO(rows, firstTotal, secondTotal,
            totalDifference, percent(totalDifference, firstTotal));
    }

    private BigDecimal percent(BigDecimal difference, BigDecimal base) {
        return base.signum() > 0
            ? difference.multiply(BigDecimal.valueOf(100))
                .divide(base, 2, RoundingMode.HALF_UP)
            : null;
    }
}
