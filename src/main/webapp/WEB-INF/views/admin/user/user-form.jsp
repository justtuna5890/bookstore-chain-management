<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html class="dark" lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>${empty userId ? 'Thêm Tài Khoản' : 'Chỉnh Sửa Tài Khoản'} — OldBook</title>
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
    
        /* Dark Luxury Styling for Select Dropdowns & Options */
        select, .glass-input {
            color-scheme: dark !important;
        }
        select option {
            background-color: #12151c !important;
            color: #f1f5f9 !important;
            padding: 8px 12px !important;
        }
        select option:hover,
        select option:focus,
        select option:checked {
            background-color: #1e2430 !important;
            color: #D4AF37 !important;
            font-weight: 600 !important;
        }
    </style>
</head>
<body class="bg-reading-room min-h-screen relative flex flex-col justify-between py-6 px-4 md:px-8 lg:px-12 selection:bg-brand-gold selection:text-black">
    <div class="ambient-glow top-12 left-1/4"></div>
    <div class="ambient-glow bottom-20 right-10"></div>

    <div class="relative z-10 max-w-[1520px] w-full mx-auto flex flex-col xl:flex-row items-start gap-5">
        <!-- Unified Navigation Dock -->
        <jsp:include page="/WEB-INF/views/admin/common/nav-dock.jsp">
            <jsp:param name="active" value="users"/>
        </jsp:include>

        <!-- Main Display Frame -->
        <main class="flex-1 w-full glass-panel rounded-[32px] p-5 sm:p-7 shadow-glass-elevated border border-white/10 relative overflow-hidden">
            <!-- Top Nav & Breadcrumbs -->
            <div class="flex items-center justify-between pb-6 border-b border-white/5 mb-6">
                <div>
                    <a href="/admin/users" class="text-xs text-brand-gold hover:text-brand-goldLight transition flex items-center gap-1.5 mb-1.5">
                        <i class="fa-solid fa-arrow-left"></i>
                        <span>Quay Lại Danh Sách Nhân Sự</span>
                    </a>
                    <h1 class="text-2xl sm:text-3xl font-serif font-bold text-white tracking-tight">
                        <c:choose>
                            <c:when test="${empty userId}">Thêm Tài Khoản Mới</c:when>
                            <c:otherwise>Chỉnh Sửa Tài Khoản #${userId}</c:otherwise>
                        </c:choose>
                    </h1>
                    <p class="text-xs text-slate-400 mt-1">Cập nhật hồ sơ tài khoản người dùng và thiết lập phân quyền.</p>
                </div>
            </div>

            <!-- Flash error message -->
            <c:if test="${not empty errorMessage}">
                <div class="mb-5 p-3.5 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-300 text-xs flex items-center justify-between">
                    <div class="flex items-center gap-2">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <span>${errorMessage}</span>
                    </div>
                    <button onclick="this.parentElement.remove()" class="text-rose-400 hover:text-rose-200"><i class="fa-solid fa-xmark"></i></button>
                </div>
            </c:if>

            <div class="max-w-2xl">
                <c:set var="formAction" value="${empty userId ? '/admin/users/new' : '/admin/users/' += userId += '/edit'}"/>
                <form action="${formAction}" method="post">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-5 text-xs">
                        <!-- Username -->
                        <div>
                            <label class="block text-slate-400 font-medium mb-1.5 uppercase tracking-wider text-[11px]">
                                Tên đăng nhập <span class="text-rose-400">*</span>
                            </label>
                            <input type="text" name="username" value="${userForm.username}"
                                   class="w-full px-3.5 py-2.5 rounded-xl glass-input focus:ring-0 text-slate-100 placeholder-slate-500 ${not empty userId ? 'opacity-60 cursor-not-allowed bg-white/[0.02]' : ''}"
                                   ${not empty userId ? 'readonly' : 'required placeholder="VD: nguyenvan_a"'}/>
                            <c:if test="${not empty userId}">
                                <p class="text-[11px] text-slate-500 mt-1 italic"><i class="fa-solid fa-circle-info mr-1 text-[10px]"></i>Tên đăng nhập không thể thay đổi sau khi tạo.</p>
                            </c:if>
                        </div>

                        <!-- Password -->
                        <div>
                            <label class="block text-slate-400 font-medium mb-1.5 uppercase tracking-wider text-[11px]">
                                Mật khẩu
                                <c:if test="${empty userId}"><span class="text-rose-400">*</span></c:if>
                                <c:if test="${not empty userId}"><span class="text-slate-500 font-normal lowercase">(để trống = giữ nguyên)</span></c:if>
                            </label>
                            <input type="password" name="password"
                                   class="w-full px-3.5 py-2.5 rounded-xl glass-input focus:ring-0 text-slate-100 placeholder-slate-500"
                                   ${empty userId ? 'required minlength=6 placeholder="Tối thiểu 6 ký tự"' : 'placeholder="Nhập nếu muốn đổi mật khẩu mới"'}/>
                        </div>

                        <!-- Full Name -->
                        <div class="sm:col-span-2">
                            <label class="block text-slate-400 font-medium mb-1.5 uppercase tracking-wider text-[11px]">
                                Họ và Tên <span class="text-rose-400">*</span>
                            </label>
                            <input type="text" name="fullName" value="${userForm.fullName}" required
                                   placeholder="VD: Nguyễn Văn A"
                                   class="w-full px-3.5 py-2.5 rounded-xl glass-input focus:ring-0 text-slate-100 placeholder-slate-500"/>
                        </div>

                        <!-- Email -->
                        <div>
                            <label class="block text-slate-400 font-medium mb-1.5 uppercase tracking-wider text-[11px]">
                                Email Liên Hệ <span class="text-rose-400">*</span>
                            </label>
                            <input type="email" name="email" value="${userForm.email}" required
                                   placeholder="email@example.com"
                                   class="w-full px-3.5 py-2.5 rounded-xl glass-input focus:ring-0 text-slate-100 placeholder-slate-500"/>
                        </div>

                        <!-- Phone -->
                        <div>
                            <label class="block text-slate-400 font-medium mb-1.5 uppercase tracking-wider text-[11px]">
                                Số Điện Thoại
                            </label>
                            <input type="text" name="phone" value="${userForm.phone}"
                                   placeholder="0987654321"
                                   class="w-full px-3.5 py-2.5 rounded-xl glass-input focus:ring-0 text-slate-100 placeholder-slate-500"/>
                        </div>

                        <!-- Role -->
                        <div>
                            <label class="block text-slate-400 font-medium mb-1.5 uppercase tracking-wider text-[11px]">
                                Vai Trò Hệ Thống <span class="text-rose-400">*</span>
                            </label>
                            <select name="role" required class="w-full px-3.5 py-2.5 rounded-xl glass-input focus:ring-0 text-slate-200">
                                <c:forEach var="r" items="${roles}">
                                    <option value="${r}" ${userForm.role == r ? 'selected' : ''} class="bg-slate-900 text-slate-200">${r}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <!-- Store Assignment -->
                        <div>
                            <label class="block text-slate-400 font-medium mb-1.5 uppercase tracking-wider text-[11px]">
                                Chi Nhánh Phụ Trách
                            </label>
                            <select name="storeId" class="w-full px-3.5 py-2.5 rounded-xl glass-input focus:ring-0 text-slate-200">
                                <option value="" class="bg-slate-900 text-slate-400">— Toàn hệ thống (Không gán) —</option>
                                <c:forEach var="store" items="${stores}">
                                    <option value="${store.id}" ${userForm.storeId == store.id ? 'selected' : ''} class="bg-slate-900 text-slate-200">${store.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <!-- Action Buttons -->
                    <div class="flex items-center gap-3 mt-6 pt-4 border-t border-white/10">
                        <button type="submit"
                                class="px-5 py-2.5 rounded-xl bg-gradient-to-r from-brand-gold to-amber-600 hover:from-amber-400 text-slate-950 font-bold text-xs shadow-glow-gold transition flex items-center gap-2">
                            <i class="fa-solid fa-floppy-disk"></i>
                            <span>
                                <c:choose>
                                    <c:when test="${empty userId}">Tạo Tài Khoản</c:when>
                                    <c:otherwise>Lưu Thay Đổi</c:otherwise>
                                </c:choose>
                            </span>
                        </button>
                        <a href="/admin/users" class="px-4 py-2.5 rounded-xl bg-white/10 hover:bg-white/15 text-slate-300 text-xs transition">
                            Hủy
                        </a>
                    </div>
                </form>
            </div>
        </main>
    </div>
</body>
</html>
