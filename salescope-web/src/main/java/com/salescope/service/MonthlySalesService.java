package com.salescope.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

import com.salescope.dao.MonthlySalesDAO;
import com.salescope.dto.MonthlySalesPeriodDTO;
import com.salescope.dto.MonthlySalesStatDTO;

public class MonthlySalesService {

    private final MonthlySalesDAO monthlySalesDAO;

    public MonthlySalesService() {
        this.monthlySalesDAO = new MonthlySalesDAO();
    }

    public List<MonthlySalesStatDTO> getMonthlySales(
            long jobId,
            String startMonth,
            String endMonth) throws SQLException {

        MonthlySalesPeriodDTO availablePeriod =
            getAvailablePeriod(jobId);

        MonthlySalesPeriodDTO selectedPeriod =
            resolveSelectedPeriod(
                startMonth,
                endMonth,
                availablePeriod
            );

        return getMonthlySales(
            jobId,
            selectedPeriod,
            availablePeriod
        );
    }

    public List<MonthlySalesStatDTO> getMonthlySales(
            long jobId,
            MonthlySalesPeriodDTO selectedPeriod,
            MonthlySalesPeriodDTO availablePeriod)
            throws SQLException {

        if (jobId <= 0) {
            throw new IllegalArgumentException(
                "jobId는 1 이상이어야 합니다."
            );
        }

        if (selectedPeriod == null) {
            throw new IllegalArgumentException(
                "선택 기간은 null일 수 없습니다."
            );
        }

        if (availablePeriod == null) {
            throw new IllegalArgumentException(
                "조회 가능한 전체 기간은 null일 수 없습니다."
            );
        }

        if (selectedPeriod.getStartMonth().compareTo(
                availablePeriod.getStartMonth()) < 0
                || selectedPeriod.getEndMonth().compareTo(
                    availablePeriod.getEndMonth()) > 0) {
            throw new IllegalArgumentException(
                "조회 기간은 "
                    + availablePeriod.getStartMonth()
                    + "부터 "
                    + availablePeriod.getEndMonth()
                    + " 사이여야 합니다."
            );
        }

        return monthlySalesDAO.findByJobIdAndPeriod(
            jobId,
            selectedPeriod.getStartMonth(),
            selectedPeriod.getEndMonth()
        );
    }

    public MonthlySalesPeriodDTO getAvailablePeriod(
            long jobId) throws SQLException {

    	if (jobId <= 0) {
            throw new IllegalArgumentException(
                "jobId는 1 이상이어야 합니다."
            );
        }

        // 1. DAO에서 월별 목록(List<String>)을 가져옵니다.
        List<String> periodList = monthlySalesDAO.findAvailablePeriod(jobId);

        // 2. 데이터가 비어있는지 확인합니다.
        if (periodList == null || periodList.isEmpty()) {
            throw new IllegalArgumentException(
                "Job #" + jobId + "의 월별 통계가 없습니다."
            );
        }

        // 3. DAO에서 이미 오름차순(ORDER BY) 정렬을 해서 주었으므로,
        // 첫 번째 값이 시작 월, 가장 마지막 값이 종료 월이 됩니다.
        String startMonth = periodList.get(0);
        String endMonth = periodList.get(periodList.size() - 1);

        // 4. 추출한 시작/종료 월로 DTO를 생성해서 반환합니다.
        return new MonthlySalesPeriodDTO(startMonth, endMonth);
    }

    public MonthlySalesPeriodDTO resolveSelectedPeriod(
            String startMonth,
            String endMonth,
            MonthlySalesPeriodDTO availablePeriod) {

        if (availablePeriod == null) {
            throw new IllegalArgumentException(
                "조회 가능한 전체 기간은 null일 수 없습니다."
            );
        }

        String normalizedStart = normalizeMonth(
            startMonth,
            "시작 월",
            availablePeriod.getStartMonth()
        );

        String normalizedEnd = normalizeMonth(
            endMonth,
            "종료 월",
            availablePeriod.getEndMonth()
        );

        MonthlySalesPeriodDTO selectedPeriod =
            new MonthlySalesPeriodDTO(
                normalizedStart,
                normalizedEnd
            );

        if (selectedPeriod.getStartMonth().compareTo(
                availablePeriod.getStartMonth()) < 0
                || selectedPeriod.getEndMonth().compareTo(
                    availablePeriod.getEndMonth()) > 0) {

            throw new IllegalArgumentException(
                "조회 기간은 "
                    + availablePeriod.getStartMonth()
                    + "부터 "
                    + availablePeriod.getEndMonth()
                    + " 사이여야 합니다."
            );
        }

        return selectedPeriod;
    }

    public BigDecimal calculateGrossSales(
            List<MonthlySalesStatDTO> list) {

        BigDecimal total = BigDecimal.ZERO;
        for (MonthlySalesStatDTO item : list) {
            if (item.getGrossSales() != null) {
                total = total.add(item.getGrossSales());
            }
        }
        return total;
    }

    public BigDecimal calculateNetSales(
            List<MonthlySalesStatDTO> list) {

        BigDecimal total = BigDecimal.ZERO;
        for (MonthlySalesStatDTO item : list) {
            if (item.getNetSales() != null) {
                total = total.add(item.getNetSales());
            }
        }
        return total;
    }

    public BigDecimal calculateCancelAmount(
            List<MonthlySalesStatDTO> list) {

        BigDecimal total = BigDecimal.ZERO;
        for (MonthlySalesStatDTO item : list) {
            if (item.getCancelAmount() != null) {
                total = total.add(item.getCancelAmount());
            }
        }
        return total;
    }

    public long calculateSoldQuantity(
            List<MonthlySalesStatDTO> list) {

        long total = 0L;
        for (MonthlySalesStatDTO item : list) {
            total += item.getSoldQuantity();
        }
        return total;
    }

    private String normalizeMonth(
            String value,
            String fieldName,
            String defaultValue) {

        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        String trimmed = value.trim();

        try {
            return YearMonth.parse(trimmed).toString();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                fieldName + "은 YYYY-MM 형식이어야 합니다: " + value,
                e
            );
        }
    }
}
