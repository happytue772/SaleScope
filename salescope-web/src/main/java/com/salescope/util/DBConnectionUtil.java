package com.salescope.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnectionUtil {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (
            InputStream input =
                DBConnectionUtil.class
                    .getClassLoader()
                    .getResourceAsStream("db.properties")
        ) {
            if (input == null) {
                throw new IllegalStateException(
                    "db.properties 파일을 찾을 수 없습니다."
                );
            }

            PROPERTIES.load(
                new InputStreamReader(
                    input,
                    StandardCharsets.UTF_8
                )
            );

            Class.forName(requiredProperty("db.driver"));

        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBConnectionUtil() {
        // 객체 생성 방지
    }

    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(
            requiredProperty("db.url"),
            requiredProperty("db.user"),
            requiredProperty("db.password")
        );
    }

    private static String requiredProperty(String key) {

        String value = PROPERTIES.getProperty(key);

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                "필수 DB 설정이 없습니다: " + key
            );
        }

        return value.trim();
    }
}