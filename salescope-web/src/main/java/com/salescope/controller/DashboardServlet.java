package com.salescope.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.salescope.dto.AnalysisJobDTO;
import com.salescope.dto.DashboardSummaryDTO;
import com.salescope.dto.DataQualityStatDTO;
import com.salescope.dto.DatasetDTO;
import com.salescope.dto.MonthlySalesStatDTO;
import com.salescope.service.DashboardService;

@WebServlet(
    name = "DashboardServlet",
    urlPatterns = "/dashboard"
)
public class DashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private DashboardService dashboardService;

    @Override
    public void init() throws ServletException {
        dashboardService = new DashboardService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            List<DatasetDTO> datasets = dashboardService.getDatasets();
            request.setAttribute("datasets", datasets);

            if (datasets.isEmpty()) {
                setEmptyDashboard(request);
                request.setAttribute("message", "등록된 데이터셋이 없습니다.");
                forwardDashboard(request, response);
                return;
            }

            long selectedDatasetId = selectDatasetId(
                request.getParameter("datasetId"),
                datasets
            );

            List<AnalysisJobDTO> jobs =
                dashboardService.getJobs(selectedDatasetId);

            Long selectedJobId = selectJobId(
                request.getParameter("jobId"),
                jobs
            );

            request.setAttribute("selectedDatasetId", selectedDatasetId);
            request.setAttribute("jobs", jobs);
            request.setAttribute("selectedJobId", selectedJobId);

            if (selectedJobId == null) {
                setEmptyDashboard(request);
                request.setAttribute(
                    "message",
                    "선택한 데이터셋에 분석 작업이 없습니다."
                );
                forwardDashboard(request, response);
                return;
            }

            List<MonthlySalesStatDTO> monthlyStats =
                dashboardService.getMonthlyStats(selectedJobId);
            List<DataQualityStatDTO> qualityStats =
                dashboardService.getQualityStats(selectedJobId);

            DashboardSummaryDTO summary =
                dashboardService.createSummary(
                    selectedJobId,
                    monthlyStats,
                    qualityStats
                );

            request.setAttribute("monthlyStats", monthlyStats);
            request.setAttribute("qualityStats", qualityStats);
            request.setAttribute("dashboardSummary", summary);

            // 기존 JSP 속성명도 함께 유지한다.
            request.setAttribute(
                "totalGrossSales",
                summary.getTotalGrossSales()
            );
            request.setAttribute(
                "totalNetSales",
                summary.getTotalNetSales()
            );
            request.setAttribute(
                "totalSoldQuantity",
                summary.getTotalSoldQuantity()
            );
            request.setAttribute(
                "totalCancelAmount",
                summary.getTotalCancelAmount()
            );

            if (monthlyStats.isEmpty()) {
                request.setAttribute(
                    "message",
                    "선택한 작업에 적재된 월별 통계가 없습니다."
                );
            }

            forwardDashboard(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                "대시보드 데이터 조회 중 오류가 발생했습니다.",
                e
            );
        }
    }

    private long selectDatasetId(
            String parameter,
            List<DatasetDTO> datasets) {

        Long requestedId = parseLong(parameter);

        if (requestedId != null) {
            for (DatasetDTO dataset : datasets) {
                if (dataset.getDatasetId() == requestedId.longValue()) {
                    return requestedId;
                }
            }
        }

        return datasets.get(0).getDatasetId();
    }

    private Long selectJobId(
            String parameter,
            List<AnalysisJobDTO> jobs) {

        Long requestedId = parseLong(parameter);

        if (requestedId != null) {
            for (AnalysisJobDTO job : jobs) {
                if (job.getJobId() == requestedId.longValue()) {
                    return requestedId;
                }
            }
        }

        // SUCCESS이며 Oracle 적재가 끝난 최신 순서의 Job을 선택한다.
        for (AnalysisJobDTO job : jobs) {
            if ("SUCCESS".equals(job.getStatus())
                    && "Y".equals(job.getResultImportedYn())) {
                return job.getJobId();
            }
        }

        return jobs.isEmpty() ? null : jobs.get(0).getJobId();
    }

    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setEmptyDashboard(HttpServletRequest request) {
        request.setAttribute(
            "monthlyStats",
            Collections.<MonthlySalesStatDTO>emptyList()
        );
        request.setAttribute(
            "qualityStats",
            Collections.<DataQualityStatDTO>emptyList()
        );
        request.setAttribute("dashboardSummary", DashboardSummaryDTO.empty());
        request.setAttribute("totalGrossSales", BigDecimal.ZERO);
        request.setAttribute("totalNetSales", BigDecimal.ZERO);
        request.setAttribute("totalSoldQuantity", 0L);
        request.setAttribute("totalCancelAmount", BigDecimal.ZERO);

        if (request.getAttribute("jobs") == null) {
            request.setAttribute("jobs", new ArrayList<AnalysisJobDTO>());
        }
    }

    private void forwardDashboard(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(
            "/WEB-INF/views/dashboard.jsp"
        ).forward(request, response);
    }
}