<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html class="dark" data-bs-theme="dark" lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>Đăng Nhập — OldBook Hệ Thống Quản Trị & Đọc Sách</title>
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
    <!-- Core Layout Styles -->
    <style>
        /* Universal Responsive Baseline */
        html, body {
            min-height: 100%;
            margin: 0;
            padding: 0;
        }
        img, svg, video {
            max-width: 100%;
            height: auto;
        }
        * {
            box-sizing: border-box;
            -webkit-tap-highlight-color: transparent;
        }

        body {
            background-color: #0b0c10;
            font-family: 'Plus Jakarta Sans', sans-serif;
            color: #e2e8f0;
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
</head>
<body class="bg-reading-room min-h-screen relative flex items-center justify-center p-2.5 sm:p-4 selection:bg-brand-gold selection:text-black">
    <!-- Ambient background glow lights -->
    <div class="fixed inset-0 overflow-hidden pointer-events-none z-0" aria-hidden="true">
        <div class="ambient-glow -top-20 -left-20"></div>
    <div class="ambient-glow -bottom-20 -right-20"></div>
    </div>

<!-- Back to Home Button -->
    <a href="/" class="absolute top-4 left-4 sm:top-6 sm:left-6 z-20 flex items-center gap-2 px-4 py-2 rounded-full glass-panel text-xs text-slate-300 hover:text-brand-gold hover:border-brand-gold/40 transition">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18"/>
        </svg>
        <span>Về trang chủ</span>
    </a>

    <!-- Login Card Container -->
    <div class="relative z-10 w-full max-w-md mx-auto">
        <div class="glass-panel rounded-2xl sm:rounded-3xl p-4 sm:p-8 md:p-10 shadow-glass-elevated border border-white/10 relative overflow-hidden">
            <!-- Decorative corner accent -->
            <div class="absolute -top-12 -right-12 w-28 h-28 bg-brand-gold/10 rounded-full blur-2xl"></div>

            <!-- Brand Header -->
            <div class="text-center mb-8">
                <div class="w-14 h-14 mx-auto mb-4 rounded-2xl bg-brand-gold/15 border border-brand-gold/40 flex items-center justify-center shadow-glow-gold">
                    <svg class="w-7 h-7 text-brand-gold" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
                    </svg>
                </div>
                <h1 class="text-3xl font-serif font-bold text-white tracking-wide">
                    Old<span class="text-brand-gold">Book</span>
                </h1>
                <p class="text-xs text-slate-400 mt-1 font-light tracking-wider uppercase">
                    Cổng Quản Trị & Vận Hành Chuỗi
                </p>
            </div>

            <!-- Feedback Alerts -->
            <c:if test="${not empty errorMessage}">
                <div class="mb-5 p-3 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs flex items-center gap-2">
                    <svg class="w-4 h-4 shrink-0 text-rose-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z"/>
                    </svg>
                    <span>${errorMessage}</span>
                </div>
            </c:if>

            <c:if test="${not empty successMessage}">
                <div class="mb-5 p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-xs flex items-center gap-2">
                    <svg class="w-4 h-4 shrink-0 text-emerald-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                    <span>${successMessage}</span>
                </div>
            </c:if>

            <!-- Login Form -->
            <form action="${pageContext.request.contextPath}/perform-login" method="post" class="space-y-4">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                <div>
                    <label class="block text-xs font-medium text-slate-300 mb-1.5" for="username">
                        Tên đăng nhập hoặc Email
                    </label>
                    <div class="relative">
                        <span class="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none text-slate-400">
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z"/>
                            </svg>
                        </span>
                        <input class="glass-input w-full pl-10 pr-4 py-2.5 text-sm rounded-xl text-slate-100 placeholder-slate-500 transition" 
                               id="username" name="username" type="text" required autofocus 
                               placeholder="admin, staff hoặc email..."/>
                    </div>
                </div>

                <div>
                    <label class="block text-xs font-medium text-slate-300 mb-1.5" for="password">
                        Mật khẩu bảo mật
                    </label>
                    <div class="relative">
                        <span class="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none text-slate-400">
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 10-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 002.25-2.25v-6.75a2.25 2.25 0 00-2.25-2.25H6.75a2.25 2.25 0 00-2.25 2.25v6.75a2.25 2.25 0 002.25 2.25z"/>
                            </svg>
                        </span>
                        <input class="glass-input w-full pl-10 pr-4 py-2.5 text-sm rounded-xl text-slate-100 placeholder-slate-500 transition" 
                               id="password" name="password" type="password" required 
                               placeholder="••••••••"/>
                    </div>
                </div>

                <div class="pt-2">
                    <button type="submit" 
                            class="w-full py-2.5 px-4 rounded-xl bg-gradient-to-r from-brand-gold to-yellow-500 hover:from-yellow-400 hover:to-amber-500 text-slate-950 font-bold text-sm shadow-glow-gold transition duration-200 flex items-center justify-center gap-2">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15m3 0l3-3m0 0l-3-3m3 3H9"/>
                        </svg>
                        <span>Đăng Nhập Hệ Thống</span>
                    </button>
                </div>
            </form>

            <!-- Quick Demo Accounts Help Pill -->
            <div class="mt-6 pt-5 border-t border-white/5 text-center">
                <span class="text-[11px] text-slate-400 block mb-2 font-medium">Tài khoản mẫu kiểm thử hệ thống:</span>
                <div class="flex flex-wrap items-center justify-center gap-1.5 text-[10px]">
                    <span class="px-2 py-0.5 rounded-md bg-rose-500/10 border border-rose-500/20 text-rose-300">
                        ADMIN: <strong>admin</strong> | <strong>admin123</strong>
                    </span>
                    <span class="px-2 py-0.5 rounded-md bg-purple-500/10 border border-purple-500/20 text-purple-300">
                        MANAGER: <strong>manager</strong> | <strong>manager123</strong>
                    </span>
                    <span class="px-2 py-0.5 rounded-md bg-sky-500/10 border border-sky-500/20 text-sky-300">
                        STAFF: <strong>staff</strong> | <strong>staff123</strong>
                    </span>
                    <span class="px-2 py-0.5 rounded-md bg-emerald-500/10 border border-emerald-500/20 text-emerald-300">
                        CUSTOMER: <strong>customer</strong> | <strong>customer123</strong>
                    </span>
                </div>
                <div class="mt-4">
                    <a href="/register" class="text-xs text-brand-gold hover:text-brand-goldLight transition underline underline-offset-4">
                        Chưa có tài khoản? Đăng ký thành viên
                    </a>
                </div>
            </div>
        </div>
    </div>
</body>
</html>