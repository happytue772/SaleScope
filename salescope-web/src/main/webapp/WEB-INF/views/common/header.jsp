<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<header style="
    display: flex; 
    justify-content: space-between; 
    align-items: center; 
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(248, 250, 252, 0.8));
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
    border: 1px solid rgba(226, 232, 240, 0.8);
    border-bottom: 2px solid rgba(59, 130, 246, 0.25);
    padding: 18px 26px; 
    border-radius: 12px; 
    margin-bottom: 24px;
    box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05), inset 0 1px 0 rgba(255, 255, 255, 0.8);
">
    <div style="display: flex; align-items: center; gap: 14px;">
        <div style="
            width: 42px; 
            height: 42px; 
            background: linear-gradient(135deg, #3b82f6, #06b6d4); 
            border-radius: 10px; 
            display: flex; 
            align-items: center; 
            justify-content: center; 
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.25);
            font-size: 1.2rem;
            color: #ffffff;
            font-weight: 700;
        ">S</div>
        <div>
            <h1 style="margin: 0; font-size: 1.6rem; color: #0f172a; font-weight: 700; letter-spacing: -0.5px;">SaleScope</h1>
            <p class="subtitle" style="margin: 3px 0 0 0; color: #64748b; font-size: 0.85rem; font-weight: 500;">Job #<c:out value="${jobId}" /> 데이터 분석 대시보드</p>
        </div>
    </div>
    
    <div id="realtime-clock" style="
        text-align: right; 
        background: rgba(255, 255, 255, 0.7); 
        padding: 8px 14px; 
        border-radius: 8px; 
        border: 1px solid rgba(226, 232, 240, 0.8); 
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    ">
        <div id="current-date" style="font-size: 0.75rem; color: #64748b; font-weight: 500;"></div>
        <div id="current-time" style="font-size: 0.95rem; color: #2563eb; font-weight: 700; letter-spacing: 0.5px;"></div>
    </div>
</header>

<script>
    function updateRealtimeClock() {
        const now = new Date();
        const optionsDate = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' };
        const dateString = now.toLocaleDateString('ko-KR', optionsDate);
        const timeString = now.toLocaleTimeString('ko-KR', { 
            hour: '2-digit', minute: '2-digit', second: '2-digit' 
        });
        
        const dateElem = document.getElementById('current-date');
        const timeElem = document.getElementById('current-time');
        
        if (dateElem) dateElem.innerText = dateString;
        if (timeElem) timeElem.innerText = "🕒 " + timeString;
    }

    document.addEventListener("DOMContentLoaded", function() {
        updateRealtimeClock();
        setInterval(updateRealtimeClock, 1000);
    });
</script>