<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html class="dark" lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>Quản Lý Chi Nhánh — OldBook</title>
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
        body {
            background-color: #0b0c10;
            font-family: 'Plus Jakarta Sans', sans-serif;
            color: #e2e8f0;
            overflow-x: hidden;
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
<body class="bg-reading-room min-h-screen relative flex flex-col justify-between py-6 px-4 md:px-8 lg:px-12 selection:bg-brand-gold selection:text-black">
    <div class="ambient-glow top-12 left-1/4"></div>
    <div class="ambient-glow bottom-20 right-10"></div>

    <div class="relative z-10 max-w-[1520px] w-full mx-auto flex flex-col xl:flex-row items-start gap-5">
        <!-- Unified Navigation Dock -->
        <jsp:include page="/WEB-INF/views/admin/common/nav-dock.jsp">
            <jsp:param name="active" value="stores"/>
        </jsp:include>

        <!-- Main Display Frame -->
        <main class="flex-1 w-full glass-panel rounded-[32px] p-5 sm:p-7 shadow-glass-elevated border border-white/10 relative overflow-hidden">
            <!-- Top Nav & Breadcrumbs -->
            <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b border-white/5 mb-6">
                <div>
                    <div class="flex items-center gap-2 mb-1">
                        <span class="px-2.5 py-0.5 rounded-full bg-brand-gold/15 text-brand-gold text-[10px] font-semibold border border-brand-gold/30">
                            HỆ THỐNG VẬN HÀNH
                        </span>
                    </div>
                    <h1 class="text-2xl sm:text-3xl font-serif font-bold text-white tracking-tight">
                        Mạng Lưới Chi Nhánh Chuỗi
                    </h1>
                    <p class="text-xs text-slate-400 mt-0.5">Quản lý các cơ sở tiếp nhận, lưu trữ và điều chuyển sách</p>
                </div>
                <button onclick="document.getElementById('createStoreModal').classList.remove('hidden')" 
                        class="px-4 py-2.5 rounded-xl bg-gradient-to-r from-brand-gold to-amber-600 hover:from-amber-400 hover:to-amber-600 text-slate-950 font-bold text-xs shadow-glow-gold transition flex items-center gap-2">
                    <i class="fa-solid fa-plus"></i>
                    <span>Thêm Chi Nhánh Mới</span>
                </button>
            </div>

            <!-- Feedback Messages -->
            <c:if test="${not empty successMessage}">
                <div class="mb-5 p-3.5 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-300 text-xs flex items-center justify-between">
                    <div class="flex items-center gap-2">
                        <i class="fa-solid fa-circle-check"></i>
                        <span>${successMessage}</span>
                    </div>
                    <button onclick="this.parentElement.remove()" class="text-emerald-400 hover:text-emerald-200"><i class="fa-solid fa-xmark"></i></button>
                </div>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <div class="mb-5 p-3.5 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-300 text-xs flex items-center justify-between">
                    <div class="flex items-center gap-2">
                        <i class="fa-solid fa-triangle-exclamation"></i>
                        <span>${errorMessage}</span>
                    </div>
                    <button onclick="this.parentElement.remove()" class="text-rose-400 hover:text-rose-200"><i class="fa-solid fa-xmark"></i></button>
                </div>
            </c:if>

            <!-- Main Glass Table Container -->
            <div class="glass-panel rounded-2xl border border-white/10 overflow-hidden shadow-lg">
                <div class="overflow-x-auto">
                    <table class="w-full text-left text-xs text-slate-300">
                        <thead class="bg-white/5 text-[11px] text-brand-gold uppercase tracking-wider border-b border-white/10">
                            <tr>
                                <th class="py-3.5 px-4">Mã Chi Nhánh</th>
                                <th class="py-3.5 px-4">Tên Cửa Hàng</th>
                                <th class="py-3.5 px-4">Địa Chỉ</th>
                                <th class="py-3.5 px-4">Hotline</th>
                                <th class="py-3.5 px-4 text-center">Nhân Lực</th>
                                <th class="py-3.5 px-4 text-center">Trạng Thái</th>
                                <th class="py-3.5 px-4 text-right">Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-white/5">
                            <c:forEach var="store" items="${stores}">
                                <tr class="hover:bg-white/5 transition">
                                    <td class="py-3.5 px-4 font-mono text-amber-300">#${store.id}</td>
                                    <td class="py-3.5 px-4 font-semibold text-white">
                                        <div class="flex items-center gap-2">
                                            <i class="fa-solid fa-store text-brand-gold text-xs"></i>
                                            <span>${store.name}</span>
                                        </div>
                                    </td>
                                    <td class="py-3.5 px-4 text-slate-400">${store.address}</td>
                                    <td class="py-3.5 px-4 text-slate-400 font-mono">${store.phone}</td>
                                    <td class="py-3.5 px-4 text-center">
                                        <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-medium bg-white/5 border border-white/10 text-slate-300">
                                            <i class="fa-solid fa-users text-[9px] text-brand-gold"></i>
                                            ${store.employeeCount} nhân sự
                                        </span>
                                    </td>
                                    <td class="py-3.5 px-4 text-center">
                                        <c:choose>
                                            <c:when test="${store.active}">
                                                <span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-500/15 border border-emerald-500/30 text-emerald-400">
                                                    <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
                                                    Hoạt động
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-rose-500/15 border border-rose-500/30 text-rose-400">
                                                    <span class="w-1.5 h-1.5 rounded-full bg-rose-400"></span>
                                                    Tạm dừng
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="py-3.5 px-4 text-right">
                                        <div class="inline-flex items-center gap-1.5">
                                            <a href="/admin/stores/${store.id}/edit" 
                                               class="w-7 h-7 rounded-lg bg-white/5 hover:bg-brand-gold/20 border border-white/10 hover:border-brand-gold/40 text-slate-300 hover:text-brand-gold flex items-center justify-center transition"
                                               title="Chỉnh sửa chi nhánh">
                                                <i class="fa-solid fa-pen-to-square text-[11px]"></i>
                                            </a>
                                            <form action="/admin/stores/${store.id}/toggle-status" method="post" class="inline">
                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                <button type="submit" 
                                                        onclick="return confirm('Bạn có chắc chắn muốn ${store.active ? 'TẠM DỪNG' : 'KÍCH HOẠT'} chi nhánh ${store.name}?')"
                                                        class="w-7 h-7 rounded-lg border flex items-center justify-center transition ${store.active ? 'bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border-rose-500/20' : 'bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border-emerald-500/20'}"
                                                        title="${store.active ? 'Tạm dừng hoạt động' : 'Kích hoạt hoạt động'}">
                                                    <i class="fa-solid ${store.active ? 'fa-lock' : 'fa-lock-open'} text-[11px]"></i>
                                                </button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty stores}">
                                <tr>
                                    <td colspan="7" class="py-12 text-center text-slate-500">
                                        <i class="fa-solid fa-store-slash text-3xl mb-3 text-slate-600 block"></i>
                                        Chưa có chi nhánh nào trong hệ thống.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>

    <!-- Modal Thêm Chi Nhánh -->
    <div id="createStoreModal" 
         onclick="if(event.target === this) this.classList.add('hidden')"
         class="${openModal ? '' : 'hidden'} fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md">
        <div class="glass-panel w-full max-w-md rounded-3xl p-6 sm:p-8 border border-white/10 shadow-glass-elevated relative">
            <button type="button" onclick="document.getElementById('createStoreModal').classList.add('hidden')" class="absolute top-5 right-5 text-slate-400 hover:text-white transition">
                <i class="fa-solid fa-xmark text-lg"></i>
            </button>

            <div class="flex items-center gap-2.5 mb-1">
                <div class="w-8 h-8 rounded-lg bg-brand-gold/15 border border-brand-gold/30 flex items-center justify-center text-brand-gold">
                    <i class="fa-solid fa-store text-xs"></i>
                </div>
                <h3 class="text-xl font-serif font-bold text-white">Thêm Chi Nhánh Mới</h3>
            </div>
            <p class="text-xs text-slate-400 mb-5">Nhập thông tin cơ sở tiếp nhận và phân phối sách</p>

            <form:form action="/admin/stores" method="post" modelAttribute="storeForm" cssClass="space-y-4">
                <div>
                    <label class="block text-xs font-medium text-slate-300 mb-1">Tên chi nhánh (*)</label>
                    <form:input path="name" cssClass="glass-input w-full px-3.5 py-2.5 text-xs rounded-xl" placeholder="VD: OldBook Cầu Giấy" />
                    <form:errors path="name" cssClass="text-rose-400 text-[10px] mt-1 block" />
                </div>
                <div>
                    <label class="block text-xs font-medium text-slate-300 mb-1">Địa chỉ hoạt động (*)</label>
                    <form:input path="address" cssClass="glass-input w-full px-3.5 py-2.5 text-xs rounded-xl" placeholder="VD: 123 Đường Cầu Giấy, Hà Nội" />
                    <form:errors path="address" cssClass="text-rose-400 text-[10px] mt-1 block" />
                </div>
                <div>
                    <label class="block text-xs font-medium text-slate-300 mb-1">Số điện thoại hotline (*)</label>
                    <form:input path="phone" cssClass="glass-input w-full px-3.5 py-2.5 text-xs rounded-xl" placeholder="VD: 02412345678" />
                    <form:errors path="phone" cssClass="text-rose-400 text-[10px] mt-1 block" />
                </div>

                <div class="pt-3 flex items-center justify-end gap-2.5">
                    <button type="button" onclick="document.getElementById('createStoreModal').classList.add('hidden')" class="px-4 py-2 rounded-xl bg-white/10 hover:bg-white/15 text-slate-300 text-xs transition">
                        Đóng
                    </button>
                    <button type="submit" class="px-5 py-2 rounded-xl bg-gradient-to-r from-brand-gold to-amber-600 hover:from-amber-400 text-slate-950 font-bold text-xs shadow-glow-gold transition">
                        Lưu Chi Nhánh
                    </button>
                </div>
            </form:form>
        </div>
    </div>
</body>
</html>
