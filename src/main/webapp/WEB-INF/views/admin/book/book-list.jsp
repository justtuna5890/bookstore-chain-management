<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html class="dark" data-bs-theme="dark" lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>Danh Mục Đầu Sách — OldBook</title>
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
            <jsp:param name="active" value="books"/>
        </jsp:include>

        <!-- Main Display Frame -->
        <main class="flex-1 w-full glass-panel rounded-xl sm:rounded-2xl lg:rounded-[32px] p-2.5 sm:p-4 lg:p-7 shadow-glass-elevated border border-white/10 relative overflow-hidden">
            <!-- Top Nav & Breadcrumbs -->
            <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b border-white/5 mb-6">
                <div>
                    <div class="flex items-center gap-2 mb-1">
                        <span class="px-2.5 py-0.5 rounded-full bg-brand-gold/15 text-brand-gold text-[10px] font-semibold border border-brand-gold/30">
                            QUẢN LÝ NỘI DUNG
                        </span>
                    </div>
                    <h1 class="text-2xl sm:text-3xl font-serif font-bold text-white tracking-tight">
                        Danh Mục Đầu Sách
                    </h1>
                    <p class="text-xs text-slate-400 mt-0.5">Kho lưu trữ tác phẩm, thông tin xuất bản và tác giả</p>
                </div>
                <sec:authorize access="hasAnyRole('ADMIN', 'MANAGER')">
                <a href="/admin/books/new" 
                   class="px-4 py-2.5 rounded-xl bg-gradient-to-r from-brand-gold to-amber-600 hover:from-amber-400 text-slate-950 font-bold text-xs shadow-glow-gold transition flex items-center gap-2">
                    <i class="fa-solid fa-plus"></i>
                    <span>Thêm Đầu Sách Mới</span>
                </a>
                </sec:authorize>
            </div>

            <!-- Search Bar -->
            <div class="glass-panel rounded-2xl p-4 mb-6 border border-white/10 shadow-lg">
                <form action="/admin/books" method="get" class="flex flex-col sm:flex-row gap-3 items-center">
                    <div class="relative flex-1 w-full">
                        <span class="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none text-slate-400">
                            <i class="fa-solid fa-magnifying-glass text-xs"></i>
                        </span>
                        <input type="text" name="keyword" value="${keyword}" 
                               class="glass-input w-full pl-9 pr-4 py-2 text-xs rounded-xl" 
                               placeholder="Tìm theo tựa sách, tác giả hoặc thể loại..."/>
                    </div>
                    <div class="flex items-center gap-2 w-full sm:w-auto">
                        <button type="submit" class="px-5 py-2 rounded-xl bg-white/10 hover:bg-white/20 text-white text-xs font-semibold transition flex-1 sm:flex-none">
                            Tìm Kiếm
                        </button>
                        <c:if test="${not empty keyword}">
                            <a href="/admin/books" class="px-4 py-2 rounded-xl bg-white/5 hover:bg-white/10 text-slate-400 hover:text-white text-xs transition">
                                Xóa Lọc
                            </a>
                        </c:if>
                    </div>
                </form>
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

            <!-- Main Glass Table Container -->
            <div class="glass-panel rounded-2xl border border-white/10 overflow-hidden shadow-lg">
                <div class="overflow-x-auto">
                    <table class="w-full text-left text-xs text-slate-300">
                        <thead class="bg-white/5 text-[11px] text-brand-gold uppercase tracking-wider border-b border-white/10">
                            <tr>
                                <th class="py-3.5 px-4 w-12 text-center">#</th>
                                <th class="py-3.5 px-4">Tác Phẩm & Tác Giả</th>
                                <th class="py-3.5 px-4">Thể Loại</th>
                                <th class="py-3.5 px-4">Xuất Bản</th>
                                <th class="py-3.5 px-4">Giá Bìa Gốc</th>
                                <th class="py-3.5 px-4 text-center">Trạng Thái</th>
                                <th class="py-3.5 px-4 text-right">Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-white/5">
                            <c:forEach var="book" items="${bookPage.content}">
                                <tr class="hover:bg-white/5 transition">
                                    <td class="py-3.5 px-4 text-center font-mono text-slate-500 text-[11px]">${book.id}</td>
                                    <td class="py-3.5 px-4">
                                        <div class="flex items-center gap-3">
                                            <c:choose>
                                                <c:when test="${not empty book.coverImage}">
                                                    <img src="${book.coverImage}" alt="${book.title}" class="w-10 h-14 object-cover rounded shadow border border-white/10 shrink-0" />
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="w-10 h-14 rounded bg-white/5 border border-white/10 flex items-center justify-center text-slate-600 shrink-0">
                                                        <i class="fa-solid fa-book"></i>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                            <div>
                                                <div class="font-bold text-white text-sm hover:text-brand-gold transition line-clamp-1">
                                                    ${book.title}
                                                </div>
                                                <div class="text-[11px] text-slate-400 mt-0.5">
                                                    ${book.author}
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                    <td class="py-3.5 px-4">
                                        <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-medium bg-white/5 border border-white/10 text-slate-300">
                                            ${book.category}
                                        </span>
                                    </td>
                                    <td class="py-3.5 px-4 text-slate-400">
                                        <div>${book.publisher}</div>
                                        <c:if test="${not empty book.publicationYear}">
                                            <div class="text-[10px] text-slate-500">${book.publicationYear}</div>
                                        </c:if>
                                    </td>
                                    <td class="py-3.5 px-4 font-mono font-semibold text-brand-gold">
                                        <fmt:formatNumber value="${book.coverPrice}" pattern="#,###"/> đ
                                    </td>
                                    <td class="py-3.5 px-4 text-center">
                                        <c:choose>
                                            <c:when test="${book.active}">
                                                <span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-500/15 border border-emerald-500/30 text-emerald-400">
                                                    <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
                                                    Kinh doanh
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
                                        <sec:authorize access="hasAnyRole('ADMIN', 'MANAGER')">
                                        <div class="inline-flex items-center gap-1.5">
                                            <a href="/admin/books/${book.id}/edit" 
                                               class="w-7 h-7 rounded-lg bg-white/5 hover:bg-brand-gold/20 border border-white/10 hover:border-brand-gold/40 text-slate-300 hover:text-brand-gold flex items-center justify-center transition"
                                               title="Sửa thông tin tác phẩm">
                                                <i class="fa-solid fa-pen-to-square text-[11px]"></i>
                                            </a>
                                            <form action="/admin/books/${book.id}/toggle-status" method="post" class="inline">
                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                <button type="submit" 
                                                        onclick="return confirm('Bạn có chắc muốn ${book.active ? 'ngừng kinh doanh' : 'mở bán lại'} tựa sách này?')"
                                                        class="w-7 h-7 rounded-lg border flex items-center justify-center transition ${book.active ? 'bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border-rose-500/20' : 'bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border-emerald-500/20'}"
                                                        title="${book.active ? 'Ngừng kinh doanh' : 'Mở bán lại'}">
                                                    <i class="fa-solid ${book.active ? 'fa-lock' : 'fa-lock-open'} text-[11px]"></i>
                                                </button>
                                            </form>
                                        </div>
                                        </sec:authorize>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty bookPage.content}">
                                <tr>
                                    <td colspan="7" class="py-12 text-center text-slate-500">
                                        <i class="fa-solid fa-book-open text-3xl mb-3 text-slate-600 block"></i>
                                        Không tìm thấy tác phẩm nào phù hợp.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>

                <!-- Pagination -->
                <c:if test="${bookPage.totalPages > 1}">
                    <div class="p-4 border-t border-white/10 flex items-center justify-between text-xs text-slate-400">
                        <div>
                            Trang <span class="text-white font-medium">${bookPage.number + 1}</span> / <span class="text-white font-medium">${bookPage.totalPages}</span>
                            (Tổng <span class="text-brand-gold font-medium">${bookPage.totalElements}</span> đầu sách)
                        </div>
                        <div class="flex items-center gap-1.5">
                            <c:forEach begin="0" end="${bookPage.totalPages - 1}" var="i">
                                <a href="?page=${i}&keyword=${keyword}" 
                                   class="w-8 h-8 rounded-lg flex items-center justify-center font-medium transition ${bookPage.number == i ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300 border border-white/5'}">
                                    ${i + 1}
                                </a>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>
            </div>
        </main>
    </div>
</body>
</html>
