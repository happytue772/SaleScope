document.addEventListener("DOMContentLoaded", function () {
    const slider = document.getElementById("cancelReduction");
    const canvas = document.getElementById("scenarioChart");
    const status = document.getElementById("scenarioStatus");
    if (!slider || !canvas || !status) return;

    const rows = Array.from(document.querySelectorAll("#monthlyData .data-row"))
        .map(function (element) {
            return {
                month: element.getAttribute("data-month"),
                net: Number(element.getAttribute("data-net")),
                cancel: Number(element.getAttribute("data-cancel"))
            };
        })
        .filter(function (row) {
            return row.month && Number.isFinite(row.net) &&
                Number.isFinite(row.cancel) && row.cancel >= 0;
        });

    if (!rows.length) {
        status.textContent = "표시할 월별 매출 데이터가 없습니다.";
        slider.disabled = true;
        return;
    }
    if (typeof Chart === "undefined") {
        status.textContent = "차트를 불러오지 못했습니다. 페이지를 새로고침해 주세요.";
        slider.disabled = true;
        return;
    }

    const baseline = rows.reduce((sum, row) => sum + row.net, 0);
    const cancelled = rows.reduce((sum, row) => sum + row.cancel, 0);
    const currency = new Intl.NumberFormat("ko-KR", {maximumFractionDigits: 2});
    const chart = new Chart(canvas, {
        type: "bar",
        data: {
            labels: rows.map(row => row.month),
            datasets: [
                {label: "현재 순매출", data: rows.map(row => row.net), backgroundColor: "#64748b"},
                {label: "가정 순매출", data: [], backgroundColor: "#137a55"}
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                tooltip: {callbacks: {label: context =>
                    context.dataset.label + ": " + currency.format(context.parsed.y)}}
            }
        }
    });

    function update() {
        const rate = Number(slider.value) / 100;
        document.getElementById("cancelReductionValue").textContent = slider.value + "%";
        const increase = cancelled * rate;
        document.getElementById("scenarioSummary").textContent =
            "선택된 " + rows.length + "개월 · 현재 순매출 " + currency.format(baseline) +
            " → 가정 순매출 " + currency.format(baseline + increase) +
            " (차이 +" + currency.format(increase) + ")";
        chart.data.datasets[1].data = rows.map(row => row.net + row.cancel * rate);
        chart.update();
    }

    slider.addEventListener("input", update);
    update();
});
