(function () {
    "use strict";

    var STORAGE_KEY = "salescope-theme";

    function getSavedTheme() {
        try {
            return localStorage.getItem(STORAGE_KEY);
        } catch (e) {
            return null;
        }
    }

    function saveTheme(theme) {
        try {
            localStorage.setItem(STORAGE_KEY, theme);
        } catch (e) {
        }
    }

    function isDarkMode() {
        return document.body.classList.contains("dark-mode");
    }

    function updateButton() {
        var button = document.querySelector(".theme-toggle");

        if (!button) {
            return;
        }

        var icon = button.querySelector(".theme-toggle__icon");
        var text = button.querySelector(".theme-toggle__text");

        if (isDarkMode()) {
            button.setAttribute("aria-label", "라이트 모드로 전환");
            button.setAttribute("title", "라이트 모드로 전환");

            if (icon) {
                icon.textContent = "☀";
            }

            if (text) {
                text.textContent = "라이트";
            }
        } else {
            button.setAttribute("aria-label", "다크 모드로 전환");
            button.setAttribute("title", "다크 모드로 전환");

            if (icon) {
                icon.textContent = "☾";
            }

            if (text) {
                text.textContent = "다크";
            }
        }
    }

    function updateCharts() {
        if (typeof window.Chart === "undefined") {
            return;
        }

        var dark = isDarkMode();

        var textColor = dark ? "#dbe7f5" : "#334155";
        var gridColor = dark
            ? "rgba(148, 163, 184, 0.18)"
            : "rgba(148, 163, 184, 0.25)";

        if (!window.Chart.instances) {
            return;
        }

        window.Chart.instances.forEach(function (chart) {
            if (!chart.options) {
                return;
            }

            if (
                chart.options.plugins &&
                chart.options.plugins.legend
            ) {
                chart.options.plugins.legend.labels =
                    chart.options.plugins.legend.labels || {};

                chart.options.plugins.legend.labels.color = textColor;
            }

            if (chart.options.scales) {
                Object.keys(chart.options.scales).forEach(function (axis) {
                    var scale = chart.options.scales[axis];

                    if (!scale) {
                        return;
                    }

                    scale.ticks = scale.ticks || {};
                    scale.grid = scale.grid || {};

                    scale.ticks.color = textColor;
                    scale.grid.color = gridColor;
                });
            }

            chart.update("none");
        });
    }

    function applyTheme(theme) {
        var dark = theme === "dark";

        document.body.classList.toggle("dark-mode", dark);

        updateButton();
        updateCharts();
    }

    function getInitialTheme() {
        var saved = getSavedTheme();

        if (saved === "dark" || saved === "light") {
            return saved;
        }

        if (
            window.matchMedia &&
            window.matchMedia("(prefers-color-scheme: dark)").matches
        ) {
            return "dark";
        }

        return "light";
    }

    function toggleTheme() {
        var nextTheme = isDarkMode() ? "light" : "dark";

        applyTheme(nextTheme);
        saveTheme(nextTheme);
    }

    function init() {
        applyTheme(getInitialTheme());

        var button = document.querySelector(".theme-toggle");

        if (button) {
            button.addEventListener("click", toggleTheme);
        }

        window.setTimeout(updateCharts, 0);
        window.setTimeout(updateCharts, 300);
    }

    window.SaleScopeTheme = {
        apply: applyTheme,
        updateCharts: updateCharts,
        isDark: isDarkMode
    };

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
})();