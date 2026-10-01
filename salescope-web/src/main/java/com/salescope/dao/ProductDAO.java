package com.salescope.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import com.salescope.dto.ProductDTO;

public class ProductDAO {

    private static final String MERGE_SQL =
        "MERGE INTO PRODUCT P " +
        "USING ( " +
        "    SELECT ? AS STOCK_CODE, " +
        "           ? AS PRODUCT_NAME " +
        "    FROM DUAL " +
        ") S " +
        "ON (P.STOCK_CODE = S.STOCK_CODE) " +
        "WHEN MATCHED THEN " +
        "    UPDATE SET " +
        "        P.PRODUCT_NAME = " +
        "            CASE " +
        "                WHEN S.PRODUCT_NAME IS NULL " +
        "                THEN P.PRODUCT_NAME " +
        "                ELSE S.PRODUCT_NAME " +
        "            END, " +
        "        P.UPDATED_AT = SYSTIMESTAMP " +
        "WHEN NOT MATCHED THEN " +
        "    INSERT ( " +
        "        STOCK_CODE, " +
        "        PRODUCT_NAME, " +
        "        CREATED_AT, " +
        "        UPDATED_AT " +
        "    ) " +
        "    VALUES ( " +
        "        S.STOCK_CODE, " +
        "        S.PRODUCT_NAME, " +
        "        SYSTIMESTAMP, " +
        "        SYSTIMESTAMP " +
        "    )";

    public int[] mergeBatch(
            Connection connection,
            List<ProductDTO> products)
            throws SQLException {

        if (connection == null) {
            throw new IllegalArgumentException(
                "connection은 null일 수 없습니다."
            );
        }

        if (products == null || products.isEmpty()) {
            return new int[0];
        }

        try (
            PreparedStatement pstmt =
                connection.prepareStatement(MERGE_SQL)
        ) {
            for (ProductDTO product : products) {

                if (product == null) {
                    throw new IllegalArgumentException(
                        "상품 목록에 null 객체가 있습니다."
                    );
                }

                String stockCode =
                    normalizeRequired(
                        product.getStockCode(),
                        "stockCode"
                    );

                String productName =
                    normalizeNullable(
                        product.getProductName()
                    );

                pstmt.setString(1, stockCode);

                if (productName == null) {
                    pstmt.setNull(
                        2,
                        Types.VARCHAR
                    );
                } else {
                    pstmt.setString(
                        2,
                        productName
                    );
                }

                pstmt.addBatch();
            }

            return pstmt.executeBatch();
        }
    }

    private String normalizeRequired(
            String value,
            String fieldName) {

        if (value == null ||
                value.trim().isEmpty()) {

            throw new IllegalArgumentException(
                fieldName +
                "은 비어 있을 수 없습니다."
            );
        }

        return value.trim();
    }

    private String normalizeNullable(
            String value) {

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed;
    }
}