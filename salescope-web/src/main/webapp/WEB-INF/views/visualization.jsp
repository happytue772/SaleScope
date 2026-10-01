<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SaleScope 시각화 대시보드</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/salescope.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.5.1/dist/chart.umd.min.js"></script>
    <link rel="stylesheet"
    href="${pageContext.request.contextPath}/assets/css/theme.css">
</head>
<body>
    <header class="site-header">
        <div class="site-header__inner">
            <a class="brand" href="${pageContext.request.contextPath}/dashboard">SaleScope</a>
            <span class="brand-subtitle">시각화 전용 대시보드</span>
        </div>
    </header>

    <main class="container">
    <%@ include file="common/header.jsp" %>
        <section class="page-heading page-heading--with-actions">
            <div>
                <p class="eyebrow">VISUALIZATION</p>
                <h1>종합 시각화 리포트</h1>
                <p class="page-description">Job #<c:out value="${jobId}" />의 다각적 분석 시각화 결과입니다.</p>
            </div>
            <div class="heading-actions">
                <a class="button button--secondary" href="${pageContext.request.contextPath}/dashboard">기본 대시보드로 돌아가기</a>
            </div>
        </section>

        <!-- 1. 차트를 그릴 영역 -->
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px;">
            <section class="panel chart-panel">
                <div class="panel-heading"><h2>TOP 5 상품 순매출 (막대 차트)</h2></div>
                <div class="chart-shell" style="height: 300px;"><canvas id="topProductBarChart"></canvas></div>
            </section>
            
            <section class="panel chart-panel">
                <div class="panel-heading"><h2>TOP 5 상품 점유율 (도넛 차트)</h2></div>
                <div class="chart-shell" style="height: 300px;"><canvas id="topProductDoughnutChart"></canvas></div>
            </section>
        </div>

        <section class="panel chart-panel">
            <div class="panel-heading"><h2>월별 매출 종합 추이 (혼합 차트)</h2></div>
            <div class="chart-shell" style="height: 400px;"><canvas id="monthlyTrendChart"></canvas></div>
        </section>

        <!-- 2. 자바스크립트에 전달할 데이터 은닉 영역 -->
        <div id="monthlyData" hidden>
            <c:forEach var="item" items="${monthlyStats}">
                <span class="data-row" data-month="${fn:escapeXml(item.salesMonth)}" data-gross="${item.grossSales}" data-cancel="${item.cancelAmount}" data-net="${item.netSales}"></span>
            </c:forEach>
        </div>
        
        <div id="productData" hidden>
            <c:forEach var="item" items="${topProducts}">
                <span class="data-row" data-name="${fn:escapeXml(item.productName)}" data-net="${item.netSales}"></span>
            </c:forEach>
        </div>
    </main>

    <script src="${pageContext.request.contextPath}/assets/js/visualization.js"></script>
    <script
    src="${pageContext.request.contextPath}/assets/js/theme.js">
</script>
</body>
</html>