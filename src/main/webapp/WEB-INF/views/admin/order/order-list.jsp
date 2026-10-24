<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="adminTitle" value="Đơn Hàng Của Khách" scope="request"/>
<c:set var="adminActive" value="orders" scope="request"/>
<jsp:include page="/WEB-INF/views/admin/common/page-top.jsp"/>

<div class="pb-6 border-b border-white/5 mb-6">
    <span class="px-2.5 py-0.5 rounded-full bg-brand-gold/15 text-brand-gold text-[10px] font-semibold border border-brand-gold/30">NGHIỆP VỤ BÁN HÀNG</span>
    <h1 class="text-2xl sm:text-3xl font-serif font-bold text-white tracking-tight mt-1">Đơn Hàng Của Khách</h1>
    <p class="text-xs text-slate-400 mt-0.5">Xác nhận, chuẩn bị giao nhận, ghi nhận thanh toán và hoàn tất các đơn khách đặt trực tuyến</p>
</div>

<!-- Bộ lọc -->
<div class="glass-panel rounded-2xl p-4 mb-6 border border-white/10 shadow-lg">
    <form action="/admin/orders" method="get" class="grid grid-cols-1 sm:grid-cols-12 gap-3 items-center">
        <div class="sm:col-span-3">
            <c:choose>
                <c:when test="${isStaffScoped}">
                    <div class="glass-input w-full px-3 py-2 text-xs rounded-xl bg-sky-500/10 border border-sky-500/30 text-sky-300 font-semibold flex items-center gap-1.5">
                        <i class="fa-solid fa-store text-[10px]"></i><span class="truncate">${currentStoreName}</span>
                    </div>
                    <input type="hidden" name="storeId" value="${selectedStoreId}"/>
                </c:when>
                <c:otherwise>
                    <select name="storeId" class="glass-input w-full px-3 py-2 text-xs rounded-xl bg-[#161a22]">
                        <option value="">-- Tất cả chi nhánh --</option>
                        <c:forEach var="store" items="${stores}">
                            <option value="${store.id}" ${selectedStoreId == store.id ? 'selected' : ''}>${store.name}</option>
                        </c:forEach>
                    </select>
                </c:otherwise>
            </c:choose>
        </div>
        <div class="sm:col-span-3">
            <select name="status" class="glass-input w-full px-3 py-2 text-xs rounded-xl bg-[#161a22]">
                <option value="">-- Tất cả trạng thái --</option>
                <c:forEach var="st" items="${statuses}">
                    <option value="${st}" ${selectedStatus == st ? 'selected' : ''}>${st.label}</option>
                </c:forEach>
            </select>
        </div>
        <div class="sm:col-span-4">
            <input type="text" name="keyword" value="${selectedKeyword}" placeholder="Mã đơn, tên hoặc số điện thoại người nhận..." class="glass-input w-full px-3 py-2 text-xs rounded-xl placeholder-slate-500"/>
        </div>
        <div class="sm:col-span-2">
            <button type="submit" class="w-full py-2 px-3 rounded-xl bg-white/10 hover:bg-white/20 text-white text-xs font-semibold transition">Lọc Dữ Liệu</button>
        </div>
    </form>
</div>

<c:if test="${unassignedStoreWarning}">
    <div class="mb-5 p-4 rounded-2xl bg-amber-500/15 border border-amber-500/40 text-amber-200 text-xs flex items-center gap-3 shadow-lg">
        <i class="fa-solid fa-triangle-exclamation text-amber-400 text-xl shrink-0"></i>
        <div><strong class="font-bold block text-sm mb-0.5">Tài khoản chưa được phân bổ chi nhánh công tác!</strong>
            <span class="text-slate-300">Bạn chưa thể xem đơn hàng khi chưa có chi nhánh. Vui lòng liên hệ quản lý.</span></div>
    </div>
</c:if>
<c:if test="${not empty successMessage}">
    <div class="mb-5 p-3.5 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-300 text-xs flex items-center gap-2"><i class="fa-solid fa-circle-check"></i><span>${successMessage}</span></div>
</c:if>
<c:if test="${not empty errorMessage}">
    <div class="mb-5 p-3.5 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-300 text-xs flex items-center gap-2"><i class="fa-solid fa-circle-exclamation"></i><span>${errorMessage}</span></div>
</c:if>

<!-- Bảng đơn hàng -->
<div class="glass-panel rounded-2xl border border-white/10 overflow-hidden shadow-lg">
    <div class="overflow-x-auto">
        <table class="w-full text-left text-xs text-slate-300">
            <thead class="bg-white/5 text-[11px] text-brand-gold uppercase tracking-wider border-b border-white/10">
                <tr>
                    <th class="py-3.5 px-4 font-mono">Mã Đơn</th>
                    <th class="py-3.5 px-4">Người Nhận</th>
                    <th class="py-3.5 px-4">Chi Nhánh</th>
                    <th class="py-3.5 px-4 text-center">Số Cuốn</th>
                    <th class="py-3.5 px-4 text-right">Tổng Tiền</th>
                    <th class="py-3.5 px-4">Thanh Toán</th>
                    <th class="py-3.5 px-4">Trạng Thái</th>
                    <th class="py-3.5 px-4">Ngày Đặt</th>
                    <th class="py-3.5 px-4 text-right">Xử Lý</th>
                </tr>
            </thead>
            <tbody class="divide-y divide-white/5">
                <c:forEach var="order" items="${orderPage.content}">
                    <tr class="hover:bg-white/[0.03] transition">
                        <td class="py-3.5 px-4 font-mono font-semibold text-amber-300 whitespace-nowrap">${order.orderCode}</td>
                        <td class="py-3.5 px-4"><div class="text-slate-100 font-medium">${order.customerName}</div><div class="text-[11px] text-slate-500">${order.receiverPhone}</div></td>
                        <td class="py-3.5 px-4">${order.storeName}</td>
                        <td class="py-3.5 px-4 text-center">${order.itemCount}</td>
                        <td class="py-3.5 px-4 text-right font-bold text-emerald-400 whitespace-nowrap"><fmt:formatNumber value="${order.totalAmount}" pattern="#,###"/> đ</td>
                        <td class="py-3.5 px-4">
                            <div class="text-[11px] text-slate-400">${order.paymentMethodLabel}</div>
                            <span class="inline-block whitespace-nowrap mt-0.5 px-2 py-0.5 rounded-full border text-[10px] font-semibold ${order.paymentStatusClasses}">${order.paymentStatusLabel}</span>
                        </td>
                        <td class="py-3.5 px-4"><span class="inline-block whitespace-nowrap px-2.5 py-0.5 rounded-full border text-[10px] font-semibold ${order.statusClasses}">${order.statusLabel}</span></td>
                        <td class="py-3.5 px-4 text-slate-400">${order.formattedCreatedAt}</td>
                        <td class="py-3.5 px-4 text-right">
                            <a href="/admin/orders/${order.orderId}" class="inline-flex whitespace-nowrap items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white/5 hover:bg-brand-gold/20 border border-white/10 hover:border-brand-gold/40 text-slate-200 hover:text-brand-gold font-semibold transition">
                                <i class="fa-solid fa-arrow-right text-[10px]"></i> Xử lý
                            </a>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty orderPage.content}">
                    <tr><td colspan="9" class="py-12 text-center text-slate-500"><i class="fa-solid fa-bag-shopping text-3xl mb-3 text-slate-600 block"></i>Chưa có đơn hàng nào khớp với bộ lọc.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>

    <c:if test="${orderPage.totalPages > 1}">
        <div class="p-4 border-t border-white/10 flex items-center justify-between text-xs text-slate-400">
            <div>Trang <span class="text-white font-medium">${orderPage.number + 1}</span> / <span class="text-white font-medium">${orderPage.totalPages}</span>
                (Tổng <span class="text-brand-gold font-medium">${orderPage.totalElements}</span> đơn)</div>
            <div class="flex items-center gap-1.5">
                <c:forEach begin="0" end="${orderPage.totalPages - 1}" var="i">
                    <a href="?page=${i}&storeId=${selectedStoreId}&status=${selectedStatus}&keyword=${selectedKeyword}"
                       class="w-8 h-8 rounded-lg flex items-center justify-center font-medium transition ${orderPage.number == i ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">${i + 1}</a>
                </c:forEach>
            </div>
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/views/admin/common/page-bottom.jsp"/>
