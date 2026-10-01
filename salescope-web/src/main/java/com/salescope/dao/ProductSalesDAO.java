package com.salescope.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

import com.salescope.dto.ProductSalesSearchCondition;
import com.salescope.dto.ProductSalesStatDTO;
import com.salescope.util.DBConnectionUtil;

public class ProductSalesDAO {

    private static final int MAX_ROW_LIMIT = 500;

    private static final String DELETE_SQL =
        "DELETE FROM PRODUCT_SALES_STAT "
      + "WHERE JOB_ID = ?";

    private static final String INSERT_SQL =
        "INSERT INTO PRODUCT_SALES_STAT ("
      + "JOB_ID, STOCK_CODE, "
      + "SALES_ORDER_COUNT, SOLD_QUANTITY, "
      + "GROSS_SALES, CANCEL_ORDER_COUNT, "
      + "CANCEL_QUANTITY, CANCEL_AMOUNT, "
      + "NET_QUANTITY, NET_SALES"
      + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String COUNT_SQL =
        "SELECT COUNT(*) "
      + "FROM PRODUCT_SALES_STAT "
      + "WHERE JOB_ID = ?";

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
            List<ProductSalesStatDTO> stats)
            throws SQLException {

        requireConnection(connection);

        if (stats == null || stats.isEmpty()) {
            return new int[0];
        }

        try (PreparedStatement pstmt =
                connection.prepareStatement(INSERT_SQL)) {

            for (ProductSalesStatDTO stat : stats) {
                if (stat == null) {
                    throw new IllegalArgumentException(
                        "상품 통계 목록에 null 객체가 있습니다."
                    );
                }

                pstmt.setLong(1, stat.getJobId());
                pstmt.setString(2, stat.getStockCode());
                pstmt.setLong(3, stat.getSalesOrderCount());
                pstmt.setLong(4, stat.getSoldQuantity());
                pstmt.setBigDecimal(5, stat.getGrossSales());
                pstmt.setLong(6, stat.getCancelOrderCount());
                pstmt.setLong(7, stat.getCancelQuantity());
                pstmt.setBigDecimal(8, stat.getCancelAmount());
                pstmt.setLong(9, stat.getNetQuantity());
                pstmt.setBigDecimal(10, stat.getNetSales());
                pstmt.addBatch();
            }

            return pstmt.executeBatch();
        }
    }

    /**
     * 검색어, 필터, 정렬, 조회 행 수를 적용해 상품 통계를 조회한다.
     */
    public List<ProductSalesStatDTO> findByCondition(
            ProductSalesSearchCondition condition)
            throws SQLException {

        validateCondition(condition);

        String keyword = normalizeKeyword(condition.getKeyword());
        boolean hasKeyword = keyword != null;
        boolean hasMinNetSales = condition.getMinNetSales() != null;
        boolean hasMinSoldQuantity =
            condition.getMinSoldQuantity() != null;
        boolean hasMinCancelAmount =
            condition.getMinCancelAmount() != null;

        String orderColumn =
            resolveOrderColumn(condition.getSortBy());
        String orderDirection =
            resolveOrderDirection(condition.getSortDirection());

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT JOB_ID, STOCK_CODE, PRODUCT_NAME, ");
        sql.append("       SALES_ORDER_COUNT, SOLD_QUANTITY, ");
        sql.append("       GROSS_SALES, CANCEL_ORDER_COUNT, ");
        sql.append("       CANCEL_QUANTITY, CANCEL_AMOUNT, ");
        sql.append("       NET_QUANTITY, NET_SALES ");
        sql.append("FROM ( ");
        sql.append("    SELECT PS.JOB_ID, PS.STOCK_CODE, ");
        sql.append("           P.PRODUCT_NAME, ");
        sql.append("           PS.SALES_ORDER_COUNT, ");
        sql.append("           PS.SOLD_QUANTITY, PS.GROSS_SALES, ");
        sql.append("           PS.CANCEL_ORDER_COUNT, ");
        sql.append("           PS.CANCEL_QUANTITY, ");
        sql.append("           PS.CANCEL_AMOUNT, ");
        sql.append("           PS.NET_QUANTITY, PS.NET_SALES ");
        sql.append("    FROM PRODUCT_SALES_STAT PS ");
        sql.append("    JOIN PRODUCT P ");
        sql.append("      ON P.STOCK_CODE = PS.STOCK_CODE ");
        sql.append("    WHERE PS.JOB_ID = ? ");

        if (hasKeyword) {
            sql.append("      AND (UPPER(PS.STOCK_CODE) LIKE ? ");
            sql.append("           OR UPPER(P.PRODUCT_NAME) LIKE ?) ");
        }

        if (hasMinNetSales) {
            sql.append("      AND PS.NET_SALES >= ? ");
        }

        if (hasMinSoldQuantity) {
            sql.append("      AND PS.SOLD_QUANTITY >= ? ");
        }

        if (hasMinCancelAmount) {
            sql.append("      AND PS.CANCEL_AMOUNT >= ? ");
        }

        sql.append("    ORDER BY ");
        sql.append(orderColumn);
        sql.append(" ");
        sql.append(orderDirection);
        sql.append(", PS.STOCK_CODE ASC ");
        sql.append(") ");
        sql.append("WHERE ROWNUM <= ?");

        List<ProductSalesStatDTO> results = new ArrayList<>();

        try (
            Connection connection = DBConnectionUtil.getConnection();
            PreparedStatement pstmt =
                connection.prepareStatement(sql.toString())
        ) {
            int index = 1;

            pstmt.setLong(index++, condition.getJobId());

            if (hasKeyword) {
                String likeKeyword = "%"
                    + keyword.toUpperCase(Locale.ROOT)
                    + "%";

                pstmt.setString(index++, likeKeyword);
                pstmt.setString(index++, likeKeyword);
            }

            if (hasMinNetSales) {
                pstmt.setBigDecimal(index++, condition.getMinNetSales());
            }

            if (hasMinSoldQuantity) {
                pstmt.setLong(index++, condition.getMinSoldQuantity());
            }

            if (hasMinCancelAmount) {
                pstmt.setBigDecimal(index++, condition.getMinCancelAmount());
            }

            pstmt.setInt(index, condition.getRowLimit());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }

        return results;
    }

    /**
     * 기존 호출부와의 호환성을 유지하는 간편 조회 메서드다.
     */
    public List<ProductSalesStatDTO> findTopN(
            long jobId,
            String sortBy,
            int topN,
            String keyword) throws SQLException {

        return findByCondition(
            new ProductSalesSearchCondition(
                jobId,
                keyword,
                sortBy,
                "desc",
                topN,
                null,
                null,
                null
            )
        );
    }

    public long countByJobId(long jobId) throws SQLException {
        if (jobId <= 0) {
            throw new IllegalArgumentException(
                "jobId는 1 이상이어야 합니다."
            );
        }

        try (
            Connection connection = DBConnectionUtil.getConnection();
            PreparedStatement pstmt =
                connection.prepareStatement(COUNT_SQL)
        ) {
            pstmt.setLong(1, jobId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }

    /**
     * 화면의 검색창 자동완성(Datalist)을 위해 해당 Job의 모든 상품 목록을 조회합니다.
     */
    public List<ProductSalesStatDTO> findDistinctProducts(long jobId) throws SQLException {
        String sql = 
            "SELECT DISTINCT PS.STOCK_CODE, P.PRODUCT_NAME " +
            "FROM PRODUCT_SALES_STAT PS " +
            "JOIN PRODUCT P ON P.STOCK_CODE = PS.STOCK_CODE " +
            "WHERE PS.JOB_ID = ? " +
            "ORDER BY P.PRODUCT_NAME ASC";

        List<ProductSalesStatDTO> results = new ArrayList<>();

        try (
            Connection connection = DBConnectionUtil.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            pstmt.setLong(1, jobId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // 화면 표시용으로 코드와 이름만 담아서 반환합니다.
                    ProductSalesStatDTO dto = new ProductSalesStatDTO();
                    dto.setStockCode(rs.getString("STOCK_CODE"));
                    dto.setProductName(rs.getString("PRODUCT_NAME"));
                    results.add(dto);
                }
            }
        }
        return results;
    }

    private ProductSalesStatDTO mapRow(ResultSet rs)
            throws SQLException {

        return new ProductSalesStatDTO(
            rs.getLong("JOB_ID"),
            rs.getString("STOCK_CODE"),
            rs.getString("PRODUCT_NAME"),
            rs.getLong("SALES_ORDER_COUNT"),
            rs.getLong("SOLD_QUANTITY"),
            rs.getBigDecimal("GROSS_SALES"),
            rs.getLong("CANCEL_ORDER_COUNT"),
            rs.getLong("CANCEL_QUANTITY"),
            rs.getBigDecimal("CANCEL_AMOUNT"),
            rs.getLong("NET_QUANTITY"),
            rs.getBigDecimal("NET_SALES")
        );
    }

    private void validateCondition(
            ProductSalesSearchCondition condition) {

        if (condition == null) {
            throw new IllegalArgumentException(
                "상품 조회조건은 null일 수 없습니다."
            );
        }

        if (condition.getJobId() <= 0) {
            throw new IllegalArgumentException(
                "jobId는 1 이상이어야 합니다."
            );
        }

        if (condition.getRowLimit() < 1
                || condition.getRowLimit() > MAX_ROW_LIMIT) {

            throw new IllegalArgumentException(
                "조회 행 수는 1 이상 "
                    + MAX_ROW_LIMIT
                    + " 이하여야 합니다."
            );
        }

        if (condition.getMinNetSales() != null
                && condition.getMinNetSales().signum() < 0) {
            throw new IllegalArgumentException(
                "최소 순매출은 0 이상이어야 합니다."
            );
        }

        if (condition.getMinSoldQuantity() != null
                && condition.getMinSoldQuantity() < 0) {
            throw new IllegalArgumentException(
                "최소 판매량은 0 이상이어야 합니다."
            );
        }

        if (condition.getMinCancelAmount() != null
                && condition.getMinCancelAmount().signum() < 0) {
            throw new IllegalArgumentException(
                "최소 취소금액은 0 이상이어야 합니다."
            );
        }

        resolveOrderColumn(condition.getSortBy());
        resolveOrderDirection(condition.getSortDirection());
    }

    private String resolveOrderColumn(String sortBy) {
        if ("soldQuantity".equals(sortBy)) {
            return "PS.SOLD_QUANTITY";
        }
        if ("salesOrderCount".equals(sortBy)) {
            return "PS.SALES_ORDER_COUNT";
        }
        if ("cancelAmount".equals(sortBy)) {
            return "PS.CANCEL_AMOUNT";
        }
        if ("netSales".equals(sortBy)) {
            return "PS.NET_SALES";
        }

        throw new IllegalArgumentException(
            "지원하지 않는 정렬 기준입니다: " + sortBy
        );
    }

    private String resolveOrderDirection(String direction) {
        if ("asc".equals(direction)) {
            return "ASC";
        }
        if ("desc".equals(direction)) {
            return "DESC";
        }

        throw new IllegalArgumentException(
            "정렬 방향은 asc 또는 desc여야 합니다: " + direction
        );
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void requireConnection(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException(
                "connection은 null일 수 없습니다."
            );
        }
    }
    public long countByCondition(ProductSalesSearchCondition condition) throws SQLException {
        if (condition == null || condition.getJobId() <= 0) return 0;

        String keyword = normalizeKeyword(condition.getKeyword());
        boolean hasKeyword = keyword != null;
        boolean hasMinNetSales = condition.getMinNetSales() != null;
        boolean hasMinSoldQuantity = condition.getMinSoldQuantity() != null;
        boolean hasMinCancelAmount = condition.getMinCancelAmount() != null;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) ");
        sql.append("FROM PRODUCT_SALES_STAT PS ");
        sql.append("JOIN PRODUCT P ON P.STOCK_CODE = PS.STOCK_CODE ");
        sql.append("WHERE PS.JOB_ID = ? ");

        if (hasKeyword) {
            sql.append("  AND (UPPER(PS.STOCK_CODE) LIKE ? OR UPPER(P.PRODUCT_NAME) LIKE ?) ");
        }
        if (hasMinNetSales) {
            sql.append("  AND PS.NET_SALES >= ? ");
        }
        if (hasMinSoldQuantity) {
            sql.append("  AND PS.SOLD_QUANTITY >= ? ");
        }
        if (hasMinCancelAmount) {
            sql.append("  AND PS.CANCEL_AMOUNT >= ? ");
        }

        try (
            Connection connection = DBConnectionUtil.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(sql.toString())
        ) {
            int index = 1;
            pstmt.setLong(index++, condition.getJobId());

            if (hasKeyword) {
                String likeKeyword = "%" + keyword.toUpperCase(Locale.ROOT) + "%";
                pstmt.setString(index++, likeKeyword);
                pstmt.setString(index++, likeKeyword);
            }
            if (hasMinNetSales) {
                pstmt.setBigDecimal(index++, condition.getMinNetSales());
            }
            if (hasMinSoldQuantity) {
                pstmt.setLong(index++, condition.getMinSoldQuantity());
            }
            if (hasMinCancelAmount) {
                pstmt.setBigDecimal(index++, condition.getMinCancelAmount());
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }
    public Map<String, Number> getSalesDataRange(long jobId) throws SQLException {
        String sql = 
            "SELECT " +
            "  MAX(NET_SALES) AS max_net, MIN(NET_SALES) AS min_net, " +
            "  MAX(SOLD_QUANTITY) AS max_qty, MIN(SOLD_QUANTITY) AS min_qty, " +
            "  MAX(CANCEL_AMOUNT) AS max_cancel, MIN(CANCEL_AMOUNT) AS min_cancel " +
            "FROM PRODUCT_SALES_STAT " +
            "WHERE JOB_ID = ?";

        Map<String, Number> range = new HashMap<>();

        try (
            Connection connection = DBConnectionUtil.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            pstmt.setLong(1, jobId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // JSP에서 부르기 쉽도록 키(Key) 값을 지정해 담아줍니다.
                    range.put("maxNetSales", rs.getBigDecimal("max_net"));
                    range.put("minNetSales", rs.getBigDecimal("min_net"));
                    range.put("maxQty", rs.getLong("max_qty"));
                    range.put("minQty", rs.getLong("min_qty"));
                    range.put("maxCancel", rs.getBigDecimal("max_cancel"));
                    range.put("minCancel", rs.getBigDecimal("min_cancel"));
                }
            }
        }
        return range;
    }
}