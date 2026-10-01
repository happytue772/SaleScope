package com.salescope.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.salescope.dto.ProductSalesSearchCondition;
import com.salescope.dto.ProductSalesStatDTO;
import com.salescope.service.ProductSalesService;

@WebServlet("/product-sales")
public class ProductSalesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ProductSalesService service = new ProductSalesService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            // 1. 화면에서 넘어오는 "모든" 파라미터를 빠짐없이 받습니다.
            long jobId = parseJobId(request.getParameter("jobId"));
            String sortBy = parseSortBy(request.getParameter("sortBy"));
            String sortDirection = parseSortDirection(request.getParameter("sortDirection")); // 핵심!
            int topN = parseTopN(request.getParameter("topN"));
            String keyword = normalizeKeyword(request.getParameter("keyword"));
            
            BigDecimal minNetSales = parseBigDecimal(request.getParameter("minNetSales"));
            Long minSoldQuantity = parseLong(request.getParameter("minSoldQuantity"));
            BigDecimal minCancelAmount = parseBigDecimal(request.getParameter("minCancelAmount"));

            // 2. 검색 조건 객체로 묶어서 서비스에 전달합니다.
            ProductSalesSearchCondition condition = new ProductSalesSearchCondition(
                jobId, keyword, sortBy, sortDirection, topN, minNetSales, minSoldQuantity, minCancelAmount
            );

            List<ProductSalesStatDTO> productList = service.getProductSales(condition);
            List<ProductSalesStatDTO> allProducts = service.getDistinctProducts(jobId);
            
            long totalMatchedCount = service.countProductSalesByCondition(condition);
            
            Map<String, Number> dataRange = service.getSalesDataRange(jobId);
            request.setAttribute("dataRange", dataRange);
            
            request.setAttribute("allProducts", allProducts);
            request.setAttribute("totalMatchedCount", totalMatchedCount);
            
            // 3. 화면(JSP)이 새로고침 되어도 선택한 값을 기억하도록 모두 다시 넘겨줍니다.
            request.setAttribute("totalMatchedCount", totalMatchedCount);
            request.setAttribute("jobId", jobId);
            request.setAttribute("sortBy", sortBy);
            request.setAttribute("sortDirection", sortDirection);
            request.setAttribute("topN", topN);
            request.setAttribute("keyword", keyword);
            request.setAttribute("minNetSales", minNetSales);
            request.setAttribute("minSoldQuantity", minSoldQuantity);
            request.setAttribute("minCancelAmount", minCancelAmount);
            request.setAttribute("productList", productList);

            request.getRequestDispatcher("/WEB-INF/views/product-sales.jsp").forward(request, response);

        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ServletException("상품별 매출 조회 중 오류가 발생했습니다.", e);
        }
    }

    private long parseJobId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("jobId가 필요합니다.");
        }
        try {
            long jobId = Long.parseLong(value.trim());
            if (jobId <= 0) throw new IllegalArgumentException("jobId는 1 이상이어야 합니다.");
            return jobId;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("jobId는 정수여야 합니다: " + value, e);
        }
    }

    private String parseSortBy(String value) {
        if (value == null || value.trim().isEmpty()) return "netSales";
        String sortBy = value.trim();
        if ("netSales".equals(sortBy) || "soldQuantity".equals(sortBy) || "salesOrderCount".equals(sortBy) || "cancelAmount".equals(sortBy)) {
            return sortBy;
        }
        throw new IllegalArgumentException("지원하지 않는 정렬 기준입니다: " + value);
    }

    private String parseSortDirection(String value) {
        // 정렬 방향을 파싱하는 핵심 메서드 추가
        if (value == null || value.trim().isEmpty()) return "desc";
        String dir = value.trim().toLowerCase();
        if ("asc".equals(dir) || "desc".equals(dir)) {
            return dir;
        }
        return "desc";
    }

    private int parseTopN(String value) {
        if (value == null || value.trim().isEmpty()) return 10;
        try {
            int topN = Integer.parseInt(value.trim());
            if (topN < 1 || topN > 500) throw new IllegalArgumentException("조회 행 수는 1 이상 500 이하여야 합니다.");
            return topN;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("조회 행 수는 정수여야 합니다: " + value, e);
        }
    }

    private String normalizeKeyword(String value) {
        return (value == null) ? "" : value.trim();
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}