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
        body { background-color: #0b0c10; font-family: 'Plus Jakarta Sans', sans-serif; color: #e2e8f0; overflow-x: hidden; }
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
        /* Plugin forms của Tailwind đặt nền trắng cho input, ghi đè .glass-input; selector này cụ thể hơn nên thắng */
        input.glass-input, textarea.glass-input { background-color: rgba(255, 255, 255, 0.06); color: #f1f5f9; border: 1px solid rgba(255, 255, 255, 0.12); }
        input.glass-input::placeholder, textarea.glass-input::placeholder { color: #64748b; }
        input.glass-input:focus, textarea.glass-input:focus { background-color: rgba(255, 255, 255, 0.1); border-color: rgba(212, 175, 55, 0.55); box-shadow: none; }
        input.glass-input[readonly] { cursor: not-allowed; }
    </style>
</head>
<body class="bg-reading-room min-h-screen relative flex flex-col justify-between py-6 px-4 md:px-8 lg:px-12 selection:bg-brand-gold selection:text-black">
    <div class="ambient-glow top-12 left-1/4"></div>
    <div class="ambient-glow bottom-20 right-10"></div>

    <div class="relative z-10 max-w-[1520px] w-full mx-auto flex flex-col xl:flex-row items-start gap-5">
        <jsp:include page="/WEB-INF/views/admin/common/nav-dock.jsp">
            <jsp:param name="active" value="${adminActive}"/>
        </jsp:include>

        <main class="flex-1 w-full glass-panel rounded-[32px] p-5 sm:p-7 shadow-glass-elevated border border-white/10 relative overflow-hidden">
