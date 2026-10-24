<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="adminTitle" value="Mã Giảm Giá" scope="request"/>
<c:set var="adminActive" value="promotions" scope="request"/>
<jsp:include page="/WEB-INF/views/admin/common/page-top.jsp"/>

<div class="pb-6 border-b border-white/5 mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
    <div>
        <span class="px-2.5 py-0.5 rounded-full bg-brand-gold/15 text-brand-gold text-[10px] font-semibold border border-brand-gold/30">NGHIỆP VỤ BÁN HÀNG</span>
        <h1 class="text-2xl sm:text-3xl font-serif font-bold text-white tracking-tight mt-1">Mã Giảm Giá</h1>
        <p class="text-xs text-slate-400 mt-0.5">Tạo và quản lý khuyến mãi áp dụng khi khách đặt hàng, toàn chuỗi hoặc theo từng chi nhánh</p>
    </div>
    <a href="/admin/promotions/new" class="self-start sm:self-auto px-4 py-2.5 rounded-xl bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold text-xs hover:from-yellow-400 hover:to-amber-500 transition flex items-center gap-2">
        <i class="fa-solid fa-plus"></i> Tạo mã mới
    </a>
</div>

<c:if test="${not empty successMessage}">
    <div class="mb-5 p-3.5 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-300 text-xs flex items-center gap-2"><i class="fa-solid fa-circle-check"></i><span>${successMessage}</span></div>
</c:if>

<div class="glass-panel rounded-2xl border border-white/10 overflow-hidden shadow-lg">
    <div class="overflow-x-auto">
        <table class="w-full text-left text-xs text-slate-300">
            <thead class="bg-white/5 text-[11px] text-brand-gold uppercase tracking-wider border-b border-white/10">
                <tr>
                    <th class="py-3.5 px-4">Mã</th>
                    <th class="py-3.5 px-4">Mức Giảm</th>
                    <th class="py-3.5 px-4">Điều Kiện</th>
                    <th class="py-3.5 px-4">Thời Hạn</th>
                    <th class="py-3.5 px-4 text-center">Lượt Dùng</th>
                    <th class="py-3.5 px-4">Phạm Vi</th>
                    <th class="py-3.5 px-4">Trạng Thái</th>
                    <th class="py-3.5 px-4 text-right">Thao Tác</th>
                </tr>
            </thead>
            <tbody class="divide-y divide-white/5">
                <c:forEach var="promo" items="${promotionPage.content}">
                    <tr class="hover:bg-white/[0.03] transition">
                        <td class="py-3.5 px-4">
                            <div class="font-mono font-semibold text-amber-300 whitespace-nowrap">${promo.code}</div>
                            <div class="text-[11px] text-slate-500 max-w-[220px] truncate" title="${promo.description}">${promo.description}</div>
                        </td>
                        <td class="py-3.5 px-4 whitespace-nowrap">
                            <div class="text-emerald-400 font-bold">${promo.valueLabel}</div>
                            <c:if test="${promo.type == 'PERCENT' && promo.maxDiscountAmount != null}">
                                <div class="text-[11px] text-slate-500">tối đa <fmt:formatNumber value="${promo.maxDiscountAmount}" pattern="#,###"/> đ</div>
                            </c:if>
                        </td>
                        <td class="py-3.5 px-4 whitespace-nowrap">
                            <c:choose>
                                <c:when test="${promo.minOrderAmount.signum() > 0}">Đơn từ <fmt:formatNumber value="${promo.minOrderAmount}" pattern="#,###"/> đ</c:when>
                                <c:otherwise><span class="text-slate-500">Không tối thiểu</span></c:otherwise>
                            </c:choose>
                            <div class="text-[11px] text-slate-500">${promo.onePerCustomer ? 'Mỗi khách 1 lần' : 'Không giới hạn mỗi khách'}</div>
                        </td>
                        <td class="py-3.5 px-4 whitespace-nowrap text-slate-400">${promo.formattedStartsAt}<br/>${promo.formattedEndsAt}</td>
                        <td class="py-3.5 px-4 text-center whitespace-nowrap">
                            <span class="text-white font-semibold">${promo.usedCount}</span><span class="text-slate-500"> / ${promo.usageLimit == null ? '∞' : promo.usageLimit}</span>
                        </td>
                        <td class="py-3.5 px-4 whitespace-nowrap">${promo.scopeLabel}</td>
                        <td class="py-3.5 px-4"><span class="inline-block whitespace-nowrap px-2.5 py-0.5 rounded-full border text-[10px] font-semibold ${promo.statusClasses}">${promo.statusLabel}</span></td>
                        <td class="py-3.5 px-4 text-right whitespace-nowrap">
                            <a href="/admin/promotions/${promo.id}/edit" class="inline-flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg bg-white/5 hover:bg-brand-gold/20 border border-white/10 hover:border-brand-gold/40 text-slate-200 hover:text-brand-gold font-semibold transition"><i class="fa-solid fa-pen-to-square text-[10px]"></i> Sửa</a>
                            <form action="/admin/promotions/${promo.id}/toggle" method="post" class="inline">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <button type="submit" class="inline-flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg border font-semibold transition ${promo.active ? 'border-amber-500/30 text-amber-300 hover:bg-amber-500/10' : 'border-emerald-500/30 text-emerald-300 hover:bg-emerald-500/10'}">
                                    <i class="fa-solid ${promo.active ? 'fa-pause' : 'fa-play'} text-[10px]"></i> ${promo.active ? 'Tạm dừng' : 'Kích hoạt'}
                                </button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty promotionPage.content}">
                    <tr><td colspan="8" class="py-12 text-center text-slate-500"><i class="fa-solid fa-ticket text-3xl mb-3 text-slate-600 block"></i>Chưa có mã giảm giá nào.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>

    <c:if test="${promotionPage.totalPages > 1}">
        <div class="p-4 border-t border-white/10 flex items-center justify-between text-xs text-slate-400">
            <div>Trang <span class="text-white font-medium">${promotionPage.number + 1}</span> / <span class="text-white font-medium">${promotionPage.totalPages}</span>
                (Tổng <span class="text-brand-gold font-medium">${promotionPage.totalElements}</span> mã)</div>
            <div class="flex items-center gap-1.5">
                <c:forEach begin="0" end="${promotionPage.totalPages - 1}" var="i">
                    <a href="?page=${i}" class="w-8 h-8 rounded-lg flex items-center justify-center font-medium transition ${promotionPage.number == i ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">${i + 1}</a>
                </c:forEach>
            </div>
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/views/admin/common/page-bottom.jsp"/>
