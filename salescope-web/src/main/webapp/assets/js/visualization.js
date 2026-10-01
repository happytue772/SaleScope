document.addEventListener("DOMContentLoaded", function () {
    
    // 1. 월별 데이터 파싱
    const monthlyElements = document.querySelectorAll("#monthlyData .data-row");
    const monthLabels = [], grossData = [], cancelData = [], netData = [];
    
    monthlyElements.forEach(el => {
        monthLabels.push(el.getAttribute("data-month"));
        grossData.push(parseFloat(el.getAttribute("data-gross")) || 0);
        cancelData.push(parseFloat(el.getAttribute("data-cancel")) || 0);
        netData.push(parseFloat(el.getAttribute("data-net")) || 0);
    });

    // 2. 상품별 데이터 파싱
    const productElements = document.querySelectorAll("#productData .data-row");
    const productLabels = [], productNetData = [];
    
    productElements.forEach(el => {
        // 이름이 너무 길면 자르기
        let name = el.getAttribute("data-name") || "알 수 없음";
        if(name.length > 15) name = name.substring(0, 15) + "...";
        
        productLabels.push(name);
        productNetData.push(parseFloat(el.getAttribute("data-net")) || 0);
    });

    // 공통 툴팁 포맷 (천단위 콤마)
    const tooltipFormat = {
        callbacks: {
            label: function(context) {
                return context.dataset.label + ': ' + new Intl.NumberFormat('ko-KR').format(context.parsed.y || context.parsed);
            }
        }
    };

    // --- 차트 1: TOP 5 상품 막대 차트 ---
    if (document.getElementById("topProductBarChart")) {
        new Chart(document.getElementById("topProductBarChart").getContext("2d"), {
            type: 'bar',
            data: {
                labels: productLabels,
                datasets: [{
                    label: '순매출',
                    data: productNetData,
                    backgroundColor: 'rgba(54, 162, 235, 0.7)'
                }]
            },
            options: { responsive: true, maintainAspectRatio: false, plugins: { tooltip: tooltipFormat } }
        });
    }

    // --- 차트 2: TOP 5 상품 도넛 차트 ---
    if (document.getElementById("topProductDoughnutChart")) {
        new Chart(document.getElementById("topProductDoughnutChart").getContext("2d"), {
            type: 'doughnut',
            data: {
                labels: productLabels,
                datasets: [{
                    label: '순매출',
                    data: productNetData,
                    backgroundColor: [
                        'rgba(255, 99, 132, 0.7)', 'rgba(54, 162, 235, 0.7)',
                        'rgba(255, 206, 86, 0.7)', 'rgba(75, 192, 192, 0.7)', 'rgba(153, 102, 255, 0.7)'
                    ]
                }]
            },
            options: { responsive: true, maintainAspectRatio: false, plugins: { tooltip: tooltipFormat } }
        });
    }

    // --- 차트 3: 월별 종합 혼합 차트 ---
    if (document.getElementById("monthlyTrendChart")) {
        new Chart(document.getElementById("monthlyTrendChart").getContext("2d"), {
            type: 'bar',
            data: {
                labels: monthLabels,
                datasets: [
                    { label: '순매출 (Bar)', data: netData, backgroundColor: 'rgba(75, 192, 192, 0.6)', order: 2 },
                    { label: '총매출 (Line)', data: grossData, type: 'line', borderColor: 'rgba(54, 162, 235, 1)', fill: false, tension: 0.1, order: 1 },
                    { label: '취소금액 (Line)', data: cancelData, type: 'line', borderColor: 'rgba(255, 99, 132, 1)', fill: false, tension: 0.1, order: 0 }
                ]
            },
            options: { responsive: true, maintainAspectRatio: false, plugins: { tooltip: tooltipFormat } }
        });
    }
});