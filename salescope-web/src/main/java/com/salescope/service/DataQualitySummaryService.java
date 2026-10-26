package com.salescope.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.salescope.dto.DataQualityStatDTO;
import com.salescope.dto.DataQualitySummaryDTO;

/** 결측 속성·헤더와 중복 계산하지 않고 배타적 분류 6종만 합산한다. */
public class DataQualitySummaryService {

    public DataQualitySummaryDTO summarize(List<DataQualityStatDTO> stats) {
        long included = 0;
        long excluded = 0;

        if (stats != null) {
            for (DataQualityStatDTO stat : stats) {
                if (stat == null || stat.getQualityType() == null) {
                    continue;
                }

                String type = stat.getQualityType();
                if ("NORMAL".equals(type) || "CANCELLED".equals(type)) {
                    included = Math.addExact(included, stat.getRecordCount());
                } else if ("ADJUSTMENT".equals(type)
                        || "ZERO_OR_NEG_PRICE".equals(type)
                        || "UNCLASSIFIED".equals(type)
                        || "PARSE_ERROR".equals(type)) {
                    excluded = Math.addExact(excluded, stat.getRecordCount());
                }
            }
        }

        long total = Math.addExact(included, excluded);
        BigDecimal percent = total == 0 ? BigDecimal.ZERO
            : BigDecimal.valueOf(excluded)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);

        return new DataQualitySummaryDTO(total, included, excluded, percent);
    }
}
