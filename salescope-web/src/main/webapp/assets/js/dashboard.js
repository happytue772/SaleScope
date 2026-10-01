(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", initializeMonthlyChart);

    function initializeMonthlyChart() {
        var canvas = document.getElementById("monthlySalesChart");
        var status = document.getElementById("monthlyChartStatus");

        if (!canvas) {
            return;
        }

        var rows = readMonthlyChartData();

        if (rows.length === 0) {
            setStatus(status, "표시할 월별 매출 데이터가 없습니다.", false);
            return;
        }

        // 월을 기준으로 오름차순 정렬
        rows.sort(function (left, right) {
            return left.salesMonth.localeCompare(right.salesMonth);
        });

        // Chart.js 로드 실패 시 대체(Fallback) 렌더링 실행
        if (typeof window.Chart === "undefined") {
            drawFallbackChart(canvas, rows);
            setStatus(status, "네트워크 문제로 기본 차트 모드로 표시했습니다.", false);

            var resizeTimer = null;
            window.addEventListener("resize", function () {
                window.clearTimeout(resizeTimer);
                resizeTimer = window.setTimeout(function () {
                    drawFallbackChart(canvas, rows);
                }, 150);
            });
            return;
        }

        var context = canvas.getContext("2d");

        // Chart.js를 이용한 세련된 차트 렌더링
        new window.Chart(context, {
            type: "line",
            data: {
                labels: rows.map(function (row) { return row.salesMonth; }),
                datasets: [
                    createDataset("총매출", rows.map(function (row) { return row.grossSales; }), "#315efb", "rgba(49, 94, 251, 0.12)"),
                    createDataset("순매출", rows.map(function (row) { return row.netSales; }), "#137a55", "rgba(19, 122, 85, 0.10)"),
                    createDataset("취소금액", rows.map(function (row) { return row.cancelAmount; }), "#c2414b", "rgba(194, 65, 75, 0.08)")
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    mode: "index",
                    intersect: false
                },
                plugins: {
                    legend: {
                        position: "bottom",
                        labels: {
                            usePointStyle: true,
                            boxWidth: 9,
                            boxHeight: 9,
                            padding: 20,
                            color: "#3e4a5d",
                            font: {
                                family: "Pretendard, Noto Sans KR, Malgun Gothic, sans-serif",
                                size: 12,
                                weight: "600"
                            }
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                return context.dataset.label + ": " + formatNumber(context.parsed.y);
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        grid: { display: false },
                        ticks: { color: "#687386" }
                    },
                    y: {
                        beginAtZero: true,
                        border: { display: false },
                        grid: { color: "rgba(104, 115, 134, 0.15)" },
                        ticks: {
                            color: "#687386",
                            callback: function (value) {
                                return formatCompactNumber(value);
                            }
                        }
                    }
                }
            }
        });
    }

    // JSP가 숨겨둔 span 태그들에서 데이터 읽어오기
    function readMonthlyChartData() {
        var elements = document.querySelectorAll("#monthlyChartData .monthly-chart-data");
        var rows = [];

        Array.prototype.forEach.call(elements, function (element) {
            var salesMonth = element.getAttribute("data-sales-month");
            var grossSales = parseNumber(element.getAttribute("data-gross-sales"));
            var cancelAmount = parseNumber(element.getAttribute("data-cancel-amount"));
            var netSales = parseNumber(element.getAttribute("data-net-sales"));

            if (salesMonth) {
                rows.push({
                    salesMonth: salesMonth,
                    grossSales: grossSales,
                    cancelAmount: cancelAmount,
                    netSales: netSales
                });
            }
        });
        return rows;
    }

    // Chart.js 데이터셋 공통 스타일 생성
    function createDataset(label, values, color, backgroundColor) {
        return {
            label: label,
            data: values,
            borderColor: color,
            backgroundColor: backgroundColor,
            borderWidth: 2.5,
            pointRadius: 3,
            pointHoverRadius: 5,
            pointBackgroundColor: "#ffffff",
            pointBorderColor: color,
            pointBorderWidth: 2,
            tension: 0.28,
            fill: false
        };
    }

    function parseNumber(value) {
        if (value === null || value.trim() === "") return 0;
        var parsed = Number(value.replace(/,/g, ""));
        return Number.isFinite(parsed) ? parsed : 0;
    }

    function formatNumber(value) {
        return new Intl.NumberFormat("ko-KR", {
            minimumFractionDigits: 0,
            maximumFractionDigits: 2
        }).format(value);
    }

    function formatCompactNumber(value) {
        var absoluteValue = Math.abs(value);
        if (absoluteValue >= 100000000) return formatNumber(value / 100000000) + "억";
        if (absoluteValue >= 10000) return formatNumber(value / 10000) + "만";
        return formatNumber(value);
    }

    // ==========================================
    // Chart.js 로드 실패 시 작동하는 Fallback 로직 
    // ==========================================
    function drawFallbackChart(canvas, rows) {
        var parent = canvas.parentElement;
        var width = Math.max(320, parent && parent.clientWidth ? parent.clientWidth : 900);
        var height = width <= 700 ? 310 : 390;
        var ratio = window.devicePixelRatio || 1;
        var context = canvas.getContext("2d");
        var padding = { top: 48, right: 28, bottom: 58, left: width <= 520 ? 58 : 78 };
        var chartWidth = width - padding.left - padding.right;
        var chartHeight = height - padding.top - padding.bottom;
        
        var series = [
            { label: "총매출", color: "#315efb", values: rows.map(function (row) { return row.grossSales; }) },
            { label: "순매출", color: "#137a55", values: rows.map(function (row) { return row.netSales; }) },
            { label: "취소금액", color: "#c2414b", values: rows.map(function (row) { return row.cancelAmount; }) }
        ];
        
        var allValues = [];
        series.forEach(function (item) { allValues = allValues.concat(item.values); });

        var minimum = Math.min.apply(null, [0].concat(allValues));
        var maximum = Math.max.apply(null, [0].concat(allValues));
        if (minimum === maximum) maximum = minimum + 1;

        var range = maximum - minimum;
        maximum += range * 0.08;
        range = maximum - minimum;

        canvas.width = Math.round(width * ratio);
        canvas.height = Math.round(height * ratio);
        canvas.style.width = width + "px";
        canvas.style.height = height + "px";

        context.setTransform(ratio, 0, 0, ratio, 0, 0);
        context.clearRect(0, 0, width, height);
        context.font = "12px Pretendard, Noto Sans KR, Malgun Gothic, sans-serif";
        context.lineCap = "round";
        context.lineJoin = "round";

        drawFallbackGrid(context, padding, chartWidth, chartHeight, minimum, maximum);
        series.forEach(function (item) { drawFallbackLine(context, item, rows.length, padding, chartWidth, chartHeight, minimum, range); });
        drawFallbackLabels(context, rows, padding, chartWidth, chartHeight);
        drawFallbackLegend(context, series, width);
    }

    function drawFallbackGrid(context, padding, chartWidth, chartHeight, minimum, maximum) {
        var gridCount = 5;
        context.textAlign = "right";
        context.textBaseline = "middle";

        for (var index = 0; index <= gridCount; index += 1) {
            var ratio = index / gridCount;
            var y = padding.top + chartHeight * ratio;
            var value = maximum - (maximum - minimum) * ratio;

            context.beginPath();
            context.strokeStyle = "rgba(104, 115, 134, 0.18)";
            context.lineWidth = 1;
            context.moveTo(padding.left, y);
            context.lineTo(padding.left + chartWidth, y);
            context.stroke();

            context.fillStyle = "#687386";
            context.fillText(formatCompactNumber(value), padding.left - 10, y);
        }
    }

    function drawFallbackLine(context, item, rowCount, padding, chartWidth, chartHeight, minimum, range) {
        context.beginPath();
        context.strokeStyle = item.color;
        context.lineWidth = 2.5;

        item.values.forEach(function (value, index) {
            var x = rowCount <= 1 ? padding.left + chartWidth / 2 : padding.left + chartWidth * index / (rowCount - 1);
            var y = padding.top + chartHeight - ((value - minimum) / range) * chartHeight;
            index === 0 ? context.moveTo(x, y) : context.lineTo(x, y);
        });
        context.stroke();

        item.values.forEach(function (value, index) {
            var x = rowCount <= 1 ? padding.left + chartWidth / 2 : padding.left + chartWidth * index / (rowCount - 1);
            var y = padding.top + chartHeight - ((value - minimum) / range) * chartHeight;
            
            context.beginPath();
            context.fillStyle = "#ffffff";
            context.strokeStyle = item.color;
            context.lineWidth = 2;
            context.arc(x, y, 3.5, 0, Math.PI * 2);
            context.fill();
            context.stroke();
        });
    }

    function drawFallbackLabels(context, rows, padding, chartWidth, chartHeight) {
        var labelStep = rows.length > 8 ? 2 : 1;
        context.fillStyle = "#687386";
        context.textAlign = "center";
        context.textBaseline = "top";

        rows.forEach(function (row, index) {
            if (index % labelStep !== 0 && index !== rows.length - 1) return;
            var x = rows.length <= 1 ? padding.left + chartWidth / 2 : padding.left + chartWidth * index / (rows.length - 1);
            context.fillText(row.salesMonth, x, padding.top + chartHeight + 12);
        });
    }

    function drawFallbackLegend(context, series, width) {
        var itemWidth = width <= 520 ? 88 : 104;
        var totalWidth = itemWidth * series.length;
        var startX = Math.max(12, (width - totalWidth) / 2);

        context.textAlign = "left";
        context.textBaseline = "middle";

        series.forEach(function (item, index) {
            var x = startX + itemWidth * index;
            context.beginPath();
            context.strokeStyle = item.color;
            context.lineWidth = 3;
            context.moveTo(x, 21);
            context.lineTo(x + 18, 21);
            context.stroke();

            context.fillStyle = "#3e4a5d";
            context.fillText(item.label, x + 25, 21);
        });
    }

    function setStatus(element, message, isError) {
        if (!element) return;
        element.textContent = message;
        element.classList.toggle("chart-status--error", isError);
    }
}());