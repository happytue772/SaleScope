package com.salescope.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.salescope.dto.DataQualityStatDTO;
import com.salescope.util.DBConnectionUtil;

public class DataQualityDAO {

    private static final String DELETE_SQL =
        "DELETE FROM DATA_QUALITY_STAT "
      + "WHERE JOB_ID = ?";

    private static final String INSERT_SQL =
        "INSERT INTO DATA_QUALITY_STAT ("
      + "JOB_ID, QUALITY_TYPE, "
      + "RECORD_COUNT, SAMPLE_MESSAGE"
      + ") VALUES (?, ?, ?, ?)";

    private static final String SELECT_SQL =
        "SELECT JOB_ID, QUALITY_TYPE, "
      + "RECORD_COUNT, SAMPLE_MESSAGE "
      + "FROM DATA_QUALITY_STAT "
      + "WHERE JOB_ID = ? "
      + "ORDER BY QUALITY_TYPE";

    /**
     * 특정 Job의 기존 데이터 품질 통계를 삭제한다.
     *
     * 외부 Service에서 전달한 Connection을 사용한다.
     */
    public int deleteByJobId(
            Connection connection,
            long jobId) throws SQLException {

        try (
            PreparedStatement pstmt =
                connection.prepareStatement(DELETE_SQL)
        ) {

            pstmt.setLong(1, jobId);

            return pstmt.executeUpdate();
        }
    }

    /**
     * 데이터 품질 통계 목록을 Batch INSERT한다.
     *
     * 외부 Service에서 전달한 Connection을 사용한다.
     */
    public int[] insertBatch(
            Connection connection,
            List<DataQualityStatDTO> list)
            throws SQLException {

        try (
            PreparedStatement pstmt =
                connection.prepareStatement(INSERT_SQL)
        ) {

            for (DataQualityStatDTO dto : list) {

                pstmt.setLong(
                    1,
                    dto.getJobId()
                );

                pstmt.setString(
                    2,
                    dto.getQualityType()
                );

                pstmt.setLong(
                    3,
                    dto.getRecordCount()
                );

                pstmt.setString(
                    4,
                    dto.getSampleMessage()
                );

                pstmt.addBatch();
            }

            return pstmt.executeBatch();
        }
    }

    /**
     * 외부에서 전달된 Connection으로
     * 특정 Job의 데이터 품질 통계를 조회한다.
     */
    public List<DataQualityStatDTO> findByJobId(
            Connection connection,
            long jobId) throws SQLException {

        List<DataQualityStatDTO> results =
            new ArrayList<>();

        try (
            PreparedStatement pstmt =
                connection.prepareStatement(SELECT_SQL)
        ) {

            pstmt.setLong(1, jobId);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {

                    DataQualityStatDTO dto =
                        new DataQualityStatDTO(
                            rs.getLong("JOB_ID"),
                            rs.getString(
                                "QUALITY_TYPE"
                            ),
                            rs.getLong(
                                "RECORD_COUNT"
                            ),
                            rs.getString(
                                "SAMPLE_MESSAGE"
                            )
                        );

                    results.add(dto);
                }
            }
        }

        return results;
    }

    /**
     * 대시보드에서 사용할 간편 조회 메서드다.
     *
     * DAO 내부에서 Connection을 생성하고
     * 기존 findByJobId(Connection, long)를 재사용한다.
     */
    public List<DataQualityStatDTO> findByJobId(
            long jobId) throws SQLException {

        try (
            Connection connection =
                DBConnectionUtil.getConnection()
        ) {

            return findByJobId(
                connection,
                jobId
            );
        }
    }
}