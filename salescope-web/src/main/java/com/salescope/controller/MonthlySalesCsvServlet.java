package com.salescope.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.salescope.dto.MonthlySalesChangeDTO;
import com.salescope.dto.MonthlySalesStatDTO;
import com.salescope.service.MonthlySalesChangeService;
import com.salescope.service.MonthlySalesService;

/** 화면의 Job·기간 조건을 그대로 사용해 월별 결과를 내려받는다. */
@WebServlet("/monthly-sales/csv")
public class MonthlySalesCsvServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final MonthlySalesService service = new MonthlySalesService();
    private final MonthlySalesChangeService changeService =
        new MonthlySalesChangeService();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try {
            long jobId = parseJobId(request.getParameter("jobId"));
            List<MonthlySalesStatDTO> rows = service.getMonthlySales(
                jobId,
                request.getParameter("startMonth"),
                request.getParameter("endMonth")
            );
            Map<String, MonthlySalesChangeDTO> changes =
                changeService.calculate(rows);

            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader(
                "Content-Disposition",
                "attachment; filename=monthly-sales-job-" + jobId + ".csv"
            );

            try (PrintWriter writer = response.getWriter()) {
                writer.write('\uFEFF');
                writer.println("월,정상주문수,판매수량,총매출,취소주문수,취소수량,취소금액,순매출,전월대비변화액,전월대비변화율(%)");

                for (MonthlySalesStatDTO row : rows) {
                    MonthlySalesChangeDTO change = changes.get(row.getSalesMonth());
                    writer.print(csv(row.getSalesMonth()));
                    writer.print(',');
                    writer.print(row.getSalesOrderCount());
                    writer.print(',');
                    writer.print(row.getSoldQuantity());
                    writer.print(',');
                    writer.print(decimal(row.getGrossSales()));
                    writer.print(',');
                    writer.print(row.getCancelOrderCount());
                    writer.print(',');
                    writer.print(row.getCancelQuantity());
                    writer.print(',');
                    writer.print(decimal(row.getCancelAmount()));
                    writer.print(',');
                    writer.print(decimal(row.getNetSales()));
                    writer.print(',');
                    writer.print(change == null ? "" : decimal(change.getChangeAmount()));
                    writer.print(',');
                    writer.println(change == null ? "" : decimal(change.getChangePercent()));
                }
            }

        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ServletException("월별 CSV 다운로드 중 오류가 발생했습니다.", e);
        }
    }

    private long parseJobId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("jobId가 필요합니다.");
        }

        try {
            long jobId = Long.parseLong(value.trim());
            if (jobId <= 0) {
                throw new IllegalArgumentException("jobId는 1 이상이어야 합니다.");
            }
            return jobId;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("jobId는 정수여야 합니다.", e);
        }
    }

    private String decimal(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }

        // 스프레드시트에서 수식으로 해석될 수 있는 문자열을 막는다.
        String safe = value.matches("^[\\s]*[=+@-].*") ? "'" + value : value;
        return '"' + safe.replace("\"", "\"\"") + '"';
    }
}
