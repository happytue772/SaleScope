package com.salescope.controller;

import java.io.IOException;
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
import com.salescope.service.DashboardService;
import com.salescope.service.JobComparisonService;

@WebServlet("/job-comparison")
public class JobComparisonServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final DashboardService dashboardService = new DashboardService();
    private final JobComparisonService comparisonService =
        new JobComparisonService();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            List<DatasetDTO> datasets = dashboardService.getDatasets();
            request.setAttribute("datasets", datasets);
            if (datasets.isEmpty()) {
                request.setAttribute("message", "등록된 데이터셋이 없습니다.");
                forward(request, response);
                return;
            }

            long datasetId = selectDatasetId(request.getParameter("datasetId"),
                datasets);
            request.setAttribute("selectedDatasetId", datasetId);

            List<AnalysisJobDTO> jobs = new ArrayList<>();
            for (AnalysisJobDTO job : dashboardService.getJobs(datasetId)) {
                if ("SUCCESS".equals(job.getStatus())
                        && "Y".equals(job.getResultImportedYn())) {
                    jobs.add(job);
                }
            }
            request.setAttribute("jobs", jobs);

            Long firstJobId = selectJobId(request.getParameter("firstJobId"),
                jobs);
            if (firstJobId == null && !jobs.isEmpty()) {
                firstJobId = jobs.get(0).getJobId();
            }
            Long secondJobId = selectJobId(request.getParameter("secondJobId"),
                jobs);
            if (secondJobId == null) {
                for (AnalysisJobDTO job : jobs) {
                    if (!Long.valueOf(job.getJobId()).equals(firstJobId)) {
                        secondJobId = job.getJobId();
                        break;
                    }
                }
            }

            request.setAttribute("firstJobId", firstJobId);
            request.setAttribute("secondJobId", secondJobId);
            request.setAttribute("jobId", firstJobId);

            if (jobs.size() < 2) {
                request.setAttribute("message",
                    "같은 데이터셋에 결과 적재가 끝난 Job이 2개 이상 있어야 비교할 수 있습니다.");
            } else if (firstJobId.equals(secondJobId)) {
                request.setAttribute("message", "서로 다른 Job을 선택해 주세요.");
            } else {
                request.setAttribute("comparison", comparisonService.compare(
                    dashboardService.getMonthlyStats(firstJobId),
                    dashboardService.getMonthlyStats(secondJobId)));
            }
            forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("분석 Job 비교 중 오류가 발생했습니다.", e);
        }
    }

    private long selectDatasetId(String requested, List<DatasetDTO> datasets) {
        Long id = parseId(requested);
        for (DatasetDTO dataset : datasets) {
            if (id != null && dataset.getDatasetId() == id.longValue()) {
                return id;
            }
        }
        return datasets.get(0).getDatasetId();
    }

    private Long selectJobId(String requested, List<AnalysisJobDTO> jobs) {
        Long id = parseId(requested);
        for (AnalysisJobDTO job : jobs) {
            if (id != null && job.getJobId() == id.longValue()) {
                return id;
            }
        }
        return null;
    }

    private Long parseId(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            long id = Long.parseLong(value.trim());
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void forward(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/job-comparison.jsp")
            .forward(request, response);
    }
}
