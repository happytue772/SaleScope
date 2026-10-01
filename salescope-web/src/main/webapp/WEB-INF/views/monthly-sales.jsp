monthly-sales.jsp

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SaleScope 월별 매출 분석</title>
    <link rel="stylesheet"
    type="text/css"
    href="${pageContext.request.contextPath}/assets/css/salescope.css?v=4">
    <link rel="stylesheet"
    href="${pageContext.request.contextPath}/assets/css/theme.css">
</head>
<body>
    <header class="site-header">
        <div class="site-header__inner">
            <a class="brand" href="${pageContext.request.contextPath}/dashboard">
                SaleScope
            </a>
            <span class="brand-subtitle">월별 매출 분석</span>
        </div>
    </header>

    <main class="container">
    <%@ include file="common/header.jsp" %>
        <section class="page-heading page-heading--with-actions">
            <div>
                <p class="eyebrow">MONTHLY SALES</p>
                <h1>월별 매출 분석</h1>
                <p class="page-description">
                    Job #<c:out value="${jobId}" />의 월별 매출과 취소 내역입니다.
                </p>
            </div>
            <div class="heading-actions">
                <a class="button button--secondary"
                    href="${pageContext.request.contextPath}/dashboard">
                    대시보드
                </a>
                <a class="button button--secondary"
                    href="${pageContext.request.contextPath}/product-sales?jobId=${jobId}">
                    상품별 분석
                </a>
            </div>
        </section>

        <nav class="page-nav" aria-label="분석 페이지 이동">
            <a class="page-nav__link"
                href="${pageContext.request.contextPath}/dashboard">
                대시보드
            </a>
            <a class="page-nav__link page-nav__link--active"
                href="${pageContext.request.contextPath}/monthly-sales?jobId=${jobId}">
                월별 분석
            </a>
            <a class="page-nav__link"
                href="${pageContext.request.contextPath}/product-sales?jobId=${jobId}">
                상품별 분석
            </a>
            <a class="page-nav__link" href="${pageContext.request.contextPath}/visualization?jobId=${jobId}">시각화 대시보드</a>
        </nav>

        <section class="panel">
            <div class="panel-heading">
                <h2>기간 검색</h2>
            </div>

            <form class="filter-grid filter-grid--monthly" method="get"
                action="${pageContext.request.contextPath}/monthly-sales">

                <input type="hidden" name="jobId" value="${jobId}">

               <div class="form-field">
                    <label for="startMonth">시작 월</label>
                    <input type="month" id="startMonth" name="startMonth"
                        value="${fn:escapeXml(startMonth)}"
                        min="${availableStartMonth}" 
                        max="${availableEndMonth}">
                </div>

                <div class="form-field">
                    <label for="endMonth">종료 월</label>
                    <input type="month" id="endMonth" name="endMonth"
                        value="${fn:escapeXml(endMonth)}"
                        min="${availableStartMonth}" 
                        max="${availableEndMonth}">
                </div>

                <div class="form-actions">
                    <button class="button button--primary" type="submit">
                        기간 조회
                    </button>
                    <a class="button button--ghost"
                        href="${pageContext.request.contextPath}/monthly-sales?jobId=${jobId}">
                        초기화
                    </a>
                </div>
            </form>
        </section>

        <section class="kpi-grid kpi-grid--four">
            <article class="kpi-card">
                <span class="kpi-card__label">조회 기간 총매출</span>
                <strong class="kpi-card__value">
                    <fmt:formatNumber value="${totalGrossSales}" pattern="#,##0.00" />
                </strong>
            </article>
            <article class="kpi-card kpi-card--positive">
                <span class="kpi-card__label">조회 기간 순매출</span>
                <strong class="kpi-card__value">
                    <fmt:formatNumber value="${totalNetSales}" pattern="#,##0.00" />
                </strong>
            </article>
            <article class="kpi-card kpi-card--danger">
                <span class="kpi-card__label">조회 기간 취소금액</span>
                <strong class="kpi-card__value">
                    <fmt:formatNumber value="${totalCancelAmount}" pattern="#,##0.00" />
                </strong>
            </article>
            <article class="kpi-card">
                <span class="kpi-card__label">조회 기간 판매수량</span>
                <strong class="kpi-card__value">
                    <fmt:formatNumber value="${totalSoldQuantity}" pattern="#,##0" />
                </strong>
            </article>
        </section>

        <section class="panel">
            <div class="panel-heading">
                <h2>월별 상세 결과</h2>
                <span class="result-count">
                    <c:out value="${fn:length(monthlyList)}" />건
                </span>
            </div>

            <div class="table-scroll">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>월</th>
                            <th>정상 주문수</th>
                            <th>판매수량</th>
                            <th>총매출</th>
                            <th>취소 주문수</th>
                            <th>취소수량</th>
                            <th>취소금액</th>
                            <th>순매출</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty monthlyList}">
                                <tr>
                                    <td class="empty-cell" colspan="8">
                                        조건에 해당하는 월별 통계가 없습니다.
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="item" items="${monthlyList}">
                                    <tr>
                                        <td><c:out value="${item.salesMonth}" /></td>
                                        <td><fmt:formatNumber value="${item.salesOrderCount}" pattern="#,##0" /></td>
                                        <td><fmt:formatNumber value="${item.soldQuantity}" pattern="#,##0" /></td>
                                        <td><fmt:formatNumber value="${item.grossSales}" pattern="#,##0.00" /></td>
                                        <td><fmt:formatNumber value="${item.cancelOrderCount}" pattern="#,##0" /></td>
                                        <td><fmt:formatNumber value="${item.cancelQuantity}" pattern="#,##0" /></td>
                                        <td class="number-danger"><fmt:formatNumber value="${item.cancelAmount}" pattern="#,##0.00" /></td>
                                        <td class="number-positive"><fmt:formatNumber value="${item.netSales}" pattern="#,##0.00" /></td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </section>
    </main>
    <script
    src="${pageContext.request.contextPath}/assets/js/theme.js">
</script>
</body>
</html>
