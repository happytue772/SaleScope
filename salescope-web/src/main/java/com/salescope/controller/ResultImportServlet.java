package com.salescope.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.salescope.service.ResultImportService;
import com.salescope.service.ResultImportService.ImportResult;
import com.salescope.service.ResultImportService.ProductImportResult;

@WebServlet("/result/import")
public class ResultImportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ResultImportService importService =
        new ResultImportService();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        response.setCharacterEncoding("UTF-8");
        response.setContentType(
            "text/plain; charset=UTF-8"
        );

        PrintWriter writer =
            response.getWriter();

        try {
            long jobId =
                parseJobId(
                    request.getParameter(
                        "jobId"
                    )
                );

            String resultType =
                parseResultType(
                    request.getParameter(
                        "resultType"
                    )
                );

            Path resultDirectory =
                parseResultDirectory(
                    request.getParameter(
                        "resultDir"
                    )
                );

            if ("product".equals(resultType)) {
                importProduct(
                    writer,
                    response,
                    jobId,
                    resultDirectory
                );

            } else {
                importMonthly(
                    writer,
                    response,
                    jobId,
                    resultDirectory
                );
            }

        } catch (IllegalArgumentException e) {
            response.setStatus(
                HttpServletResponse
                    .SC_BAD_REQUEST
            );

            writer.println(
                "요청 값 오류: "
                    + e.getMessage()
            );

        } catch (Exception e) {
            getServletContext().log(
                "SaleScope 결과 적재 실패",
                e
            );

            response.setStatus(
                HttpServletResponse
                    .SC_INTERNAL_SERVER_ERROR
            );

            writer.println(
                "결과 적재 실패: "
                    + e.getMessage()
            );
        }
    }

    // 기존 월별·품질 결과 적재
    private void importMonthly(
            PrintWriter writer,
            HttpServletResponse response,
            long jobId,
            Path resultDirectory)
            throws Exception {

        ImportResult result =
            importService.importMonthlyResult(
                jobId,
                resultDirectory
            );

        response.setStatus(
            HttpServletResponse.SC_OK
        );

        writer.println(
            "SaleScope 월별 결과 적재 성공"
        );
        writer.println(
            "JOB_ID: " + jobId
        );
        writer.println(
            "월별 적재 건수: "
                + result.getMonthlyCount()
        );
        writer.println(
            "품질 적재 건수: "
                + result.getQualityCount()
        );
    }

    // 상품 마스터·상품 통계 결과 적재
    private void importProduct(
            PrintWriter writer,
            HttpServletResponse response,
            long jobId,
            Path resultDirectory)
            throws Exception {

        ProductImportResult result =
            importService.importProductResult(
                jobId,
                resultDirectory
            );

        response.setStatus(
            HttpServletResponse.SC_OK
        );

        writer.println(
            "SaleScope 상품 결과 적재 성공"
        );
        writer.println(
            "JOB_ID: " + jobId
        );
        writer.println(
            "상품 마스터 처리 건수: "
                + result.getProductMasterCount()
        );
        writer.println(
            "상품 통계 적재 건수: "
                + result.getProductSalesCount()
        );
    }

    private long parseJobId(String value) {

        if (value == null
                || value.trim().isEmpty()) {

            throw new IllegalArgumentException(
                "jobId가 필요합니다."
            );
        }

        try {
            long jobId =
                Long.parseLong(value.trim());

            if (jobId <= 0) {
                throw new IllegalArgumentException(
                    "jobId는 1 이상이어야 합니다."
                );
            }

            return jobId;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "jobId는 정수여야 합니다: "
                    + value,
                e
            );
        }
    }

    private String parseResultType(
            String value) {

        /*
         * 기존 월별 요청과의 호환성을 위해
         * resultType이 없으면 monthly로 처리한다.
         */
        if (value == null
                || value.trim().isEmpty()) {

            return "monthly";
        }

        String resultType =
            value.trim()
                .toLowerCase(Locale.ROOT);

        if (!"monthly".equals(resultType)
                && !"product".equals(
                    resultType
                )) {

            throw new IllegalArgumentException(
                "resultType은 monthly 또는 "
                    + "product여야 합니다: "
                    + value
            );
        }

        return resultType;
    }

    private Path parseResultDirectory(
            String value) {

        if (value == null
                || value.trim().isEmpty()) {

            throw new IllegalArgumentException(
                "resultDir이 필요합니다."
            );
        }

        return Paths.get(value.trim());
    }
}