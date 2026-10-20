<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!-- ========================================== -->
<!-- UNIFIED ADMIN NAVIGATION DOCK (OLDBOOK)   -->
<!-- Support: Desktop Dock + Mobile Header Bar  -->
<!-- ========================================== -->

<!-- 1. Mobile & Tablet Top Bar (xl:hidden) -->
<div class="xl:hidden w-full mb-6 glass-panel rounded-2xl p-3 border border-white/10 shadow-glass-elevated">
    <div class="flex items-center justify-between gap-3 mb-2.5 pb-2.5 border-b border-white/10">
        <a href="/" class="flex items-center gap-2 text-brand-gold font-serif font-bold text-sm">
            <div class="w-8 h-8 rounded-full bg-brand-gold/15 border border-brand-gold/30 flex items-center justify-center">
                <i class="fa-solid fa-feather-pointed text-xs"></i>
            </div>
            <span>OldBook Admin</span>
        </a>
        <div class="flex items-center gap-2">
            <sec:authorize access="hasRole('ADMIN')">
                <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-rose-500/15 border border-rose-500/30 text-rose-300">ADMIN</span>
            </sec:authorize>
            <sec:authorize access="hasRole('MANAGER')">
                <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-purple-500/15 border border-purple-500/30 text-purple-300">MANAGER</span>
            </sec:authorize>
            <sec:authorize access="hasRole('STAFF')">
                <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-sky-500/15 border border-sky-500/30 text-sky-300">STAFF</span>
            </sec:authorize>
            <form action="/logout" method="post" class="m-0 inline">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <button type="submit" class="w-7 h-7 rounded-lg bg-rose-500/10 hover:bg-rose-500/20 text-rose-300 flex items-center justify-center transition" title="Đăng xuất">
                    <i class="fa-solid fa-power-off text-xs"></i>
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
        <sec:authorize access="hasRole('ADMIN')">
            <a href="/admin/users" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'users' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">
                <i class="fa-solid fa-users-gear text-xs"></i>
                <span>Nhân Sự</span>
            </a>
        </sec:authorize>
        <sec:authorize access="hasAnyRole('ADMIN', 'MANAGER')">
            <a href="/admin/stores" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'stores' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">
                <i class="fa-solid fa-store text-xs"></i>
                <span>Chi Nhánh</span>
            </a>
        </sec:authorize>
        <a href="/admin/books" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'books' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">
            <i class="fa-solid fa-book text-xs"></i>
            <span>Đầu Sách</span>
        </a>
        <a href="/admin/book-copies" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'copies' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">
            <i class="fa-solid fa-boxes-stacked text-xs"></i>
            <span>Kho Bản Sách</span>
        </a>
        <a href="/admin/buybacks" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'buybacks' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">
            <i class="fa-solid fa-hand-holding-dollar text-xs"></i>
            <span>Thu Mua</span>
        </a>
        <a href="/admin/transfers" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'transfers' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">
            <i class="fa-solid fa-truck-ramp-box text-xs"></i>
            <span>Chuyển Kho</span>
        </a>
    </div>
</div>

<!-- 2. Desktop Floating Left Dock (hidden xl:flex) -->
<aside class="hidden xl:flex flex-col items-center justify-between py-6 px-3 rounded-full glass-panel shadow-glass-elevated sticky top-10 h-[620px] w-[64px] border border-white/10 shrink-0">
    <a class="w-10 h-10 rounded-full bg-brand-gold/15 border border-brand-gold/30 flex items-center justify-center text-brand-gold hover:bg-brand-gold/25 transition-all" href="/" title="Về Sàn Khám Phá Trang Chủ">
        <i class="fa-solid fa-feather-pointed text-brand-gold"></i>
    </a>

    <nav class="flex flex-col items-center gap-4 my-auto">
        <!-- Dashboard -->
        <a class="p-2.5 rounded-full transition group relative ${param.active == 'dashboard' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
           href="/admin/dashboard" title="Tổng quan Dashboard">
            <i class="fa-solid fa-gauge-high text-base"></i>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Tổng Quan Dashboard</span>
        </a>

        <!-- Nhân sự (Chỉ ADMIN) -->
        <sec:authorize access="hasRole('ADMIN')">
            <a class="p-2.5 rounded-full transition group relative ${param.active == 'users' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
               href="/admin/users" title="Quản lý nhân sự">
                <i class="fa-solid fa-users-gear text-base"></i>
                <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Tài Khoản & Quyền</span>
            </a>
        </sec:authorize>

        <!-- Chi nhánh (ADMIN & MANAGER) -->
        <sec:authorize access="hasAnyRole('ADMIN', 'MANAGER')">
            <a class="p-2.5 rounded-full transition group relative ${param.active == 'stores' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
               href="/admin/stores" title="Mạng lưới chi nhánh">
                <i class="fa-solid fa-store text-base"></i>
                <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Chi Nhánh</span>
            </a>
        </sec:authorize>

        <!-- Đầu sách -->
        <a class="p-2.5 rounded-full transition group relative ${param.active == 'books' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
           href="/admin/books" title="Danh mục đầu sách">
            <i class="fa-solid fa-book text-base"></i>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Đầu Sách</span>
        </a>

        <!-- Kho bản sách -->
        <a class="p-2.5 rounded-full transition group relative ${param.active == 'copies' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
           href="/admin/book-copies" title="Quản lý bản sao tồn kho">
            <i class="fa-solid fa-boxes-stacked text-base"></i>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Kho Bản Sách</span>
        </a>

        <!-- Thu mua -->
        <a class="p-2.5 rounded-full transition group relative ${param.active == 'buybacks' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
           href="/admin/buybacks" title="Duyệt thu mua sách cũ">
            <i class="fa-solid fa-hand-holding-dollar text-base"></i>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Duyệt Thu Mua</span>
        </a>

        <!-- Chuyển kho -->
        <a class="p-2.5 rounded-full transition group relative ${param.active == 'transfers' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
           href="/admin/transfers" title="Điều chuyển kho chi nhánh">
            <i class="fa-solid fa-truck-ramp-box text-base"></i>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Chuyển Kho</span>
        </a>
    </nav>

    <!-- Logout -->
    <form action="/logout" method="post" class="m-0">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <button type="submit" class="p-2.5 rounded-full text-rose-400 hover:text-rose-300 hover:bg-rose-500/10 transition group relative" title="Đăng xuất">
            <i class="fa-solid fa-power-off text-base"></i>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-rose-300 opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Đăng Xuất</span>
        </button>
    </form>
</aside>
