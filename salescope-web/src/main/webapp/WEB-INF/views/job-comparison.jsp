<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SaleScope 분석 Job 비교</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/salescope.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/job-comparison.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.5.1/dist/chart.umd.min.js"></script>
</head>
<body>
    <header class="site-header">
        <div class="site-header__inner">
            <a class="brand" href="${pageContext.request.contextPath}/dashboard">SaleScope</a>
            <span class="brand-subtitle">분석 Job 비교</span>
        </div>
    </header>
    <main class="container">
        <%@ include file="common/header.jsp" %>
        <section class="page-heading page-heading--with-actions">
            <div>
                <p class="eyebrow">JOB COMPARISON</p>
                <h1>분석 Job 비교</h1>
                <p class="page-description">같은 데이터셋의 적재 완료 Job을 공통 월 기준으로 비교합니다.</p>
            </div>
            <a class="button button--secondary" href="${pageContext.request.contextPath}/dashboard">대시보드</a>
        </section>

        <section class="panel">
            <div class="panel-heading"><h2>비교 대상 선택</h2></div>
            <form class="filter-grid" method="get" action="${pageContext.request.contextPath}/job-comparison">
                <div class="form-field">
                    <label for="datasetId">데이터셋</label>
                    <select id="datasetId" name="datasetId" onchange="this.form.submit()">
                        <c:forEach var="dataset" items="${datasets}">
                            <option value="${dataset.datasetId}" ${dataset.datasetId == selectedDatasetId ? 'selected' : ''}>
                                Dataset #<c:out value="${dataset.datasetId}" /> · <c:out value="${dataset.displayName}" />
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-field">
                    <label for="firstJobId">기준 Job</label>
                    <select id="firstJobId" name="firstJobId">
                        <c:forEach var="job" items="${jobs}">
                            <option value="${job.jobId}" ${job.jobId == firstJobId ? 'selected' : ''}>
                                Job #<c:out value="${job.jobId}" /> · <c:out value="${job.analysisVersion}" />
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-field">
                    <label for="secondJobId">비교 Job</label>
                    <select id="secondJobId" name="secondJobId">
                        <c:forEach var="job" items="${jobs}">
                            <option value="${job.jobId}" ${job.jobId == secondJobId ? 'selected' : ''}>
                                Job #<c:out value="${job.jobId}" /> · <c:out value="${job.analysisVersion}" />
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-actions">
                    <button class="button button--primary" type="submit">비교하기</button>
                </div>
            </form>
        </section>

        <c:if test="${not empty message}"><div class="notice"><c:out value="${message}" /></div></c:if>

        <c:if test="${not empty comparison}">
            <c:choose>
                <c:when test="${empty comparison.rows}">
                    <div class="notice">두 Job에 공통으로 존재하는 월별 순매출 데이터가 없습니다.</div>
                </c:when>
                <c:otherwise>
                    <p class="chart-description comparison-note">
                        Job #<c:out value="${firstJobId}" />을 기준으로 Job #<c:out value="${secondJobId}" />을 비교합니다.
                        두 Job에 모두 존재하는 <c:out value="${fn:length(comparison.rows)}" />개월만 합산했습니다.
                        기준 순매출이 0 이하인 경우 변화율은 표시하지 않습니다.
                    </p>
                    <section class="kpi-grid kpi-grid--four" aria-label="공통 월 순매출 요약">
                        <article class="kpi-card">
                            <span class="kpi-card__label">기준 Job #<c:out value="${firstJobId}" /> 순매출</span>
                            <strong class="kpi-card__value"><fmt:formatNumber value="${comparison.firstTotal}" pattern="#,##0.00" /></strong>
                        </article>
                        <article class="kpi-card">
                            <span class="kpi-card__label">비교 Job #<c:out value="${secondJobId}" /> 순매출</span>
                            <strong class="kpi-card__value"><fmt:formatNumber value="${comparison.secondTotal}" pattern="#,##0.00" /></strong>
                        </article>
                        <article class="kpi-card">
                            <span class="kpi-card__label">순매출 차이 (비교 − 기준)</span>
                            <strong class="kpi-card__value"><fmt:formatNumber value="${comparison.totalDifference}" pattern="+#,##0.00;-#,##0.00" /></strong>
                        </article>
                        <article class="kpi-card">
                            <span class="kpi-card__label">순매출 차이율</span>
                            <strong class="kpi-card__value">
                                <c:choose>
                                    <c:when test="${not empty comparison.totalDifferencePercent}"><fmt:formatNumber value="${comparison.totalDifferencePercent}" pattern="+#,##0.00;-#,##0.00" />%</c:when>
                                    <c:otherwise>—</c:otherwise>
                                </c:choose>
                            </strong>
                        </article>
                    </section>

                    <div class="comparison-charts">
                        <section class="panel chart-panel">
                            <div class="panel-heading"><h2>공통 월 순매출 추이</h2></div>
                            <div class="chart-shell"><canvas id="comparisonTrendChart" role="img" aria-label="두 Job의 월별 순매출 선 그래프"></canvas></div>
                        </section>
                        <section class="panel chart-panel">
                            <div class="panel-heading"><h2>월별 순매출 차이</h2></div>
                            <div class="chart-shell"><canvas id="comparisonDifferenceChart" role="img" aria-label="비교 Job에서 기준 Job을 뺀 월별 순매출 막대 그래프"></canvas></div>
                        </section>
                    </div>
                    <p id="comparisonChartStatus" class="chart-status" aria-live="polite"></p>

                    <section class="panel">
                        <div class="panel-heading"><h2>공통 월 상세 비교</h2></div>
                        <div class="table-scroll">
                            <table class="data-table comparison-table">
                                <thead><tr><th>월</th><th>기준 순매출</th><th>비교 순매출</th><th>차이</th><th>차이율</th></tr></thead>
                                <tbody>
                                    <c:forEach var="row" items="${comparison.rows}">
                                        <tr>
                                            <td><c:out value="${row.salesMonth}" /></td>
                                            <td><fmt:formatNumber value="${row.firstNetSales}" pattern="#,##0.00" /></td>
                                            <td><fmt:formatNumber value="${row.secondNetSales}" pattern="#,##0.00" /></td>
                                            <td><fmt:formatNumber value="${row.difference}" pattern="+#,##0.00;-#,##0.00" /></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty row.differencePercent}"><fmt:formatNumber value="${row.differencePercent}" pattern="+#,##0.00;-#,##0.00" />%</c:when>
                                                    <c:otherwise>—</c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </section>
                    <div id="comparisonData" hidden data-first-job="${firstJobId}" data-second-job="${secondJobId}">
                        <c:forEach var="row" items="${comparison.rows}">
                            <span class="data-row" data-month="${fn:escapeXml(row.salesMonth)}"
                                data-first="${row.firstNetSales}" data-second="${row.secondNetSales}"
                                data-difference="${row.difference}"></span>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </c:if>
    </main>
    <script src="${pageContext.request.contextPath}/assets/js/job-comparison.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/theme.js"></script>
</body>
</html>
