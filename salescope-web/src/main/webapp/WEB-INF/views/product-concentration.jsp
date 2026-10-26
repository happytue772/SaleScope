<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SaleScope 상품 매출 집중도</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/salescope.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/product-concentration.css?v=20261014-1">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.5.1/dist/chart.umd.min.js"></script>
</head>
<body>
    <header class="site-header">
        <div class="site-header__inner">
            <a class="brand" href="${pageContext.request.contextPath}/dashboard">SaleScope</a>
            <span class="brand-subtitle">상품 매출 집중도</span>
        </div>
    </header>
    <main class="container">
        <%@ include file="common/header.jsp" %>
        <section class="page-heading page-heading--with-actions">
            <div>
                <p class="eyebrow">PRODUCT CONCENTRATION</p>
                <h1>상품 매출 집중도</h1>
                <p class="page-description">어떤 상품에 순매출이 집중되는지 누적 비중과 등급으로 살펴봅니다.</p>
            </div>
            <a class="button button--secondary" href="${pageContext.request.contextPath}/dashboard">대시보드</a>
        </section>

        <c:if test="${not empty datasets}">
            <section class="panel">
                <div class="panel-heading"><h2>분석 결과 선택</h2></div>
                <form class="filter-grid" method="get" action="${pageContext.request.contextPath}/product-concentration">
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
                        <label for="jobId">적재 완료 Job</label>
                        <select id="jobId" name="jobId">
                            <c:forEach var="job" items="${jobs}">
                                <option value="${job.jobId}" ${job.jobId == selectedJobId ? 'selected' : ''}>
                                    Job #<c:out value="${job.jobId}" /> · <c:out value="${job.analysisVersion}" />
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="form-actions"><button class="button button--primary" type="submit">집중도 보기</button></div>
                </form>
            </section>
        </c:if>

        <c:if test="${not empty message}"><div class="notice"><c:out value="${message}" /></div></c:if>
        <c:if test="${not empty concentration}">
            <c:choose>
                <c:when test="${empty concentration.topRows}">
                    <div class="notice">양수 순매출이 있는 상품 결과가 없습니다. 상품별 결과 적재 상태를 확인해 주세요.</div>
                </c:when>
                <c:otherwise>
                    <p class="chart-description concentration-note">
                        Job #<c:out value="${selectedJobId}" />의 양수 순매출 상품 전체를 분모로 계산했습니다.
                        순매출 0 이하 상품은 제외합니다. A는 상품을 더하기 전 누적 비중이 80% 미만,
                        B는 80~95% 미만, C는 그 이후입니다. 경계에 걸친 상품은 앞 등급에 포함됩니다.
                    </p>
                    <section class="kpi-grid kpi-grid--four" aria-label="상품 매출 집중도 요약">
                        <article class="kpi-card">
                            <span class="kpi-card__label">분석 상품 수</span>
                            <strong class="kpi-card__value"><fmt:formatNumber value="${concentration.productCount}" pattern="#,##0" /></strong>
                        </article>
                        <article class="kpi-card">
                            <span class="kpi-card__label">양수 순매출 합계</span>
                            <strong class="kpi-card__value"><fmt:formatNumber value="${concentration.positiveNetTotal}" pattern="#,##0.00" /></strong>
                        </article>
                        <article class="kpi-card">
                            <span class="kpi-card__label">상위 5개 상품 비중</span>
                            <strong class="kpi-card__value"><fmt:formatNumber value="${concentration.topFivePercent}" pattern="#,##0.00" />%</strong>
                        </article>
                        <article class="kpi-card">
                            <span class="kpi-card__label">A / B / C 상품 수</span>
                            <strong class="kpi-card__value"><c:out value="${concentration.gradeACount}" /> / <c:out value="${concentration.gradeBCount}" /> / <c:out value="${concentration.gradeCCount}" /></strong>
                        </article>
                    </section>
                    <section class="panel chart-panel">
                        <div class="panel-heading"><h2>상품별 누적 순매출 비중</h2></div>
                        <p class="chart-description">그래프는 상위 20개를 표시합니다. 기준선은 전체 상품 순매출 합계의 80%와 95%입니다.</p>
                        <div class="chart-shell"><canvas id="concentrationChart" role="img" aria-label="상품별 누적 순매출 비중 그래프"></canvas></div>
                        <p id="concentrationChartStatus" class="chart-status" aria-live="polite"></p>
                    </section>
                    <section class="panel">
                        <div class="panel-heading"><h2>상위 20개 상품 상세</h2></div>
                        <p class="chart-description">CSV에는 상위 20개 제한 없이 계산에 포함된 모든 상품과 등급이 들어갑니다.</p>
                        <a class="button button--secondary concentration-download"
                            href="${pageContext.request.contextPath}/product-concentration/csv?datasetId=${selectedDatasetId}&amp;jobId=${selectedJobId}">전체 상품 등급 CSV 다운로드</a>
                        <div class="table-scroll">
                            <table class="data-table concentration-table">
                                <thead><tr><th>코드</th><th>상품명</th><th>순매출</th><th>개별 비중</th><th>누적 비중</th><th>등급</th></tr></thead>
                                <tbody>
                                    <c:forEach var="row" items="${concentration.topRows}">
                                        <tr>
                                            <td><c:out value="${row.stockCode}" /></td>
                                            <td><c:out value="${row.productName}" /></td>
                                            <td><fmt:formatNumber value="${row.netSales}" pattern="#,##0.00" /></td>
                                            <td><fmt:formatNumber value="${row.sharePercent}" pattern="#,##0.00" />%</td>
                                            <td><fmt:formatNumber value="${row.cumulativePercent}" pattern="#,##0.00" />%</td>
                                            <td><c:out value="${row.grade}" /></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </section>
                    <div id="concentrationData" hidden>
                        <c:forEach var="row" items="${concentration.topRows}">
                            <span class="data-row" data-code="${fn:escapeXml(row.stockCode)}"
                                data-cumulative="${row.cumulativePercent}"></span>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </c:if>
    </main>
    <script src="${pageContext.request.contextPath}/assets/js/concentration.js?v=20261014-1"></script>
    <script src="${pageContext.request.contextPath}/assets/js/theme.js"></script>
</body>
</html>
