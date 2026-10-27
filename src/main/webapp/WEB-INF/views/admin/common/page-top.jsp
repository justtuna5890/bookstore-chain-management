<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
    Khung trang quản trị dùng chung (head, theme, thanh điều hướng, mở thẻ main).
    Trang gọi đặt trước request attribute: adminTitle (tiêu đề tab), adminActive (mục sáng trong nav-dock).
    Kết thúc trang bằng page-bottom.jsp.
--%>
<!DOCTYPE html>
<html class="dark" lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>${empty adminTitle ? 'Quản trị' : adminTitle} — OldBook</title>
    <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
    <script>
        tailwind.config = {
            darkMode: 'class',
            theme: {
                extend: {
                    colors: {
                        brand: {
                            gold: '#D4AF37', goldLight: '#F3E5AB', goldHover: '#c19e2e', darkBg: '#0b0c10',
                            glassBg: 'rgba(18, 20, 26, 0.72)', glassBorder: 'rgba(255, 255, 255, 0.08)', surface: '#15181E'
                        }
                    },
                    fontFamily: {
                        serif: ['Playfair Display', 'Merriweather', 'Georgia', 'serif'],
                        sans: ['Plus Jakarta Sans', 'Inter', 'system-ui', 'sans-serif'],
                    },
                    boxShadow: {
                        'glow-gold': '0 0 25px -5px rgba(212, 175, 55, 0.35)',
                        'glass-elevated': '0 20px 50px rgba(0, 0, 0, 0.65), 0 0 1px 1px rgba(255, 255, 255, 0.08)',
                    }
                }
            }
        }
    </script>
    <link href="https://fonts.googleapis.com" rel="preconnect"/>
    <link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect"/>
    <link href="https://fonts.googleapis.com/css2?family=Merriweather:ital,wght@0,300;0,400;0,700;1,400&family=Playfair+Display:ital,wght@0,500;0,600;0,700;1,600&family=Plus+Jakarta+Sans:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css"/>
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
        .no-scrollbar::-webkit-scrollbar { display: none; }
        .no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
        body { background-color: #0b0c10; font-family: 'Plus Jakarta Sans', sans-serif; color: #e2e8f0; }
        .bg-reading-room {
            background: radial-gradient(circle at 15% 20%, rgba(212, 175, 55, 0.08) 0%, transparent 45%),
                        radial-gradient(circle at 85% 65%, rgba(166, 85, 59, 0.07) 0%, transparent 50%),
                        linear-gradient(180deg, #090a0d 0%, #11141a 50%, #0d0f14 100%);
            background-attachment: fixed;
        }
        .ambient-glow {
            position: absolute; width: 650px; height: 650px;
            background: radial-gradient(circle, rgba(212, 175, 55, 0.12) 0%, rgba(193, 158, 46, 0.03) 45%, transparent 70%);
            filter: blur(80px); pointer-events: none; z-index: 0;
        }
        .glass-panel { background: rgba(18, 21, 28, 0.65); backdrop-filter: blur(28px) saturate(160%); -webkit-backdrop-filter: blur(28px) saturate(160%); border: 1px solid rgba(255, 255, 255, 0.09); }
        .glass-input { background: rgba(255, 255, 255, 0.05); backdrop-filter: blur(12px); border: 1px solid rgba(255, 255, 255, 0.08); color: #f1f5f9; }
        .glass-input:focus { background: rgba(255, 255, 255, 0.08); border-color: rgba(212, 175, 55, 0.45); outline: none; }
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

        input.glass-input[readonly] { cursor: not-allowed; }
    </style>
</head>
<body class="bg-reading-room min-h-screen relative flex flex-col justify-between py-3 sm:py-6 px-2.5 sm:px-4 md:px-8 lg:px-12 pb-24 lg:pb-6 selection:bg-brand-gold selection:text-black">
    <div class="fixed inset-0 overflow-hidden pointer-events-none z-0" aria-hidden="true">
        <div class="ambient-glow top-12 left-1/4"></div>
    <div class="ambient-glow bottom-20 right-10"></div>
    </div>

<div class="relative z-10 max-w-[1520px] w-full mx-auto flex flex-col lg:flex-row items-start gap-5">
        <jsp:include page="/WEB-INF/views/admin/common/nav-dock.jsp">
            <jsp:param name="active" value="${adminActive}"/>
        </jsp:include>

        <main class="flex-1 w-full glass-panel rounded-xl sm:rounded-2xl lg:rounded-[32px] p-2.5 sm:p-4 lg:p-7 shadow-glass-elevated border border-white/10 relative overflow-hidden">
