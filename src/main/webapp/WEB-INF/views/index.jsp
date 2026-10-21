<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:set var="isLoggedIn" value="${pageContext.request.userPrincipal != null}" />
<c:set var="userRole" value="${isLoggedIn ? pageContext.request.userPrincipal.principal.user.role.name() : ''}" />
<c:set var="isStaffOrAdmin" value="${userRole == 'ADMIN' || userRole == 'MANAGER' || userRole == 'STAFF'}" />
<!DOCTYPE html>
<html class="dark" lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>OldBook — Sàn Giao Dịch & Khám Phá Sách Cũ Tuyển Chọn</title>
    <!-- Tailwind CSS v3 with Plugins -->
    <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
    <script>
        tailwind.config = {
            darkMode: 'class',
            theme: {
                extend: {
                    colors: {
                        brand: {
                            gold: '#D4AF37',
                            goldLight: '#F3E5AB',
                            goldHover: '#c19e2e',
                            darkBg: '#0b0c10',
                            glassBg: 'rgba(18, 20, 26, 0.72)',
                            glassBorder: 'rgba(255, 255, 255, 0.08)',
                            glassHighlight: 'rgba(255, 255, 255, 0.12)',
                            surface: '#15181E',
                            forest: '#1E382B',
                            terracotta: '#A6553B'
                        }
                    },
                    fontFamily: {
                        serif: ['Playfair Display', 'Merriweather', 'Georgia', 'serif'],
                        sans: ['Plus Jakarta Sans', 'Inter', 'system-ui', 'sans-serif'],
                    },
                    boxShadow: {
                        'glow-gold': '0 0 25px -5px rgba(212, 175, 55, 0.35)',
                        'glass-elevated': '0 20px 50px rgba(0, 0, 0, 0.65), 0 0 1px 1px rgba(255, 255, 255, 0.08)',
                        'book-3d': '-10px 14px 28px rgba(0,0,0,0.7), -2px 0px 8px rgba(0,0,0,0.5)',
                    }
                }
            }
        }
    </script>
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com" rel="preconnect"/>
    <link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect"/>
    <link href="https://fonts.googleapis.com/css2?family=Merriweather:ital,wght@0,300;0,400;0,700;1,400&family=Playfair+Display:ital,wght@0,500;0,600;0,700;1,600&family=Plus+Jakarta+Sans:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
    <!-- FontAwesome 6 -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet"/>

    <style data-purpose="custom-layout">
        body {
            background-color: #0b0c10;
            font-family: 'Plus Jakarta Sans', sans-serif;
            color: #e2e8f0;
            overflow-x: hidden;
        }

        .bg-reading-room {
            background: radial-gradient(circle at 15% 20%, rgba(212, 175, 55, 0.08) 0%, transparent 45%),
                        radial-gradient(circle at 85% 65%, rgba(166, 85, 59, 0.07) 0%, transparent 50%),
                        radial-gradient(circle at 50% 100%, rgba(30, 56, 43, 0.25) 0%, transparent 60%),
                        linear-gradient(180deg, #090a0d 0%, #11141a 50%, #0d0f14 100%);
            background-attachment: fixed;
        }

        .ambient-glow {
            position: absolute;
            width: 650px;
            height: 650px;
            background: radial-gradient(circle, rgba(212, 175, 55, 0.12) 0%, rgba(193, 158, 46, 0.03) 45%, transparent 70%);
            filter: blur(80px);
            pointer-events: none;
            z-index: 0;
        }

        .glass-panel {
            background: rgba(18, 21, 28, 0.65);
            backdrop-filter: blur(28px) saturate(160%);
            -webkit-backdrop-filter: blur(28px) saturate(160%);
            border: 1px solid rgba(255, 255, 255, 0.09);
        }

        .glass-panel-subtle {
            background: rgba(25, 30, 40, 0.5);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid rgba(255, 255, 255, 0.06);
        }

        .glass-input {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(12px);
            border: 1px solid rgba(255, 255, 255, 0.08);
        }
        .glass-input:focus {
            background: rgba(255, 255, 255, 0.08);
            border-color: rgba(212, 175, 55, 0.45);
            outline: none;
        }

        ::-webkit-scrollbar {
            width: 6px;
            height: 6px;
        }
        ::-webkit-scrollbar-track {
            background: rgba(0, 0, 0, 0.2);
        }
        ::-webkit-scrollbar-thumb {
            background: rgba(255, 255, 255, 0.15);
            border-radius: 9999px;
        }
        ::-webkit-scrollbar-thumb:hover {
            background: rgba(212, 175, 55, 0.35);
        }
    </style>

    <style data-purpose="book-3d-styles">
        .book-spine-effect {
            box-shadow: 
                inset 5px 0 10px -2px rgba(0,0,0,0.6),
                inset -2px 0 5px rgba(255,255,255,0.1),
                0 15px 30px rgba(0,0,0,0.65);
        }
        .hero-book-spine {
            box-shadow: 
                inset 10px 0 16px -4px rgba(0,0,0,0.85),
                inset -3px 0 8px rgba(255,255,255,0.15),
                -16px 20px 45px rgba(0,0,0,0.8),
                0 0 25px rgba(212, 175, 55, 0.12);
        }
    </style>
</head>
<body class="bg-reading-room min-h-screen relative flex flex-col justify-between py-6 px-4 md:px-8 lg:px-12 selection:bg-brand-gold selection:text-black">
    <!-- Ambient background glow lights -->
    <div class="ambient-glow top-12 left-1/4"></div>
    <div class="ambient-glow bottom-20 right-10"></div>

    <!-- BEGIN: MasterLayoutWrapper -->
    <div class="relative z-10 max-w-[1520px] w-full mx-auto flex items-start gap-5">
        <!-- BEGIN: LeftFloatingDock -->
        <aside class="hidden xl:flex flex-col items-center justify-between py-6 px-3 rounded-full glass-panel shadow-glass-elevated sticky top-10 h-[560px] w-[64px] border border-white/10 shrink-0" data-purpose="vertical-dock">
            <!-- Logo Mark Top -->
            <a class="w-10 h-10 rounded-full bg-brand-gold/15 border border-brand-gold/30 flex items-center justify-center text-brand-gold hover:bg-brand-gold/25 transition-all" href="/" title="OldBook Trang Chủ">
                <svg class="w-5 h-5 text-brand-gold" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                    <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" stroke-linecap="round" stroke-linejoin="round"></path>
                </svg>
            </a>

            <!-- Primary Dock Actions -->
            <nav class="flex flex-col items-center gap-6 my-auto">
                <!-- Home (Active) -->
                <a class="relative p-2.5 rounded-full bg-white/10 text-brand-gold shadow-glow-gold hover:text-white transition group" href="/" title="Trang chủ">
                    <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M11.47 3.84a.75.75 0 011.06 0l8.69 8.69a.75.75 0 101.06-1.06l-8.689-8.69a2.25 2.25 0 00-3.182 0l-8.69 8.69a.75.75 0 001.061 1.06l8.69-8.69z"></path>
                        <path d="M12 5.432l8.159 8.159c.03.03.06.058.091.086v6.198c0 1.035-.84 1.875-1.875 1.875H15a.75.75 0 01-.75-.75v-4.5a.75.75 0 00-.75-.75h-3a.75.75 0 00-.75.75V21a.75.75 0 01-.75.75H5.625a1.875 1.875 0 01-1.875-1.875v-6.198a2.29 2.29 0 00.091-.086L12 5.432z"></path>
                    </svg>
                    <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Khám Phá Sách</span>
                </a>

                <!-- Wishlist / Kho sách -->
                <a class="p-2.5 rounded-full text-slate-400 hover:text-brand-gold hover:bg-white/5 transition group relative" href="/#catalog" title="Kho sách">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                        <path d="M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12z" stroke-linecap="round" stroke-linejoin="round"></path>
                    </svg>
                    <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Kho Sách Tuyển Chọn</span>
                </a>

                <!-- Thu mua sách -->
                <a class="p-2.5 rounded-full text-slate-400 hover:text-brand-gold hover:bg-white/5 transition group relative" href="/#buyback" title="Thu mua sách cũ">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                        <path d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5M16.5 12L12 16.5m0 0L7.5 12m4.5 4.5V3" stroke-linecap="round" stroke-linejoin="round"></path>
                    </svg>
                    <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Dịch Vụ Thu Mua</span>
                </a>

                <!-- Hệ thống chi nhánh -->
                <a class="p-2.5 rounded-full text-slate-400 hover:text-brand-gold hover:bg-white/5 transition group relative" href="/#stores" title="Chi nhánh chuỗi">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                        <path d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z" stroke-linecap="round" stroke-linejoin="round"></path>
                    </svg>
                    <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Hệ Thống Chi Nhánh</span>
                </a>

                <!-- Admin Link if Authorized -->
                <c:if test="${isStaffOrAdmin}">
                    <a class="p-2.5 rounded-full text-brand-gold bg-brand-gold/15 hover:bg-brand-gold/30 transition group relative" href="/admin/dashboard" title="Trang Quản Trị">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M10.5 6a7.5 7.5 0 107.5 7.5h-7.5V6z"/>
                            <path stroke-linecap="round" stroke-linejoin="round" d="M13.5 10.5H21A7.5 7.5 0 0013.5 3v7.5z"/>
                        </svg>
                        <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-brand-gold opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Vào Quản Trị</span>
                    </a>
                </c:if>
            </nav>

            <!-- Settings / Login Bottom -->
            <c:if test="${!isLoggedIn}">
                <a class="p-2.5 rounded-full text-slate-400 hover:text-brand-gold hover:bg-white/5 transition group relative" href="/login" title="Đăng nhập">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15m3 0l3-3m0 0l-3-3m3 3H9"/>
                    </svg>
                    <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Đăng Nhập</span>
                </a>
            </c:if>
            <c:if test="${isLoggedIn}">
                <form action="/logout" method="post" class="m-0">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                    <button type="submit" class="p-2.5 rounded-full text-rose-400 hover:text-rose-300 hover:bg-rose-500/10 transition group relative" title="Đăng xuất">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M5.636 5.636a9 9 0 1012.728 0M12 3v9"/>
                        </svg>
                        <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-rose-300 opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Đăng Xuất</span>
                    </button>
                </form>
            </c:if>
        </aside>
        <!-- END: LeftFloatingDock -->

        <!-- BEGIN: MainContentContainer -->
        <main class="flex-1 w-full glass-panel rounded-[32px] p-5 sm:p-7 shadow-glass-elevated border border-white/10 relative overflow-hidden" data-purpose="main-display-frame">
            <!-- BEGIN: TopNavigationBar -->
            <header class="flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-4 pb-6 border-b border-white/5" data-purpose="header-navigation">
                <!-- Search bar + Brand icon -->
                <div class="flex items-center gap-3 w-full lg:w-72 xl:w-80">
                    <form action="/" method="get" class="relative w-full">
                        <span class="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none text-slate-400">
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                <path d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" stroke-linecap="round" stroke-linejoin="round"></path>
                            </svg>
                        </span>
                        <input name="keyword" value="${selectedKeyword}" class="glass-input w-full pl-10 pr-12 py-2 text-xs sm:text-sm rounded-full text-slate-100 placeholder-slate-400 transition" placeholder="Tìm kiếm sách, tác giả, tiệm sách..." type="text"/>
                        <span class="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none">
                            <kbd class="px-1.5 py-0.5 text-[10px] bg-white/10 text-slate-300 rounded font-mono border border-white/10">⌘K</kbd>
                        </span>
                    </form>
                </div>

                <!-- Category Pills (Center Tabs) -->
                <nav class="flex items-center gap-1.5 sm:gap-2 overflow-x-auto py-1 no-scrollbar text-xs sm:text-sm font-medium" data-purpose="category-tabs">
                    <a href="/" class="px-4 py-1.5 rounded-full ${empty selectedCategory ? 'bg-white/15 text-white border border-white/10 shadow-sm' : 'text-slate-400 hover:text-white hover:bg-white/5'} transition whitespace-nowrap">
                        Khám phá tất cả
                    </a>
                    <c:forEach var="cat" items="${categories}">
                        <a href="/?category=${cat}" class="px-4 py-1.5 rounded-full ${selectedCategory == cat ? 'bg-white/15 text-white border border-white/10 shadow-sm' : 'text-slate-400 hover:text-white hover:bg-white/5'} transition whitespace-nowrap">
                            ${cat}
                        </a>
                    </c:forEach>
                </nav>

                <!-- User profile & Quick notification (Right) -->
                <div class="flex items-center justify-end gap-3 shrink-0" data-purpose="user-actions">
                    <!-- Notification Icon -->
                    <a href="/#stores" class="relative p-2 rounded-full glass-panel-subtle hover:bg-white/10 transition text-slate-300" title="Chi nhánh hoạt động">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                            <path d="M14.857 17.082a23.848 23.848 0 005.454-1.31A8.967 8.967 0 0118 9.75v-.7V9A6 6 0 006 9v.75a8.967 8.967 0 01-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.255 24.255 0 01-5.714 0m5.714 0a3 3 0 11-5.714 0" stroke-linecap="round" stroke-linejoin="round"></path>
                        </svg>
                        <span class="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-emerald-400 ring-2 ring-black"></span>
                    </a>

                    <!-- User status -->
                    <c:if test="${!isLoggedIn}">
                        <a href="/login" class="flex items-center gap-2 pl-3 pr-4 py-1.5 rounded-full bg-gradient-to-r from-brand-gold to-yellow-500 text-slate-950 font-bold text-xs shadow-glow-gold hover:from-yellow-400 hover:to-amber-500 transition">
                            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15m3 0l3-3m0 0l-3-3m3 3H9"/>
                            </svg>
                            <span>Đăng nhập</span>
                        </a>
                    </c:if>

                    <c:if test="${isLoggedIn}">
                        <c:if test="${isStaffOrAdmin}">
                            <a href="/admin/dashboard" class="hidden md:flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-brand-gold/15 hover:bg-brand-gold/25 border border-brand-gold/30 text-xs text-brand-gold hover:text-white transition">
                                <i class="fa-solid fa-gauge text-[11px]"></i>
                                <span>Quản Trị</span>
                            </a>
                        </c:if>
                        <div class="flex items-center gap-2.5 pl-2 pr-3 py-1 rounded-full glass-panel-subtle border border-white/10 hover:border-brand-gold/40 transition">
                            <div class="w-7 h-7 rounded-full bg-gradient-to-tr from-amber-600 via-brand-gold to-yellow-200 p-[1px] flex items-center justify-center">
                                <div class="w-full h-full rounded-full bg-[#1e2330] flex items-center justify-center text-xs font-bold text-amber-200">
                                    ${pageContext.request.userPrincipal.principal.username.substring(0, 1).toUpperCase()}
                                </div>
                            </div>
                            <div class="hidden sm:flex flex-col text-left">
                                <span class="text-xs font-semibold text-slate-200 leading-tight">
                                    ${pageContext.request.userPrincipal.principal.user.fullName}
                                </span>
                                <span class="text-[10px] text-brand-goldLight leading-none">
                                    ${pageContext.request.userPrincipal.principal.user.role}
                                </span>
                            </div>
                        </div>
                    </c:if>
                </div>
            </header>
            <!-- END: TopNavigationBar -->

            <!-- Alert if access denied -->
            <c:if test="${param.denied == 'true'}">
                <div class="mt-4 p-3.5 rounded-2xl bg-rose-500/15 border border-rose-500/30 text-rose-200 text-xs flex items-center justify-between gap-3 shadow-lg">
                    <div class="flex items-center gap-2.5">
                        <i class="fa-solid fa-shield-halved text-rose-400 text-base"></i>
                        <span><strong>Truy cập bị từ chối:</strong> Bạn không có quyền truy cập khu vực Quản trị hệ thống. Khu vực này chỉ dành cho Nhân viên (STAFF) và Ban Quản trị chuỗi (MANAGER, ADMIN).</span>
                    </div>
                    <a href="/" class="text-rose-300 hover:text-white text-xs underline font-semibold">Đóng</a>
                </div>
            </c:if>

            <!-- BEGIN: ContentGrid -->
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 mt-6">
                <!-- ======================= LEFT COLUMN (4 Cols) ======================= -->
                <div class="lg:col-span-4 xl:col-span-3 flex flex-col gap-6" data-purpose="left-column">
                    <!-- SECTION: Sách Mới Cập Nhật -->
                    <section class="flex flex-col" data-purpose="new-releases-section">
                        <div class="flex items-center justify-between mb-3 px-1">
                            <div class="flex items-center gap-2">
                                <svg class="w-4 h-4 text-brand-gold" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                    <path d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25" stroke-linecap="round" stroke-linejoin="round"></path>
                                </svg>
                                <h2 class="text-xs uppercase tracking-wider font-semibold text-slate-300">Sách Mới Cập Nhật</h2>
                            </div>
                            <a href="/#catalog" class="text-[11px] text-slate-400 hover:text-brand-gold flex items-center gap-1 transition">
                                Toàn bộ ▾
                            </a>
                        </div>

                        <!-- Dynamic Real Books List -->
                        <div class="flex flex-col gap-2.5">
                            <c:forEach var="item" items="${catalog}" begin="0" end="4">
                                <article class="group p-2 rounded-xl hover:bg-white/5 transition border border-transparent hover:border-white/5 flex items-center justify-between cursor-pointer" onclick="openBookModal('${item.id}')">
                                    <div class="flex items-center gap-3 min-w-0">
                                        <div class="w-10 h-14 rounded bg-gradient-to-br from-amber-700 via-amber-900 to-black p-0.5 shrink-0 book-spine-effect relative overflow-hidden">
                                            <c:choose>
                                                <c:when test="${not empty item.coverImage}">
                                                    <img src="${item.coverImage}" class="w-full h-full object-cover rounded-[2px]" alt="${item.title}"/>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="w-full h-full bg-[#1b1511] flex flex-col items-center justify-center p-1 text-center">
                                                        <span class="text-[7px] font-bold text-amber-400 tracking-tighter uppercase font-serif line-clamp-2">${item.title}</span>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div class="min-w-0">
                                            <h3 class="text-xs font-semibold text-slate-200 truncate group-hover:text-brand-gold transition">${item.title}</h3>
                                            <p class="text-[11px] text-slate-400 truncate">${item.author}</p>
                                            <div class="flex items-center gap-2 mt-0.5">
                                                <span class="text-[10px] text-emerald-400 font-medium">
                                                    <fmt:formatNumber value="${item.minPrice}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                                                </span>
                                                <c:choose>
                                                    <c:when test="${item.availableCopiesCount > 0}">
                                                        <span class="text-[9px] px-1.5 py-0.2 rounded bg-emerald-500/10 text-emerald-300 border border-emerald-500/20">Còn ${item.availableCopiesCount} cuốn</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-[9px] px-1.5 py-0.2 rounded bg-rose-500/10 text-rose-300 border border-rose-500/20">Tạm hết</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </div>
                                    </div>
                                    <button class="text-slate-500 hover:text-brand-gold p-1.5 transition shrink-0" title="Lưu sách">
                                        <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                            <path d="M17.593 3.322c1.1.128 1.907 1.077 1.907 2.185V21L12 17.25 4.5 21V5.507c0-1.108.806-2.057 1.907-2.185a48.507 48.507 0 0111.186 0z" stroke-linecap="round" stroke-linejoin="round"></path>
                                        </svg>
                                    </button>
                                </article>
                            </c:forEach>
                        </div>
                    </section>

                    <!-- SECTION: Tiếp Tục Đọc & Xem (Curated Literary Classics) -->
                    <section class="flex flex-col pt-3 border-t border-white/5" data-purpose="continue-reading-section">
                        <div class="flex items-center justify-between mb-3 px-1">
                            <h2 class="text-xs uppercase tracking-wider font-semibold text-slate-300">Tủ Sách Bạn Đọc Quan Tâm</h2>
                            <span class="text-[11px] text-slate-500">Kinh điển</span>
                        </div>
                        <div class="flex flex-col gap-3">
                            <div class="flex items-center gap-3 group">
                                <div class="w-9 h-12 rounded bg-gradient-to-b from-amber-100 to-amber-300 p-0.5 shrink-0 book-spine-effect">
                                    <div class="w-full h-full bg-slate-900 rounded-[2px] flex items-center justify-center text-[7px] font-bold text-amber-200">
                                        ATOMIC
                                    </div>
                                </div>
                                <div class="flex-1 min-w-0">
                                    <div class="flex items-center justify-between text-xs mb-1">
                                        <span class="font-medium text-slate-200 truncate group-hover:text-brand-gold transition">Atomic Habits</span>
                                        <span class="text-[10px] text-slate-400 font-mono">56%</span>
                                    </div>
                                    <div class="w-full h-1 bg-white/10 rounded-full overflow-hidden">
                                        <div class="h-full bg-brand-gold rounded-full" style="width: 56%"></div>
                                    </div>
                                </div>
                            </div>

                            <div class="flex items-center gap-3 group">
                                <div class="w-9 h-12 rounded bg-gradient-to-b from-indigo-700 to-blue-950 p-0.5 shrink-0 book-spine-effect">
                                    <div class="w-full h-full bg-[#0a1128] rounded-[2px] flex items-center justify-center text-[6px] font-bold text-indigo-200 text-center leading-tight">
                                        MIDNIGHT<br/>LIB
                                    </div>
                                </div>
                                <div class="flex-1 min-w-0">
                                    <div class="flex items-center justify-between text-xs mb-1">
                                        <span class="font-medium text-slate-200 truncate group-hover:text-brand-gold transition">The Midnight Library</span>
                                        <span class="text-[10px] text-slate-400 font-mono">32%</span>
                                    </div>
                                    <div class="w-full h-1 bg-white/10 rounded-full overflow-hidden">
                                        <div class="h-full bg-blue-400 rounded-full" style="width: 32%"></div>
                                    </div>
                                </div>
                            </div>

                            <div class="flex items-center gap-3 group">
                                <div class="w-9 h-12 rounded bg-gradient-to-b from-rose-800 to-amber-900 p-0.5 shrink-0 book-spine-effect">
                                    <div class="w-full h-full bg-[#200e0e] rounded-[2px] flex items-center justify-center text-[7px] font-bold text-rose-200">
                                        SAPIENS
                                    </div>
                                </div>
                                <div class="flex-1 min-w-0">
                                    <div class="flex items-center justify-between text-xs mb-1">
                                        <span class="font-medium text-slate-200 truncate group-hover:text-brand-gold transition">Sapiens — Lược Sử Loài Người</span>
                                        <span class="text-[10px] text-slate-400 font-mono">71%</span>
                                    </div>
                                    <div class="w-full h-1 bg-white/10 rounded-full overflow-hidden">
                                        <div class="h-full bg-emerald-400 rounded-full" style="width: 71%"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </section>
                </div>
                <!-- ======================= END LEFT COLUMN ======================= -->

                <!-- ======================= RIGHT COLUMN (8/9 Cols): Hero + Recommendation ======================= -->
                <div class="lg:col-span-8 xl:col-span-9 flex flex-col gap-6" data-purpose="right-stage">
                    <!-- BEGIN: CenterHeroBanner ("The Starless Archive") -->
                    <section class="relative rounded-2xl overflow-hidden glass-panel-subtle border border-white/10 p-6 md:p-8 flex flex-col lg:flex-row items-center justify-between gap-8 min-h-[360px] bg-gradient-to-r from-black/80 via-[#10151f]/85 to-black/90" data-purpose="hero-featured-book">
                        <div class="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-amber-500/10 via-blue-900/15 to-transparent pointer-events-none"></div>

                        <!-- Left Details Information -->
                        <div class="relative z-10 flex-1 max-w-xl">
                            <!-- Feature Badges -->
                            <div class="flex flex-wrap items-center gap-2 mb-3">
                                <span class="px-2.5 py-0.5 text-[11px] font-medium tracking-wide rounded-full bg-brand-gold/20 text-brand-goldLight border border-brand-gold/40">
                                    ★ Editor's Pick
                                </span>
                                <span class="px-2.5 py-0.5 text-[11px] font-medium rounded-full bg-white/10 text-slate-300">
                                    Dark Fantasy
                                </span>
                                <span class="px-2.5 py-0.5 text-[11px] font-medium rounded-full bg-white/10 text-slate-300">
                                    Huyền Bí & Cổ Điển
                                </span>
                                <span class="px-2 py-0.5 text-[11px] font-medium rounded-full bg-emerald-500/15 text-emerald-300 border border-emerald-500/30">
                                    Like New 96%
                                </span>
                            </div>

                            <!-- Main Title & Author -->
                            <h1 class="text-3xl md:text-4xl lg:text-4xl font-serif font-bold text-white tracking-tight leading-tight">
                                The Starless Archive
                            </h1>
                            <div class="flex items-center gap-2 text-xs md:text-sm text-slate-300 mt-1 mb-3">
                                <span class="font-medium text-brand-goldLight">Tác giả: Elian Voss</span>
                                <span class="text-slate-600">•</span>
                                <span class="text-slate-400">Bản in First Edition (1984) — Arkham House</span>
                            </div>

                            <!-- Excerpt description -->
                            <p class="text-xs md:text-sm text-slate-300 leading-relaxed font-light line-clamp-3 mb-5">
                                "Bên dưới thư viện chìm ngàn năm, người lưu trữ già khám phá ra nơi cất giữ những ký ức từng bị thời gian xóa bỏ khỏi bầu trời sao... Một kiệt tác kỳ ảo đen tối hiếm có vừa cập bến OldBook từ tiệm sưu tầm Tràng Tiền."
                            </p>

                            <!-- Price & Verified seller trust badge -->
                            <div class="flex flex-wrap items-center gap-4 py-2.5 px-3.5 rounded-xl bg-black/40 border border-white/10 mb-6 backdrop-blur-md">
                                <div>
                                    <div class="flex items-baseline gap-2">
                                        <span class="text-xl font-bold text-brand-gold">245.000₫</span>
                                        <span class="text-xs text-slate-400 line-through">490.000₫</span>
                                        <span class="text-[10px] font-semibold text-rose-400 bg-rose-500/15 px-1.5 py-0.5 rounded">-50%</span>
                                    </div>
                                    <span class="text-[10px] text-amber-200/70">Tình trạng: Giữ nguyên bìa áo zin, giấy ngả vàng đẹp</span>
                                </div>
                                <div class="h-7 w-[1px] bg-white/10 hidden sm:block"></div>
                                <div class="text-[11px] text-slate-300 flex items-center gap-1.5">
                                    <span class="w-2 h-2 rounded-full bg-emerald-400"></span>
                                    <span>Người bán: <strong class="text-white">Hệ Thống OldBook Toàn Chuỗi</strong> (4.9★ • Đã kiểm định)</span>
                                </div>
                            </div>

                            <!-- Action CTAs -->
                            <div class="flex flex-wrap items-center gap-2.5">
                                <a href="/#catalog" class="px-5 py-2.5 rounded-full bg-gradient-to-r from-brand-gold to-yellow-500 hover:from-yellow-400 hover:to-amber-500 text-slate-950 font-bold text-xs sm:text-sm shadow-glow-gold transition flex items-center gap-2">
                                    <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                                        <path d="M12 2.25c-5.385 0-9.75 4.365-9.75 9.75s4.365 9.75 9.75 9.75 9.75-4.365 9.75-9.75S17.385 2.25 12 2.25zm4.28 10.28l-5.25 5.25a.75.75 0 01-1.06 0l-2.25-2.25a.75.75 0 111.06-1.06l1.72 1.72 4.72-4.72a.75.75 0 111.06 1.06z"></path>
                                    </svg>
                                    Khám Phá Toàn Kho Sách
                                </a>
                                <a href="/#buyback" class="px-4 py-2.5 rounded-full bg-white/10 hover:bg-white/15 text-white font-medium text-xs sm:text-sm border border-white/15 transition flex items-center gap-1.5">
                                    <svg class="w-4 h-4 text-slate-300" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                        <path d="M17.593 3.322c1.1.128 1.907 1.077 1.907 2.185V21L12 17.25 4.5 21V5.507c0-1.108.806-2.057 1.907-2.185a48.507 48.507 0 0111.186 0z" stroke-linecap="round" stroke-linejoin="round"></path>
                                    </svg>
                                    Gửi Thu Mua Sách Cũ
                                </a>
                            </div>
                        </div>

                        <!-- Right Giant Realistic 3D Book Display -->
                        <div class="relative z-10 shrink-0 flex flex-col items-center">
                            <div class="relative w-48 sm:w-56 md:w-60 h-72 sm:h-80 md:h-84 rounded-r-lg rounded-l-[4px] bg-[#11141c] hero-book-spine border-t border-r border-b border-white/20 p-4 flex flex-col justify-between overflow-hidden group transition transform hover:-translate-y-1.5 duration-300">
                                <div class="absolute inset-0 bg-gradient-to-tr from-black via-[#161a24] to-[#252c3c] opacity-95"></div>
                                <div class="absolute inset-2 border border-brand-gold/30 rounded pointer-events-none"></div>
                                <div class="absolute inset-3 border border-brand-gold/15 rounded pointer-events-none"></div>

                                <div class="relative z-10 text-center pt-2">
                                    <span class="text-[9px] tracking-[0.25em] text-brand-gold uppercase block font-serif">THE</span>
                                    <h2 class="text-xl sm:text-2xl font-serif font-black tracking-widest text-transparent bg-clip-text bg-gradient-to-b from-brand-goldLight via-brand-gold to-yellow-600 uppercase mt-0.5">
                                        STARLESS
                                    </h2>
                                    <h3 class="text-xs sm:text-sm font-serif tracking-[0.3em] text-slate-300 uppercase -mt-1 font-semibold">
                                        ARCHIVE
                                    </h3>
                                </div>

                                <div class="relative z-10 my-auto flex items-center justify-center">
                                    <div class="w-20 h-20 rounded-full border border-brand-gold/40 flex items-center justify-center relative shadow-[0_0_20px_rgba(212,175,55,0.2)]">
                                        <div class="w-14 h-14 rounded-full border border-dashed border-brand-gold/60 flex items-center justify-center animate-spin-slow">
                                            <div class="w-8 h-8 rounded-full bg-gradient-to-br from-amber-400/20 to-black border border-brand-gold/80 flex items-center justify-center">
                                                <div class="w-2.5 h-2.5 rounded-full bg-brand-gold shadow-[0_0_12px_#D4AF37]"></div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div class="relative z-10 text-center pb-2">
                                    <p class="text-[10px] tracking-[0.2em] font-serif uppercase text-brand-goldLight">ELIAN VOSS</p>
                                    <span class="text-[7px] tracking-widest text-slate-500 uppercase block mt-0.5">FIRST EDITION • 1984</span>
                                </div>
                                <div class="absolute -inset-full w-[200%] h-[200%] bg-gradient-to-br from-white/15 via-transparent to-transparent rotate-45 pointer-events-none"></div>
                            </div>

                            <div class="flex items-center gap-2 mt-4">
                                <span class="w-2 h-2 rounded-full bg-brand-gold"></span>
                                <span class="w-1.5 h-1.5 rounded-full bg-white/20"></span>
                                <span class="w-1.5 h-1.5 rounded-full bg-white/20"></span>
                            </div>
                        </div>
                    </section>
                    <!-- END: CenterHeroBanner -->

                    <!-- BEGIN: RecommendationsSection / Real Catalog Gallery -->
                    <section id="catalog" class="flex flex-col" data-purpose="recommendations-gallery">
                        <div class="flex items-center justify-between mb-4">
                            <div>
                                <h2 class="text-base sm:text-lg font-serif font-semibold text-white tracking-tight">
                                    Có thể bạn sẽ thích (Kho Sách Hiện Có)
                                </h2>
                                <p class="text-xs text-slate-400">Tuyển lựa sách thật từ hệ thống chi nhánh OldBook toàn quốc</p>
                            </div>
                            <span class="text-xs text-brand-goldLight font-medium">
                                ${catalog.size()} tác phẩm sẵn sàng
                            </span>
                        </div>

                        <!-- Grid 5 columns matching user reference -->
                        <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-3 xl:grid-cols-5 gap-3.5">
                            <c:forEach var="book" items="${catalog}">
                                <article class="group rounded-2xl glass-panel-subtle border border-white/10 p-2.5 flex flex-col justify-between hover:border-brand-gold/40 hover:bg-white/5 transition duration-300">
                                    <div class="relative aspect-[3/4.2] w-full rounded-xl overflow-hidden mb-2.5 bg-gradient-to-b from-[#1b120c] to-[#0c0806] book-spine-effect cursor-pointer" onclick="openBookModal('${book.id}')">
                                        <c:choose>
                                            <c:when test="${not empty book.coverImage}">
                                                <img src="${book.coverImage}" class="w-full h-full object-cover transition transform group-hover:scale-105 duration-300" alt="${book.title}"/>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="absolute inset-0 bg-gradient-to-t from-amber-950 via-amber-900/60 to-black/80 flex flex-col items-center justify-center p-2 text-center">
                                                    <div class="w-10 h-10 rounded-full border border-amber-500/40 mb-2 shadow-[0_0_15px_#f59e0b] bg-gradient-to-br from-amber-500/20 to-transparent flex items-center justify-center">
                                                        <span class="text-brand-gold text-xs font-serif">✦</span>
                                                    </div>
                                                    <span class="text-xs font-serif font-black tracking-widest text-amber-100 uppercase line-clamp-2">${book.title}</span>
                                                    <span class="text-[8px] text-amber-300/80 tracking-wider truncate mt-1">${book.author}</span>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>

                                        <!-- Category Overlay -->
                                        <div class="absolute top-2 left-2 z-10">
                                            <span class="text-[9px] px-2 py-0.5 rounded-full bg-black/60 backdrop-blur-md text-amber-200 border border-white/10 font-medium">
                                                ${book.category != null ? book.category : 'Sách Cũ'}
                                            </span>
                                        </div>

                                        <!-- Copies Badge -->
                                        <div class="absolute bottom-2 left-2 z-10">
                                            <c:choose>
                                                <c:when test="${book.availableCopiesCount > 0}">
                                                    <span class="text-[8px] px-1.5 py-0.2 rounded bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                                                        Còn ${book.availableCopiesCount} cuốn
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-[8px] px-1.5 py-0.2 rounded bg-rose-500/20 text-rose-300 border border-rose-500/30">
                                                        Hết bản sao
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>

                                    <!-- Info footer -->
                                    <div>
                                        <h3 class="text-xs font-semibold text-white truncate group-hover:text-brand-gold transition cursor-pointer" onclick="openBookModal('${book.id}')">
                                            ${book.title}
                                        </h3>
                                        <p class="text-[11px] text-slate-400 truncate">${book.author}</p>
                                        <div class="flex items-center justify-between mt-2 pt-1.5 border-t border-white/5">
                                            <div class="flex items-center gap-1">
                                                <span class="text-[10px] text-amber-400 font-semibold">★ 4.9</span>
                                                <span class="text-[11px] font-bold text-emerald-400 ml-1">
                                                    <fmt:formatNumber value="${book.minPrice}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                                                </span>
                                            </div>
                                            <button class="text-slate-400 hover:text-brand-gold transition" title="Lưu sách">
                                                <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                                    <path d="M17.593 3.322c1.1.128 1.907 1.077 1.907 2.185V21L12 17.25 4.5 21V5.507c0-1.108.806-2.057 1.907-2.185a48.507 48.507 0 0111.186 0z" stroke-linecap="round" stroke-linejoin="round"></path>
                                                </svg>
                                            </button>
                                        </div>
                                    </div>
                                </article>

                                <!-- Hidden Details Modal for each Book -->
                                <div id="modal-${book.id}" class="hidden fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md">
                                    <div class="glass-panel w-full max-w-2xl rounded-3xl p-6 sm:p-8 border border-white/10 shadow-glass-elevated relative overflow-hidden">
                                        <button onclick="closeBookModal('${book.id}')" class="absolute top-5 right-5 text-slate-400 hover:text-white transition">
                                            <svg class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/>
                                            </svg>
                                        </button>

                                        <div class="flex flex-col sm:flex-row gap-6 mb-6">
                                            <div class="w-32 h-44 rounded-xl overflow-hidden shrink-0 book-spine-effect bg-[#15181E] mx-auto sm:mx-0">
                                                <c:choose>
                                                    <c:when test="${not empty book.coverImage}">
                                                        <img src="${book.coverImage}" class="w-full h-full object-cover" alt="${book.title}"/>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="w-full h-full flex items-center justify-center p-2 text-center text-xs font-serif text-amber-200">
                                                            ${book.title}
                                                        </div>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                            <div class="flex-1">
                                                <span class="text-[10px] px-2.5 py-0.5 rounded-full bg-brand-gold/15 text-brand-gold border border-brand-gold/30">
                                                    ${book.category}
                                                </span>
                                                <h2 class="text-xl font-serif font-bold text-white mt-1.5">${book.title}</h2>
                                                <p class="text-xs text-brand-goldLight mt-0.5"><i class="fa-solid fa-pen-nib me-1"></i>${book.author}</p>
                                                <p class="text-xs text-slate-300 mt-3 leading-relaxed font-light line-clamp-4">
                                                    ${not empty book.description ? book.description : 'Bản in quý hiếm được lưu giữ tại hệ thống chuỗi cửa hàng OldBook.'}
                                                </p>
                                            </div>
                                        </div>

                                        <h4 class="text-xs uppercase tracking-wider font-semibold text-brand-gold mb-3">
                                            Danh Sách Bản Sao Khả Dụng (${book.copies.size()} cuốn)
                                        </h4>
                                        <div class="overflow-x-auto max-h-48 rounded-xl border border-white/5">
                                            <table class="w-full text-left text-xs text-slate-300">
                                                <thead class="bg-white/5 text-[11px] text-slate-400 uppercase tracking-wider">
                                                    <tr>
                                                        <th class="p-2.5">Mã Barcode</th>
                                                        <th class="p-2.5">Chi Nhánh</th>
                                                        <th class="p-2.5">Tình Trạng</th>
                                                        <th class="p-2.5 text-right">Giá Bán</th>
                                                    </tr>
                                                </thead>
                                                <tbody class="divide-y divide-white/5">
                                                    <c:forEach var="cp" items="${book.copies}">
                                                        <tr class="hover:bg-white/5">
                                                            <td class="p-2.5 font-mono text-amber-300">${cp.copyCode}</td>
                                                            <td class="p-2.5">${cp.storeName}</td>
                                                            <td class="p-2.5">
                                                                <span class="px-2 py-0.5 rounded bg-white/10 text-[10px] text-slate-200">
                                                                    ${cp.bookCondition}
                                                                </span>
                                                            </td>
                                                            <td class="p-2.5 text-right font-bold text-emerald-400">
                                                                <fmt:formatNumber value="${cp.price}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                    <c:if test="${empty book.copies}">
                                                        <tr>
                                                            <td colspan="4" class="p-4 text-center text-slate-500">Tất cả bản sao hiện đã được bán hoặc đang điều chuyển.</td>
                                                        </tr>
                                                    </c:if>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </section>
                    <!-- END: RecommendationsSection -->

                    <!-- BEGIN: Buyback Section -->
                    <section id="buyback" class="mt-4 p-6 md:p-8 rounded-2xl glass-panel-subtle border border-white/10">
                        <div class="flex flex-col lg:flex-row gap-8 items-center">
                            <div class="lg:w-5/12">
                                <span class="px-2.5 py-1 text-[11px] font-medium tracking-wide rounded-full bg-brand-gold/15 text-brand-gold border border-brand-gold/30">
                                    Dịch Vụ Thu Mua Sách Cũ
                                </span>
                                <h2 class="text-2xl sm:text-3xl font-serif font-bold text-white mt-2 mb-3">
                                    Trao Sách Cũ — <span class="text-brand-gold">Mở Tri Thức Mới</span>
                                </h2>
                                <p class="text-xs sm:text-sm text-slate-300 leading-relaxed font-light mb-4">
                                    Bạn có những cuốn sách đã đọc xong, tác phẩm hiếm hoặc trọn bộ sưu tầm cần thanh lý? OldBook hỗ trợ thẩm định chuyên sâu và định giá minh bạch tại tất cả chi nhánh.
                                </p>
                                <div class="space-y-3 text-xs text-slate-300">
                                    <div class="flex items-center gap-2.5">
                                        <div class="w-5 h-5 rounded-full bg-brand-gold/20 text-brand-gold flex items-center justify-center font-bold text-[10px]">1</div>
                                        <span>Điền biểu mẫu trực tuyến bên cạnh</span>
                                    </div>
                                    <div class="flex items-center gap-2.5">
                                        <div class="w-5 h-5 rounded-full bg-brand-gold/20 text-brand-gold flex items-center justify-center font-bold text-[10px]">2</div>
                                        <span>Chi nhánh liên hệ thẩm định trong vòng 24h</span>
                                    </div>
                                    <div class="flex items-center gap-2.5">
                                        <div class="w-5 h-5 rounded-full bg-brand-gold/20 text-brand-gold flex items-center justify-center font-bold text-[10px]">3</div>
                                        <span>Nhận tiền thanh toán trực tiếp hoặc chuyển khoản</span>
                                    </div>
                                </div>
                            </div>

                            <div class="lg:w-7/12 w-full">
                                <c:if test="${not empty buybackSuccessMsg}">
                                    <div class="mb-4 p-3 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-300 text-xs">
                                        ${buybackSuccessMsg}
                                    </div>
                                </c:if>
                                <c:if test="${not empty buybackError}">
                                    <div class="mb-4 p-3 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-300 text-xs">
                                        ${buybackError}
                                    </div>
                                </c:if>

                                <form action="/buyback-request" method="post" class="space-y-3">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                                        <div>
                                            <label class="block text-[11px] font-medium text-slate-300 mb-1">Họ tên của bạn (*)</label>
                                            <input name="customerName" value="${buybackForm.customerName}" class="glass-input w-full px-3 py-2 text-xs rounded-xl text-slate-100 placeholder-slate-500" required placeholder="Nguyễn Văn A"/>
                                        </div>
                                        <div>
                                            <label class="block text-[11px] font-medium text-slate-300 mb-1">Email liên hệ (*)</label>
                                            <input name="customerEmail" value="${buybackForm.customerEmail}" class="glass-input w-full px-3 py-2 text-xs rounded-xl text-slate-100 placeholder-slate-500" required placeholder="email@domain.com"/>
                                        </div>
                                    </div>

                                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                                        <div>
                                            <label class="block text-[11px] font-medium text-slate-300 mb-1">Số điện thoại</label>
                                            <input name="customerPhone" value="${buybackForm.customerPhone}" class="glass-input w-full px-3 py-2 text-xs rounded-xl text-slate-100 placeholder-slate-500" placeholder="09xxxxxxxx"/>
                                        </div>
                                        <div>
                                            <label class="block text-[11px] font-medium text-slate-300 mb-1">Chi nhánh tiếp nhận (*)</label>
                                            <select name="storeId" class="glass-input w-full px-3 py-2 text-xs rounded-xl text-slate-100 bg-[#161a22]" required>
                                                <option value="">-- Chọn chi nhánh gần bạn --</option>
                                                <c:forEach var="st" items="${stores}">
                                                    <option value="${st.id}" ${buybackForm.storeId == st.id ? 'selected' : ''}>${st.name}</option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                    </div>

                                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                                        <div>
                                            <label class="block text-[11px] font-medium text-slate-300 mb-1">Tựa sách muốn bán (*)</label>
                                            <input name="bookTitle" value="${buybackForm.bookTitle}" class="glass-input w-full px-3 py-2 text-xs rounded-xl text-slate-100 placeholder-slate-500" required placeholder="VD: Chiến Tranh và Hòa Bình"/>
                                        </div>
                                        <div>
                                            <label class="block text-[11px] font-medium text-slate-300 mb-1">Tình trạng sách ước tính (*)</label>
                                            <select name="condition" class="glass-input w-full px-3 py-2 text-xs rounded-xl text-slate-100 bg-[#161a22]" required>
                                                <c:forEach var="cond" items="${conditions}">
                                                    <option value="${cond}" ${buybackForm.condition == cond ? 'selected' : ''}>${cond}</option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                    </div>

                                    <div>
                                        <label class="block text-[11px] font-medium text-slate-300 mb-1">Ghi chú tình trạng sách</label>
                                        <textarea name="note" rows="2" class="glass-input w-full px-3 py-2 text-xs rounded-xl text-slate-100 placeholder-slate-500" placeholder="VD: Sách bìa cứng năm 1990, gáy còn nguyên...">${buybackForm.note}</textarea>
                                    </div>

                                    <button type="submit" class="w-full py-2.5 px-4 rounded-xl bg-gradient-to-r from-brand-gold to-yellow-500 hover:from-yellow-400 hover:to-amber-500 text-slate-950 font-bold text-xs shadow-glow-gold transition">
                                        Gửi Yêu Cầu Thu Mua Cho Chi Nhánh
                                    </button>
                                </form>
                            </div>
                        </div>
                    </section>
                    <!-- END: Buyback Section -->

                    <!-- BEGIN: Stores Section -->
                    <section id="stores" class="mt-4 p-6 md:p-8 rounded-2xl glass-panel-subtle border border-white/10">
                        <div class="flex items-center justify-between mb-4">
                            <div>
                                <h2 class="text-base sm:text-lg font-serif font-semibold text-white tracking-tight">
                                    Hệ Thống Chi Nhánh Toàn Chuỗi
                                </h2>
                                <p class="text-xs text-slate-400">Các không gian đọc sách và tiếp nhận trao đổi sách cũ</p>
                            </div>
                        </div>
                        <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
                            <c:forEach var="store" items="${stores}">
                                <div class="p-4 rounded-xl glass-panel border border-white/5 flex flex-col justify-between">
                                    <div>
                                        <div class="flex items-center justify-between mb-2">
                                            <span class="text-[9px] px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                                                Đang hoạt động
                                            </span>
                                            <svg class="w-4 h-4 text-brand-gold" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" d="M13.5 21v-7.5a.75.75 0 01.75-.75h3a.75.75 0 01.75.75V21m-4.5 0H2.36m11.14 0H18m0 0h3.64m-1.39 0V9.349m-16.5 11.651V9.35m0 0a3.001 3.001 0 003.75-.615A2.993 2.993 0 009 9.35c.692 0 1.332-.236 1.846-.634a3.002 3.002 0 004.308 0 2.993 2.993 0 001.846.634c.692 0 1.332-.236 1.846-.634a3.001 3.001 0 003.75.614m-16.5 0a3.004 3.004 0 01-.621-1.94c0-1.657 1.343-3 3-3 .692 0 1.332.236 1.846.634m0 0a3.001 3.001 0 004.308 0m0 0a3.001 3.001 0 004.308 0m0 0a3.004 3.004 0 011.846-.634c1.657 0 3 1.343 3 3 0 .72-.254 1.383-.681 1.902"/>
                                            </svg>
                                        </div>
                                        <h3 class="text-sm font-serif font-bold text-white mb-1">${store.name}</h3>
                                        <p class="text-xs text-slate-400 mb-2">${store.address}</p>
                                    </div>
                                    <div class="pt-2 border-t border-white/5 flex items-center justify-between text-xs">
                                        <span class="text-brand-gold">${store.phone}</span>
                                        <a href="/?storeId=${store.id}#catalog" class="text-slate-300 hover:text-white transition">
                                            Xem sách →
                                        </a>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </section>
                    <!-- END: Stores Section -->
                </div>
                <!-- ======================= END RIGHT COLUMN ======================= -->
            </div>
            <!-- END: ContentGrid -->
        </main>
        <!-- END: MainContentContainer -->
    </div>
    <!-- END: MasterLayoutWrapper -->

    <!-- BEGIN: MinimalistFooter -->
    <footer class="max-w-[1520px] w-full mx-auto mt-6 pt-4 border-t border-white/5 flex flex-col md:flex-row items-center justify-between text-xs text-slate-500 gap-4" data-purpose="site-footer">
        <div class="flex items-center gap-3">
            <span class="font-serif font-bold text-slate-300 tracking-wider">OLDBOOK</span>
            <span class="text-slate-700">|</span>
            <p class="italic text-slate-400 text-[11px]">"Trao sách cũ — Mở tri thức mới."</p>
        </div>
        <div class="flex flex-wrap items-center gap-5 text-[11px]">
            <a class="hover:text-brand-gold transition" href="/#catalog">Về OldBook</a>
            <a class="hover:text-brand-gold transition" href="/#buyback">Quy chuẩn kiểm định sách</a>
            <a class="hover:text-brand-gold transition" href="/#stores">Hệ thống chi nhánh</a>
            <a class="hover:text-brand-gold transition" href="/login">Cổng quản trị</a>
            <span class="text-brand-gold">Hotline: 1900 6886</span>
        </div>
        <div class="text-[11px] text-slate-600">
            © 2026 OldBook Inc. Bản quyền thuộc về sàn sách cổ điển.
        </div>
    </footer>
    <!-- END: MinimalistFooter -->

    <script data-purpose="interactive-enhancements">
        document.addEventListener('DOMContentLoaded', () => {
            // Bookmark button active toggle
            const bookmarkButtons = document.querySelectorAll('button[title="Lưu sách"]');
            bookmarkButtons.forEach(btn => {
                btn.addEventListener('click', (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    const svg = btn.querySelector('svg');
                    if (btn.classList.contains('text-brand-gold')) {
                        btn.classList.remove('text-brand-gold');
                        btn.classList.add('text-slate-400');
                        svg.setAttribute('fill', 'none');
                    } else {
                        btn.classList.add('text-brand-gold');
                        btn.classList.remove('text-slate-400');
                        svg.setAttribute('fill', 'currentColor');
                    }
                });
            });

            // Quick Search shortcut listener (Cmd+K / Ctrl+K)
            window.addEventListener('keydown', (e) => {
                if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
                    e.preventDefault();
                    const searchInput = document.querySelector('input[name="keyword"]');
                    if (searchInput) searchInput.focus();
                }
            });
        });

        function openBookModal(id) {
            const m = document.getElementById('modal-' + id);
            if (m) m.classList.remove('hidden');
        }

        function closeBookModal(id) {
            const m = document.getElementById('modal-' + id);
            if (m) m.classList.add('hidden');
        }
    </script>
</body>
</html>