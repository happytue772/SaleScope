<%@ page language="java"
contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt"
uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn"
uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>

<html lang="ko">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
        content="width=device-width, initial-scale=1.0">

    <title>SaleScope 상품별 매출 분석</title>

    <link rel="stylesheet"
        type="text/css"
        href="${pageContext.request.contextPath}/assets/css/salescope.css?v=4">

    <script
        src="https://code.jquery.com/jquery-3.7.1.min.js">
    </script>

    <link rel="stylesheet"
        href="https://cdn.jsdelivr.net/npm/select2@4.1.0-rc.0/dist/css/select2.min.css">

    <script
        src="https://cdn.jsdelivr.net/npm/select2@4.1.0-rc.0/dist/js/select2.min.js">
    </script>

    <link rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/theme.css?v=20260831">

</head>

<body>

    <header class="site-header">

        <div class="site-header__inner">

            <a class="brand"
                href="${pageContext.request.contextPath}/dashboard">
                SaleScope
            </a>

            <span class="brand-subtitle">
                상품별 매출 분석
            </span>

        </div>

    </header>

    <main class="container">

      

    <%@ include file="common/header.jsp" %>
    <section class="page-heading page-heading--with-actions">

    <div>
        <p class="eyebrow">PRODUCT SALES</p>

        <h1>상품별 매출 분석</h1>

        <p class="page-description">
            Job #<c:out value="${jobId}" />의 상품별 매출과 취소 내역입니다.
        </p>
    </div>

    <div class="heading-actions">
        <a
            class="button button--secondary"
            href="${pageContext.request.contextPath}/dashboard?jobId=${jobId}">
            대시보드
        </a>

        <a
            class="button button--secondary"
            href="${pageContext.request.contextPath}/monthly-sales?jobId=${jobId}">
            월별 분석
        </a>
    </div>

</section>

    <nav
        class="page-nav"
        aria-label="분석 페이지 이동"
        style="margin-top: 20px;">

        <a
            class="page-nav__link"
            href="${pageContext.request.contextPath}/dashboard?jobId=${jobId}">
            대시보드 홈
        </a>

        <a
            class="page-nav__link"
            href="${pageContext.request.contextPath}/monthly-sales?jobId=${jobId}">
            월별 분석
        </a>

        <a
            class="page-nav__link page-nav__link--active"
            href="${pageContext.request.contextPath}/product-sales?jobId=${jobId}">
            상품별 분석
        </a>

        <a
            class="page-nav__link"
            href="${pageContext.request.contextPath}/visualization?jobId=${jobId}">
            시각화 대시보드
        </a>
    </nav>

    <section class="panel">
        <div class="panel-heading">
            <h2>상품 조회조건</h2>
        </div>

        <form
            class="filter-grid"
            method="get"
            action="${pageContext.request.contextPath}/product-sales">

            <input
                type="hidden"
                name="jobId"
                value="${jobId}">

            <div
                class="form-field form-field--wide"
                style="
                    background: #f8fafc;
                    padding: 18px;
                    border-radius: 8px;
                    border: 1px solid #e2e8f0;
                    box-shadow:
                        0 1px 3px
                        rgba(0, 0, 0, 0.02);
                ">

                <label
                    for="keyword"
                    style="
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        margin-bottom: 10px;
                    ">

                    <span
                        style="
                            font-weight: 600;
                            color: #1e293b;
                            font-size: 0.95rem;
                            display: flex;
                            align-items: center;
                            gap: 6px;
                        ">

                        <span style="color: #3b82f6;">
                            🔍
                        </span>

                        상품 선택 및 검색
                    </span>

                    <span
                        style="
                            font-size: 0.8rem;
                            color: #2563eb;
                            font-weight: 600;
                            background-color: #eff6ff;
                            padding: 3px 10px;
                            border-radius: 20px;
                            border: 1px solid #bfdbfe;
                        ">

                        검색 가능 상품:

                        <strong>
                            <fmt:formatNumber
                                value="${fn:length(allProducts)}"
                                pattern="#,##0" />
                        </strong>개
                    </span>
                </label>

                <select
                    id="keyword"
                    name="keyword"
                    class="select2"
                    style="width: 100%;">

                    <option value="">
                        🔍 상품코드 또는 상품명을 입력하여 검색하세요
                    </option>

                    <c:forEach
                        var="prod"
                        items="${allProducts}">

                        <option
                            value="${prod.stockCode}"
                            ${keyword == prod.stockCode
                                ? 'selected'
                                : ''}>

                            [${prod.stockCode}]
                            ${prod.productName}
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-field">
                <label for="topN">
                    TOP N 조회
                </label>

                <select
                    id="topN"
                    name="topN">

                    <option
                        value="5"
                        ${topN == 5 ? 'selected' : ''}>
                        TOP 5 (상위 5개)
                    </option>

                    <option
                        value="10"
                        ${topN == 10 || empty topN
                            ? 'selected'
                            : ''}>
                        TOP 10 (상위 10개)
                    </option>

                    <option
                        value="20"
                        ${topN == 20 ? 'selected' : ''}>
                        TOP 20 (상위 20개)
                    </option>

                    <option
                        value="50"
                        ${topN == 50 ? 'selected' : ''}>
                        TOP 50 (상위 50개)
                    </option>

                    <option
                        value="100"
                        ${topN == 100 ? 'selected' : ''}>
                        TOP 100 (상위 100개)
                    </option>
                </select>
            </div>

            <div class="form-field">
                <label for="sortBy">
                    정렬 기준
                </label>

                <select
                    id="sortBy"
                    name="sortBy">

                    <option
                        value="netSales"
                        ${sortBy == 'netSales'
                            ? 'selected'
                            : ''}>
                        순매출
                    </option>

                    <option
                        value="soldQuantity"
                        ${sortBy == 'soldQuantity'
                            ? 'selected'
                            : ''}>
                        판매수량
                    </option>

                    <option
                        value="salesOrderCount"
                        ${sortBy == 'salesOrderCount'
                            ? 'selected'
                            : ''}>
                        주문수
                    </option>

                    <option
                        value="cancelAmount"
                        ${sortBy == 'cancelAmount'
                            ? 'selected'
                            : ''}>
                        취소금액
                    </option>
                </select>
            </div>

            <div class="form-field">
                <label for="sortDirection">
                    정렬 방향
                </label>

                <select
                    id="sortDirection"
                    name="sortDirection">

                    <option
                        value="desc"
                        ${sortDirection == 'desc'
                            ? 'selected'
                            : ''}>
                        높은 값부터 (내림차순)
                    </option>

                    <option
                        value="asc"
                        ${sortDirection == 'asc'
                            ? 'selected'
                            : ''}>
                        낮은 값부터 (오름차순)
                    </option>
                </select>
            </div>

            <div class="form-field">
                <label for="minNetSales">
                    최소 순매출
                </label>

                <input
                    type="number"
                    id="minNetSales"
                    name="minNetSales"
                    min="0"
                    step="0.01"
                    value="${minNetSales}"
                    placeholder="예: 10000">

                <span
                    style="
                        font-size: 0.75rem;
                        color: #64748b;
                        display: block;
                        margin-top: 4px;
                    ">

                    범위:

                    <fmt:formatNumber
                        value="${dataRange.minNetSales}"
                        pattern="#,##0.##" />

                    ~

                    <fmt:formatNumber
                        value="${dataRange.maxNetSales}"
                        pattern="#,##0.##" />
                </span>
            </div>

            <div class="form-field">
                <label for="minSoldQuantity">
                    최소 판매량
                </label>

                <input
                    type="number"
                    id="minSoldQuantity"
                    name="minSoldQuantity"
                    min="0"
                    step="1"
                    value="${minSoldQuantity}"
                    placeholder="예: 100">

                <span
                    style="
                        font-size: 0.75rem;
                        color: #64748b;
                        display: block;
                        margin-top: 4px;
                    ">

                    범위:

                    <fmt:formatNumber
                        value="${dataRange.minQty}"
                        pattern="#,##0" />

                    ~

                    <fmt:formatNumber
                        value="${dataRange.maxQty}"
                        pattern="#,##0" />
                </span>
            </div>

            <div class="form-field">
                <label for="minCancelAmount">
                    최소 취소금액
                </label>

                <input
                    type="number"
                    id="minCancelAmount"
                    name="minCancelAmount"
                    min="0"
                    step="0.01"
                    value="${minCancelAmount}"
                    placeholder="예: 1000">

                <span
                    style="
                        font-size: 0.75rem;
                        color: #64748b;
                        display: block;
                        margin-top: 4px;
                    ">

                    범위:

                    <fmt:formatNumber
                        value="${dataRange.minCancel}"
                        pattern="#,##0.##" />

                    ~

                    <fmt:formatNumber
                        value="${dataRange.maxCancel}"
                        pattern="#,##0.##" />
                </span>
            </div>

            <div
                class="form-actions form-actions--full">

                <button
                    class="button button--primary"
                    type="submit">
                    조건 조회
                </button>

                <a
                    class="button button--ghost"
                    href="${pageContext.request.contextPath}/product-sales?jobId=${jobId}">
                    초기화
                </a>

                <button
                    class="button button--success"
                    type="submit"
                    formaction="${pageContext.request.contextPath}/product-sales/csv"
                    formmethod="get">
                    CSV 다운로드
                </button>
            </div>
        </form>
    </section>

    <section class="panel">
        <div
            class="panel-heading"
            style="
                display: flex;
                justify-content: space-between;
                align-items: center;
            ">

            <div>
                <h2>상품별 상세 결과</h2>

                <p
                    class="chart-description"
                    style="
                        margin-top: 4px;
                        color: #315efb;
                        font-weight: 500;
                    ">

                    정렬 기준:

                    <strong>
                        <c:choose>
                            <c:when
                                test="${sortBy == 'soldQuantity'}">
                                판매수량
                            </c:when>

                            <c:when
                                test="${sortBy == 'salesOrderCount'}">
                                주문수
                            </c:when>

                            <c:when
                                test="${sortBy == 'cancelAmount'}">
                                취소금액
                            </c:when>

                            <c:otherwise>
                                순매출
                            </c:otherwise>
                        </c:choose>
                    </strong>

                    (${sortDirection == 'asc'
                        ? '낮은 값부터 ▲'
                        : '높은 값부터 ▼'})
                </p>
            </div>

            <div style="text-align: right;">
                <span
                    style="
                        font-size: 0.85rem;
                        color: #6b7280;
                        margin-right: 8px;
                    ">

                    조건에 맞는 전체 데이터:

                    <strong>
                        <fmt:formatNumber
                            value="${totalMatchedCount}"
                            pattern="#,##0" />
                    </strong>건
                </span>

                <span class="result-count">
                    현재 화면

                    <strong>
                        <c:out
                            value="${fn:length(productList)}" />
                    </strong>건 조회됨
                </span>
            </div>
        </div>

        <div class="table-scroll">
            <table
                class="data-table data-table--products">

                <thead>
                    <tr>
                        <th>순번</th>
                        <th>상품코드</th>
                        <th>상품명</th>

                        <th
                            style="${sortBy == 'salesOrderCount'
                                ? 'color: #315efb; font-weight: bold; background-color: #f0f4ff;'
                                : ''}">

                            주문수

                            <c:if
                                test="${sortBy == 'salesOrderCount'}">
                                ${sortDirection == 'asc'
                                    ? '▲'
                                    : '▼'}
                            </c:if>
                        </th>

                        <th
                            style="${sortBy == 'soldQuantity'
                                ? 'color: #315efb; font-weight: bold; background-color: #f0f4ff;'
                                : ''}">

                            판매수량

                            <c:if
                                test="${sortBy == 'soldQuantity'}">
                                ${sortDirection == 'asc'
                                    ? '▲'
                                    : '▼'}
                            </c:if>
                        </th>

                        <th>총매출</th>
                        <th>취소주문수</th>
                        <th>취소수량</th>

                        <th
                            style="${sortBy == 'cancelAmount'
                                ? 'color: #c2414b; font-weight: bold; background-color: #fff0f0;'
                                : ''}">

                            취소금액

                            <c:if
                                test="${sortBy == 'cancelAmount'}">
                                ${sortDirection == 'asc'
                                    ? '▲'
                                    : '▼'}
                            </c:if>
                        </th>

                        <th>순판매수량</th>

                        <th
                            style="${sortBy == 'netSales'
                                ? 'color: #137a55; font-weight: bold; background-color: #f0fff4;'
                                : ''}">

                            순매출

                            <c:if
                                test="${sortBy == 'netSales'}">
                                ${sortDirection == 'asc'
                                    ? '▲'
                                    : '▼'}
                            </c:if>
                        </th>
                    </tr>
                </thead>

                <tbody>
                    <c:choose>
                        <c:when
                            test="${empty productList}">

                            <tr>
                                <td
                                    class="empty-cell"
                                    colspan="11">

                                    조회조건에 해당하는 상품이 없습니다.
                                </td>
                            </tr>
                        </c:when>

                        <c:otherwise>
                            <c:forEach
                                var="product"
                                items="${productList}"
                                varStatus="status">

                                <tr>
                                    <td>
                                        <c:out
                                            value="${status.count}" />
                                    </td>

                                    <td>
                                        <c:out
                                            value="${product.stockCode}" />
                                    </td>

                                    <td class="product-name">
                                        <c:out
                                            value="${product.productName}"
                                            default="-" />
                                    </td>

                                    <td
                                        style="${sortBy == 'salesOrderCount'
                                            ? 'background-color: #f8faff;'
                                            : ''}">

                                        <fmt:formatNumber
                                            value="${product.salesOrderCount}"
                                            pattern="#,##0" />
                                    </td>

                                    <td
                                        style="${sortBy == 'soldQuantity'
                                            ? 'background-color: #f8faff;'
                                            : ''}">

                                        <fmt:formatNumber
                                            value="${product.soldQuantity}"
                                            pattern="#,##0" />
                                    </td>

                                    <td>
                                        <fmt:formatNumber
                                            value="${product.grossSales}"
                                            pattern="#,##0.00" />
                                    </td>

                                    <td>
                                        <fmt:formatNumber
                                            value="${product.cancelOrderCount}"
                                            pattern="#,##0" />
                                    </td>

                                    <td>
                                        <fmt:formatNumber
                                            value="${product.cancelQuantity}"
                                            pattern="#,##0" />
                                    </td>

                                    <td
                                        class="number-danger"
                                        style="${sortBy == 'cancelAmount'
                                            ? 'background-color: #fff8f8;'
                                            : ''}">

                                        <fmt:formatNumber
                                            value="${product.cancelAmount}"
                                            pattern="#,##0.00" />
                                    </td>

                                    <td>
                                        <fmt:formatNumber
                                            value="${product.netQuantity}"
                                            pattern="#,##0" />
                                    </td>

                                    <td
                                        class="number-positive"
                                        style="${sortBy == 'netSales'
                                            ? 'background-color: #f8fff9;'
                                            : ''}">

                                        <fmt:formatNumber
                                            value="${product.netSales}"
                                            pattern="#,##0.00" />
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </section>
</main>

<script>
    $(document).ready(function() {
        $(".select2").select2({
            placeholder:
                "🔍 상품코드 또는 상품명을 입력하여 검색하세요",

            allowClear: true,

            language: {
                noResults: function() {
                    return "일치하는 상품이 없습니다.";
                }
            }
        });
    });
</script>
<script
    src="${pageContext.request.contextPath}/assets/js/theme.js">
</script>
</body>
</html>