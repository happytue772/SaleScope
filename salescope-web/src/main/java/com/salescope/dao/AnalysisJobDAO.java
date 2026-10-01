package com.salescope.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.salescope.dto.AnalysisJobDTO;
import com.salescope.util.DBConnectionUtil;

public class AnalysisJobDAO {

    /*
     * 특정 Job의 분석 상태 조회
     */
    private static final String STATUS_SQL =
        "SELECT STATUS "
      + "FROM ANALYSIS_JOB "
      + "WHERE JOB_ID = ?";

    /*
     * 특정 Job의 결과 적재 여부 조회
     */
    private static final String IMPORTED_SQL =
        "SELECT RESULT_IMPORTED_YN "
      + "FROM ANALYSIS_JOB "
      + "WHERE JOB_ID = ?";

    /*
     * 결과 적재 성공 상태 수정
     */
    private static final String SUCCESS_SQL =
        "UPDATE ANALYSIS_JOB "
      + "SET RESULT_IMPORTED_YN = 'Y', "
      + "RESULT_FILE_PATH = ?, "
      + "ERROR_STEP = NULL, "
      + "ERROR_MESSAGE = NULL "
      + "WHERE JOB_ID = ?";

    /*
     * 결과 적재 실패 상태 수정
     */
    private static final String FAILURE_SQL =
        "UPDATE ANALYSIS_JOB "
      + "SET RESULT_IMPORTED_YN = 'N', "
      + "ERROR_STEP = 'RESULT_IMPORT', "
      + "ERROR_MESSAGE = ? "
      + "WHERE JOB_ID = ?";

    /*
     * 데이터셋별 분석 작업 목록 조회
     */
    private static final String DASHBOARD_LIST_SQL =
        "SELECT JOB_ID, DATASET_ID, STATUS, "
      + "ANALYSIS_VERSION, REQUESTED_AT, "
      + "FINISHED_AT, RESULT_IMPORTED_YN "
      + "FROM ANALYSIS_JOB "
      + "WHERE DATASET_ID = ? "
      + "ORDER BY REQUESTED_AT DESC, JOB_ID DESC";

    /**
     * 특정 Job의 Hadoop 분석 상태를 조회한다.
     *
     * @param connection 외부 트랜잭션 Connection
     * @param jobId 분석 작업 ID
     * @return Job 상태 또는 존재하지 않으면 null
     */
    public String findStatus(
            Connection connection,
            long jobId) throws SQLException {

        try (
            PreparedStatement pstmt =
                connection.prepareStatement(STATUS_SQL)
        ) {

            pstmt.setLong(1, jobId);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                return rs.getString("STATUS");
            }
        }
    }

    /**
     * 특정 Job의 결과가 Oracle에 적재됐는지 확인한다.
     *
     * @param connection 외부 트랜잭션 Connection
     * @param jobId 분석 작업 ID
     * @return 적재 완료 여부
     */
    public boolean isResultImported(
            Connection connection,
            long jobId) throws SQLException {

        try (
            PreparedStatement pstmt =
                connection.prepareStatement(IMPORTED_SQL)
        ) {

            pstmt.setLong(1, jobId);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (!rs.next()) {
                    return false;
                }

                return "Y".equals(
                    rs.getString(
                        "RESULT_IMPORTED_YN"
                    )
                );
            }
        }
    }

    /**
     * 결과 적재 성공 정보를 ANALYSIS_JOB에 반영한다.
     *
     * @param connection 외부 트랜잭션 Connection
     * @param jobId 분석 작업 ID
     * @param resultFilePath 결과 파일 디렉터리
     * @return 수정된 행 수
     */
    public int updateImportSuccess(
            Connection connection,
            long jobId,
            String resultFilePath)
            throws SQLException {

        try (
            PreparedStatement pstmt =
                connection.prepareStatement(SUCCESS_SQL)
        ) {

            pstmt.setString(
                1,
                resultFilePath
            );

            pstmt.setLong(
                2,
                jobId
            );

            return pstmt.executeUpdate();
        }
    }

    /**
     * 결과 적재 실패 정보를 ANALYSIS_JOB에 반영한다.
     *
     * @param connection 외부 트랜잭션 Connection
     * @param jobId 분석 작업 ID
     * @param errorMessage 오류 메시지
     * @return 수정된 행 수
     */
    public int updateImportFailure(
            Connection connection,
            long jobId,
            String errorMessage)
            throws SQLException {

        String safeMessage = errorMessage;

        if (safeMessage == null
                || safeMessage.trim().isEmpty()) {

            safeMessage =
                "알 수 없는 결과 적재 오류";
        }

        /*
         * Oracle ERROR_MESSAGE 컬럼이
         * VARCHAR2(2000)이므로 최대 길이를 제한한다.
         */
        if (safeMessage.length() > 2000) {

            safeMessage =
                safeMessage.substring(0, 2000);
        }

        try (
            PreparedStatement pstmt =
                connection.prepareStatement(FAILURE_SQL)
        ) {

            pstmt.setString(
                1,
                safeMessage
            );

            pstmt.setLong(
                2,
                jobId
            );

            return pstmt.executeUpdate();
        }
    }

    /**
     * 대시보드 데이터셋 선택에 따라
     * 해당 데이터셋의 분석 작업 목록을 조회한다.
     *
     * 최신 분석 요청이 먼저 출력된다.
     *
     * @param datasetId 데이터셋 ID
     * @return 분석 작업 목록
     */
    public List<AnalysisJobDTO>
            findByDatasetIdForDashboard(
                long datasetId)
                throws SQLException {

        List<AnalysisJobDTO> jobs =
            new ArrayList<>();

        try (
            Connection connection =
                DBConnectionUtil.getConnection();

            PreparedStatement pstmt =
                connection.prepareStatement(
                    DASHBOARD_LIST_SQL
                )
        ) {

            pstmt.setLong(
                1,
                datasetId
            );

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {

                    AnalysisJobDTO job =
                        new AnalysisJobDTO();

                    job.setJobId(
                        rs.getLong("JOB_ID")
                    );

                    job.setDatasetId(
                        rs.getLong("DATASET_ID")
                    );

                    job.setStatus(
                        rs.getString("STATUS")
                    );

                    job.setAnalysisVersion(
                        rs.getString(
                            "ANALYSIS_VERSION"
                        )
                    );

                    job.setRequestedAt(
                        rs.getTimestamp(
                            "REQUESTED_AT"
                        )
                    );

                    job.setFinishedAt(
                        rs.getTimestamp(
                            "FINISHED_AT"
                        )
                    );

                    job.setResultImportedYn(
                        rs.getString(
                            "RESULT_IMPORTED_YN"
                        )
                    );

                    jobs.add(job);
                }
            }
        }

        return jobs;
    }
}