package com.salescope.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.salescope.dto.MonthlySalesStatDTO;
import com.salescope.util.DBConnectionUtil;

public class MonthlySalesDAO {

    private static final String DELETE_SQL =
        "DELETE FROM MONTHLY_SALES_STAT "
      + "WHERE JOB_ID = ?";

    private static final String INSERT_SQL =
        "INSERT INTO MONTHLY_SALES_STAT ("
      + "JOB_ID, SALES_MONTH, "
      + "SALES_ORDER_COUNT, SOLD_QUANTITY, "
      + "GROSS_SALES, CANCEL_ORDER_COUNT, "
      + "CANCEL_QUANTITY, CANCEL_AMOUNT, NET_SALES"
      + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public int deleteByJobId(
            Connection connection,
            long jobId) throws SQLException {

        requireConnection(connection);

        try (PreparedStatement pstmt =
                connection.prepareStatement(DELETE_SQL)) {
            pstmt.setLong(1, jobId);
            return pstmt.executeUpdate();
        }
    }

    public int[] insertBatch(
            Connection connection,
            List<MonthlySalesStatDTO> list)
            throws SQLException {

        requireConnection(connection);

        if (list == null || list.isEmpty()) {
            return new int[0];
        }

        try (PreparedStatement pstmt =
                connection.prepareStatement(INSERT_SQL)) {

            for (MonthlySalesStatDTO dto : list) {
                if (dto == null) {
                    throw new IllegalArgumentException(
                        "월별 통계 목록에 null 객체가 있습니다."
                    );
                }

                pstmt.setLong(1, dto.getJobId());
                pstmt.setString(2, dto.getSalesMonth());
                pstmt.setLong(3, dto.getSalesOrderCount());
                pstmt.setLong(4, dto.getSoldQuantity());
                pstmt.setBigDecimal(5, dto.getGrossSales());
                pstmt.setLong(6, dto.getCancelOrderCount());
                pstmt.setLong(7, dto.getCancelQuantity());
                pstmt.setBigDecimal(8, dto.getCancelAmount());
                pstmt.setBigDecimal(9, dto.getNetSales());
                pstmt.addBatch();
            }

            return pstmt.executeBatch();
        }
    }

    public List<MonthlySalesStatDTO> findByJobId(
            Connection connection,
            long jobId) throws SQLException {

        return findByJobIdAndPeriod(
            connection,
            jobId,
            null,
            null
        );
    }

    public List<MonthlySalesStatDTO> findByJobId(
            long jobId) throws SQLException {

        return findByJobIdAndPeriod(jobId, null, null);
    }

    /**
     * 시작 월과 종료 월이 있을 때만 기간조건을 적용한다.
     */
    public List<MonthlySalesStatDTO> findByJobIdAndPeriod(
            long jobId,
            String startMonth,
            String endMonth) throws SQLException {

        try (Connection connection =
                DBConnectionUtil.getConnection()) {

            return findByJobIdAndPeriod(
                connection,
                jobId,
                startMonth,
                endMonth
            );
        }
    }

    public List<MonthlySalesStatDTO> findByJobIdAndPeriod(
            Connection connection,
            long jobId,
            String startMonth,
            String endMonth) throws SQLException {

        requireConnection(connection);

        boolean hasStartMonth =
            startMonth != null && !startMonth.trim().isEmpty();
        boolean hasEndMonth =
            endMonth != null && !endMonth.trim().isEmpty();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT JOB_ID, SALES_MONTH, ");
        sql.append("       SALES_ORDER_COUNT, SOLD_QUANTITY, ");
        sql.append("       GROSS_SALES, CANCEL_ORDER_COUNT, ");
        sql.append("       CANCEL_QUANTITY, CANCEL_AMOUNT, ");
        sql.append("       NET_SALES ");
        sql.append("FROM MONTHLY_SALES_STAT ");
        sql.append("WHERE JOB_ID = ? ");

        if (hasStartMonth) {
            sql.append("  AND SALES_MONTH >= ? ");
        }
        if (hasEndMonth) {
            sql.append("  AND SALES_MONTH <= ? ");
        }

        sql.append("ORDER BY SALES_MONTH");

        List<MonthlySalesStatDTO> results = new ArrayList<>();

        try (PreparedStatement pstmt =
                connection.prepareStatement(sql.toString())) {

            int index = 1;
            pstmt.setLong(index++, jobId);

            if (hasStartMonth) {
                pstmt.setString(index++, startMonth.trim());
            }
            if (hasEndMonth) {
                pstmt.setString(index, endMonth.trim());
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }

        return results;
    }
    
    /**
     * 특정 작업(Job)에 존재하는 분석 대상 월(YYYY-MM) 목록을 중복 없이 조회합니다.
     * 대시보드 화면의 '기간 선택 콤보박스'를 구성하는 데 사용됩니다.
     */
    public List<String> findAvailablePeriod(long jobId) throws SQLException {
        List<String> periods = new ArrayList<>();
        
        String sql = "SELECT DISTINCT SALES_MONTH FROM MONTHLY_SALES_STAT WHERE JOB_ID = ? ORDER BY SALES_MONTH";

        try (Connection connection = DBConnectionUtil.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, jobId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    periods.add(rs.getString("SALES_MONTH"));
                }
            }
        }
        return periods;
    }

    private MonthlySalesStatDTO mapRow(ResultSet rs)
            throws SQLException {

        return new MonthlySalesStatDTO(
            rs.getLong("JOB_ID"),
            rs.getString("SALES_MONTH"),
            rs.getLong("SALES_ORDER_COUNT"),
            rs.getLong("SOLD_QUANTITY"),
            rs.getBigDecimal("GROSS_SALES"),
            rs.getLong("CANCEL_ORDER_COUNT"),
            rs.getLong("CANCEL_QUANTITY"),
            rs.getBigDecimal("CANCEL_AMOUNT"),
            rs.getBigDecimal("NET_SALES")
        );
    }

    private void requireConnection(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException(
                "connection은 null일 수 없습니다."
            );
        }
    }
}