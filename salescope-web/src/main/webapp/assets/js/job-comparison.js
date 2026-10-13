document.addEventListener("DOMContentLoaded", function () {
    const source = document.getElementById("comparisonData");
    if (!source) return;

    const status = document.getElementById("comparisonChartStatus");
    if (typeof Chart === "undefined") {
        status.textContent = "차트 라이브러리를 불러오지 못했습니다. 아래 표에서 수치를 확인할 수 있습니다.";
        status.classList.add("chart-status--error");
        return;
    }

    const rows = Array.from(source.querySelectorAll(".data-row"));
    const months = rows.map(row => row.dataset.month);
    const first = rows.map(row => Number(row.dataset.first));
    const second = rows.map(row => Number(row.dataset.second));
    const differences = rows.map(row => Number(row.dataset.difference));
    const format = value => new Intl.NumberFormat("ko-KR", {maximumFractionDigits: 2}).format(value);

    new Chart(document.getElementById("comparisonTrendChart"), {
        type: "line",
        data: {
            labels: months,
            datasets: [
                {label: "Job #" + source.dataset.firstJob, data: first, borderColor: "#315efb", tension: 0.2},
                {label: "Job #" + source.dataset.secondJob, data: second, borderColor: "#137a55", tension: 0.2}
            ]
        },
        options: {
            responsive: true, maintainAspectRatio: false,
            plugins: {tooltip: {callbacks: {label: context => context.dataset.label + ": " + format(context.parsed.y)}}}
        }
    });

    new Chart(document.getElementById("comparisonDifferenceChart"), {
        type: "bar",
        data: {
            labels: months,
            datasets: [{
                label: "비교 − 기준", data: differences,
                backgroundColor: differences.map(value => value < 0 ? "#c2414b" : "#137a55")
            }]
        },
        options: {
            responsive: true, maintainAspectRatio: false,
            plugins: {tooltip: {callbacks: {label: context => context.dataset.label + ": " + format(context.parsed.y)}}}
        }
    });
});
