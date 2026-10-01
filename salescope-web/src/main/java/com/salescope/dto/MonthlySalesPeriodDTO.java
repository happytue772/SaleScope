package com.salescope.dto;

/**
 * 월별 매출 조회에서 사용할 시작 월과 종료 월을 전달한다.
 */
public class MonthlySalesPeriodDTO {

    private final String startMonth;
    private final String endMonth;

    public MonthlySalesPeriodDTO(
            String startMonth,
            String endMonth) {

        if (startMonth == null || startMonth.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "startMonth는 비어 있을 수 없습니다."
            );
        }

        if (endMonth == null || endMonth.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "endMonth는 비어 있을 수 없습니다."
            );
        }

        String normalizedStart = startMonth.trim();
        String normalizedEnd = endMonth.trim();

        if (normalizedStart.compareTo(normalizedEnd) > 0) {
            throw new IllegalArgumentException(
                "시작 월은 종료 월보다 늦을 수 없습니다."
            );
        }

        this.startMonth = normalizedStart;
        this.endMonth = normalizedEnd;
    }

    public String getStartMonth() {
        return startMonth;
    }

    public String getEndMonth() {
        return endMonth;
    }
}