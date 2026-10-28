<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html class="dark" data-bs-theme="dark" lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>Đăng Ký Thành Viên — OldBook</title>
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
    <div class="fixed inset-0 overflow-hidden pointer-events-none z-0" aria-hidden="true">
        <div class="ambient-glow -top-20 -left-20"></div>
    <div class="ambient-glow -bottom-20 -right-20"></div>
    </div>

<a href="/" class="absolute top-4 left-4 sm:top-6 sm:left-6 z-20 flex items-center gap-2 px-4 py-2 rounded-full glass-panel text-xs text-slate-300 hover:text-brand-gold hover:border-brand-gold/40 transition">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18"/>
        </svg>
        <span>Về trang chủ</span>
    </a>

    <div class="relative z-10 w-full max-w-lg mx-auto py-8">
        <div class="glass-panel rounded-2xl sm:rounded-3xl p-4 sm:p-8 md:p-10 shadow-glass-elevated border border-white/10 relative overflow-hidden">
            <div class="text-center mb-8">
                <div class="w-14 h-14 mx-auto mb-4 rounded-2xl bg-brand-gold/15 border border-brand-gold/40 flex items-center justify-center shadow-glow-gold">
                    <svg class="w-7 h-7 text-brand-gold" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M19 7.5v3m0 0v3m0-3h3m-3 0h-3m-2.25-4.125a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zM4 19.235v-.11a6.375 6.375 0 0112.75 0v.109A12.375 12.375 0 014 19.235z"/>
                    </svg>
                </div>
                <h1 class="text-3xl font-serif font-bold text-white tracking-wide">
                    Gia Nhập <span class="text-brand-gold">OldBook</span>
                </h1>
                <p class="text-xs text-slate-400 mt-1 font-light tracking-wider">
                    Đăng ký tài khoản bạn đọc & trao đổi sách cũ
                </p>
            </div>

            <c:if test="${not empty errorMessage}">
                <div class="mb-5 p-3 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs flex items-center gap-2">
                    <svg class="w-4 h-4 shrink-0 text-rose-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z"/>
                    </svg>
                    <span>${errorMessage}</span>
                </div>
            </c:if>

            <form action="/register" method="post" class="space-y-4">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                        <label class="block text-xs font-medium text-slate-300 mb-1.5" for="username">
                            Tên đăng nhập (*)
                        </label>
                        <input class="glass-input w-full px-3.5 py-2.5 text-sm rounded-xl text-slate-100 placeholder-slate-500 transition" 
                               id="username" name="username" type="text" required placeholder="VD: reader_nam"/>
                    </div>
                    <div>
                        <label class="block text-xs font-medium text-slate-300 mb-1.5" for="fullName">
                            Họ và tên (*)
                        </label>
                        <input class="glass-input w-full px-3.5 py-2.5 text-sm rounded-xl text-slate-100 placeholder-slate-500 transition" 
                               id="fullName" name="fullName" type="text" required placeholder="Nguyễn Văn A"/>
                    </div>
                </div>

                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                        <label class="block text-xs font-medium text-slate-300 mb-1.5" for="email">
                            Địa chỉ Email (*)
                        </label>
                        <input class="glass-input w-full px-3.5 py-2.5 text-sm rounded-xl text-slate-100 placeholder-slate-500 transition" 
                               id="email" name="email" type="email" required placeholder="name@email.com"/>
                    </div>
                    <div>
                        <label class="block text-xs font-medium text-slate-300 mb-1.5" for="phone">
                            Số điện thoại
                        </label>
                        <input class="glass-input w-full px-3.5 py-2.5 text-sm rounded-xl text-slate-100 placeholder-slate-500 transition" 
                               id="phone" name="phone" type="tel" placeholder="09xxxxxxxx"/>
                    </div>
                </div>

                <div>
                    <label class="block text-xs font-medium text-slate-300 mb-1.5" for="password">
                        Mật khẩu bảo mật (*)
                    </label>
                    <input class="glass-input w-full px-3.5 py-2.5 text-sm rounded-xl text-slate-100 placeholder-slate-500 transition" 
                           id="password" name="password" type="password" required minlength="6" placeholder="Tối thiểu 6 ký tự"/>
                </div>

                <div class="pt-3">
                    <button type="submit" 
                            class="w-full py-2.5 px-4 rounded-xl bg-gradient-to-r from-brand-gold to-yellow-500 hover:from-yellow-400 hover:to-amber-500 text-slate-950 font-bold text-sm shadow-glow-gold transition duration-200 flex items-center justify-center gap-2">
                        <span>Hoàn Tất Đăng Ký</span>
                    </button>
                </div>
            </form>

            <div class="mt-6 pt-5 border-t border-white/5 text-center">
                <a href="/login" class="text-xs text-brand-gold hover:text-brand-goldLight transition underline underline-offset-4">
                    Đã có tài khoản? Đăng nhập ngay
                </a>
            </div>
        </div>
    </div>
</body>
</html>
