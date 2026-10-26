package com.salescope.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.salescope.dto.AnalysisJobDTO;
import com.salescope.dto.DatasetDTO;
import com.salescope.dto.ProductConcentrationDTO;
import com.salescope.dto.ProductConcentrationRowDTO;
import com.salescope.service.DashboardService;
import com.salescope.service.ProductConcentrationService;
import com.salescope.service.ProductSalesService;

@WebServlet(urlPatterns = {"/product-concentration", "/product-concentration/csv"})
public class ProductConcentrationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final DashboardService dashboardService = new DashboardService();
    private final ProductSalesService productSalesService = new ProductSalesService();
    private final ProductConcentrationService concentrationService =
        new ProductConcentrationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        boolean csv = request.getServletPath().endsWith("/csv");

        try {
            List<DatasetDTO> datasets = dashboardService.getDatasets();
            if (datasets.isEmpty()) {
                if (csv) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "등록된 데이터셋이 없습니다.");
                } else {
                    request.setAttribute("message", "등록된 데이터셋이 없습니다.");
                    request.getRequestDispatcher("/WEB-INF/views/product-concentration.jsp")
                        .forward(request, response);
                }
                return;
            }

            // CSV에서는 임의의 Job ID를 받지 않고, 선택된 데이터셋의 적재 완료 Job인지 검사한다.
            long datasetId = chooseDatasetId(request.getParameter("datasetId"), datasets, csv);
            List<AnalysisJobDTO> jobs = new ArrayList<>();
            for (AnalysisJobDTO job : dashboardService.getJobs(datasetId)) {
                if ("SUCCESS".equals(job.getStatus())
                        && "Y".equals(job.getResultImportedYn())) {
                    jobs.add(job);
                }
            }
            Long jobId = chooseJobId(request.getParameter("jobId"), jobs, csv);
            ProductConcentrationDTO concentration = jobId == null ? null
                : concentrationService.summarize(
                    productSalesService.getPositiveNetSalesForConcentration(jobId));

            if (csv) {
                if (jobId == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "결과 적재가 끝난 Job이 없습니다.");
                    return;
                }
                writeCsv(response, jobId, concentration);
                return;
            }

            request.setAttribute("datasets", datasets);
            request.setAttribute("selectedDatasetId", datasetId);
            request.setAttribute("jobs", jobs);
            request.setAttribute("selectedJobId", jobId);
            request.setAttribute("jobId", jobId);
            request.setAttribute("concentration", concentration);
            if (jobs.isEmpty()) {
                request.setAttribute("message", "결과 적재가 끝난 Job이 없습니다.");
            }
            request.getRequestDispatcher("/WEB-INF/views/product-concentration.jsp")
                .forward(request, response);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            throw new ServletException("상품 집중도 조회 중 오류가 발생했습니다.", e);
        }
    }

    private long chooseDatasetId(String requested, List<DatasetDTO> datasets, boolean strict) {
        if (strict && requested == null) {
            throw new IllegalArgumentException("데이터셋 ID가 필요합니다.");
        }
        Long id = parseId(requested);
        if (requested != null && id == null && strict) {
            throw new IllegalArgumentException("유효한 데이터셋 ID가 필요합니다.");
        }
        for (DatasetDTO dataset : datasets) {
            if (id != null && dataset.getDatasetId() == id) {
                return id;
            }
        }
        if (strict) {
            throw new IllegalArgumentException("선택한 데이터셋을 찾을 수 없습니다.");
        }
        return datasets.get(0).getDatasetId();
    }

    private Long chooseJobId(String requested, List<AnalysisJobDTO> jobs, boolean strict) {
        if (strict && requested == null) {
            throw new IllegalArgumentException("Job ID가 필요합니다.");
        }
        Long id = parseId(requested);
        if (requested != null && id == null && strict) {
            throw new IllegalArgumentException("유효한 Job ID가 필요합니다.");
        }
        for (AnalysisJobDTO job : jobs) {
            if (id != null && job.getJobId() == id) {
                return id;
            }
        }
        if (strict && requested != null) {
            throw new IllegalArgumentException("선택한 데이터셋의 적재 완료 Job이 아닙니다.");
        }
        return jobs.isEmpty() ? null : jobs.get(0).getJobId();
    }

    private Long parseId(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            long id = Long.parseLong(value.trim());
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void writeCsv(HttpServletResponse response, long jobId,
            ProductConcentrationDTO concentration) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
            "attachment; filename=product-concentration-job-" + jobId + ".csv");
        try (PrintWriter writer = response.getWriter()) {
            writer.write('\uFEFF'); // Excel에서 한글이 깨지지 않도록 UTF-8 BOM을 쓴다.
            writer.println("상품코드,상품명,순매출,개별비중(%),누적비중(%),등급");
            for (ProductConcentrationRowDTO row : concentration.getAllRows()) {
                writer.print(csvCell(row.getStockCode()));
                writer.print(',');
                writer.print(csvCell(row.getProductName()));
                writer.print(',');
                writer.print(row.getNetSales().toPlainString());
                writer.print(',');
                writer.print(row.getSharePercent().toPlainString());
                writer.print(',');
                writer.print(row.getCumulativePercent().toPlainString());
                writer.print(',');
                writer.println(row.getGrade());
            }
        }
    }

    private String csvCell(String raw) {
        String value = raw == null ? "" : raw;
        // 스프레드시트에서 상품명이 수식으로 실행되지 않도록 텍스트로 취급한다.
        if (value.matches("(?s)^\\s*[=+@-].*")) value = "'" + value;
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
