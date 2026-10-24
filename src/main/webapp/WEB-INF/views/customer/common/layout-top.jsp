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
        .glass-input { background: rgba(255, 255, 255, 0.05); backdrop-filter: blur(12px); border: 1px solid rgba(255, 255, 255, 0.08); }
        .glass-input:focus { background: rgba(255, 255, 255, 0.08); border-color: rgba(212, 175, 55, 0.45); outline: none; }
        input.form-field, textarea.form-field, select.form-field {
            background-color: rgba(255, 255, 255, 0.06); color: #f1f5f9; border: 1px solid rgba(255, 255, 255, 0.12);
        }
        input.form-field::placeholder, textarea.form-field::placeholder { color: #64748b; }
        input.form-field:focus, textarea.form-field:focus, select.form-field:focus {
            background-color: rgba(255, 255, 255, 0.1); border-color: rgba(212, 175, 55, 0.55); outline: none; box-shadow: none;
        }
        .book-spine-effect { box-shadow: inset 5px 0 10px -2px rgba(0,0,0,0.6), inset -2px 0 5px rgba(255,255,255,0.1), 0 15px 30px rgba(0,0,0,0.65); }
        ::-webkit-scrollbar { width: 6px; height: 6px; }
        ::-webkit-scrollbar-track { background: rgba(0, 0, 0, 0.2); }
        ::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.15); border-radius: 9999px; }
        ::-webkit-scrollbar-thumb:hover { background: rgba(212, 175, 55, 0.35); }
    </style>
</head>
<body data-logged-in="${isLoggedIn}" class="bg-reading-room min-h-screen relative flex flex-col justify-between py-6 px-4 md:px-8 lg:px-12 selection:bg-brand-gold selection:text-black">
    <div class="ambient-glow top-12 left-1/4"></div>
    <div class="ambient-glow bottom-20 right-10"></div>

    <div class="relative z-10 max-w-[1520px] w-full mx-auto flex items-start gap-5">
        <aside class="hidden xl:flex flex-col items-center justify-between py-6 px-3 rounded-full glass-panel shadow-glass-elevated sticky top-10 h-[560px] w-[64px] border border-white/10 shrink-0">
            <a class="w-10 h-10 rounded-full bg-brand-gold/15 border border-brand-gold/30 flex items-center justify-center text-brand-gold hover:bg-brand-gold/25 transition-all" href="/" title="OldBook Trang Chủ">
                <i class="fa-solid fa-book-open text-sm"></i>
            </a>
            <nav class="flex flex-col items-center gap-6 my-auto">
                <a class="p-2.5 rounded-full text-slate-400 hover:text-brand-gold hover:bg-white/5 transition" href="/" title="Trang chủ"><i class="fa-solid fa-house text-base"></i></a>
                <a class="p-2.5 rounded-full text-slate-400 hover:text-brand-gold hover:bg-white/5 transition" href="/#catalog" title="Kho sách"><i class="fa-solid fa-book text-base"></i></a>
                <a class="p-2.5 rounded-full text-slate-400 hover:text-brand-gold hover:bg-white/5 transition" href="/#buyback" title="Thu mua sách cũ"><i class="fa-solid fa-hand-holding-dollar text-base"></i></a>
                <a class="p-2.5 rounded-full text-slate-400 hover:text-brand-gold hover:bg-white/5 transition" href="/#stores" title="Chi nhánh"><i class="fa-solid fa-store text-base"></i></a>
                <c:if test="${isStaffOrAdmin}">
                    <a class="p-2.5 rounded-full text-brand-gold bg-brand-gold/15 hover:bg-brand-gold/30 transition" href="/admin/dashboard" title="Vào quản trị"><i class="fa-solid fa-gauge text-base"></i></a>
                </c:if>
            </nav>
            <c:if test="${!isLoggedIn}">
                <a class="p-2.5 rounded-full text-slate-400 hover:text-brand-gold hover:bg-white/5 transition" href="/login" title="Đăng nhập"><i class="fa-solid fa-right-to-bracket text-base"></i></a>
            </c:if>
            <c:if test="${isLoggedIn}">
                <form action="/logout" method="post" class="m-0">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                    <button type="submit" class="p-2.5 rounded-full text-rose-400 hover:text-rose-300 hover:bg-rose-500/10 transition" title="Đăng xuất"><i class="fa-solid fa-power-off text-base"></i></button>
                </form>
            </c:if>
        </aside>

        <main class="flex-1 w-full glass-panel rounded-[32px] p-5 sm:p-7 shadow-glass-elevated border border-white/10 relative overflow-hidden">
            <header class="flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-4 pb-6 border-b border-white/5">
                <div class="flex items-center gap-3 w-full lg:w-80">
                    <form action="/" method="get" class="relative w-full">
                        <span class="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none text-slate-400"><i class="fa-solid fa-magnifying-glass text-xs"></i></span>
                        <input name="keyword" class="glass-input w-full pl-10 pr-4 py-2 text-xs sm:text-sm rounded-full text-slate-100 placeholder-slate-400 transition" placeholder="Tìm kiếm sách, tác giả, tiệm sách..." type="text"/>
                    </form>
                </div>
                <div class="flex items-center justify-end gap-3 shrink-0">
                    <a href="/cart" class="relative p-2 rounded-full glass-panel-subtle hover:bg-white/10 transition text-slate-300" title="Giỏ hàng">
                        <i class="fa-solid fa-cart-shopping text-sm"></i>
                        <span data-cart-count class="absolute -top-1 -right-1 min-w-[18px] h-[18px] px-1 rounded-full bg-brand-gold text-slate-950 text-[10px] font-bold flex items-center justify-center ${empty cartCount || cartCount == 0 ? 'hidden' : ''}">${empty cartCount ? 0 : cartCount}</span>
                    </a>
                    <c:if test="${isLoggedIn}">
                        <jsp:include page="/WEB-INF/views/customer/common/notification-bell.jsp"/>
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
                    <c:if test="${!isLoggedIn}">
                        <a href="/login" class="flex items-center gap-2 pl-3 pr-4 py-1.5 rounded-full bg-gradient-to-r from-brand-gold to-yellow-500 text-slate-950 font-bold text-xs shadow-glow-gold hover:from-yellow-400 hover:to-amber-500 transition">
                            <i class="fa-solid fa-right-to-bracket text-[11px]"></i><span>Đăng nhập</span>
                        </a>
                    </c:if>
                    <c:if test="${isLoggedIn}">
                        <c:if test="${isStaffOrAdmin}">
                            <a href="/admin/dashboard" class="hidden md:flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-brand-gold/15 hover:bg-brand-gold/25 border border-brand-gold/30 text-xs text-brand-gold hover:text-white transition">
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
