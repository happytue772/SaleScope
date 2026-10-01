<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SaleScope 대시보드</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/salescope.css?v=4">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/dashboard.css?v=4">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.5.1/dist/chart.umd.min.js"></script>
    <link rel="stylesheet"
    href="${pageContext.request.contextPath}/assets/css/theme.css">
</head>
<body>
    <header class="site-header">
        <div class="site-header__inner">
            <a class="brand" href="${pageContext.request.contextPath}/dashboard">SaleScope</a>
            <span class="brand-subtitle">온라인 쇼핑몰 매출 분석</span>
        </div>
    </header>

    <main class="container">
    <%@ include file="common/header.jsp" %>
        <section class="page-heading">
            <div>
                <p class="eyebrow">ANALYTICS OVERVIEW</p>
                <h1>매출 분석 대시보드</h1>
                <p class="page-description">Hadoop 집계 결과를 Oracle에서 조회한 분석 화면입니다.</p>
            </div>
        </section>

        <c:if test="${not empty message}">
            <div class="notice">
                <c:out value="${message}" />
            </div>
        </c:if>

        <section class="panel">
            <div class="panel-heading">
                <h2>분석 대상 선택</h2>
            </div>

            <form class="filter-grid" method="get" action="${pageContext.request.contextPath}/dashboard">
                <div class="form-field">
                    <label for="datasetId">데이터셋</label>
                    <select id="datasetId" name="datasetId">
                        <c:forEach var="dataset" items="${datasets}">
                            <option value="${dataset.datasetId}" ${dataset.datasetId == selectedDatasetId ? 'selected' : ''}>
                                Dataset #<c:out value="${dataset.datasetId}" />
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="form-field">
                    <label for="jobId">분석 Job</label>
                    <select id="jobId" name="jobId">
                        <c:forEach var="job" items="${jobs}">
                            <option value="${job.jobId}" ${job.jobId == selectedJobId ? 'selected' : ''}>
                                Job #<c:out value="${job.jobId}" /> · <c:out value="${job.status}" />
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="form-actions">
                    <button class="button button--primary" type="submit">대시보드 조회</button>
                </div>
            </form>
        </section>

        <c:if test="${not empty selectedJobId}">
            <nav class="page-nav" aria-label="분석 페이지 이동">
                <a class="page-nav__link page-nav__link--active"
                    href="${pageContext.request.contextPath}/dashboard?datasetId=${selectedDatasetId}&jobId=${selectedJobId}">대시보드</a>
                <a class="page-nav__link"
                    href="${pageContext.request.contextPath}/monthly-sales?jobId=${selectedJobId}">월별 분석</a>
                <a class="page-nav__link"
                    href="${pageContext.request.contextPath}/product-sales?jobId=${selectedJobId}">상품별 분석</a>
                <a class="page-nav__link"
                    href="${pageContext.request.contextPath}/visualization?jobId=${selectedJobId}">시각화 대시보드</a>
            </nav>
        </c:if>

        <section class="kpi-grid">
            <article class="kpi-card">
                <span class="kpi-card__label">총매출</span>
                <strong class="kpi-card__value"><fmt:formatNumber value="${dashboardSummary.totalGrossSales}" pattern="#,##0.00" /></strong>
            </article>
            <article class="kpi-card kpi-card--positive">
                <span class="kpi-card__label">총순매출</span>
                <strong class="kpi-card__value"><fmt:formatNumber value="${dashboardSummary.totalNetSales}" pattern="#,##0.00" /></strong>
            </article>
            <article class="kpi-card kpi-card--danger">
                <span class="kpi-card__label">총취소금액</span>
                <strong class="kpi-card__value"><fmt:formatNumber value="${dashboardSummary.totalCancelAmount}" pattern="#,##0.00" /></strong>
            </article>
            <article class="kpi-card">
                <span class="kpi-card__label">총판매수량</span>
                <strong class="kpi-card__value"><fmt:formatNumber value="${dashboardSummary.totalSoldQuantity}" pattern="#,##0" /></strong>
            </article>
            <article class="kpi-card">
                <span class="kpi-card__label">최고 매출 월</span>
                <strong class="kpi-card__value kpi-card__value--text"><c:out value="${dashboardSummary.bestSalesMonth}" default="-" /></strong>
                <span class="kpi-card__meta">순매출 <fmt:formatNumber value="${dashboardSummary.bestMonthNetSales}" pattern="#,##0.00" /></span>
            </article>
            <article class="kpi-card">
                <span class="kpi-card__label">최고 매출 상품</span>
                <strong class="kpi-card__value kpi-card__value--text"><c:out value="${dashboardSummary.topProductName}" default="-" /></strong>
                <span class="kpi-card__meta"><c:out value="${dashboardSummary.topProductCode}" default="-" /></span>
            </article>
            <article class="kpi-card">
                <span class="kpi-card__label">분석 상품 수</span>
                <strong class="kpi-card__value"><fmt:formatNumber value="${dashboardSummary.productCount}" pattern="#,##0" /></strong>
            </article>
            <article class="kpi-card">
                <span class="kpi-card__label">품질 통계 유형</span>
                <strong class="kpi-card__value"><fmt:formatNumber value="${dashboardSummary.qualityTypeCount}" pattern="#,##0" /></strong>
            </article>
        </section>

        <section class="panel chart-panel" aria-labelledby="monthlyChartTitle">
            <div class="panel-heading">
                <div>
                    <h2 id="monthlyChartTitle">월별 매출 추이</h2>
                    <p class="chart-description">총매출·취소금액·순매출의 월별 변화를 비교합니다.</p>
                </div>
            </div>
            <div class="chart-shell">
                <canvas id="monthlySalesChart" role="img" aria-label="월별 총매출, 취소금액, 순매출 추이 차트"></canvas>
            </div>
            <p id="monthlyChartStatus" class="chart-status" aria-live="polite"></p>
            <div id="monthlyChartData" hidden>
                <c:forEach var="item" items="${monthlyStats}">
                    <span class="monthly-chart-data"
                        data-sales-month="${fn:escapeXml(item.salesMonth)}"
                        data-gross-sales="${item.grossSales}"
                        data-cancel-amount="${item.cancelAmount}"
                        data-net-sales="${item.netSales}"></span>
                </c:forEach>
            </div>
        </section>

        <section class="panel">
            <div class="panel-heading">
                <h2>월별 매출 요약</h2>
                <c:if test="${not empty selectedJobId}">
                    <a class="text-link" href="${pageContext.request.contextPath}/monthly-sales?jobId=${selectedJobId}">전체 보기</a>
                </c:if>
            </div>
            <div class="table-scroll">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>월</th>
                            <th>주문수</th>
                            <th>판매수량</th>
                            <th>총매출</th>
                            <th>취소금액</th>
                            <th>순매출</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty monthlyStats}">
                                <tr>
                                    <td class="empty-cell" colspan="6">월별 통계가 없습니다.</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="item" items="${monthlyStats}">
                                    <tr>
                                        <td><c:out value="${item.salesMonth}" /></td>
                                        <td><fmt:formatNumber value="${item.salesOrderCount}" pattern="#,##0" /></td>
                                        <td><fmt:formatNumber value="${item.soldQuantity}" pattern="#,##0" /></td>
                                        <td><fmt:formatNumber value="${item.grossSales}" pattern="#,##0.00" /></td>
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

        <!-- 데이터 품질 통계 영역 (유형 설명 추가 완료) -->
        <section class="panel">
            <div class="panel-heading">
                <h2>데이터 품질 통계</h2>
                <p class="chart-description">하둡(Hadoop) 파싱 과정에서 분류된 데이터의 상태 및 결함 내역입니다.</p>
            </div>
            <div class="table-scroll">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>품질 유형</th>
                            <th>유형 설명</th>
                            <th>건수</th>
                            <th>예시 메시지</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty qualityStats}">
                                <tr>
                                    <td class="empty-cell" colspan="4">데이터 품질 통계가 없습니다.</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="quality" items="${qualityStats}">
                                    <tr>
                                        <td><strong><c:out value="${quality.qualityType}" /></strong></td>
                                        <td class="text-muted">
                                            <c:choose>
                                                <c:when test="${quality.qualityType == 'NORMAL'}">정상적인 매출 거래 데이터</c:when>
                                                <c:when test="${quality.qualityType == 'CANCELLED'}">취소된 거래 (주문번호 C 포함)</c:when>
                                                <c:when test="${quality.qualityType == 'ADJUSTMENT'}">금액 또는 수량이 조정된 내역</c:when>
                                                <c:when test="${quality.qualityType == 'HEADER_SKIPPED'}">CSV 첫 줄(헤더)이라서 분석 제외</c:when>
                                                <c:when test="${quality.qualityType == 'MISSING_CUSTOMER_ID'}">고객 ID가 누락된 데이터</c:when>
                                                <c:when test="${quality.qualityType == 'MISSING_DESCRIPTION'}">상품명(설명)이 비어있는 데이터</c:when>
                                                <c:when test="${quality.qualityType == 'ZERO_OR_NEG_PRICE'}">단가가 0원이거나 음수인 비정상 데이터</c:when>
                                                <c:when test="${quality.qualityType == 'PARSE_ERROR'}">숫자 변환 실패 등 읽을 수 없는 에러</c:when>
                                                <c:otherwise>기타 미분류 오류 데이터</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><fmt:formatNumber value="${quality.recordCount}" pattern="#,##0" /></td>
                                        <td><c:out value="${quality.sampleMessage}" default="-" /></td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </section>
    </main>

    <script src="${pageContext.request.contextPath}/assets/js/dashboard.js?v=4"></script>
    <script
    src="${pageContext.request.contextPath}/assets/js/theme.js">
</script>
</body>
</html>