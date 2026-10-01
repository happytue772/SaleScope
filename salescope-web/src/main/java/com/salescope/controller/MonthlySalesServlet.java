package com.salescope.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.salescope.dto.MonthlySalesPeriodDTO;
import com.salescope.dto.MonthlySalesStatDTO;
import com.salescope.service.MonthlySalesService;

@WebServlet("/monthly-sales")
public class MonthlySalesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final MonthlySalesService service =
        new MonthlySalesService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            long jobId = parseJobId(request.getParameter("jobId"));

            MonthlySalesPeriodDTO availablePeriod =
                service.getAvailablePeriod(jobId);

            MonthlySalesPeriodDTO selectedPeriod =
                service.resolveSelectedPeriod(
                    request.getParameter("startMonth"),
                    request.getParameter("endMonth"),
                    availablePeriod
                );

            List<MonthlySalesStatDTO> monthlyList =
                service.getMonthlySales(
                    jobId,
                    selectedPeriod,
                    availablePeriod
                );

            request.setAttribute("jobId", jobId);
            request.setAttribute(
                "startMonth",
                selectedPeriod.getStartMonth()
            );
            request.setAttribute(
                "endMonth",
                selectedPeriod.getEndMonth()
            );
            request.setAttribute(
                "availableStartMonth",
                availablePeriod.getStartMonth()
            );
            request.setAttribute(
                "availableEndMonth",
                availablePeriod.getEndMonth()
            );
            request.setAttribute("monthlyList", monthlyList);
            request.setAttribute(
                "totalGrossSales",
                service.calculateGrossSales(monthlyList)
            );
            request.setAttribute(
                "totalNetSales",
                service.calculateNetSales(monthlyList)
            );
            request.setAttribute(
                "totalCancelAmount",
                service.calculateCancelAmount(monthlyList)
            );
            request.setAttribute(
                "totalSoldQuantity",
                service.calculateSoldQuantity(monthlyList)
            );

            request.getRequestDispatcher(
                "/WEB-INF/views/monthly-sales.jsp"
            ).forward(request, response);

        } catch (IllegalArgumentException e) {
            // 기존의 response.sendError(...) 부분을 지우고 아래 코드로 교체합니다.
            
            // 1. 에러 메시지를 JSP로 보내기 위해 바구니에 담기
            request.setAttribute("errorMessage", e.getMessage());
            
            // 2. 화면이 깨지지 않도록 사용자가 원래 입력했던 jobId도 다시 담아주기
            request.setAttribute("jobId", request.getParameter("jobId"));
            
            // 3. 에러 페이지 대신 원래 대시보드 화면(JSP)으로 포워딩
            request.getRequestDispatcher("/WEB-INF/views/monthly-sales.jsp").forward(request, response);

        } catch (Exception e) {
            throw new ServletException(
                "월별 매출 조회 중 오류가 발생했습니다.",
                e
            );
        }
    }

    private long parseJobId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("jobId가 필요합니다.");
        }

        try {
            long jobId = Long.parseLong(value.trim());
            if (jobId <= 0) {
                throw new IllegalArgumentException(
                    "jobId는 1 이상이어야 합니다."
                );
            }
            return jobId;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "jobId는 정수여야 합니다: " + value,
                e
            );
        }
    }

}
