document.addEventListener("DOMContentLoaded", function () {
    const source = document.getElementById("concentrationData");
    if (!source) return;

    const status = document.getElementById("concentrationChartStatus");
    if (typeof Chart === "undefined") {
        status.textContent = "차트를 불러오지 못했습니다. 아래 표에서 누적 비중을 확인할 수 있습니다.";
        return;
    }

    const rows = Array.from(source.querySelectorAll(".data-row"));
    const labels = rows.map(row => row.dataset.code);
    const cumulative = rows.map(row => Number(row.dataset.cumulative));
    new Chart(document.getElementById("concentrationChart"), {
        type: "line",
        data: {
            labels,
            datasets: [
                {label: "누적 순매출 비중", data: cumulative, borderColor: "#315efb",
                    backgroundColor: "rgba(49, 94, 251, 0.12)", fill: true, tension: 0.2},
                {label: "80% 기준", data: labels.map(() => 80), borderColor: "#137a55",
                    borderDash: [6, 4], pointRadius: 0},
                {label: "95% 기준", data: labels.map(() => 95), borderColor: "#c2414b",
                    borderDash: [6, 4], pointRadius: 0}
            ]
        },
        options: {
            responsive: true, maintainAspectRatio: false,
            scales: {y: {min: 0, max: 100, ticks: {callback: value => value + "%"}}},
            plugins: {tooltip: {callbacks: {label: context =>
                context.dataset.label + ": " + context.parsed.y.toFixed(2) + "%"}}}
        }
    });
});
