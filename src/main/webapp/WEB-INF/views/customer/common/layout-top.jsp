<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="isLoggedIn" value="${pageContext.request.userPrincipal != null}" />
<c:set var="userRole" value="${isLoggedIn ? pageContext.request.userPrincipal.principal.user.role.name() : ''}" />
<c:set var="isStaffOrAdmin" value="${userRole == 'ADMIN' || userRole == 'MANAGER' || userRole == 'STAFF'}" />
<!DOCTYPE html>
<html class="dark" lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
    <title>${empty pageTitle ? 'OldBook' : pageTitle} — OldBook</title>
    <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
    <script>
        tailwind.config = {
            darkMode: 'class',
            theme: {
                extend: {
                    colors: {
                        brand: {
                            gold: '#D4AF37', goldLight: '#F3E5AB', goldHover: '#c19e2e', darkBg: '#0b0c10',
                            glassBg: 'rgba(18, 20, 26, 0.72)', glassBorder: 'rgba(255, 255, 255, 0.08)',
                            glassHighlight: 'rgba(255, 255, 255, 0.12)', surface: '#15181E', forest: '#1E382B', terracotta: '#A6553B'
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
    <link href="https://fonts.googleapis.com" rel="preconnect"/>
    <link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect"/>
    <link href="https://fonts.googleapis.com/css2?family=Merriweather:ital,wght@0,300;0,400;0,700;1,400&family=Playfair+Display:ital,wght@0,500;0,600;0,700;1,600&family=Plus+Jakarta+Sans:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet"/>
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
        .no-scrollbar::-webkit-scrollbar { display: none; }
        .no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
        body { background-color: #0b0c10; font-family: 'Plus Jakarta Sans', sans-serif; color: #e2e8f0; overflow-x: hidden; }
        .bg-reading-room {
            background: radial-gradient(circle at 15% 20%, rgba(212, 175, 55, 0.08) 0%, transparent 45%),
                        radial-gradient(circle at 85% 65%, rgba(166, 85, 59, 0.07) 0%, transparent 50%),
                        radial-gradient(circle at 50% 100%, rgba(30, 56, 43, 0.25) 0%, transparent 60%),
                        linear-gradient(180deg, #090a0d 0%, #11141a 50%, #0d0f14 100%);
            background-attachment: fixed;
        }
        .ambient-glow {
            position: absolute; width: 650px; height: 650px;
            background: radial-gradient(circle, rgba(212, 175, 55, 0.12) 0%, rgba(193, 158, 46, 0.03) 45%, transparent 70%);
            filter: blur(80px); pointer-events: none; z-index: 0;
        }
        .glass-panel { background: rgba(18, 21, 28, 0.65); backdrop-filter: blur(28px) saturate(160%); -webkit-backdrop-filter: blur(28px) saturate(160%); border: 1px solid rgba(255, 255, 255, 0.09); }
        .glass-panel-subtle { background: rgba(25, 30, 40, 0.5); backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px); border: 1px solid rgba(255, 255, 255, 0.06); }
        /* High-priority override: ALL text inputs, textareas, selects must ALWAYS be dark glass - NEVER solid white */
        input[type="text"],
        input[type="search"],
        input[type="email"],
        input[type="password"],
        input[type="tel"],
        input[type="number"],
        textarea,
        select,
        .glass-input,
        input.form-field,
        textarea.form-field,
        select.form-field {
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
        .glass-input:focus,
        input.form-field:focus,
        textarea.form-field:focus,
        select.form-field:focus {
            background-color: rgba(20, 25, 38, 0.98) !important;
            color: #ffffff !important;
            border-color: rgba(212, 175, 55, 0.7) !important;
            box-shadow: 0 0 0 3px rgba(212, 175, 55, 0.25), inset 0 2px 4px rgba(0, 0, 0, 0.4) !important;
            outline: none !important;
        }

        input::placeholder,
        textarea::placeholder,
        .glass-input::placeholder,
        input.form-field::placeholder,
        textarea.form-field::placeholder {
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
        .book-spine-effect { box-shadow: inset 5px 0 10px -2px rgba(0,0,0,0.6), inset -2px 0 5px rgba(255,255,255,0.1), 0 15px 30px rgba(0,0,0,0.65); }
        ::-webkit-scrollbar { width: 6px; height: 6px; }
        ::-webkit-scrollbar-track { background: rgba(0, 0, 0, 0.2); }
        ::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.15); border-radius: 9999px; }
        ::-webkit-scrollbar-thumb:hover { background: rgba(212, 175, 55, 0.35); }
    
    /* Customer Dock Auto-Scaling Pill */
    .customer-dock-pill {
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
    .customer-dock-pill::-webkit-scrollbar {
        display: none;
    }

    .customer-dock-btn {
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
    .customer-dock-btn i {
        font-size: var(--dock-icon-size, 13.5px) !important;
        transition: font-size 0.2s ease;
    }
    .customer-dock-divider {
        width: calc(var(--dock-btn-size, 36px) * 0.58) !important;
        height: 1px;
        background: rgba(255, 255, 255, 0.12);
        margin: 1px 0;
        transition: width 0.2s ease;
    }
    .customer-dock-nav {
        gap: var(--dock-gap, 3.5px) !important;
    }

    </style>
<script>
    // =========================================================================
    // Dynamic Auto-Scaling Customer Dock: Adjusts button & pill size based on button count & screen height
    // =========================================================================
    function autoScaleCustomerDock() {
        const dock = document.querySelector('[data-purpose="customer-vertical-dock"]');
        if (!dock) return;

        const btns = dock.querySelectorAll('.customer-dock-btn');
        const count = btns.length;
        if (count === 0) return;

        const vh = window.innerHeight || document.documentElement.clientHeight;
        const reservedSpace = 60;
        const availableHeight = Math.max(280, vh - reservedSpace);

        let btnSize = Math.floor(availableHeight / (count * 1.12 + 1));
        const maxBtnSize = count <= 8 ? 38 : 36;
        const minBtnSize = 24;
        btnSize = Math.max(minBtnSize, Math.min(maxBtnSize, btnSize));

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

    window.addEventListener('resize', autoScaleCustomerDock, { passive: true });
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', autoScaleCustomerDock);
    } else {
        autoScaleCustomerDock();
    }
</script>
</head>
<body data-logged-in="${isLoggedIn}" class="bg-reading-room min-h-screen relative flex flex-col justify-between py-4 sm:py-6 px-2.5 sm:px-6 md:px-8 lg:px-12 pb-24 lg:pb-6 selection:bg-brand-gold selection:text-black">
    <div class="ambient-glow top-12 left-1/4"></div>
    <div class="ambient-glow bottom-20 right-10"></div>

    <div class="relative z-10 max-w-[1520px] w-full mx-auto flex items-start gap-5">
        <!-- Customer / Homepage Left Floating Dock -->
        <aside class="customer-dock-pill hidden lg:flex flex-col items-center justify-start rounded-full glass-panel shadow-glass-elevated sticky top-10 border border-white/10 shrink-0 z-40" data-purpose="customer-vertical-dock">
            <!-- Top: Brand Logo & Logout / Login -->
            <div class="flex flex-col items-center gap-1.5 shrink-0">
                <!-- Brand Logo (Về trang chủ) -->
                <a class="customer-dock-btn rounded-full bg-brand-gold/15 border border-brand-gold/30 flex items-center justify-center text-brand-gold hover:bg-brand-gold/25 transition-all shadow-sm" href="/" title="OldBook Trang Chủ">
                    <i class="fa-solid fa-book-open text-brand-gold"></i>
                </a>

                <!-- Logout Button (Top action if logged in) -->
                <c:if test="${isLoggedIn}">
                    <form action="/logout" method="post" class="m-0">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <button type="submit" class="customer-dock-btn rounded-full flex items-center justify-center text-rose-400 hover:text-white hover:bg-rose-500/25 border border-rose-500/20 hover:border-rose-500/50 transition group relative" title="Đăng xuất">
                            <i class="fa-solid fa-arrow-right-from-bracket"></i>
                            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-rose-300 opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-rose-500/20 z-50">Đăng Xuất</span>
                        </button>
                    </form>
                </c:if>

                <!-- Login Button (Top action if not logged in) -->
                <c:if test="${!isLoggedIn}">
                    <a href="/login" class="customer-dock-btn rounded-full flex items-center justify-center text-brand-gold hover:text-white hover:bg-brand-gold/20 border border-brand-gold/30 transition group relative" title="Đăng nhập tài khoản">
                        <i class="fa-solid fa-arrow-right-to-bracket"></i>
                        <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-brand-gold opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-brand-gold/20 z-50">Đăng Nhập</span>
                    </a>
                </c:if>
            </div>

            <!-- Divider Top -->
            <div class="customer-dock-divider shrink-0"></div>

            <!-- Navigation Links -->
            <nav class="customer-dock-nav flex flex-col items-center my-1">
                <!-- 1. Trang chủ / Khám phá -->
                <a class="customer-dock-btn rounded-full flex items-center justify-center text-slate-400 hover:text-brand-gold hover:bg-white/5 transition group relative" href="/" title="Trang chủ khám phá">
                    <i class="fa-solid fa-house"></i>
                    <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Khám Phá Sách</span>
                </a>

                <!-- 2. Kho sách tuyển chọn (Icon danh mục sách) -->
                <a class="customer-dock-btn rounded-full flex items-center justify-center text-slate-400 hover:text-brand-gold hover:bg-white/5 transition group relative" href="/#catalog" title="Kho sách tuyển chọn">
                    <i class="fa-solid fa-book-bookmark"></i>
                    <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Kho Sách Tuyển Chọn</span>
                </a>

                <!-- 3. Thu mua sách cũ từ khách (Icon thu mua) -->
                <a class="customer-dock-btn rounded-full flex items-center justify-center text-slate-400 hover:text-brand-gold hover:bg-white/5 transition group relative" href="/#buyback" title="Đăng ký bán lại sách cũ">
                    <i class="fa-solid fa-hand-holding-dollar"></i>
                    <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Dịch Vụ Thu Mua</span>
                </a>

                <!-- 4. Mạng lưới chi nhánh (Icon cửa hàng) -->
                <a class="customer-dock-btn rounded-full flex items-center justify-center text-slate-400 hover:text-brand-gold hover:bg-white/5 transition group relative" href="/#stores" title="Mạng lưới chi nhánh chuỗi">
                    <i class="fa-solid fa-store"></i>
                    <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Hệ Thống Chi Nhánh</span>
                </a>

                <!-- 5. Giỏ hàng mua sắm (Icon giỏ hàng + badge số lượng) -->
                <a class="customer-dock-btn rounded-full flex items-center justify-center text-slate-400 hover:text-brand-gold hover:bg-white/5 transition group relative shrink-0" href="/cart" title="Giỏ hàng của bạn">
                    <i class="fa-solid fa-cart-shopping"></i>
                    <span data-cart-count class="absolute -top-1 -right-1 min-w-[16px] h-[16px] px-1 rounded-full bg-brand-gold text-slate-950 text-[9px] font-black flex items-center justify-center shadow-md ${empty cartCount || cartCount == 0 ? 'hidden' : ''}">${empty cartCount ? 0 : cartCount}</span>
                    <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Giỏ Hàng Mua Sách</span>
                </a>

                <!-- 6. NÚT THÔNG BÁO NỔI BẬT NHẤT TRÊN THANH DOCK (HIGHLIGHTED NOTIFICATION) -->
                <c:if test="${isLoggedIn}">
                    <button type="button" data-notification-toggle aria-haspopup="true" aria-expanded="false"
                            class="customer-dock-btn rounded-full flex items-center justify-center bg-gradient-to-br from-amber-500/25 via-brand-gold/35 to-amber-600/25 border border-brand-gold text-brand-gold shadow-[0_0_16px_rgba(212,175,55,0.4)] hover:shadow-[0_0_24px_rgba(212,175,55,0.7)] hover:scale-105 hover:border-brand-gold transition-all group relative shrink-0"
                            title="Trung tâm Thông Báo">
                        <span class="relative flex items-center justify-center">
                            <i class="fa-solid fa-bell text-brand-gold group-hover:animate-bounce"></i>
                            <span class="absolute -top-1 -right-1 flex h-2 w-2">
                                <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75"></span>
                                <span class="relative inline-flex rounded-full h-2 w-2 bg-amber-500"></span>
                            </span>
                        </span>
                        <span data-notification-badge class="hidden absolute -top-1.5 -right-1.5 min-w-[16px] h-[16px] px-1 rounded-full bg-rose-500 text-white text-[9px] font-black flex items-center justify-center shadow-lg border border-rose-300 ring-2 ring-[#0b0c10] animate-pulse">0</span>
                        <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-brand-gold font-bold opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-brand-gold/40 z-50">
                            <i class="fa-solid fa-bell mr-1"></i>Trung Tâm Thông Báo
                        </span>
                    </button>
                </c:if>
                <c:if test="${!isLoggedIn}">
                    <a href="/login" class="customer-dock-btn rounded-full flex items-center justify-center bg-gradient-to-br from-amber-500/20 via-brand-gold/30 to-amber-600/20 border border-brand-gold/60 text-brand-gold shadow-[0_0_14px_rgba(212,175,55,0.3)] hover:shadow-[0_0_22px_rgba(212,175,55,0.6)] hover:scale-105 transition-all group relative shrink-0" title="Đăng nhập để nhận thông báo">
                        <i class="fa-solid fa-bell text-brand-gold"></i>
                        <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-brand-gold font-bold opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-brand-gold/40 z-50">
                            <i class="fa-solid fa-bell mr-1"></i>Thông Báo
                        </span>
                    </a>
                </c:if>

                <c:if test="${isLoggedIn}">
                    <!-- 7. Đơn hàng của tôi (Icon hóa đơn đơn hàng) -->
                    <a class="customer-dock-btn rounded-full flex items-center justify-center text-slate-400 hover:text-brand-gold hover:bg-white/5 transition group relative shrink-0" href="/orders" title="Lịch sử đơn hàng của tôi">
                        <i class="fa-solid fa-receipt"></i>
                        <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Đơn Hàng Của Tôi</span>
                    </a>

                    <!-- 8. Sách yêu thích (Icon trái tim) -->
                    <a class="customer-dock-btn rounded-full flex items-center justify-center text-slate-400 hover:text-rose-400 hover:bg-white/5 transition group relative shrink-0" href="/wishlist" title="Danh sách sách yêu thích">
                        <i class="fa-solid fa-heart"></i>
                        <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Sách Yêu Thích</span>
                    </a>

                    <!-- 9. Chat hỗ trợ trực tuyến (Icon chat) -->
                    <a class="customer-dock-btn rounded-full flex items-center justify-center text-slate-400 hover:text-sky-400 hover:bg-white/5 transition group relative shrink-0" href="/chat" title="Chat hỗ trợ với nhân viên">
                        <i class="fa-solid fa-comments"></i>
                        <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Chat Hỗ Trợ</span>
                    </a>

                    <!-- 10. Tài khoản cá nhân (Icon người dùng) -->
                    <a class="customer-dock-btn rounded-full flex items-center justify-center text-slate-400 hover:text-brand-gold hover:bg-white/5 transition group relative shrink-0" href="/account" title="Thông tin tài khoản cá nhân">
                        <i class="fa-solid fa-user"></i>
                        <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-white/10 z-50">Tài Khoản Cá Nhân</span>
                    </a>
                </c:if>

                <!-- 10. Lối vào Quản Trị dành cho Nhân sự / Quản lý / Admin -->
                <c:if test="${isStaffOrAdmin}">
                    <a class="customer-dock-btn rounded-full flex items-center justify-center text-brand-gold bg-brand-gold/15 hover:bg-brand-gold/25 border border-brand-gold/30 transition group relative" href="/admin/dashboard" title="Vào bàn làm việc Quản trị">
                        <i class="fa-solid fa-shield-halved"></i>
                        <span class="absolute left-14 ml-2 px-2.5 py-1 rounded-xl bg-black/95 text-xs text-brand-gold opacity-0 group-hover:opacity-100 pointer-events-none transition-all duration-200 whitespace-nowrap shadow-2xl border border-brand-gold/20 z-50">Bàn Làm Việc Quản Trị</span>
                    </a>
                </c:if>
            </nav>
        </aside>

        <!-- Real-Time Notification Flyout Drawer for Customer (Outside dock to prevent clipping) -->
        <c:if test="${isLoggedIn}">
            <div data-notification-panel class="hidden fixed top-16 inset-x-3 sm:inset-x-auto sm:right-6 lg:right-auto lg:left-24 lg:top-20 z-50 w-auto sm:w-96 max-w-[95vw] rounded-2xl border border-white/15 bg-[#12151c]/95 backdrop-blur-2xl shadow-glass-elevated overflow-hidden text-left">
                <div class="flex items-center justify-between px-4 py-3 border-b border-white/10 bg-white/[0.02]">
                    <div class="flex items-center gap-2">
                        <i class="fa-solid fa-bell text-brand-gold text-xs"></i>
                        <span class="text-xs font-bold text-white uppercase tracking-wider">Thông Báo Của Bạn</span>
                    </div>
                    <button type="button" data-notification-read-all class="text-[11px] text-brand-gold hover:text-white transition font-medium">Đánh dấu đã đọc tất cả</button>
                </div>
                <ul data-notification-list class="max-h-96 overflow-y-auto divide-y divide-white/5">
                    <li class="px-4 py-6 text-center text-xs text-slate-500">Đang tải...</li>
                </ul>
                <a href="/notifications" class="block px-4 py-2.5 text-center text-xs font-semibold text-brand-gold hover:text-white bg-white/[0.02] hover:bg-white/[0.06] border-t border-white/10 transition">
                    Xem tất cả thông báo <i class="fa-solid fa-arrow-right text-[10px] ml-1"></i>
                </a>
            </div>
        </c:if>

        <main class="flex-1 w-full glass-panel rounded-2xl sm:rounded-[32px] p-3.5 sm:p-5 lg:p-7 shadow-glass-elevated border border-white/10 relative overflow-hidden">
            <!-- Mobile Brand Bar (Visible on < lg screens where left dock is hidden) -->
            <div class="flex lg:hidden items-center justify-between pb-3.5 mb-3 border-b border-white/10" data-purpose="mobile-brand-bar">
                <a href="/" class="flex items-center gap-2.5 group">
                    <div class="w-8 h-8 rounded-full bg-brand-gold/15 border border-brand-gold/40 flex items-center justify-center text-brand-gold group-hover:bg-brand-gold/25 transition shadow-sm">
                        <i class="fa-solid fa-book-open text-xs"></i>
                    </div>
                    <span class="font-serif font-bold text-lg text-white tracking-wide">Old<span class="text-brand-gold">Book</span></span>
                </a>
                <div class="flex items-center gap-2">
                    <c:if test="${isLoggedIn}">
                        <button type="button" data-notification-toggle aria-haspopup="true" aria-expanded="false" title="Thông báo"
                                class="relative w-8 h-8 rounded-full bg-brand-gold/15 text-brand-gold border border-brand-gold/40 flex items-center justify-center hover:bg-brand-gold/25 transition">
                            <i class="fa-solid fa-bell text-xs"></i>
                            <span data-notification-badge class="hidden absolute -top-1 -right-1 min-w-[16px] h-[16px] px-1 rounded-full bg-rose-500 text-white text-[9px] font-bold flex items-center justify-center animate-pulse">0</span>
                        </button>
                    </c:if>
                    <a href="/cart" class="relative w-8 h-8 rounded-full glass-panel-subtle hover:bg-white/10 flex items-center justify-center transition text-slate-300" title="Giỏ hàng">
                        <i class="fa-solid fa-cart-shopping text-xs"></i>
                        <span data-cart-count class="absolute -top-1 -right-1 min-w-[17px] h-[17px] px-1 rounded-full bg-brand-gold text-slate-950 text-[9px] font-black flex items-center justify-center ${empty cartCount || cartCount == 0 ? 'hidden' : ''}">${empty cartCount ? 0 : cartCount}</span>
                    </a>
                    <c:if test="${!isLoggedIn}">
                        <a href="/login" class="flex items-center gap-1.5 px-3 py-1 rounded-full bg-gradient-to-r from-brand-gold to-yellow-500 text-slate-950 font-bold text-xs shadow-glow-gold hover:from-yellow-400 hover:to-amber-500 transition">
                            <i class="fa-solid fa-right-to-bracket text-[10px]"></i>
                            <span>Đăng nhập</span>
                        </a>
                    </c:if>
                    <c:if test="${isLoggedIn}">
                        <a href="/account" class="w-8 h-8 rounded-full bg-gradient-to-tr from-amber-600 via-brand-gold to-yellow-200 p-[1px] flex items-center justify-center" title="Tài khoản cá nhân">
                            <div class="w-full h-full rounded-full bg-[#1e2330] flex items-center justify-center text-xs font-bold text-amber-200">
                                ${fn:escapeXml(pageContext.request.userPrincipal.principal.username.substring(0, 1).toUpperCase())}
                            </div>
                        </a>
                    </c:if>
                </div>
            </div>
            <header class="flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-3 lg:gap-4 pb-4 lg:pb-6 border-b border-white/5">
                <div class="flex items-center gap-3 w-full sm:w-80 lg:w-80 shrink-0">
                    <form action="/" method="get" class="relative w-full">
                        <span class="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none text-slate-400"><i class="fa-solid fa-magnifying-glass text-xs"></i></span>
                        <input name="keyword" autocomplete="off" class="glass-input w-full pl-10 pr-4 py-2 text-xs sm:text-sm rounded-full text-white placeholder-slate-400 transition" placeholder="Tìm kiếm sách, tác giả, tiệm sách..." type="text"/>
                    </form>
                </div>
                <div class="hidden lg:flex items-center justify-end gap-3 shrink-0">
                    <!-- Mobile only: Quick actions for small screens where left dock is hidden (< xl) -->
                    <div class="flex lg:hidden items-center gap-2">
                        <a href="/cart" class="relative p-2 rounded-full glass-panel-subtle hover:bg-white/10 transition text-slate-300" title="Giỏ hàng">
                            <i class="fa-solid fa-cart-shopping text-sm"></i>
                            <span data-cart-count class="absolute -top-1 -right-1 min-w-[18px] h-[18px] px-1 rounded-full bg-brand-gold text-slate-950 text-[10px] font-bold flex items-center justify-center ${empty cartCount || cartCount == 0 ? 'hidden' : ''}">${empty cartCount ? 0 : cartCount}</span>
                        </a>
                        <c:if test="${isLoggedIn}">
                            <button type="button" data-notification-toggle aria-haspopup="true" aria-expanded="false" title="Thông báo"
                                    class="relative p-2 rounded-full bg-brand-gold/15 text-brand-gold border border-brand-gold/40 hover:bg-brand-gold/25 transition">
                                <i class="fa-solid fa-bell text-sm"></i>
                                <span data-notification-badge class="hidden absolute -top-1 -right-1 min-w-[16px] h-[16px] px-1 rounded-full bg-rose-500 text-white text-[9px] font-bold flex items-center justify-center animate-pulse">0</span>
                            </button>
                            <a href="/orders" class="relative p-2 rounded-full glass-panel-subtle hover:bg-white/10 transition text-slate-300" title="Đơn hàng của tôi">
                                <i class="fa-solid fa-receipt text-sm"></i>
                            </a>
                            <a href="/wishlist" class="relative p-2 rounded-full glass-panel-subtle hover:bg-white/10 transition text-slate-300" title="Sách yêu thích">
                                <i class="fa-solid fa-heart text-sm"></i>
                            </a>
                        </c:if>
                    </div>

                    <c:if test="${!isLoggedIn}">
                        <a href="/login" class="flex items-center gap-2 pl-3 pr-4 py-1.5 rounded-full bg-gradient-to-r from-brand-gold to-yellow-500 text-slate-950 font-bold text-xs shadow-glow-gold hover:from-yellow-400 hover:to-amber-500 transition">
                            <i class="fa-solid fa-right-to-bracket text-[11px]"></i><span>Đăng nhập</span>
                        </a>
                    </c:if>
                    <c:if test="${isLoggedIn}">
                        <c:if test="${isStaffOrAdmin}">
                            <a href="/admin/dashboard" class="hidden md:flex lg:hidden items-center gap-1.5 px-3 py-1.5 rounded-full bg-brand-gold/15 hover:bg-brand-gold/25 border border-brand-gold/30 text-xs text-brand-gold hover:text-white transition">
                                <i class="fa-solid fa-gauge text-[11px]"></i><span>Quản Trị</span>
                            </a>
                        </c:if>
                        <a href="/account" title="Tài khoản của tôi" class="flex items-center gap-2.5 pl-2 pr-3 py-1 rounded-full glass-panel-subtle border border-white/10 hover:border-brand-gold/40 transition">
                            <div class="w-7 h-7 rounded-full bg-gradient-to-tr from-amber-600 via-brand-gold to-yellow-200 p-[1px] flex items-center justify-center">
                                <div class="w-full h-full rounded-full bg-[#1e2330] flex items-center justify-center text-xs font-bold text-amber-200">${fn:escapeXml(pageContext.request.userPrincipal.principal.username.substring(0, 1).toUpperCase())}</div>
                            </div>
                            <div class="hidden sm:flex flex-col text-left">
                                <span class="text-xs font-semibold text-slate-200 leading-tight">${fn:escapeXml(pageContext.request.userPrincipal.principal.user.fullName)}</span>
                                <span class="text-[10px] text-brand-goldLight leading-none">${pageContext.request.userPrincipal.principal.user.role}</span>
                            </div>
                        </a>
                    </c:if>
                </div>
            </header>
