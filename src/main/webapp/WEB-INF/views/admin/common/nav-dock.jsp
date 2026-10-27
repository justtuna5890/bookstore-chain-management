<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<sec:authorize access="hasRole('ADMIN')">

    <c:set var="userRole" value="ADMIN" />

    <c:set var="isAdmin" value="true" />

</sec:authorize>

<sec:authorize access="hasRole('MANAGER')">

    <c:set var="userRole" value="MANAGER" />

    <c:set var="isManager" value="true" />

</sec:authorize>

<sec:authorize access="hasRole('STAFF')">

    <c:set var="userRole" value="STAFF" />

    <c:set var="isStaff" value="true" />

</sec:authorize>



<!-- ========================================== -->

<!-- UNIFIED ADMIN NAVIGATION DOCK (OLDBOOK)   -->

<!-- Support: Desktop Dock + Mobile Header Bar  -->

<!-- ========================================== -->



<!-- 1. Mobile & Tablet Top Bar (xl:hidden) -->

<div class="lg:hidden w-full mb-6 glass-panel rounded-2xl p-3 border border-white/10 shadow-glass-elevated">

    <div class="flex items-center justify-between gap-3 mb-2.5 pb-2.5 border-b border-white/10">

        <a href="/" class="flex items-center gap-2 text-brand-gold font-serif font-bold text-sm">

            <div class="w-8 h-8 rounded-full bg-brand-gold/15 border border-brand-gold/30 flex items-center justify-center">

                <i class="fa-solid fa-feather-pointed text-xs"></i>

            </div>

            <span>OldBook Admin</span>

        </a>

        <div class="flex items-center gap-2">

            <c:choose>

                <c:when test="${isAdmin}">

                    <span class="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-rose-600/20 border border-rose-500/40 text-rose-300">Quản trị hệ thống</span>

                </c:when>

                <c:when test="${isManager}">

                    <span class="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-purple-600/20 border border-purple-500/40 text-purple-300">Quản lý vận hành</span>

                </c:when>

                <c:when test="${isStaff}">

                    <span class="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-sky-600/20 border border-sky-500/40 text-sky-300">Nhân viên chi nhánh</span>

                </c:when>

            </c:choose>



            <!-- Mobile Notification Bell (Nổi bật) -->

            <div class="relative">

                <button type="button" onclick="toggleNotifDropdown()" class="w-8 h-8 rounded-xl bg-gradient-to-br from-amber-500/25 via-brand-gold/35 to-amber-600/25 border border-brand-gold text-brand-gold shadow-[0_0_12px_rgba(212,175,55,0.4)] flex items-center justify-center transition relative group" title="Thông báo thời gian thực">

                    <i class="fa-solid fa-bell text-xs group-hover:animate-bounce"></i>

                    <span id="mobileNotifBadge" class="hidden absolute -top-1.5 -right-1.5 min-w-[16px] h-[16px] px-1 rounded-full bg-rose-500 text-white text-[9px] font-black flex items-center justify-center shadow-md animate-pulse">0</span>

                </button>

            </div>



            <form action="/logout" method="post" class="m-0 inline">

                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                <button type="submit" class="w-7 h-7 rounded-lg bg-rose-500/10 hover:bg-rose-500/20 text-rose-300 flex items-center justify-center transition" title="Đăng xuất">

                    <i class="fa-solid fa-arrow-right-from-bracket text-xs"></i>

                </button>

            </form>

        </div>

    </div>

    <!-- Horizontal Scrollable Nav Pills for Mobile -->

    <div class="flex items-center gap-2 overflow-x-auto pb-1 text-xs no-scrollbar">

        <a href="/admin/dashboard" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'dashboard' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">

            <i class="fa-solid fa-gauge-high text-xs"></i>

            <span>Dashboard</span>

        </a>

        <c:if test="${isAdmin}">

            <a href="/admin/users" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'users' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">

                <i class="fa-solid fa-users-gear text-xs"></i>

                <span>Nhân Sự</span>

            </a>

        </c:if>

        <c:if test="${isAdmin || isManager}">

            <a href="/admin/stores" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'stores' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">

                <i class="fa-solid fa-store text-xs"></i>

                <span>Chi Nhánh</span>

            </a>

        </c:if>

        <a href="/admin/books" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'books' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">

            <i class="fa-solid fa-book text-xs"></i>

            <span>Đầu Sách</span>

        </a>

        <a href="/admin/book-copies" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'copies' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">

            <i class="fa-solid fa-boxes-stacked text-xs"></i>

            <span>Kho Bản Sách</span>

        </a>

        <a href="/admin/orders" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'orders' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">

            <i class="fa-solid fa-bag-shopping text-xs"></i>

            <span>Đơn Hàng</span>

        </a>

        <c:if test="${isAdmin || isManager}">

            <a href="/admin/promotions" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'promotions' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">

                <i class="fa-solid fa-ticket text-xs"></i>

                <span>Khuyến Mãi</span>

            </a>

        </c:if>

        <c:if test="${isAdmin || isManager}">

            <a href="/admin/reviews" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'reviews' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">

                <i class="fa-solid fa-star-half-stroke text-xs"></i>

                <span>Đánh Giá</span>

            </a>

        </c:if>

        <a href="/admin/buybacks" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'buybacks' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">

            <i class="fa-solid fa-hand-holding-dollar text-xs"></i>

            <span>Thu Mua</span>

        </a>

        <a href="/admin/chat" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'chat' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">

            <i class="fa-solid fa-comments text-xs"></i>

            <span>Chat Hỗ Trợ</span>

        </a>

        <a href="/admin/transfers" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'transfers' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">

            <i class="fa-solid fa-truck-ramp-box text-xs"></i>

            <span>Chuyển Kho</span>

        </a>

        <c:if test="${isAdmin || isManager}">

            <a href="/admin/reports" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'reports' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">

                <i class="fa-solid fa-chart-line text-xs"></i>

                <span>Báo Cáo Kỳ</span>

            </a>

        </c:if>

    </div>

</div>



<!-- 2. Desktop Floating Left Dock (hidden xl:flex) -->
<style>
        /* Universal Responsive Safeguards */
        html, body {
            overflow-x: hidden !important;
            width: 100% !important;
            max-width: 100vw !important;
        }
        img, svg, video {
            max-width: 100%;
            height: auto;
        }
        * {
            box-sizing: border-box;
            -webkit-tap-highlight-color: transparent;
        }

    /* Dynamic Auto-Scaling Adaptive Dock Pill */
    .admin-dock-pill {
        --dock-btn-size: 36px;
        --dock-icon-size: 13.5px;
        --dock-gap: 3.5px;
        --dock-pad-y: 8px;
        --dock-pad-x: 6px;
        --dock-width: 52px;

        width: var(--dock-width, 52px);
        height: fit-content !important;
        max-height: calc(100vh - 2rem);
        padding-top: var(--dock-pad-y, 8px) !important;
        padding-bottom: var(--dock-pad-y, 8px) !important;
        padding-left: var(--dock-pad-x, 6px) !important;
        padding-right: var(--dock-pad-x, 6px) !important;
        gap: var(--dock-gap, 3.5px) !important;
        transition: width 0.22s cubic-bezier(0.4, 0, 0.2, 1),
                    padding 0.22s ease,
                    gap 0.22s ease;
        overflow-y: auto;
        overflow-x: hidden;
        scrollbar-width: none;
        -ms-overflow-style: none;
    }
    .admin-dock-pill::-webkit-scrollbar {
        display: none;
    }

    .admin-dock-btn {
        width: var(--dock-btn-size, 36px) !important;
        height: var(--dock-btn-size, 36px) !important;
        min-width: var(--dock-btn-size, 36px) !important;
        min-height: var(--dock-btn-size, 36px) !important;
        transition: width 0.2s cubic-bezier(0.4, 0, 0.2, 1),
                    height 0.2s cubic-bezier(0.4, 0, 0.2, 1),
                    background-color 0.15s ease,
                    border-color 0.15s ease,
                    color 0.15s ease;
    }
    .admin-dock-btn i {
        font-size: var(--dock-icon-size, 13.5px) !important;
        transition: font-size 0.2s ease;
    }
    .admin-dock-divider {
        width: calc(var(--dock-btn-size, 36px) * 0.58) !important;
        height: 1px;
        background: rgba(255, 255, 255, 0.12);
        margin: 1px 0;
        transition: width 0.2s ease;
    }
    .admin-dock-nav {
        gap: var(--dock-gap, 3.5px) !important;
    }

    /* Global Dark Glass Forms, Inputs & Dropdowns Override */
    input:not([type="checkbox"]):not([type="radio"]):not([type="submit"]):not([type="button"]):not([type="reset"]),
    textarea,
    select,
    .glass-input,
    .form-field,
    .form-control,
    .form-select {
        background-color: rgba(16, 20, 29, 0.92) !important;
        color: #f8fafc !important;
        border: 1px solid rgba(255, 255, 255, 0.15) !important;
        box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.4) !important;
    }

    input:not([type="checkbox"]):not([type="radio"]):not([type="submit"]):not([type="button"]):not([type="reset"]):focus,
    textarea:focus,
    select:focus,
    .glass-input:focus,
    .form-field:focus,
    .form-control:focus,
    .form-select:focus {
        background-color: rgba(20, 25, 38, 0.98) !important;
        color: #ffffff !important;
        border-color: rgba(212, 175, 55, 0.7) !important;
        box-shadow: 0 0 0 3px rgba(212, 175, 55, 0.25), inset 0 2px 4px rgba(0, 0, 0, 0.4) !important;
        outline: none !important;
    }

    input::placeholder,
    textarea::placeholder,
    .glass-input::placeholder,
    .form-field::placeholder,
    .form-control::placeholder {
        color: #94a3b8 !important;
        opacity: 1 !important;
    }

    /* Dropdown (select) options: NEVER white background */
    select option,
    select optgroup,
    option {
        background-color: #121620 !important;
        color: #f8fafc !important;
    }

    select option:hover,
    select option:focus,
    select option:checked {
        background-color: #1e2638 !important;
        color: #D4AF37 !important;
    }

    /* Defeat Chrome / Edge / Safari White Autofill & History Dropdown */
    input:-webkit-autofill,
    input:-webkit-autofill:hover, 
    input:-webkit-autofill:focus, 
    input:-webkit-autofill:active,
    textarea:-webkit-autofill,
    select:-webkit-autofill {
        -webkit-text-fill-color: #f8fafc !important;
        -webkit-box-shadow: 0 0 0px 1000px #10141e inset !important;
        box-shadow: 0 0 0px 1000px #10141e inset !important;
        transition: background-color 5000s ease-in-out 0s !important;
        color: #f8fafc !important;
        caret-color: #D4AF37 !important;
    }
</style>

<aside class="admin-dock-pill hidden lg:flex flex-col items-center justify-start rounded-full glass-panel shadow-glass-elevated sticky top-4 border border-white/10 shrink-0 z-40" data-purpose="vertical-dock">
    <!-- Top: Brand Logo & Logout Action -->
    <div class="flex flex-col items-center gap-1.5 shrink-0">
        <!-- Logo Brand -->
        <a class="admin-dock-btn rounded-full bg-brand-gold/15 border border-brand-gold/30 flex items-center justify-center text-brand-gold hover:bg-brand-gold/25 transition-all shadow-sm" href="/" title="Về Sàn Khám Phá Trang Chủ">
            <i class="fa-solid fa-feather-pointed text-brand-gold"></i>
        </a>

        <!-- Logout Button (Đưa lên trên theo yêu cầu) -->
        <form action="/logout" method="post" class="m-0">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <button type="submit" class="admin-dock-btn rounded-full flex items-center justify-center text-rose-400 hover:text-white hover:bg-rose-500/25 border border-rose-500/20 hover:border-rose-500/50 transition group relative" title="Đăng xuất">
                <i class="fa-solid fa-arrow-right-from-bracket"></i>
                <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-rose-300 opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-rose-500/20 z-50">Đăng Xuất</span>
            </button>
        </form>
    </div>

    <!-- Divider Top -->
    <div class="admin-dock-divider shrink-0"></div>

    <!-- Center: Business Navigation -->
    <nav class="admin-dock-nav flex flex-col items-center my-1">
        <!-- Dashboard -->
        <a class="admin-dock-btn rounded-full flex items-center justify-center transition group relative shrink-0 ${param.active == 'dashboard' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
           href="/admin/dashboard" title="Tổng quan Dashboard">
            <i class="fa-solid fa-table-cells-large"></i>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Tổng Quan Dashboard</span>
        </a>

        <!-- Nhân sự (Chỉ ADMIN) -->
        <c:if test="${isAdmin}">
            <a class="admin-dock-btn rounded-full flex items-center justify-center transition group relative shrink-0 ${param.active == 'users' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
               href="/admin/users" title="Quản lý nhân sự">
                <i class="fa-solid fa-users-gear"></i>
                <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Tài Khoản & Quyền</span>
            </a>
        </c:if>

        <!-- Chi nhánh (ADMIN & MANAGER) -->
        <c:if test="${isAdmin || isManager}">
            <a class="admin-dock-btn rounded-full flex items-center justify-center transition group relative shrink-0 ${param.active == 'stores' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
               href="/admin/stores" title="Mạng lưới chi nhánh">
                <i class="fa-solid fa-store"></i>
                <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Chi Nhánh</span>
            </a>
        </c:if>

        <!-- Đơn hàng của khách (Tất cả vai trò ADMIN, MANAGER, STAFF) -->
        <a class="admin-dock-btn rounded-full flex items-center justify-center transition group relative shrink-0 ${param.active == 'orders' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
           href="/admin/orders" title="Quản lý đơn hàng của khách">
            <i class="fa-solid fa-receipt"></i>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Đơn Hàng</span>
        </a>

        <!-- Đầu sách -->
        <a class="admin-dock-btn rounded-full flex items-center justify-center transition group relative shrink-0 ${param.active == 'books' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
           href="/admin/books" title="Danh mục đầu sách">
            <i class="fa-solid fa-book-bookmark"></i>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Đầu Sách</span>
        </a>

        <!-- Kho bản sách -->
        <a class="admin-dock-btn rounded-full flex items-center justify-center transition group relative shrink-0 ${param.active == 'copies' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
           href="/admin/book-copies" title="Quản lý bản sao tồn kho">
            <i class="fa-solid fa-boxes-stacked"></i>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Kho Bản Sách</span>
        </a>

        <!-- Thu mua -->
        <a class="admin-dock-btn rounded-full flex items-center justify-center transition group relative shrink-0 ${param.active == 'buybacks' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
           href="/admin/buybacks" title="Duyệt thu mua sách cũ">
            <i class="fa-solid fa-handshake"></i>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Duyệt Thu Mua</span>
        </a>

        <!-- Chuyển kho -->
        <a class="admin-dock-btn rounded-full flex items-center justify-center transition group relative shrink-0 ${param.active == 'transfers' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
           href="/admin/transfers" title="Điều chuyển kho chi nhánh">
            <i class="fa-solid fa-truck-fast"></i>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Chuyển Kho</span>
        </a>

        <!-- Mã giảm giá (ADMIN & MANAGER) -->
        <c:if test="${isAdmin || isManager}">
            <a class="admin-dock-btn rounded-full flex items-center justify-center transition group relative shrink-0 ${param.active == 'promotions' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
               href="/admin/promotions" title="Mã giảm giá">
                <i class="fa-solid fa-ticket"></i>
                <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Khuyến Mãi</span>
            </a>
        </c:if>

        <!-- Kiểm duyệt đánh giá (ADMIN & MANAGER) -->
        <c:if test="${isAdmin || isManager}">
            <a class="admin-dock-btn rounded-full flex items-center justify-center transition group relative shrink-0 ${param.active == 'reviews' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}"
               href="/admin/reviews" title="Kiểm duyệt đánh giá">
                <i class="fa-solid fa-star-half-stroke"></i>
                <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Kiểm duyệt đánh giá</span>
            </a>
        </c:if>

        <!-- Chat hỗ trợ khách hàng -->
        <a class="admin-dock-btn rounded-full flex items-center justify-center transition group relative shrink-0 ${param.active == 'chat' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}"
           href="/admin/chat" title="Chat hỗ trợ">
            <i class="fa-solid fa-comments"></i>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Chat hỗ trợ</span>
        </a>

        <!-- Báo cáo kỳ chuyên sâu (ADMIN & MANAGER) -->
        <c:if test="${isAdmin || isManager}">
            <a class="admin-dock-btn rounded-full flex items-center justify-center transition group relative shrink-0 ${param.active == 'reports' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
               href="/admin/reports" title="Báo cáo chuyên sâu theo kỳ">
                <i class="fa-solid fa-chart-line"></i>
                <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Báo Cáo Kỳ</span>
            </a>
        </c:if>
    </nav>

    <!-- Divider Bottom -->
    <div class="admin-dock-divider shrink-0"></div>

    <!-- Bottom: Utility & User Avatar -->
    <div class="flex flex-col items-center gap-1.5 shrink-0">
        <!-- Desktop Notification Bell (Nổi bật rực rỡ trên thanh dock) -->
        <div class="relative">
            <button type="button" onclick="toggleNotifDropdown()" class="admin-dock-btn rounded-full flex items-center justify-center bg-gradient-to-br from-amber-500/25 via-brand-gold/35 to-amber-600/25 border border-brand-gold text-brand-gold shadow-[0_0_16px_rgba(212,175,55,0.4)] hover:shadow-[0_0_24px_rgba(212,175,55,0.7)] hover:scale-105 hover:border-brand-gold transition-all group relative" title="Trung tâm Thông Báo Thời Gian Thực">
                <span class="relative flex items-center justify-center">
                    <i class="fa-solid fa-bell text-brand-gold group-hover:animate-bounce"></i>
                    <span class="absolute -top-1 -right-1 flex h-2 w-2">
                        <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75"></span>
                        <span class="relative inline-flex rounded-full h-2 w-2 bg-amber-500"></span>
                    </span>
                </span>
                <span id="desktopNotifBadge" class="hidden absolute -top-1.5 -right-1.5 min-w-[16px] h-[16px] px-1 rounded-full bg-rose-500 text-white text-[9px] font-black flex items-center justify-center shadow-lg border border-rose-300 ring-2 ring-[#0b0c10] animate-pulse">0</span>
                <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-brand-gold font-bold opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-brand-gold/40 z-50">
                    <i class="fa-solid fa-bell mr-1"></i>Trung Tâm Thông Báo
                </span>
            </button>
        </div>

        <!-- Desktop User Role Avatar -->
        <div class="relative group">
            <div class="admin-dock-btn rounded-full flex items-center justify-center font-bold text-xs border cursor-pointer transition shadow-sm ${isAdmin ? 'bg-rose-500/20 border-rose-500/40 text-rose-300' : isManager ? 'bg-purple-500/20 border-purple-500/40 text-purple-300' : 'bg-sky-500/20 border-sky-500/40 text-sky-300'}">
                ${pageContext.request.userPrincipal != null ? pageContext.request.userPrincipal.name.substring(0, 1).toUpperCase() : 'U'}
            </div>
            <div class="absolute left-14 bottom-0 ml-2 p-3 rounded-2xl glass-panel shadow-glass-elevated border border-white/10 opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap z-50 text-left backdrop-blur-xl">
                <div class="text-xs font-bold text-white">${pageContext.request.userPrincipal.principal.user.fullName}</div>
                <div class="text-[10px] ${isAdmin ? 'text-rose-400' : isManager ? 'text-purple-400' : 'text-sky-400'} font-semibold mt-0.5">
                    ${isAdmin ? 'Quản trị hệ thống (ADMIN)' : isManager ? 'Quản lý vận hành (MANAGER)' : 'Nhân viên chi nhánh (STAFF)'}
                </div>
                <c:if test="${not empty pageContext.request.userPrincipal.principal.user.store}">
                    <div class="text-[10px] text-brand-gold mt-1 flex items-center gap-1">
                        <i class="fa-solid fa-store text-[9px]"></i>
                        <span>${pageContext.request.userPrincipal.principal.user.store.name}</span>
                    </div>
                </c:if>
            </div>
        </div>
    </div>
</aside>
<!-- ============================================== -->

<!-- 3. REAL-TIME NOTIFICATION DRAWER & TOAST POPUP -->

<!-- ============================================== -->



<!-- Notification Dropdown Panel -->

<div id="notifDropdown" class="hidden fixed top-16 inset-x-3 sm:inset-x-auto sm:right-12 lg:right-auto lg:left-24 lg:top-20 z-50 w-auto sm:w-96 max-w-[95vw] rounded-2xl glass-panel p-4 shadow-glass-elevated border border-white/15 backdrop-blur-2xl">

    <div class="flex items-center justify-between pb-3 border-b border-white/10 mb-3">

        <div class="flex items-center gap-2">

            <i class="fa-solid fa-bell text-brand-gold text-xs"></i>

            <span class="text-xs font-bold text-white uppercase tracking-wider">Thông Báo Thời Gian Thực</span>

        </div>

        <button type="button" onclick="toggleNotifDropdown()" class="text-slate-400 hover:text-white text-xs p-1">

            <i class="fa-solid fa-xmark"></i>

        </button>

    </div>

    <div id="notifList" class="space-y-2.5 max-h-72 overflow-y-auto no-scrollbar text-xs">

        <div id="emptyNotifMsg" class="text-center py-6 text-slate-400">

            <i class="fa-regular fa-bell-slash text-xl mb-1.5 block opacity-40"></i>

            <span>Chưa có thông báo mới nào</span>

        </div>

    </div>

</div>



<!-- Floating Toast Container (Bottom-Right) -->

<div id="toastContainer" class="fixed bottom-6 right-6 z-50 flex flex-col gap-3 max-w-sm w-full pointer-events-none"></div>



<!-- ============================================== -->

<!-- 4. WEBSOCKET & AUDIO SYNTHESIZER SCRIPT        -->

<!-- ============================================== -->

<script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js"></script>

<script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>



<script>

    let stompClient = null;

    // Nội dung thông báo có thể chứa chữ do khách nhập (tên, tin nhắn): luôn escape trước khi đưa vào innerHTML

    function escapeHtml(value) {

        return String(value).replace(/[&<>"']/g, function (c) {

            return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];

        });

    }



    let unreadCount = 0;



    // 1. Âm thanh chuông báo (Web Audio API Synthesizer - không phụ thuộc file ngoài, không lo 404)

    function playChimeSound() {

        try {

            const AudioCtx = window.AudioContext || window.webkitAudioContext;

            if (!AudioCtx) return;

            const ctx = new AudioCtx();



            // Nốt 1: 587.33Hz (D5)

            const osc1 = ctx.createOscillator();

            const gain1 = ctx.createGain();

            osc1.type = 'sine';

            osc1.frequency.setValueAtTime(587.33, ctx.currentTime);

            gain1.gain.setValueAtTime(0.12, ctx.currentTime);

            gain1.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.3);

            osc1.connect(gain1);

            gain1.connect(ctx.destination);

            osc1.start();

            osc1.stop(ctx.currentTime + 0.3);



            // Nốt 2: 880Hz (A5) ngân vang sau 100ms

            setTimeout(() => {

                const osc2 = ctx.createOscillator();

                const gain2 = ctx.createGain();

                osc2.type = 'triangle';

                osc2.frequency.setValueAtTime(880, ctx.currentTime);

                gain2.gain.setValueAtTime(0.18, ctx.currentTime);

                gain2.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + 0.55);

                osc2.connect(gain2);

                gain2.connect(ctx.destination);

                osc2.start();

                osc2.stop(ctx.currentTime + 0.55);

            }, 100);

        } catch (e) {

            console.warn("Audio autoplay blocked or unsupported:", e);

        }

    }



    // 2. Bật / Tắt danh sách thông báo

    function toggleNotifDropdown() {

        const dd = document.getElementById('notifDropdown');

        if (!dd) return;

        dd.classList.toggle('hidden');

        if (!dd.classList.contains('hidden')) {

            // Đặt lại số đếm khi mở xem

            unreadCount = 0;

            updateBadgeUI();

        }

    }



    // 3. Cập nhật Badge số lượng thông báo

    function updateBadgeUI() {

        const mob = document.getElementById('mobileNotifBadge');

        const desk = document.getElementById('desktopNotifBadge');

        [mob, desk].forEach(badge => {

            if (!badge) return;

            if (unreadCount > 0) {

                badge.innerText = unreadCount > 9 ? '9+' : unreadCount;

                badge.classList.remove('hidden');

            } else {

                badge.classList.add('hidden');

            }

        });

    }



    // 4. Xử lý nhận thông báo mới từ WebSocket

    function handleIncomingNotification(data) {

        // Phát âm thanh chuông "ting"

        playChimeSound();



        // Tăng đếm chưa đọc

        unreadCount++;

        updateBadgeUI();



        // Ẩn thông báo rỗng nếu có

        const emptyMsg = document.getElementById('emptyNotifMsg');

        if (emptyMsg) emptyMsg.style.display = 'none';



        // Thêm vào danh sách Dropdown

        const notifList = document.getElementById('notifList');

        if (notifList) {

            const isBuyback = data.type === 'BUYBACK'; const isChat = data.type === 'CHAT';

            const icon = data.type === 'ORDER' ? 'fa-bag-shopping text-emerald-400' : (isBuyback ? 'fa-hand-holding-dollar text-amber-400' : (isChat ? 'fa-comments text-sky-400' : 'fa-truck-ramp-box text-blue-400'));

            const borderCol = data.type === 'ORDER' ? 'border-emerald-500/30' : (isBuyback ? 'border-amber-500/30' : (isChat ? 'border-sky-500/30' : 'border-blue-500/30'));



            const item = document.createElement('a');

            item.href = data.targetUrl || '#';

            item.className = 'block p-3 rounded-xl bg-white/[0.03] hover:bg-white/[0.08] border ' + borderCol + ' transition';

            item.innerHTML = '<div class="flex items-start gap-2.5">' +

                '<i class="fa-solid ' + icon + ' text-sm mt-0.5 shrink-0"></i>' +

                '<div class="flex-1">' +

                    '<div class="font-semibold text-white leading-tight">' + escapeHtml(data.title || 'Thông báo mới') + '</div>' +

                    '<p class="text-slate-300 text-[11px] mt-0.5 leading-snug">' + escapeHtml(data.message || '') + '</p>' +

                    '<span class="text-[10px] text-brand-gold block mt-1"><i class="fa-regular fa-clock mr-1"></i>Vừa xong</span>' +

                '</div>' +

            '</div>';



            notifList.prepend(item);

        }



        // Tạo Toast Popup góc màn hình

        showToast(data);

    }



    // 5. Hiển thị Toast Popup nổi góc màn hình

    function showToast(data) {

        const container = document.getElementById('toastContainer');

        if (!container) return;



        const isBuyback = data.type === 'BUYBACK'; const isChat = data.type === 'CHAT';

        const icon = data.type === 'ORDER' ? 'fa-bag-shopping text-emerald-400' : (isBuyback ? 'fa-hand-holding-dollar text-amber-400' : (isChat ? 'fa-comments text-sky-400' : 'fa-truck-ramp-box text-blue-400'));

        const borderColor = data.type === 'ORDER' ? 'border-emerald-500/40' : (isBuyback ? 'border-amber-500/40' : (isChat ? 'border-sky-500/40' : 'border-blue-500/40'));



        const toast = document.createElement('div');

        toast.className = 'pointer-events-auto p-4 rounded-2xl glass-panel shadow-glass-elevated border ' + borderColor + ' transition-all duration-300 transform translate-y-3 opacity-0';

        toast.innerHTML = '<div class="flex items-start gap-3">' +

            '<div class="w-8 h-8 rounded-xl bg-white/10 flex items-center justify-center shrink-0">' +

                '<i class="fa-solid ' + icon + ' text-sm"></i>' +

            '</div>' +

            '<div class="flex-1">' +

                '<div class="flex items-center justify-between">' +

                    '<h4 class="text-xs font-bold text-white uppercase tracking-wider">' + (data.type === 'ORDER' ? 'Đơn Hàng Mới' : (isBuyback ? 'Thu Mua Mới' : (isChat ? 'Tin Nhắn Hỗ Trợ' : 'Điều Chuyển Kho'))) + '</h4>' +

                    '<span class="text-[10px] text-slate-400">Vừa xong</span>' +

                '</div>' +

                '<p class="text-xs text-slate-200 font-medium mt-1 leading-snug">' + escapeHtml(data.message || '') + '</p>' +

                '<div class="mt-2.5 flex items-center gap-2">' +

                    '<a href="' + escapeHtml(data.targetUrl || '#') + '" class="px-2.5 py-1 rounded-lg bg-brand-gold text-slate-950 font-bold text-[10px] hover:bg-yellow-400 transition">Xem Chi Tiết</a>' +

                    '<button type="button" onclick="this.closest(\'.pointer-events-auto\').remove()" class="px-2 py-1 rounded-lg text-slate-400 hover:text-white text-[10px]">Đóng</button>' +

                '</div>' +

            '</div>' +

        '</div>';



        container.appendChild(toast);



        // Slide-in animation

        requestAnimationFrame(() => {

            toast.classList.remove('translate-y-3', 'opacity-0');

            toast.classList.add('translate-y-0', 'opacity-100');

        });



        // Tự động biến mất sau 6 giây

        setTimeout(() => {

            toast.classList.remove('translate-y-0', 'opacity-100');

            toast.classList.add('translate-y-3', 'opacity-0');

            setTimeout(() => toast.remove(), 300);

        }, 6000);

    }



    // 6. Kết nối WebSocket STOMP

    function initWebSocket() {

        if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {

            console.warn("SockJS or Stomp not loaded");

            return;

        }



        try {

            const socket = new SockJS('/ws');

            stompClient = Stomp.over(socket);

            stompClient.debug = null; // Tắt log debug



            stompClient.connect({}, function (frame) {

                // Topic chung: Toàn bộ Admin / Manager

                stompClient.subscribe('/topic/notifications', function (res) {

                    try {

                        const notif = JSON.parse(res.body);

                        handleIncomingNotification(notif);

                    } catch (e) {

                        console.error("Lỗi parse thông báo:", e);

                    }

                });



                // Topic riêng: Theo chi nhánh của nhân viên phụ trách (nếu có)

                <c:set var="uStoreId" value="${pageContext.request.userPrincipal != null && pageContext.request.userPrincipal.principal.user.store != null ? pageContext.request.userPrincipal.principal.user.store.id : ''}" />

                <c:if test="${not empty uStoreId}">

                    stompClient.subscribe('/topic/store/' + '${uStoreId}', function (res) {

                        try {

                            const notif = JSON.parse(res.body);

                            handleIncomingNotification(notif);

                        } catch (e) {

                            console.error("Lỗi parse thông báo chi nhánh:", e);

                        }

                    });

                </c:if>

                // Quản trị chưa gán chi nhánh nhận đơn hàng mới của mọi chi nhánh

                <c:if test="${empty uStoreId}">

                    stompClient.subscribe('/topic/store/all', function (res) {

                        try {

                            handleIncomingNotification(JSON.parse(res.body));

                        } catch (e) {

                            console.error("Lỗi parse thông báo đơn hàng:", e);

                        }

                    });

                </c:if>

            }, function (err) {

                console.warn("WebSocket disconnect, retrying in 5s...", err);

                setTimeout(initWebSocket, 5000);

            });

        } catch (err) {

            console.error("Lỗi khởi tạo WebSocket:", err);

        }

    }



    // Kích hoạt khi trang đã sẵn sàng

    if (document.readyState === 'loading') {

        document.addEventListener('DOMContentLoaded', initWebSocket);

    } else {

        initWebSocket();

    }


    // =========================================================================
    // Dynamic Auto-Scaling Dock: Adjusts button & pill size based on button count & screen height
    // =========================================================================
    function autoScaleAdminDock() {
        const dock = document.querySelector('[data-purpose="vertical-dock"]');
        if (!dock) return;

        const btns = dock.querySelectorAll('.admin-dock-btn');
        const count = btns.length;
        if (count === 0) return;

        // Viewport height
        const vh = window.innerHeight || document.documentElement.clientHeight;
        
        // Reserved space: Top spacing (sticky top-4 = 16px), bottom buffer (16px),
        // dividers (2 * 4px), container vertical paddings (2 * 8px = 16px) -> ~60px
        const reservedSpace = 60;
        const availableHeight = Math.max(280, vh - reservedSpace);

        // Target: count * btnSize + (count + 2) * gap <= availableHeight
        let btnSize = Math.floor(availableHeight / (count * 1.12 + 1));

        // Clamp button size based on count and comfort:
        const maxBtnSize = count <= 11 ? 38 : 36;
        const minBtnSize = 24;
        btnSize = Math.max(minBtnSize, Math.min(maxBtnSize, btnSize));

        // Proportionally compute gap, icon, padding, pill width
        let gap = btnSize <= 26 ? 1.5 : btnSize <= 30 ? 2 : btnSize <= 34 ? 3 : 3.5;
        let iconSize = btnSize <= 26 ? 10.5 : btnSize <= 30 ? 11.5 : btnSize <= 34 ? 13 : 14;
        let padY = btnSize <= 28 ? 6 : btnSize <= 32 ? 8 : 10;
        let padX = btnSize <= 28 ? 4 : 6;
        let pillWidth = btnSize + (padX * 2) + 4;

        dock.style.setProperty('--dock-btn-size', btnSize + 'px');
        dock.style.setProperty('--dock-icon-size', iconSize + 'px');
        dock.style.setProperty('--dock-gap', gap + 'px');
        dock.style.setProperty('--dock-pad-y', padY + 'px');
        dock.style.setProperty('--dock-pad-x', padX + 'px');
        dock.style.setProperty('--dock-width', pillWidth + 'px');
    }

    window.addEventListener('resize', autoScaleAdminDock, { passive: true });
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', autoScaleAdminDock);
    } else {
        autoScaleAdminDock();
    }

</script>

