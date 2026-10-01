package com.salescope.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.salescope.dto.ProductSalesSearchCondition;
import com.salescope.dto.ProductSalesStatDTO;
import com.salescope.service.ProductSalesService;

@WebServlet("/product-sales/csv")
public class ProductSalesCsvServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_ROW_LIMIT = 10;

    private static final int MAX_ROW_LIMIT = 500;

    private final ProductSalesService service = new ProductSalesService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            ProductSalesSearchCondition condition = parseCondition(request);

            List<ProductSalesStatDTO> list = service.getProductSales(condition);

            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            response.setContentType("text/csv; charset=UTF-8");

            response.setHeader(
                "Content-Disposition",
                "attachment; filename=product-sales-job-"
                    + condition.getJobId()
                    + ".csv"
            );

            try (PrintWriter writer = response.getWriter()) {
                writer.write('\uFEFF');

                writer.println(
                    "상품코드,상품명,주문수,판매수량,총매출,"
                        + "취소주문수,취소수량,취소금액,순판매수량,순매출"
                );

                for (ProductSalesStatDTO item : list) {
                    writer.print(csv(item.getStockCode()));
                    writer.print(',');

                    writer.print(csv(item.getProductName()));
                    writer.print(',');

                    writer.print(item.getSalesOrderCount());
                    writer.print(',');

                    writer.print(item.getSoldQuantity());
                    writer.print(',');

                    writer.print(decimal(item.getGrossSales()));
                    writer.print(',');

                    writer.print(item.getCancelOrderCount());
                    writer.print(',');

                    writer.print(item.getCancelQuantity());
                    writer.print(',');

                    writer.print(decimal(item.getCancelAmount()));
                    writer.print(',');

                    writer.print(item.getNetQuantity());
                    writer.print(',');

                    writer.println(decimal(item.getNetSales()));
                }
            }

        } catch (IllegalArgumentException e) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                e.getMessage()
            );
        } catch (Exception e) {
            throw new ServletException(
                "상품별 CSV 다운로드 중 오류가 발생했습니다.",
                e
            );
        }
    }

    private ProductSalesSearchCondition parseCondition(
            HttpServletRequest request) {

        return new ProductSalesSearchCondition(
            parsePositiveLong(
                request.getParameter("jobId"),
                "jobId"
            ),
            normalize(request.getParameter("keyword")),
            parseSortBy(request.getParameter("sortBy")),
            parseDirection(request.getParameter("sortDirection")),
            parseRowLimit(request.getParameter("topN")),
            parseDecimal(
                request.getParameter("minNetSales"),
                "최소 순매출"
            ),
            parseOptionalLong(
                request.getParameter("minSoldQuantity"),
                "최소 판매량"
            ),
            parseDecimal(
                request.getParameter("minCancelAmount"),
                "최소 취소금액"
            )
        );
    }

    private long parsePositiveLong(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "가 필요합니다.");
        }

        try {
            long result = Long.parseLong(value.trim());

            if (result <= 0) {
                throw new IllegalArgumentException(
                    fieldName + "는 1 이상이어야 합니다."
                );
            }

            return result;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                fieldName + "는 정수여야 합니다.",
                e
            );
        }
    }

    private int parseRowLimit(String value) {
        if (value == null || value.trim().isEmpty()) {
            return DEFAULT_ROW_LIMIT;
        }

        try {
            int result = Integer.parseInt(value.trim());

            if (result < 1 || result > MAX_ROW_LIMIT) {
                throw new IllegalArgumentException(
                    "조회 행 수는 1 이상 500 이하여야 합니다."
                );
            }

            return result;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "조회 행 수는 정수여야 합니다.",
                e
            );
        }
    }

    private String parseSortBy(String value) {
        String result = normalize(value);

        if (result == null) {
            return "netSales";
        }

        if ("netSales".equals(result)
                || "soldQuantity".equals(result)
                || "salesOrderCount".equals(result)
                || "cancelAmount".equals(result)) {
            return result;
        }

        throw new IllegalArgumentException("지원하지 않는 정렬 기준입니다.");
    }

    private String parseDirection(String value) {
        String result = normalize(value);

        if (result == null) {
            return "desc";
        }

        result = result.toLowerCase(Locale.ROOT);

        if ("asc".equals(result) || "desc".equals(result)) {
            return result;
        }

        throw new IllegalArgumentException("지원하지 않는 정렬 방향입니다.");
    }

    private BigDecimal parseDecimal(String value, String fieldName) {
        String normalized = normalize(value);

        if (normalized == null) {
            return null;
        }

        try {
            BigDecimal result = new BigDecimal(normalized);

            if (result.signum() < 0) {
                throw new IllegalArgumentException(
                    fieldName + "은 0 이상이어야 합니다."
                );
            }

            return result;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                fieldName + "은 숫자여야 합니다.",
                e
            );
        }
    }

    private Long parseOptionalLong(String value, String fieldName) {
        String normalized = normalize(value);

        if (normalized == null) {
            return null;
        }

        try {
            long result = Long.parseLong(normalized);

            if (result < 0) {
                throw new IllegalArgumentException(
                    fieldName + "은 0 이상이어야 합니다."
                );
            }

            return result;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                fieldName + "은 정수여야 합니다.",
                e
            );
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }

    private String csv(String value) {
        String safe = value == null ? "" : value;

        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private String decimal(BigDecimal value) {
        return value == null ? "0" : value.toPlainString();
    }
}