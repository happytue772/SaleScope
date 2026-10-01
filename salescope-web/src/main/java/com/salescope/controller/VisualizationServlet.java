package com.salescope.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.salescope.dto.MonthlySalesStatDTO;
import com.salescope.dto.ProductSalesStatDTO;
import com.salescope.service.DashboardService;
import com.salescope.service.ProductSalesService;

@WebServlet("/visualization")
public class VisualizationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final DashboardService dashboardService = new DashboardService();
    private final ProductSalesService productSalesService = new ProductSalesService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            String jobIdParam = request.getParameter("jobId");
            if (jobIdParam == null || jobIdParam.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }

            long jobId = Long.parseLong(jobIdParam.trim());

            // 1. 월별 매출 통계 가져오기
            List<MonthlySalesStatDTO> monthlyStats = dashboardService.getMonthlyStats(jobId);
            
            // 2. 상품별 매출 TOP 5 가져오기 (파이/막대 차트용)
            List<ProductSalesStatDTO> topProducts = productSalesService.getProductSales(jobId, "netSales", 5, "");

            request.setAttribute("jobId", jobId);
            request.setAttribute("monthlyStats", monthlyStats);
            request.setAttribute("topProducts", topProducts);

            request.getRequestDispatcher("/WEB-INF/views/visualization.jsp").forward(request, response);

        } catch (Exception e) {
            throw new ServletException("시각화 대시보드 로딩 중 오류가 발생했습니다.", e);
        }
    }
}