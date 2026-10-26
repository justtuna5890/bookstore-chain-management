<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:set var="isLoggedIn" value="${pageContext.request.userPrincipal != null}" />
<c:set var="userRole" value="${isLoggedIn ? pageContext.request.userPrincipal.principal.user.role.name() : ''}" />
<c:set var="isStaffOrAdmin" value="${userRole == 'ADMIN' || userRole == 'MANAGER' || userRole == 'STAFF'}" />
<!DOCTYPE html>
<html class="dark" data-bs-theme="dark" lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>OldBook — Sàn Giao Dịch & Khám Phá Sách Cũ Tuyển Chọn</title>
        <!-- Bootstrap 5.3.3 Framework (Dark Theme Native) -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
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

        /* High-priority override: ALL text inputs, textareas, selects must ALWAYS be dark glass - NEVER solid white */
        input[type="text"],
        input[type="search"],
        input[type="email"],
        input[type="password"],
        input[type="tel"],
        input[type="number"],
        textarea,
        select,
        .glass-input {
            background-color: rgba(16, 20, 29, 0.92) !important;
            color: #f8fafc !important;
            border: 1px solid rgba(255, 255, 255, 0.15) !important;
            box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.4) !important;
            backdrop-filter: blur(12px) !important;
            -webkit-backdrop-filter: blur(12px) !important;
        }

        input[type="text"]:focus,
        input[type="search"]:focus,
        input[type="email"]:focus,
        input[type="password"]:focus,
        input[type="tel"]:focus,
        input[type="number"]:focus,
        textarea:focus,
        select:focus,
        .glass-input:focus {
            background-color: rgba(20, 25, 38, 0.98) !important;
            color: #ffffff !important;
            border-color: rgba(212, 175, 55, 0.7) !important;
            box-shadow: 0 0 0 3px rgba(212, 175, 55, 0.25), inset 0 2px 4px rgba(0, 0, 0, 0.4) !important;
            outline: none !important;
        }

        input::placeholder,
        textarea::placeholder,
        .glass-input::placeholder {
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
                <div class="flex items-center gap-3 w-full sm:w-80 lg:w-80 xl:w-96 shrink-0">
                    <form action="/" method="get" class="relative w-full group">
                        <span class="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none text-slate-400 z-10">
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                <path d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" stroke-linecap="round" stroke-linejoin="round"></path>
                            </svg>
                        </span>
                        <input name="keyword" value="${selectedKeyword}" autocomplete="off" class="glass-input w-full pl-10 pr-12 py-2 text-xs sm:text-sm rounded-full text-white placeholder-slate-400 focus:outline-none transition-all shadow-inner" placeholder="Tìm kiếm sách, tác giả, tiệm sách..." type="text"/>
                        <span class="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none z-10">
                            <kbd class="px-1.5 py-0.5 text-[10px] bg-white/10 text-slate-300 rounded font-mono border border-white/10">⌘K</kbd>
                        </span>
                    </form>
                </div>

                <!-- Category Pills (Center Tabs) -->
                <nav class="flex-1 min-w-0 flex items-center gap-1.5 sm:gap-2 overflow-x-auto py-1 no-scrollbar text-xs sm:text-sm font-medium" data-purpose="category-tabs">
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
                    <!-- Cart Icon -->
                    <a href="/cart" class="relative p-2 rounded-full glass-panel-subtle hover:bg-white/10 transition text-slate-300" title="Giỏ hàng">
                        <i class="fa-solid fa-cart-shopping text-sm"></i>
                        <c:if test="${cartCount > 0}">
                            <span class="absolute -top-1 -right-1 min-w-[18px] h-[18px] px-1 rounded-full bg-brand-gold text-slate-950 text-[10px] font-bold flex items-center justify-center">${cartCount}</span>
                        </c:if>
                    </a>
                    <c:if test="${isLoggedIn}">
                        <a href="/chat" class="relative p-2 rounded-full glass-panel-subtle hover:bg-white/10 transition text-slate-300" title="Chat với chi nhánh">
                            <i class="fa-regular fa-comments text-sm"></i>
                        </a>
                        <a href="/wishlist" class="relative p-2 rounded-full glass-panel-subtle hover:bg-white/10 transition text-slate-300" title="Sách yêu thích">
                            <i class="fa-regular fa-heart text-sm"></i>
                        </a>
                        <a href="/orders" class="relative p-2 rounded-full glass-panel-subtle hover:bg-white/10 transition text-slate-300" title="Đơn hàng của tôi">
                            <i class="fa-solid fa-receipt text-sm"></i>
                        </a>
                    </c:if>
                    <!-- Notification Icon -->
                    <c:if test="${isLoggedIn}">
                        <jsp:include page="/WEB-INF/views/customer/common/notification-bell.jsp"/>
                    </c:if>
                    <c:if test="${!isLoggedIn}">
                    <a href="/#stores" class="relative p-2 rounded-full glass-panel-subtle hover:bg-white/10 transition text-slate-300" title="Chi nhánh hoạt động">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                            <path d="M14.857 17.082a23.848 23.848 0 005.454-1.31A8.967 8.967 0 0118 9.75v-.7V9A6 6 0 006 9v.75a8.967 8.967 0 01-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.255 24.255 0 01-5.714 0m5.714 0a3 3 0 11-5.714 0" stroke-linecap="round" stroke-linejoin="round"></path>
                        </svg>
                        <span class="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-emerald-400 ring-2 ring-black"></span>
                    </a>
                    </c:if>

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
                        <a href="/account" title="Tài khoản của tôi" class="flex items-center gap-2.5 pl-2 pr-3 py-1 rounded-full glass-panel-subtle border border-white/10 hover:border-brand-gold/40 transition">
                            <div class="w-7 h-7 rounded-full bg-gradient-to-tr from-amber-600 via-brand-gold to-yellow-200 p-[1px] flex items-center justify-center">
                                <div class="w-full h-full rounded-full bg-[#1e2330] flex items-center justify-center text-xs font-bold text-amber-200">
                                    ${fn:escapeXml(pageContext.request.userPrincipal.principal.username.substring(0, 1).toUpperCase())}
                                </div>
                            </div>
                            <div class="hidden sm:flex flex-col text-left">
                                <span class="text-xs font-semibold text-slate-200 leading-tight">
                                    ${fn:escapeXml(pageContext.request.userPrincipal.principal.user.fullName)}
                                </span>
                                <span class="text-[10px] text-brand-goldLight leading-none">
                                    ${pageContext.request.userPrincipal.principal.user.role}
                                </span>
                            </div>
                        </a>
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

            <c:set var="isSearching" value="${not empty fn:trim(selectedKeyword)}" />

            <!-- BEGIN: ContentGrid -->
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 mt-6">
                <!-- ======================= LEFT COLUMN (4 Cols) ======================= -->
                <c:if test="${not isSearching}">
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
                                <article class="group p-2 rounded-xl hover:bg-white/5 transition border border-transparent hover:border-white/5 flex items-center justify-between cursor-pointer" onclick="location.href='/books/${item.id}'">
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
                                    <c:set var="isItemWishlisted" value="${wishlistedIds.contains(item.id)}"/>
                                    <button type="button" data-wishlist-toggle="${item.id}" data-wishlisted="${isItemWishlisted}" aria-pressed="${isItemWishlisted}" title="Yêu thích"
                                            class="${isItemWishlisted ? 'text-rose-400' : 'text-slate-400'} hover:text-rose-300 p-1.5 transition shrink-0">
                                        <i class="${isItemWishlisted ? 'fa-solid' : 'fa-regular'} fa-heart text-sm"></i>
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
                </c:if>
                <!-- ======================= END LEFT COLUMN ======================= -->

                <!-- ======================= RIGHT COLUMN: Hero/Search + Recommendation ======================= -->
                <div class="${isSearching ? 'lg:col-span-12' : 'lg:col-span-8 xl:col-span-9'} flex flex-col gap-6" data-purpose="right-stage">
                    <c:choose>
                        <c:when test="${isSearching}">
                            <!-- BEGIN: SearchResultsHeader (Banner hidden when searching) -->
                            <section class="glass-panel-subtle p-5 sm:p-6 rounded-2xl border border-white/10 flex flex-col sm:flex-row sm:items-center justify-between gap-4 shadow-lg" data-purpose="search-header">
                                <div class="flex items-start sm:items-center gap-3.5">
                                    <div class="w-12 h-12 rounded-xl bg-brand-gold/15 border border-brand-gold/30 flex items-center justify-center shrink-0 text-brand-gold text-lg shadow-[0_0_20px_rgba(212,175,55,0.2)]">
                                        <svg class="w-5 h-5 text-brand-gold" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                            <path d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" stroke-linecap="round" stroke-linejoin="round"></path>
                                        </svg>
                                    </div>
                                    <div>
                                        <div class="flex items-center gap-2 text-[11px] text-brand-gold uppercase tracking-wider font-semibold">
                                            <span>Kết quả tìm kiếm sách</span>
                                            <c:if test="${not empty selectedCategory}">
                                                <span class="text-slate-500">•</span>
                                                <span class="text-slate-300">Thể loại: ${selectedCategory}</span>
                                            </c:if>
                                        </div>
                                        <h1 class="text-lg sm:text-2xl font-bold text-white mt-0.5 tracking-tight font-serif">
                                            Từ khóa: "<span class="text-brand-goldLight">${fn:escapeXml(selectedKeyword)}</span>"
                                        </h1>
                                        <p class="text-xs text-slate-400 mt-0.5">
                                            Tìm thấy <strong class="text-emerald-400 font-semibold">${catalog.size()}</strong> tác phẩm phù hợp trong toàn hệ thống OldBook
                                        </p>
                                    </div>
                                </div>
                                <a href="/" class="self-start sm:self-center px-4 py-2.5 rounded-xl bg-white/10 hover:bg-white/15 text-slate-200 hover:text-white text-xs font-semibold border border-white/10 hover:border-brand-gold/40 transition flex items-center gap-2 shrink-0">
                                    <svg class="w-3.5 h-3.5 text-slate-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                        <path d="M6 18L18 6M6 6l12 12" stroke-linecap="round" stroke-linejoin="round"></path>
                                    </svg>
                                    <span>Xóa tìm kiếm / Về trang chủ</span>
                                </a>
                            </section>
                            <!-- END: SearchResultsHeader -->
                        </c:when>
                        <c:otherwise>
                            <!-- BEGIN: Multi-Book Carousel Hero Banner -->
                            <section id="heroBannerCarousel" class="relative rounded-2xl overflow-hidden glass-panel-subtle border border-white/10 min-h-[500px] md:min-h-[460px] lg:min-h-[440px] group shadow-xl" data-purpose="hero-carousel">
                                <!-- Slides Track -->
                                <div class="relative w-full h-full min-h-[500px] md:min-h-[460px] lg:min-h-[440px]">

                                    <!-- SLIDE 1: The Starless Archive (Editor's Pick) -->
                                    <div class="hero-slide absolute inset-0 w-full h-full px-8 sm:px-14 lg:px-16 py-7 sm:py-8 pb-14 flex flex-col lg:flex-row items-center justify-between gap-6 lg:gap-8 bg-gradient-to-r from-black/95 via-[#10151f]/90 to-black/95 transition-opacity duration-700 ease-in-out opacity-100 z-10 pointer-events-auto" data-slide-index="0">
                                        <div class="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-amber-500/15 via-blue-900/15 to-transparent pointer-events-none"></div>

                                        <!-- Left Details -->
                                        <div class="relative z-10 flex-1 max-w-xl pr-0 lg:pr-2">
                                            <div class="flex flex-wrap items-center gap-2 mb-2.5">
                                                <span class="px-2.5 py-0.5 text-[11px] font-medium tracking-wide rounded-full bg-brand-gold/20 text-brand-goldLight border border-brand-gold/40">
                                                    ★ Đề Xuất Biên Tập Viên
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

                                            <h2 class="text-2xl sm:text-3xl lg:text-4xl font-serif font-bold text-white tracking-tight leading-tight line-clamp-2">
                                                The Starless Archive
                                            </h2>
                                            <div class="flex items-center gap-2 text-xs md:text-sm text-slate-300 mt-1 mb-2.5">
                                                <span class="font-medium text-brand-goldLight">Tác giả: Elian Voss</span>
                                                <span class="text-slate-600">•</span>
                                                <span class="text-slate-400">Bản in First Edition (1984) — Arkham House</span>
                                            </div>

                                            <p class="text-xs md:text-sm text-slate-300 leading-relaxed font-light line-clamp-2 sm:line-clamp-3 mb-4 sm:mb-5">
                                                "Bên dưới thư viện chìm ngàn năm, người lưu trữ già khám phá ra nơi cất giữ những ký ức từng bị thời gian xóa bỏ khỏi bầu trời sao... Một kiệt tác kỳ ảo đen tối hiếm có vừa cập bến OldBook từ tiệm sưu tầm Tràng Tiền."
                                            </p>

                                            <div class="flex flex-wrap items-center gap-3 sm:gap-4 py-2 px-3 sm:py-2.5 sm:px-3.5 rounded-xl bg-black/50 border border-white/10 mb-4 sm:mb-5 backdrop-blur-md">
                                                <div>
                                                    <div class="flex items-baseline gap-2">
                                                        <span class="text-xl font-bold text-brand-gold">245.000₫</span>
                                                        <span class="text-xs text-slate-400 line-through">490.000₫</span>
                                                        <span class="text-[10px] font-semibold text-rose-400 bg-rose-500/15 px-1.5 py-0.5 rounded">-50%</span>
                                                    </div>
                                                    <span class="text-[10px] text-amber-200/70">Tình trạng: Giữ nguyên bìa áo zin, giấy ngả vàng đẹp</span>
                                                </div>
                                                <div class="h-6 w-[1px] bg-white/10 hidden sm:block"></div>
                                                <div class="text-[11px] text-slate-300 flex items-center gap-1.5">
                                                    <span class="w-2 h-2 rounded-full bg-emerald-400 shrink-0"></span>
                                                    <span>Người bán: <strong class="text-white">Hệ Thống OldBook</strong> (4.9★ • Đã kiểm định)</span>
                                                </div>
                                            </div>

                                            <div class="flex flex-wrap items-center gap-2.5">
                                                <a href="/#catalog" class="px-5 py-2.5 rounded-full bg-gradient-to-r from-brand-gold to-yellow-500 hover:from-yellow-400 hover:to-amber-500 text-slate-950 font-bold text-xs sm:text-sm shadow-glow-gold transition flex items-center gap-2">
                                                    <i class="fa-solid fa-book-open"></i>
                                                    Khám Phá Toàn Kho Sách
                                                </a>
                                                <a href="/#buyback" class="px-4 py-2.5 rounded-full bg-white/10 hover:bg-white/15 text-white font-medium text-xs sm:text-sm border border-white/15 transition flex items-center gap-1.5">
                                                    <i class="fa-solid fa-arrow-right-arrow-left text-slate-300 text-xs"></i>
                                                    Gửi Thu Mua Sách Cũ
                                                </a>
                                            </div>
                                        </div>

                                        <!-- Right 3D Book Visual -->
                                        <div class="relative z-10 shrink-0 flex flex-col items-center">
                                            <div class="relative w-36 sm:w-44 lg:w-48 h-56 sm:h-68 lg:h-72 rounded-r-lg rounded-l-[4px] bg-[#11141c] hero-book-spine border-t border-r border-b border-white/20 p-3 sm:p-4 flex flex-col justify-between overflow-hidden transition transform hover:-translate-y-1.5 duration-300 shadow-2xl">
                                                <div class="absolute inset-0 bg-gradient-to-tr from-black via-[#161a24] to-[#252c3c] opacity-95"></div>
                                                <div class="absolute inset-2 border border-brand-gold/30 rounded pointer-events-none"></div>
                                                <div class="absolute inset-3 border border-brand-gold/15 rounded pointer-events-none"></div>

                                                <div class="relative z-10 text-center pt-1.5">
                                                    <span class="text-[8px] sm:text-[9px] tracking-[0.25em] text-brand-gold uppercase block font-serif">THE</span>
                                                    <h3 class="text-lg sm:text-xl font-serif font-black tracking-widest text-transparent bg-clip-text bg-gradient-to-b from-brand-goldLight via-brand-gold to-yellow-600 uppercase mt-0.5">
                                                        STARLESS
                                                    </h3>
                                                    <h4 class="text-[11px] sm:text-xs font-serif tracking-[0.3em] text-slate-300 uppercase -mt-1 font-semibold">
                                                        ARCHIVE
                                                    </h4>
                                                </div>

                                                <div class="relative z-10 my-auto flex items-center justify-center">
                                                    <div class="w-14 h-14 sm:w-16 sm:h-16 rounded-full border border-brand-gold/40 flex items-center justify-center relative shadow-[0_0_20px_rgba(212,175,55,0.2)]">
                                                        <div class="w-10 h-10 sm:w-12 sm:h-12 rounded-full border border-dashed border-brand-gold/60 flex items-center justify-center animate-spin-slow">
                                                            <div class="w-6 h-6 sm:w-7 sm:h-7 rounded-full bg-gradient-to-br from-amber-400/20 to-black border border-brand-gold/80 flex items-center justify-center">
                                                                <div class="w-2 h-2 rounded-full bg-brand-gold shadow-[0_0_12px_#D4AF37]"></div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>

                                                <div class="relative z-10 text-center pb-1.5">
                                                    <p class="text-[9px] sm:text-[10px] tracking-[0.2em] font-serif uppercase text-brand-goldLight">ELIAN VOSS</p>
                                                    <span class="text-[7px] tracking-widest text-slate-500 uppercase block mt-0.5">FIRST EDITION • 1984</span>
                                                </div>
                                            </div>
                                            <span class="text-[10px] text-brand-goldLight/80 mt-2 font-mono">Bản sưu tầm số #01/01</span>
                                        </div>
                                    </div>

                                    <!-- SLIDE 2: Hoàng Tử Bé (ID: 1) -->
                                    <div class="hero-slide absolute inset-0 w-full h-full px-8 sm:px-14 lg:px-16 py-7 sm:py-8 pb-14 flex flex-col lg:flex-row items-center justify-between gap-6 lg:gap-8 bg-gradient-to-r from-black/95 via-[#161224]/90 to-black/95 transition-opacity duration-700 ease-in-out opacity-0 z-0 pointer-events-none" data-slide-index="1">
                                        <div class="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-indigo-500/15 via-purple-900/15 to-transparent pointer-events-none"></div>

                                        <!-- Left Details -->
                                        <div class="relative z-10 flex-1 max-w-xl pr-0 lg:pr-2">
                                            <div class="flex flex-wrap items-center gap-2 mb-2.5">
                                                <span class="px-2.5 py-0.5 text-[11px] font-medium tracking-wide rounded-full bg-indigo-500/20 text-indigo-200 border border-indigo-500/40">
                                                    ★ Kiệt Tác Bất Hủ
                                                </span>
                                                <span class="px-2.5 py-0.5 text-[11px] font-medium rounded-full bg-white/10 text-slate-300">
                                                    Văn Học Cổ Điển
                                                </span>
                                                <span class="px-2.5 py-0.5 text-[11px] font-medium rounded-full bg-white/10 text-slate-300">
                                                    Triết Lý Nhân Gian
                                                </span>
                                                <span class="px-2 py-0.5 text-[11px] font-medium rounded-full bg-emerald-500/15 text-emerald-300 border border-emerald-500/30">
                                                    Mới 95%
                                                </span>
                                            </div>

                                            <h2 class="text-2xl sm:text-3xl lg:text-4xl font-serif font-bold text-white tracking-tight leading-tight line-clamp-2">
                                                Hoàng Tử Bé
                                            </h2>
                                            <div class="flex items-center gap-2 text-xs md:text-sm text-slate-300 mt-1 mb-2.5">
                                                <span class="font-medium text-brand-goldLight">Tác giả: Antoine de Saint-Exupéry</span>
                                                <span class="text-slate-600">•</span>
                                                <span class="text-slate-400">Ấn bản tái bản mỹ thuật (2021) — NXB Văn Học</span>
                                            </div>

                                            <p class="text-xs md:text-sm text-slate-300 leading-relaxed font-light line-clamp-2 sm:line-clamp-3 mb-4 sm:mb-5">
                                                "Người ta chỉ nhìn thấy thật rõ ràng bằng trái tim. Cái cốt tử thì mắt thường không thể nhìn thấy được... Một tác phẩm triết lý nhẹ nhàng, sâu lắng về tình yêu, sự cô đơn và cái nhìn trong trẻo của trẻ thơ về thế giới người lớn."
                                            </p>

                                            <div class="flex flex-wrap items-center gap-3 sm:gap-4 py-2 px-3 sm:py-2.5 sm:px-3.5 rounded-xl bg-black/50 border border-white/10 mb-4 sm:mb-5 backdrop-blur-md">
                                                <div>
                                                    <div class="flex items-baseline gap-2">
                                                        <span class="text-xl font-bold text-brand-gold">68.000₫</span>
                                                        <span class="text-xs text-slate-400 line-through">85.000₫</span>
                                                        <span class="text-[10px] font-semibold text-rose-400 bg-rose-500/15 px-1.5 py-0.5 rounded">-20%</span>
                                                    </div>
                                                    <span class="text-[10px] text-amber-200/70">Tình trạng: Bìa áo còn nguyên vẹn, gáy cứng cáp, giấy sạch đẹp</span>
                                                </div>
                                                <div class="h-6 w-[1px] bg-white/10 hidden sm:block"></div>
                                                <div class="text-[11px] text-slate-300 flex items-center gap-1.5">
                                                    <span class="w-2 h-2 rounded-full bg-emerald-400 shrink-0"></span>
                                                    <span>Chi nhánh: <strong class="text-white">Nguyễn Huệ (TP.HCM)</strong> (5.0★ • Sẵn 2 bản)</span>
                                                </div>
                                            </div>

                                            <div class="flex flex-wrap items-center gap-2.5">
                                                <a href="/books/1" class="px-5 py-2.5 rounded-full bg-gradient-to-r from-brand-gold to-yellow-500 hover:from-yellow-400 hover:to-amber-500 text-slate-950 font-bold text-xs sm:text-sm shadow-glow-gold transition flex items-center gap-2">
                                                    <i class="fa-solid fa-book-open"></i>
                                                    Xem Chi Tiết Tác Phẩm
                                                </a>
                                                <a href="/books/1" class="px-4 py-2.5 rounded-full bg-white/10 hover:bg-white/15 text-white font-medium text-xs sm:text-sm border border-white/15 transition flex items-center gap-1.5">
                                                    <i class="fa-regular fa-eye text-slate-300 text-xs"></i>
                                                    Xem Chi Tiết Sách
                                                </a>
                                            </div>
                                        </div>

                                        <!-- Right 3D Book Visual -->
                                        <div class="relative z-10 shrink-0 flex flex-col items-center">
                                            <a href="/books/1" class="relative w-36 sm:w-44 lg:w-48 h-56 sm:h-68 lg:h-72 rounded-r-lg rounded-l-[4px] overflow-hidden hero-book-spine border-t border-r border-b border-white/20 block transition transform hover:-translate-y-1.5 duration-300 group/book shadow-2xl">
                                                <img src="https://images.unsplash.com/photo-1544947950-fa07a98d237f?q=80&w=600&auto=format&fit=crop" class="w-full h-full object-cover transition transform group-hover/book:scale-105 duration-300" alt="Hoàng Tử Bé"/>
                                                <div class="absolute inset-0 bg-gradient-to-t from-black/90 via-black/40 to-transparent flex flex-col justify-between p-3">
                                                    <div class="flex justify-between items-start">
                                                        <span class="px-2 py-0.5 rounded-full bg-black/60 backdrop-blur-md text-[9px] text-amber-300 font-serif border border-amber-400/30">★ 5.0 Rating</span>
                                                        <span class="w-5 h-5 rounded-full bg-brand-gold/20 flex items-center justify-center text-brand-gold text-xs">✦</span>
                                                    </div>
                                                    <div>
                                                        <h3 class="text-xs sm:text-sm font-serif font-bold text-white tracking-wide uppercase line-clamp-1">Hoàng Tử Bé</h3>
                                                        <p class="text-[9px] sm:text-[10px] text-amber-200/90 truncate font-light">Antoine de Saint-Exupéry</p>
                                                    </div>
                                                </div>
                                            </a>
                                            <span class="text-[10px] text-indigo-300/80 mt-2 font-mono">Bấm vào bìa để xem chi tiết</span>
                                        </div>
                                    </div>

                                    <!-- SLIDE 3: Nhà Giả Kim (ID: 2) -->
                                    <div class="hero-slide absolute inset-0 w-full h-full px-8 sm:px-14 lg:px-16 py-7 sm:py-8 pb-14 flex flex-col lg:flex-row items-center justify-between gap-6 lg:gap-8 bg-gradient-to-r from-black/95 via-[#21160e]/90 to-black/95 transition-opacity duration-700 ease-in-out opacity-0 z-0 pointer-events-none" data-slide-index="2">
                                        <div class="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-amber-600/15 via-orange-950/20 to-transparent pointer-events-none"></div>

                                        <!-- Left Details -->
                                        <div class="relative z-10 flex-1 max-w-xl pr-0 lg:pr-2">
                                            <div class="flex flex-wrap items-center gap-2 mb-2.5">
                                                <span class="px-2.5 py-0.5 text-[11px] font-medium tracking-wide rounded-full bg-amber-500/20 text-amber-200 border border-amber-500/40">
                                                    🔥 Bestseller Thế Giới
                                                </span>
                                                <span class="px-2.5 py-0.5 text-[11px] font-medium rounded-full bg-white/10 text-slate-300">
                                                    Tiểu Thuyết Triết Lý
                                                </span>
                                                <span class="px-2.5 py-0.5 text-[11px] font-medium rounded-full bg-white/10 text-slate-300">
                                                    Cảm Hứng Sống
                                                </span>
                                                <span class="px-2 py-0.5 text-[11px] font-medium rounded-full bg-emerald-500/15 text-emerald-300 border border-emerald-500/30">
                                                    Mới 90%
                                                </span>
                                            </div>

                                            <h2 class="text-2xl sm:text-3xl lg:text-4xl font-serif font-bold text-white tracking-tight leading-tight line-clamp-2">
                                                Nhà Giả Kim
                                            </h2>
                                            <div class="flex items-center gap-2 text-xs md:text-sm text-slate-300 mt-1 mb-2.5">
                                                <span class="font-medium text-brand-goldLight">Tác giả: Paulo Coelho</span>
                                                <span class="text-slate-600">•</span>
                                                <span class="text-slate-400">Ấn bản kỷ niệm (2020) — NXB Hội Nhà Văn</span>
                                            </div>

                                            <p class="text-xs md:text-sm text-slate-300 leading-relaxed font-light line-clamp-2 sm:line-clamp-3 mb-4 sm:mb-5">
                                                "Khi bạn thực sự khao khát một điều gì, toàn bộ vũ trụ sẽ hợp sức giúp bạn đạt được điều đó... Hành trình đi tìm kho báu của chàng chăn cừu Santiago dạy chúng ta về việc lắng nghe tiếng gọi trái tim và theo đuổi Ước mơ cuộc đời."
                                            </p>

                                            <div class="flex flex-wrap items-center gap-3 sm:gap-4 py-2 px-3 sm:py-2.5 sm:px-3.5 rounded-xl bg-black/50 border border-white/10 mb-4 sm:mb-5 backdrop-blur-md">
                                                <div>
                                                    <div class="flex items-baseline gap-2">
                                                        <span class="text-xl font-bold text-brand-gold">59.000₫</span>
                                                        <span class="text-xs text-slate-400 line-through">79.000₫</span>
                                                        <span class="text-[10px] font-semibold text-rose-400 bg-rose-500/15 px-1.5 py-0.5 rounded">-25%</span>
                                                    </div>
                                                    <span class="text-[10px] text-amber-200/70">Tình trạng: Giấy vàng đều đẹp, không rách, đã thẩm định</span>
                                                </div>
                                                <div class="h-6 w-[1px] bg-white/10 hidden sm:block"></div>
                                                <div class="text-[11px] text-slate-300 flex items-center gap-1.5">
                                                    <span class="w-2 h-2 rounded-full bg-emerald-400 shrink-0"></span>
                                                    <span>Chi nhánh: <strong class="text-white">Cầu Giấy (Hà Nội)</strong> (4.9★ • Sẵn 2 bản)</span>
                                                </div>
                                            </div>

                                            <div class="flex flex-wrap items-center gap-2.5">
                                                <a href="/books/2" class="px-5 py-2.5 rounded-full bg-gradient-to-r from-brand-gold to-yellow-500 hover:from-yellow-400 hover:to-amber-500 text-slate-950 font-bold text-xs sm:text-sm shadow-glow-gold transition flex items-center gap-2">
                                                    <i class="fa-solid fa-book-open"></i>
                                                    Xem Chi Tiết Tác Phẩm
                                                </a>
                                                <a href="/books/2" class="px-4 py-2.5 rounded-full bg-white/10 hover:bg-white/15 text-white font-medium text-xs sm:text-sm border border-white/15 transition flex items-center gap-1.5">
                                                    <i class="fa-regular fa-eye text-slate-300 text-xs"></i>
                                                    Xem Chi Tiết Sách
                                                </a>
                                            </div>
                                        </div>

                                        <!-- Right 3D Book Visual -->
                                        <div class="relative z-10 shrink-0 flex flex-col items-center">
                                            <a href="/books/2" class="relative w-36 sm:w-44 lg:w-48 h-56 sm:h-68 lg:h-72 rounded-r-lg rounded-l-[4px] overflow-hidden hero-book-spine border-t border-r border-b border-white/20 block transition transform hover:-translate-y-1.5 duration-300 group/book shadow-2xl">
                                                <img src="https://images.unsplash.com/photo-1512820790803-83ca734da794?q=80&w=600&auto=format&fit=crop" class="w-full h-full object-cover transition transform group-hover/book:scale-105 duration-300" alt="Nhà Giả Kim"/>
                                                <div class="absolute inset-0 bg-gradient-to-t from-black/90 via-black/40 to-transparent flex flex-col justify-between p-3">
                                                    <div class="flex justify-between items-start">
                                                        <span class="px-2 py-0.5 rounded-full bg-black/60 backdrop-blur-md text-[9px] text-amber-300 font-serif border border-amber-400/30">★ 4.9 Rating</span>
                                                        <span class="w-5 h-5 rounded-full bg-brand-gold/20 flex items-center justify-center text-brand-gold text-xs">☀️</span>
                                                    </div>
                                                    <div>
                                                        <h3 class="text-xs sm:text-sm font-serif font-bold text-white tracking-wide uppercase line-clamp-1">Nhà Giả Kim</h3>
                                                        <p class="text-[9px] sm:text-[10px] text-amber-200/90 truncate font-light">Paulo Coelho</p>
                                                    </div>
                                                </div>
                                            </a>
                                            <span class="text-[10px] text-amber-300/80 mt-2 font-mono">Bấm vào bìa để xem chi tiết</span>
                                        </div>
                                    </div>

                                    <!-- SLIDE 4: Sapiens: Lược Sử Loài Người (ID: 7) -->
                                    <div class="hero-slide absolute inset-0 w-full h-full px-8 sm:px-14 lg:px-16 py-7 sm:py-8 pb-14 flex flex-col lg:flex-row items-center justify-between gap-6 lg:gap-8 bg-gradient-to-r from-black/95 via-[#0e171f]/90 to-black/95 transition-opacity duration-700 ease-in-out opacity-0 z-0 pointer-events-none" data-slide-index="3">
                                        <div class="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-teal-500/15 via-emerald-950/20 to-transparent pointer-events-none"></div>

                                        <!-- Left Details -->
                                        <div class="relative z-10 flex-1 max-w-xl pr-0 lg:pr-2">
                                            <div class="flex flex-wrap items-center gap-2 mb-2.5">
                                                <span class="px-2.5 py-0.5 text-[11px] font-medium tracking-wide rounded-full bg-emerald-500/20 text-emerald-200 border border-emerald-500/40">
                                                    💎 Đỉnh Cao Tri Thức
                                                </span>
                                                <span class="px-2.5 py-0.5 text-[11px] font-medium rounded-full bg-white/10 text-slate-300">
                                                    Lịch Sử - Triết Học
                                                </span>
                                                <span class="px-2.5 py-0.5 text-[11px] font-medium rounded-full bg-white/10 text-slate-300">
                                                    Khuyên Đọc
                                                </span>
                                                <span class="px-2 py-0.5 text-[11px] font-medium rounded-full bg-emerald-500/15 text-emerald-300 border border-emerald-500/30">
                                                    Like New 98%
                                                </span>
                                            </div>

                                            <h2 class="text-2xl sm:text-3xl lg:text-4xl font-serif font-bold text-white tracking-tight leading-tight line-clamp-2">
                                                Sapiens: Lược Sử Loài Người
                                            </h2>
                                            <div class="flex items-center gap-2 text-xs md:text-sm text-slate-300 mt-1 mb-2.5">
                                                <span class="font-medium text-brand-goldLight">Tác giả: Yuval Noah Harari</span>
                                                <span class="text-slate-600">•</span>
                                                <span class="text-slate-400">Bản in bìa cứng tuyển chọn (2021) — NXB Tri Thức</span>
                                            </div>

                                            <p class="text-xs md:text-sm text-slate-300 leading-relaxed font-light line-clamp-2 sm:line-clamp-3 mb-4 sm:mb-5">
                                                "Một cuốn sách phi thường quét qua 70.000 năm lịch sử loài người, từ một sinh vật vô danh ở góc đông Phi trở thành kẻ thống trị Trái đất... Khám phá cách ngôn ngữ, tiền tệ và niềm tin tôn giáo định hình thế giới hiện đại."
                                            </p>

                                            <div class="flex flex-wrap items-center gap-3 sm:gap-4 py-2 px-3 sm:py-2.5 sm:px-3.5 rounded-xl bg-black/50 border border-white/10 mb-4 sm:mb-5 backdrop-blur-md">
                                                <div>
                                                    <div class="flex items-baseline gap-2">
                                                        <span class="text-xl font-bold text-brand-gold">165.000₫</span>
                                                        <span class="text-xs text-slate-400 line-through">220.000₫</span>
                                                        <span class="text-[10px] font-semibold text-rose-400 bg-rose-500/15 px-1.5 py-0.5 rounded">-25%</span>
                                                    </div>
                                                    <span class="text-[10px] text-amber-200/70">Tình trạng: Sách gần như mới tinh, nguyên bìa áo, giấy trắng đẹp</span>
                                                </div>
                                                <div class="h-6 w-[1px] bg-white/10 hidden sm:block"></div>
                                                <div class="text-[11px] text-slate-300 flex items-center gap-1.5">
                                                    <span class="w-2 h-2 rounded-full bg-emerald-400 shrink-0"></span>
                                                    <span>Chi nhánh: <strong class="text-white">Tràng Tiền (Hà Nội)</strong> (4.9★ • Sẵn 2 bản)</span>
                                                </div>
                                            </div>

                                            <div class="flex flex-wrap items-center gap-2.5">
                                                <a href="/books/7" class="px-5 py-2.5 rounded-full bg-gradient-to-r from-brand-gold to-yellow-500 hover:from-yellow-400 hover:to-amber-500 text-slate-950 font-bold text-xs sm:text-sm shadow-glow-gold transition flex items-center gap-2">
                                                    <i class="fa-solid fa-book-open"></i>
                                                    Xem Chi Tiết Tác Phẩm
                                                </a>
                                                <a href="/books/7" class="px-4 py-2.5 rounded-full bg-white/10 hover:bg-white/15 text-white font-medium text-xs sm:text-sm border border-white/15 transition flex items-center gap-1.5">
                                                    <i class="fa-regular fa-eye text-slate-300 text-xs"></i>
                                                    Xem Chi Tiết Sách
                                                </a>
                                            </div>
                                        </div>

                                        <!-- Right 3D Book Visual -->
                                        <div class="relative z-10 shrink-0 flex flex-col items-center">
                                            <a href="/books/7" class="relative w-36 sm:w-44 lg:w-48 h-56 sm:h-68 lg:h-72 rounded-r-lg rounded-l-[4px] overflow-hidden hero-book-spine border-t border-r border-b border-white/20 block transition transform hover:-translate-y-1.5 duration-300 group/book shadow-2xl">
                                                <img src="https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=600&auto=format&fit=crop" class="w-full h-full object-cover transition transform group-hover/book:scale-105 duration-300" alt="Sapiens"/>
                                                <div class="absolute inset-0 bg-gradient-to-t from-black/90 via-black/40 to-transparent flex flex-col justify-between p-3">
                                                    <div class="flex justify-between items-start">
                                                        <span class="px-2 py-0.5 rounded-full bg-black/60 backdrop-blur-md text-[9px] text-emerald-300 font-serif border border-emerald-400/30">★ 4.9 Rating</span>
                                                        <span class="w-5 h-5 rounded-full bg-emerald-400/20 flex items-center justify-center text-emerald-300 text-xs">🌿</span>
                                                    </div>
                                                    <div>
                                                        <h3 class="text-xs sm:text-sm font-serif font-bold text-white tracking-wide uppercase line-clamp-1">Sapiens</h3>
                                                        <p class="text-[9px] sm:text-[10px] text-emerald-200/90 truncate font-light">Yuval Noah Harari</p>
                                                    </div>
                                                </div>
                                            </a>
                                            <span class="text-[10px] text-teal-300/80 mt-2 font-mono">Bấm vào bìa để xem chi tiết</span>
                                        </div>
                                    </div>

                                </div>
                                <!-- END: Slides Track -->

                                <!-- Prev/Next Navigation Controls -->
                                <button type="button" onclick="prevHeroSlide()" aria-label="Đề xuất trước" class="absolute left-2.5 sm:left-3 top-1/2 -translate-y-1/2 z-20 w-10 h-10 rounded-full bg-black/70 hover:bg-brand-gold hover:text-slate-950 text-white border border-white/20 backdrop-blur-md flex items-center justify-center transition-all opacity-80 sm:opacity-0 group-hover:opacity-100 shadow-2xl focus:opacity-100 focus:outline-none">
                                    <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                        <path d="M15.75 19.5L8.25 12l7.5-7.5" stroke-linecap="round" stroke-linejoin="round"></path>
                                    </svg>
                                </button>
                                <button type="button" onclick="nextHeroSlide()" aria-label="Đề xuất tiếp theo" class="absolute right-2.5 sm:right-3 top-1/2 -translate-y-1/2 z-20 w-10 h-10 rounded-full bg-black/70 hover:bg-brand-gold hover:text-slate-950 text-white border border-white/20 backdrop-blur-md flex items-center justify-center transition-all opacity-80 sm:opacity-0 group-hover:opacity-100 shadow-2xl focus:opacity-100 focus:outline-none">
                                    <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                        <path d="M8.25 4.5l7.5 7.5-7.5 7.5" stroke-linecap="round" stroke-linejoin="round"></path>
                                    </svg>
                                </button>

                                <!-- Dots Indicators -->
                                <div class="absolute bottom-4 left-1/2 -translate-x-1/2 z-20 flex items-center gap-2 bg-black/60 px-3.5 py-1.5 rounded-full border border-white/15 backdrop-blur-md shadow-2xl">
                                    <button type="button" onclick="setHeroSlide(0)" class="hero-dot transition-all h-2 rounded-full w-6 bg-brand-gold" aria-label="Đề xuất 1: The Starless Archive"></button>
                                    <button type="button" onclick="setHeroSlide(1)" class="hero-dot transition-all h-2 rounded-full w-2 bg-white/30 hover:bg-white/60" aria-label="Đề xuất 2: Hoàng Tử Bé"></button>
                                    <button type="button" onclick="setHeroSlide(2)" class="hero-dot transition-all h-2 rounded-full w-2 bg-white/30 hover:bg-white/60" aria-label="Đề xuất 3: Nhà Giả Kim"></button>
                                    <button type="button" onclick="setHeroSlide(3)" class="hero-dot transition-all h-2 rounded-full w-2 bg-white/30 hover:bg-white/60" aria-label="Đề xuất 4: Sapiens"></button>
                                </div>
                            </section>
                        </c:otherwise>
                    </c:choose>

                    <!-- BEGIN: RecommendationsSection / Real Catalog Gallery -->
                    <section id="catalog" class="flex flex-col" data-purpose="recommendations-gallery">
                        <div class="flex items-center justify-between mb-4">
                            <div>
                                <h2 class="text-base sm:text-lg font-serif font-semibold text-white tracking-tight">
                                    <c:choose>
                                        <c:when test="${isSearching}">Danh Sách Tác Phẩm Tìm Thấy</c:when>
                                        <c:otherwise>Có thể bạn sẽ thích (Kho Sách Hiện Có)</c:otherwise>
                                    </c:choose>
                                </h2>
                                <p class="text-xs text-slate-400">
                                    <c:choose>
                                        <c:when test="${isSearching}">Khớp theo tên sách, tác giả hoặc nhà xuất bản trong hệ thống OldBook</c:when>
                                        <c:otherwise>Tuyển lựa sách thật từ hệ thống chi nhánh OldBook toàn quốc</c:otherwise>
                                    </c:choose>
                                </p>
                            </div>
                            <span class="text-xs text-brand-goldLight font-medium">
                                ${catalog.size()} tác phẩm sẵn sàng
                            </span>
                        </div>

                        <c:if test="${empty catalog}">
                            <div class="p-12 text-center rounded-2xl glass-panel-subtle border border-white/10 flex flex-col items-center justify-center my-4">
                                <div class="w-16 h-16 rounded-full bg-white/5 border border-white/10 flex items-center justify-center text-slate-400 text-2xl mb-3">
                                    <svg class="w-8 h-8 text-slate-400" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
                                        <path d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25" stroke-linecap="round" stroke-linejoin="round"></path>
                                    </svg>
                                </div>
                                <h3 class="text-base font-semibold text-white mb-1">Không tìm thấy tác phẩm phù hợp</h3>
                                <p class="text-xs text-slate-400 max-w-md mb-4">
                                    Rất tiếc, không có cuốn sách nào khớp với từ khóa "<span class="text-brand-gold font-semibold">${fn:escapeXml(selectedKeyword)}</span>". Bạn hãy thử kiểm tra lại chính tả hoặc tìm kiếm từ khóa ngắn hơn.
                                </p>
                                <div class="flex items-center gap-3">
                                    <a href="/" class="px-4 py-2 rounded-xl bg-brand-gold text-slate-950 font-bold text-xs hover:bg-yellow-400 transition">
                                        Xem Tất Cả Sách
                                    </a>
                                    <a href="/#buyback" class="px-4 py-2 rounded-xl bg-white/10 text-white font-medium text-xs hover:bg-white/15 transition border border-white/10">
                                        Gửi Yêu Cầu Tìm Sách
                                    </a>
                                </div>
                            </div>
                        </c:if>

                        <!-- Dynamic Grid: Full width multi-column when searching, or 5 columns when viewing home -->
                        <div class="grid grid-cols-2 sm:grid-cols-3 ${isSearching ? 'md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6' : 'md:grid-cols-3 xl:grid-cols-5'} gap-3.5">
                            <c:forEach var="book" items="${catalog}">
                                <article class="group rounded-2xl glass-panel-subtle border border-white/10 p-2.5 flex flex-col justify-between hover:border-brand-gold/40 hover:bg-white/5 transition duration-300">
                                    <a href="/books/${book.id}" class="block relative aspect-[3/4.2] w-full rounded-xl overflow-hidden mb-2.5 bg-gradient-to-b from-[#1b120c] to-[#0c0806] book-spine-effect" title="Xem chi tiết">
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
                                    </a>

                                    <!-- Info footer -->
                                    <div>
                                        <h3 class="text-xs font-semibold text-white truncate group-hover:text-brand-gold transition">
                                            <a href="/books/${book.id}" class="block truncate">${book.title}</a>
                                        </h3>
                                        <p class="text-[11px] text-slate-400 truncate">${book.author}</p>
                                        <div class="flex items-center justify-between mt-2 pt-1.5 border-t border-white/5">
                                            <div class="flex items-center gap-1">
                                                <c:choose>
                                                    <c:when test="${book.rating.count > 0}">
                                                        <span class="text-[10px] text-amber-400 font-semibold" title="${book.rating.count} đánh giá">★ ${book.rating.averageLabel}</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-[10px] text-slate-500" title="Chưa có đánh giá">☆ —</span>
                                                    </c:otherwise>
                                                </c:choose>
                                                <span class="text-[11px] font-bold text-emerald-400 ml-1">
                                                    <fmt:formatNumber value="${book.minPrice}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                                                </span>
                                            </div>
                                            <c:set var="isWishlisted" value="${wishlistedIds.contains(book.id)}"/>
                                            <button type="button" data-wishlist-toggle="${book.id}" data-wishlisted="${isWishlisted}" aria-pressed="${isWishlisted}" title="Yêu thích"
                                                    class="${isWishlisted ? 'text-rose-400' : 'text-slate-400'} hover:text-rose-300 transition">
                                                <i class="${isWishlisted ? 'fa-solid' : 'fa-regular'} fa-heart text-sm"></i>
                                            </button>
                                        </div>
                                    </div>
                                </article>

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
                                            <input name="customerName" value="${fn:escapeXml(buybackForm.customerName)}" class="glass-input w-full px-3 py-2 text-xs rounded-xl text-slate-100 placeholder-slate-500" required placeholder="Nguyễn Văn A"/>
                                        </div>
                                        <div>
                                            <label class="block text-[11px] font-medium text-slate-300 mb-1">Email liên hệ (*)</label>
                                            <input name="customerEmail" value="${fn:escapeXml(buybackForm.customerEmail)}" class="glass-input w-full px-3 py-2 text-xs rounded-xl text-slate-100 placeholder-slate-500" required placeholder="email@domain.com"/>
                                        </div>
                                    </div>

                                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                                        <div>
                                            <label class="block text-[11px] font-medium text-slate-300 mb-1">Số điện thoại</label>
                                            <input name="customerPhone" value="${fn:escapeXml(buybackForm.customerPhone)}" class="glass-input w-full px-3 py-2 text-xs rounded-xl text-slate-100 placeholder-slate-500" placeholder="09xxxxxxxx"/>
                                        </div>
                                        <div>
                                            <label class="block text-[11px] font-medium text-slate-300 mb-1">Chi nhánh tiếp nhận (*)</label>
                                            <select name="storeId" class="glass-input w-full px-3 py-2 text-xs rounded-xl text-slate-100 bg-[#161a22]" required>
                                                <option value="">-- Chọn chi nhánh gần bạn --</option>
                                                <c:forEach var="st" items="${stores}">
                                                    <option value="${st.id}" ${buybackForm.storeId == st.id ? 'selected' : ''}>${fn:escapeXml(st.name)}</option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                    </div>

                                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                                        <div>
                                            <label class="block text-[11px] font-medium text-slate-300 mb-1">Tựa sách muốn bán (*)</label>
                                            <input name="bookTitle" value="${fn:escapeXml(buybackForm.bookTitle)}" class="glass-input w-full px-3 py-2 text-xs rounded-xl text-slate-100 placeholder-slate-500" required placeholder="VD: Chiến Tranh và Hòa Bình"/>
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
                                        <textarea name="note" rows="2" class="glass-input w-full px-3 py-2 text-xs rounded-xl text-slate-100 placeholder-slate-500" placeholder="VD: Sách bìa cứng năm 1990, gáy còn nguyên...">${fn:escapeXml(buybackForm.note)}</textarea>
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

    <script src="/js/wishlist.js"></script>
    <script src="/js/notifications.js"></script>
    <script data-purpose="interactive-enhancements">
        // ================= Hero Banner Carousel Controller =================
        let currentHeroSlide = 0;
        let heroSlideTimer = null;
        const heroSlideInterval = 5000;

        function showHeroSlide(index) {
            const slides = document.querySelectorAll('.hero-slide');
            const dots = document.querySelectorAll('.hero-dot');
            if (!slides || slides.length === 0) return;

            if (index >= slides.length) currentHeroSlide = 0;
            else if (index < 0) currentHeroSlide = slides.length - 1;
            else currentHeroSlide = index;

            slides.forEach((slide, idx) => {
                if (idx === currentHeroSlide) {
                    slide.classList.remove('opacity-0', 'pointer-events-none', 'z-0');
                    slide.classList.add('opacity-100', 'pointer-events-auto', 'z-10');
                } else {
                    slide.classList.remove('opacity-100', 'pointer-events-auto', 'z-10');
                    slide.classList.add('opacity-0', 'pointer-events-none', 'z-0');
                }
            });

            dots.forEach((dot, idx) => {
                if (idx === currentHeroSlide) {
                    dot.classList.remove('w-2', 'bg-white/30');
                    dot.classList.add('w-6', 'bg-brand-gold');
                } else {
                    dot.classList.remove('w-6', 'bg-brand-gold');
                    dot.classList.add('w-2', 'bg-white/30');
                }
            });
        }

        function nextHeroSlide() {
            showHeroSlide(currentHeroSlide + 1);
        }

        function prevHeroSlide() {
            showHeroSlide(currentHeroSlide - 1);
        }

        function setHeroSlide(idx) {
            showHeroSlide(idx);
            resetHeroTimer();
        }

        function startHeroTimer() {
            if (heroSlideTimer) clearInterval(heroSlideTimer);
            heroSlideTimer = setInterval(() => {
                nextHeroSlide();
            }, heroSlideInterval);
        }

        function stopHeroTimer() {
            if (heroSlideTimer) {
                clearInterval(heroSlideTimer);
                heroSlideTimer = null;
            }
        }

        function resetHeroTimer() {
            stopHeroTimer();
            startHeroTimer();
        }

        document.addEventListener('DOMContentLoaded', () => {
            // Setup Hero Banner Carousel
            const carouselEl = document.getElementById('heroBannerCarousel');
            if (carouselEl) {
                carouselEl.addEventListener('mouseenter', stopHeroTimer);
                carouselEl.addEventListener('mouseleave', startHeroTimer);

                // Touch swipe support for mobile
                let touchStartX = 0;
                let touchEndX = 0;
                carouselEl.addEventListener('touchstart', (e) => {
                    touchStartX = e.changedTouches[0].screenX;
                }, { passive: true });
                carouselEl.addEventListener('touchend', (e) => {
                    touchEndX = e.changedTouches[0].screenX;
                    if (touchStartX - touchEndX > 50) {
                        nextHeroSlide();
                        resetHeroTimer();
                    } else if (touchEndX - touchStartX > 50) {
                        prevHeroSlide();
                        resetHeroTimer();
                    }
                }, { passive: true });

                startHeroTimer();
            }

            // Quick Search shortcut listener (Cmd+K / Ctrl+K)
            window.addEventListener('keydown', (e) => {
                if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
                    e.preventDefault();
                    const searchInput = document.querySelector('input[name="keyword"]');
                    if (searchInput) searchInput.focus();
                }
            });
        });
    </script>
</body>
</html>