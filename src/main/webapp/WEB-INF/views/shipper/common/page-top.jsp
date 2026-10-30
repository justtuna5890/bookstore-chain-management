<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%--
    Khung trang của shipper (head, theme, thanh đầu trang, mở thẻ main), thiết kế cho điện thoại.
    Trang gọi đặt trước request attribute: shipperTitle (tiêu đề tab); liveReload = true nếu trang danh sách
    cần tự làm mới khi có đơn mới (xem /js/shipper-live.js).
    Kết thúc trang bằng page-bottom.jsp.
--%>
<!DOCTYPE html>
<html class="dark" lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>${fn:escapeXml(empty shipperTitle ? 'Giao hàng' : shipperTitle)} — OldBook</title>
    <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
    <script>
        tailwind.config = {
            darkMode: 'class',
            theme: {
                extend: {
                    colors: {
                        brand: { gold: '#D4AF37', goldLight: '#F3E5AB', goldHover: '#c19e2e', darkBg: '#0b0c10', surface: '#15181E' }
                    },
                    fontFamily: { sans: ['Plus Jakarta Sans', 'Inter', 'system-ui', 'sans-serif'] }
                }
            }
        }
    </script>
    <link href="https://fonts.googleapis.com" rel="preconnect"/>
    <link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect"/>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css"/>
    <style>
        html, body { min-height: 100%; margin: 0; padding: 0; }
        * { box-sizing: border-box; -webkit-tap-highlight-color: transparent; }
        body { background: linear-gradient(180deg, #090a0d 0%, #11141a 50%, #0d0f14 100%) fixed; font-family: 'Plus Jakarta Sans', sans-serif; color: #e2e8f0; }
        .card { background: rgba(18, 21, 28, 0.78); border: 1px solid rgba(255, 255, 255, 0.09); }
        textarea.field {
            background-color: rgba(16, 20, 29, 0.92) !important; color: #f8fafc !important;
            border: 1px solid rgba(255, 255, 255, 0.15) !important; box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.4) !important;
        }
        textarea.field:focus { border-color: rgba(212, 175, 55, 0.5) !important; outline: none; }
    </style>
</head>
<c:set var="liveStoreId" value="${pageContext.request.userPrincipal != null && pageContext.request.userPrincipal.principal.user.store != null ? pageContext.request.userPrincipal.principal.user.store.id : ''}"/>
<body class="min-h-screen" data-store-id="${liveStoreId}" data-live-reload="${liveReload == true}">
<header class="sticky top-0 z-30 border-b border-white/10 bg-[#0b0c10]/90 backdrop-blur">
    <div class="max-w-xl mx-auto px-4 h-14 flex items-center justify-between gap-3">
        <div class="min-w-0">
            <p class="text-sm font-bold text-brand-gold leading-tight"><i class="fa-solid fa-motorcycle mr-1.5"></i>OldBook · Giao hàng <span data-live-status class="inline-block w-2 h-2 rounded-full bg-slate-600 align-middle ml-1" title="Đang kết nối..."></span></p>
            <p class="text-[11px] text-slate-400 truncate">${fn:escapeXml(shipperName)}<c:if test="${not empty storeName}"> · ${fn:escapeXml(storeName)}</c:if></p>
        </div>
        <form action="/logout" method="post" class="m-0 shrink-0">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <button type="submit" class="w-10 h-10 rounded-xl bg-rose-500/10 hover:bg-rose-500/20 text-rose-300 flex items-center justify-center transition" title="Đăng xuất" aria-label="Đăng xuất">
                <i class="fa-solid fa-arrow-right-from-bracket"></i>
            </button>
        </form>
    </div>
</header>
<main class="max-w-xl mx-auto px-4 py-5 pb-28">
