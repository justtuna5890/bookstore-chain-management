<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html class="dark" lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>Hồ Sơ Thu Mua #${buybackRequest.id} — OldBook</title>
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
            <jsp:param name="active" value="buybacks"/>
        </jsp:include>

        <!-- Main Display Frame -->
        <main class="flex-1 w-full glass-panel rounded-[32px] p-5 sm:p-7 shadow-glass-elevated border border-white/10 relative overflow-hidden">
            <!-- Top Nav & Actions -->
            <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b border-white/5 mb-6">
                <div>
                    <a href="/admin/buybacks" class="text-xs text-brand-gold hover:text-brand-goldLight transition flex items-center gap-1.5 mb-1.5">
                        <i class="fa-solid fa-arrow-left"></i>
                        <span>Quay lại Danh sách đơn thu mua</span>
                    </a>
                    <h1 class="text-2xl sm:text-3xl font-serif font-bold text-white tracking-tight">
                        Thẩm Định Hồ Sơ Thu Mua #${buybackRequest.id}
                    </h1>
                    <p class="text-xs text-slate-400 mt-0.5">Kiểm định chất lượng từng cuốn sách, định giá và hoàn tất thủ tục nhập kho</p>
                </div>
                <div class="flex items-center gap-2.5">
                    <a href="/admin/export/pdf/buyback/${buybackRequest.id}" target="_blank" 
                       class="px-3.5 py-2.5 rounded-xl bg-rose-500/10 hover:bg-rose-500/20 text-rose-300 font-semibold text-xs border border-rose-500/30 transition flex items-center gap-2 shadow-sm">
                        <i class="fa-solid fa-file-pdf text-rose-400"></i>
                        <span>Xuất Phiếu PDF</span>
                    </a>
                    <a href="/admin/buybacks" class="px-4 py-2.5 rounded-xl bg-white/10 hover:bg-white/15 text-slate-300 text-xs transition">
                        Đóng
                    </a>
                </div>
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

            <div class="max-w-4xl">
                <!-- Metadata Cards -->
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 pb-6 mb-6 border-b border-white/10">
                    <div class="p-4 rounded-2xl bg-white/5 border border-white/5">
                        <span class="text-[11px] text-slate-400 block mb-1">Khách hàng gửi sách:</span>
                        <h3 class="text-base font-serif font-bold text-white flex items-center gap-2">
                            <i class="fa-solid fa-user text-brand-gold text-xs"></i>
                            <span>${buybackRequest.customer.fullName}</span>
                        </h3>
                        <p class="text-xs text-brand-goldLight mt-1 font-mono">${buybackRequest.customer.email}</p>
                        <c:if test="${not empty buybackRequest.customer.phone}">
                            <p class="text-xs text-slate-400 font-mono mt-0.5"><i class="fa-solid fa-phone text-[10px] mr-1"></i>${buybackRequest.customer.phone}</p>
                        </c:if>
                    </div>
                    <div class="p-4 rounded-2xl bg-white/5 border border-white/5">
                        <span class="text-[11px] text-slate-400 block mb-1">Chi nhánh tiếp nhận xử lý:</span>
                        <h3 class="text-base font-serif font-bold text-white flex items-center gap-2">
                            <i class="fa-solid fa-shop text-brand-gold text-xs"></i>
                            <span>${buybackRequest.store.name}</span>
                        </h3>
                        <p class="text-xs text-slate-400 mt-1">${buybackRequest.store.address}</p>
                        <p class="text-xs text-slate-400 font-mono mt-0.5"><i class="fa-solid fa-phone text-[10px] mr-1"></i>${buybackRequest.store.phone}</p>
                    </div>
                </div>

                <form action="/admin/buybacks/${buybackRequest.id}/update" method="post" class="space-y-6">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                    <div class="w-full sm:w-1/2">
                        <label class="block text-xs font-medium text-slate-300 mb-1.5">Trạng thái phê duyệt đơn (*)</label>
                        <select name="status" class="glass-input w-full px-3.5 py-2.5 text-xs rounded-xl bg-[#161a22]">
                            <c:forEach var="st" items="${statuses}">
                                <option value="${st}" ${buybackRequest.status == st ? 'selected' : ''}>${st}</option>
                            </c:forEach>
                        </select>
                        <span class="text-[10px] text-slate-400 mt-1 block">Lưu ý: Chuyển sang <strong>COMPLETED</strong> sẽ tự động sinh mã Barcode và nhập các cuốn sách vào kho chi nhánh.</span>
                    </div>

                    <!-- Items Table -->
                    <div>
                        <h4 class="text-sm font-serif font-bold text-white mb-3">Danh sách tác phẩm thẩm định:</h4>
                        <div class="glass-panel rounded-2xl border border-white/10 overflow-hidden shadow-lg">
                            <table class="w-full text-left text-xs text-slate-300">
                                <thead class="bg-white/5 text-[11px] text-brand-gold uppercase tracking-wider border-b border-white/10">
                                    <tr>
                                        <th class="py-3 px-4">Tác Phẩm</th>
                                        <th class="py-3 px-4">Tác Giả</th>
                                        <th class="py-3 px-4">Tình Trạng</th>
                                        <th class="py-3 px-4 w-44">Giá Thu Mua Đề Xuất (VNĐ)</th>
                                    </tr>
                                </thead>
                                <tbody class="divide-y divide-white/5">
                                    <c:forEach var="item" items="${buybackRequest.items}">
                                        <tr class="hover:bg-white/5 transition">
                                            <td class="py-3 px-4 font-semibold text-white">
                                                <input type="hidden" name="itemIds" value="${item.id}" />
                                                <div class="flex items-center gap-2">
                                                    <i class="fa-solid fa-book-bookmark text-brand-gold"></i>
                                                    <span>${item.bookTitle}</span>
                                                </div>
                                            </td>
                                            <td class="py-3 px-4 text-slate-400">${item.bookAuthor}</td>
                                            <td class="py-3 px-4">
                                                <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-medium bg-white/5 border border-white/10 text-brand-goldLight">
                                                    ${item.proposedCondition}
                                                </span>
                                            </td>
                                            <td class="py-3 px-4">
                                                <input type="number" step="1000" name="offeredPrices" value="${item.offeredPrice != null ? item.offeredPrice : 0}" 
                                                       class="glass-input w-full px-3 py-1.5 text-xs rounded-xl font-mono text-brand-gold font-bold" />
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div>
                        <label class="block text-xs font-medium text-slate-300 mb-1.5">Ghi chú của nhân viên thẩm định</label>
                        <textarea name="staffNote" rows="3" class="glass-input w-full px-3.5 py-2.5 text-xs rounded-xl" placeholder="Đánh giá chi tiết tình trạng gáy, trang phụ hoặc phản hồi khách hàng...">${buybackRequest.staffNote}</textarea>
                    </div>

                    <c:choose>
                        <c:when test="${buybackRequest.status == 'COMPLETED' || buybackRequest.status == 'REJECTED'}">
                            <div class="p-4 rounded-xl bg-white/5 border border-white/10 text-slate-300 text-xs flex items-center gap-2">
                                <i class="fa-solid fa-lock text-brand-gold text-sm"></i>
                                <span>Hồ sơ này đã ở trạng thái <strong>${buybackRequest.status}</strong> (đã đóng). Không thể chỉnh sửa hoặc thay đổi trạng thái thêm.</span>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="pt-4 flex items-center gap-3">
                                <button type="submit" class="px-5 py-2.5 rounded-xl bg-gradient-to-r from-brand-gold to-amber-600 hover:from-amber-400 text-slate-950 font-bold text-xs shadow-glow-gold transition">
                                    Cập Nhật Hồ Sơ Thu Mua
                                </button>
                                <a href="/admin/buybacks" class="px-4 py-2.5 rounded-xl bg-white/10 hover:bg-white/15 text-slate-300 text-xs transition">
                                    Quay lại danh sách
                                </a>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </form>
            </div>
        </main>
    </div>
</body>
</html>
