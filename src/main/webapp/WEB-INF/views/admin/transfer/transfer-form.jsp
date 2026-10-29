<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html class="dark" data-bs-theme="dark" lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>Tạo Phiếu Điều Chuyển Kho — OldBook</title>
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
                            surface: '#15181E'
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
    <!-- Google Fonts -->
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

        body {
            background-color: #0b0c10;
            font-family: 'Plus Jakarta Sans', sans-serif;
            color: #e2e8f0;
        }
        .bg-reading-room {
            background: radial-gradient(circle at 15% 20%, rgba(212, 175, 55, 0.08) 0%, transparent 45%),
                        radial-gradient(circle at 85% 65%, rgba(166, 85, 59, 0.07) 0%, transparent 50%),
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
        .glass-input {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(12px);
            border: 1px solid rgba(255, 255, 255, 0.08);
            color: #f1f5f9;
        }
        .glass-input:focus {
            background: rgba(255, 255, 255, 0.08);
            border-color: rgba(212, 175, 55, 0.45);
            outline: none;
        }
    </style>
</head>
<body class="bg-reading-room min-h-screen relative flex flex-col justify-between py-3 sm:py-6 px-2.5 sm:px-4 md:px-8 lg:px-12 selection:bg-brand-gold selection:text-black">
    <div class="fixed inset-0 overflow-hidden pointer-events-none z-0" aria-hidden="true">
        <div class="ambient-glow top-12 left-1/4"></div>
    <div class="ambient-glow bottom-20 right-10"></div>
    </div>

<div class="relative z-10 max-w-[1520px] w-full mx-auto flex flex-col lg:flex-row items-start gap-5">
        <!-- Unified Navigation Dock -->
        <jsp:include page="/WEB-INF/views/admin/common/nav-dock.jsp">
            <jsp:param name="active" value="transfers"/>
        </jsp:include>

        <!-- Main Display Frame -->
        <main class="flex-1 w-full glass-panel rounded-xl sm:rounded-2xl lg:rounded-[32px] p-2.5 sm:p-4 lg:p-7 shadow-glass-elevated border border-white/10 relative overflow-hidden">
            <!-- Top Nav & Breadcrumbs -->
            <div class="flex items-center justify-between pb-6 border-b border-white/5 mb-6">
                <div>
                    <a href="/admin/transfers" class="text-xs text-brand-gold hover:text-brand-goldLight transition flex items-center gap-1.5 mb-1.5">
                        <i class="fa-solid fa-arrow-left"></i>
                        <span>Quay lại Danh Sách Phiếu Chuyển Kho</span>
                    </a>
                    <h1 class="text-2xl sm:text-3xl font-serif font-bold text-white tracking-tight">
                        Tạo Phiếu Điều Chuyển Kho Mới
                    </h1>
                    <p class="text-xs text-slate-400 mt-0.5">Chọn chi nhánh xuất, chi nhánh nhận và các bản sách cần luân chuyển</p>
                </div>
            </div>

            <!-- Feedback Messages -->
            <c:if test="${not empty errorMessage}">
                <div class="mb-5 p-3.5 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-300 text-xs flex items-center justify-between">
                    <div class="flex items-center gap-2">
                        <i class="fa-solid fa-triangle-exclamation"></i>
                        <span>${errorMessage}</span>
                    </div>
                    <button onclick="this.parentElement.remove()" class="text-rose-400 hover:text-rose-200"><i class="fa-solid fa-xmark"></i></button>
                </div>
            </c:if>

            <div class="max-w-2xl">
                <!-- Bước 1: Chọn chi nhánh gửi -->
                <div class="mb-5 p-4 rounded-2xl bg-white/5 border border-white/5">
                    <label class="block text-xs font-semibold text-brand-gold mb-1.5">
                        1. Chi nhánh xuất kho (Gửi):
                    </label>
                    <c:choose>
                        <c:when test="${isStaffScoped}">
                            <div class="glass-input w-full px-3.5 py-2.5 text-xs rounded-xl bg-sky-500/10 border border-sky-500/30 text-sky-300 font-semibold flex items-center gap-2">
                                <i class="fa-solid fa-store text-sky-400"></i>
                                <span>${currentStoreName} (Chi nhánh phụ trách của bạn)</span>
                            </div>
                            <span class="text-[10px] text-slate-400 mt-1 block">Nhân viên chỉ có quyền lập phiếu xuất sách từ kho chi nhánh mình</span>
                        </c:when>
                        <c:otherwise>
                            <select id="senderStoreSelect" class="glass-input w-full px-3.5 py-2.5 text-xs rounded-xl bg-[#161a22]" onchange="location.href='/admin/transfers/new?senderStoreId=' + this.value;">
                                <option value="">-- Chọn chi nhánh xuất kho --</option>
                                <c:forEach var="store" items="${stores}">
                                    <option value="${store.id}" ${selectedSenderId == store.id ? 'selected' : ''}>${store.name}</option>
                                </c:forEach>
                            </select>
                            <span class="text-[10px] text-slate-400 mt-1 block">Hệ thống sẽ tải danh sách sách khả dụng tại chi nhánh này</span>
                        </c:otherwise>
                    </c:choose>
                </div>

                <form:form action="/admin/transfers/new" method="post" modelAttribute="transferForm" cssClass="space-y-4">
                    <form:hidden path="senderStoreId" value="${selectedSenderId}" />

                    <div>
                        <label class="block text-xs font-semibold text-brand-gold mb-1.5">
                            2. Chi nhánh tiếp nhận (Đích đến) (*):
                        </label>
                        <form:select path="receiverStoreId" cssClass="glass-input w-full px-3.5 py-2.5 text-xs rounded-xl bg-[#161a22]">
                            <form:option value="">-- Chọn chi nhánh nhận --</form:option>
                            <c:forEach var="store" items="${stores}">
                                <c:if test="${store.id != selectedSenderId}">
                                    <form:option value="${store.id}">${store.name}</form:option>
                                </c:if>
                            </c:forEach>
                        </form:select>
                        <form:errors path="receiverStoreId" cssClass="text-rose-400 text-[10px] mt-1 block" />
                    </div>

                    <div>
                        <label class="block text-xs font-semibold text-brand-gold mb-1.5">
                            3. Chọn các bản sách cần chuyển (*):
                        </label>
                        <c:choose>
                            <c:when test="${not empty availableCopies}">
                                <div class="glass-panel rounded-2xl border border-white/10 max-h-60 overflow-y-auto divide-y divide-white/5 p-2">
                                    <c:forEach var="copy" items="${availableCopies}">
                                        <label class="flex items-center gap-3 p-2 hover:bg-white/5 rounded-xl cursor-pointer text-xs">
                                            <input type="checkbox" name="bookCopyIds" value="${copy.id}" class="rounded border-white/20 bg-white/5 text-brand-gold focus:ring-0" />
                                            <div class="flex-1">
                                                <div class="font-semibold text-white">${copy.bookTitle}</div>
                                                <div class="text-[10px] text-slate-400 flex items-center gap-2">
                                                    <span class="font-mono text-amber-300">#${copy.copyCode}</span>
                                                    <span>•</span>
                                                    <span>${copy.bookCondition}</span>
                                                </div>
                                            </div>
                                        </label>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="p-4 rounded-xl bg-white/5 text-center text-xs text-slate-500">
                                    Vui lòng chọn chi nhánh gửi để xem sách khả dụng trong kho.
                                </div>
                            </c:otherwise>
                        </c:choose>
                        <form:errors path="bookCopyIds" cssClass="text-rose-400 text-[10px] mt-1 block" />
                    </div>

                    <div>
                        <label class="block text-xs font-semibold text-brand-gold mb-1.5">Ghi chú điều chuyển</label>
                        <form:textarea path="note" rows="3" cssClass="glass-input w-full px-3.5 py-2.5 text-xs rounded-xl" placeholder="Lý do điều chuyển, ghi chú đóng gói thùng hàng..." />
                    </div>

                    <div class="pt-4 flex items-center gap-3">
                        <button type="submit" class="px-5 py-2.5 rounded-xl bg-gradient-to-r from-brand-gold to-amber-600 hover:from-amber-400 text-slate-950 font-bold text-xs shadow-glow-gold transition">
                            Tạo Phiếu Điều Chuyển
                        </button>
                        <a href="/admin/transfers" class="px-4 py-2.5 rounded-xl bg-white/10 hover:bg-white/15 text-slate-300 text-xs transition">
                            Hủy bỏ
                        </a>
                    </div>
                </form:form>
            </div>
        </main>
    </div>
</body>
</html>
