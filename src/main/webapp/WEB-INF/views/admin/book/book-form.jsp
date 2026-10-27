<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html class="dark" data-bs-theme="dark" lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>${bookId != null ? 'Cập nhật Đầu Sách' : 'Thêm Đầu Sách Mới'} — OldBook</title>
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

    <div class="relative z-10 max-w-[1520px] w-full mx-auto flex flex-col lg:flex-row items-start gap-5">
        <!-- Unified Navigation Dock -->
        <jsp:include page="/WEB-INF/views/admin/common/nav-dock.jsp">
            <jsp:param name="active" value="books"/>
        </jsp:include>

        <!-- Main Display Frame -->
        <main class="flex-1 w-full glass-panel rounded-2xl sm:rounded-[32px] p-3.5 sm:p-5 lg:p-7 shadow-glass-elevated border border-white/10 relative overflow-hidden">
            <!-- Top Nav & Breadcrumbs -->
            <div class="flex items-center justify-between pb-6 border-b border-white/5 mb-6">
                <div>
                    <a href="/admin/books" class="text-xs text-brand-gold hover:text-brand-goldLight transition flex items-center gap-1.5 mb-1.5">
                        <i class="fa-solid fa-arrow-left"></i>
                        <span>Quay lại Danh Mục Đầu Sách</span>
                    </a>
                    <h1 class="text-2xl sm:text-3xl font-serif font-bold text-white tracking-tight">
                        ${bookId != null ? 'Cập Nhật Đầu Sách' : 'Thêm Đầu Sách Mới'}
                    </h1>
                    <p class="text-xs text-slate-400 mt-0.5">Thông tin chi tiết về tác phẩm, tác giả và giá bìa</p>
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
                <form:form action="${bookId != null ? '/admin/books/'.concat(bookId).concat('/edit') : '/admin/books/new'}" 
                           method="post" 
                           modelAttribute="bookForm" 
                           enctype="multipart/form-data" 
                           cssClass="space-y-4">

                    <div>
                        <label class="block text-xs font-medium text-slate-300 mb-1.5">Tựa sách (*)</label>
                        <form:input path="title" cssClass="glass-input w-full px-3.5 py-2.5 text-xs rounded-xl" placeholder="VD: Chiến Tranh và Hòa Bình" />
                        <form:errors path="title" cssClass="text-rose-400 text-[10px] mt-1 block" />
                    </div>

                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-medium text-slate-300 mb-1.5">Tác giả (*)</label>
                            <form:input path="author" cssClass="glass-input w-full px-3.5 py-2.5 text-xs rounded-xl" placeholder="VD: Lev Tolstoy" />
                            <form:errors path="author" cssClass="text-rose-400 text-[10px] mt-1 block" />
                        </div>
                        <div>
                            <label class="block text-xs font-medium text-slate-300 mb-1.5">Thể loại</label>
                            <form:input path="category" cssClass="glass-input w-full px-3.5 py-2.5 text-xs rounded-xl" placeholder="Tiểu thuyết, Triết học..." />
                            <form:errors path="category" cssClass="text-rose-400 text-[10px] mt-1 block" />
                        </div>
                    </div>

                    <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
                        <div>
                            <label class="block text-xs font-medium text-slate-300 mb-1.5">Nhà xuất bản</label>
                            <form:input path="publisher" cssClass="glass-input w-full px-3.5 py-2.5 text-xs rounded-xl" placeholder="NXB Văn Học" />
                        </div>
                        <div>
                            <label class="block text-xs font-medium text-slate-300 mb-1.5">Năm xuất bản</label>
                            <form:input path="publicationYear" type="number" cssClass="glass-input w-full px-3.5 py-2.5 text-xs rounded-xl" placeholder="1995" />
                            <form:errors path="publicationYear" cssClass="text-rose-400 text-[10px] mt-1 block" />
                        </div>
                        <div>
                            <label class="block text-xs font-medium text-slate-300 mb-1.5">Giá bìa (VNĐ)</label>
                            <form:input path="coverPrice" type="number" step="1000" cssClass="glass-input w-full px-3.5 py-2.5 text-xs rounded-xl" placeholder="150000" />
                            <form:errors path="coverPrice" cssClass="text-rose-400 text-[10px] mt-1 block" />
                        </div>
                    </div>

                    <div>
                        <label class="block text-xs font-medium text-slate-300 mb-1.5">Ảnh bìa tác phẩm (Upload Cloudinary)</label>
                        <input type="file" name="imageFile" class="glass-input w-full px-3.5 py-2 text-xs rounded-xl" accept="image/*" />
                        <c:if test="${not empty bookForm.coverImage}">
                            <div class="mt-3 flex items-center gap-3 p-2.5 rounded-xl bg-white/5 border border-white/5">
                                <img src="${bookForm.coverImage}" alt="Ảnh bìa" class="h-16 w-12 object-cover rounded shadow" />
                                <div>
                                    <span class="text-[11px] text-slate-400 block">Ảnh hiện tại</span>
                                    <span class="text-[10px] text-emerald-400">Đã lưu trữ trên Cloudinary</span>
                                </div>
                                <form:hidden path="coverImage" />
                            </div>
                        </c:if>
                    </div>

                    <div>
                        <label class="block text-xs font-medium text-slate-300 mb-1.5">Mô tả tóm tắt nội dung</label>
                        <form:textarea path="description" rows="3" cssClass="glass-input w-full px-3.5 py-2.5 text-xs rounded-xl" placeholder="Nội dung, bối cảnh lịch sử hoặc ghi chú đặc biệt..." />
                    </div>

                    <div class="pt-4 flex items-center gap-3">
                        <button type="submit" class="px-5 py-2.5 rounded-xl bg-gradient-to-r from-brand-gold to-amber-600 hover:from-amber-400 text-slate-950 font-bold text-xs shadow-glow-gold transition">
                            Lưu Thông Tin Sách
                        </button>
                        <a href="/admin/books" class="px-4 py-2.5 rounded-xl bg-white/10 hover:bg-white/15 text-slate-300 text-xs transition">
                            Hủy bỏ
                        </a>
                    </div>
                </form:form>
            </div>
        </main>
    </div>
</body>
</html>
