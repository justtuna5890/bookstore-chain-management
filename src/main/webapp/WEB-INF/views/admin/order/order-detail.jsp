<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="adminTitle" value="Đơn ${fn:escapeXml(order.orderCode)}" scope="request"/>
<c:set var="adminActive" value="orders" scope="request"/>
<jsp:include page="/WEB-INF/views/admin/common/page-top.jsp"/>

<c:set var="open" value="${order.status != 'COMPLETED' && order.status != 'CANCELLED'}"/>

<div class="pb-6 border-b border-white/5 mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
    <div>
        <a href="/admin/orders" class="text-[11px] text-slate-400 hover:text-brand-gold transition"><i class="fa-solid fa-arrow-left mr-1"></i>Danh sách đơn hàng</a>
        <h1 class="text-2xl sm:text-3xl font-mono font-bold text-white tracking-tight mt-1">${fn:escapeXml(order.orderCode)}</h1>
        <p class="text-xs text-slate-400 mt-0.5">Đặt lúc ${order.formattedCreatedAt} · ${fn:escapeXml(order.storeName)}</p>
    </div>
    <span class="self-start sm:self-auto px-3.5 py-1.5 rounded-full border text-xs font-semibold ${order.statusClasses}">${order.statusLabel}</span>
</div>

<c:if test="${not empty successMessage}">
    <div class="mb-5 p-3.5 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-300 text-xs flex items-center gap-2"><i class="fa-solid fa-circle-check"></i><span>${fn:escapeXml(successMessage)}</span></div>
</c:if>
<c:if test="${not empty errorMessage}">
    <div class="mb-5 p-3.5 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-300 text-xs flex items-center gap-2"><i class="fa-solid fa-circle-exclamation"></i><span>${fn:escapeXml(errorMessage)}</span></div>
</c:if>

<!-- Tiến trình -->
<c:choose>
    <c:when test="${order.status == 'CANCELLED'}">
        <div class="mb-6 p-3.5 rounded-2xl bg-rose-500/10 border border-rose-500/30 text-rose-200 text-xs"><i class="fa-solid fa-ban mr-1.5"></i>Đơn hàng đã bị huỷ.
            <c:if test="${order.paymentStatus == 'REFUNDED'}"><strong class="text-amber-200">Cần hoàn tiền cho khách ngoài hệ thống.</strong></c:if></div>
    </c:when>
    <c:otherwise>
        <ol class="mb-6 grid grid-cols-4 gap-2">
            <c:forEach var="step" items="${order.timeline}">
                <li class="text-center">
                    <div class="h-1.5 rounded-full ${step.done ? 'bg-brand-gold' : 'bg-white/10'}"></div>
                    <p class="text-[11px] mt-1.5 ${step.current ? 'text-brand-gold font-semibold' : (step.done ? 'text-slate-200' : 'text-slate-500')}">${step.label}</p>
                </li>
            </c:forEach>
        </ol>
    </c:otherwise>
</c:choose>

<div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
    <!-- Sách trong đơn -->
    <section class="lg:col-span-7 glass-panel rounded-2xl border border-white/10 overflow-hidden self-start">
        <div class="overflow-x-auto">
        <table class="w-full text-left text-xs text-slate-300 min-w-[360px]">
            <thead class="bg-white/5 text-[11px] text-brand-gold uppercase tracking-wider border-b border-white/10">
                <tr><th class="py-3 px-4">Sách</th><th class="py-3 px-4">Mã bản sao</th><th class="py-3 px-4 text-right">Giá</th></tr>
            </thead>
            <tbody class="divide-y divide-white/5">
                <c:forEach var="line" items="${order.lines}">
                    <tr>
                        <td class="py-3 px-4 text-slate-100 font-medium">${fn:escapeXml(line.title)}</td>
                        <td class="py-3 px-4 font-mono text-amber-300">${fn:escapeXml(line.copyCode)}</td>
                        <td class="py-3 px-4 text-right"><fmt:formatNumber value="${line.price}" pattern="#,###"/> đ</td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
        </div>
        <dl class="p-4 border-t border-white/10 space-y-1.5 text-xs">
            <div class="flex justify-between"><dt class="text-slate-400">Tạm tính</dt><dd><fmt:formatNumber value="${order.subtotal}" pattern="#,###"/> đ</dd></div>
            <c:if test="${order.discountAmount.signum() > 0}">
                <div class="flex justify-between"><dt class="text-slate-400">Giảm giá<c:if test="${not empty order.promotionCode}"> (${fn:escapeXml(order.promotionCode)})</c:if></dt><dd class="text-emerald-300">-<fmt:formatNumber value="${order.discountAmount}" pattern="#,###"/> đ</dd></div>
            </c:if>
            <div class="flex justify-between"><dt class="text-slate-400">Phí giao hàng</dt><dd><fmt:formatNumber value="${order.shippingFee}" pattern="#,###"/> đ</dd></div>
            <div class="flex justify-between pt-2 border-t border-white/10 text-sm"><dt class="text-slate-200 font-semibold">Tổng cộng</dt><dd class="text-emerald-400 font-bold"><fmt:formatNumber value="${order.totalAmount}" pattern="#,###"/> đ</dd></div>
        </dl>
    </section>

    <aside class="lg:col-span-5 flex flex-col gap-4">
        <!-- Người nhận -->
        <section class="glass-panel rounded-2xl border border-white/10 p-5 text-xs">
            <h2 class="text-[11px] uppercase tracking-wider font-semibold text-brand-gold mb-3">Người nhận</h2>
            <p class="text-slate-100 font-semibold text-sm">${fn:escapeXml(order.customerName)}</p>
            <p class="text-slate-400 mt-0.5">${fn:escapeXml(order.receiverPhone)}</p>
            <p class="mt-2 text-slate-300"><i class="fa-solid ${order.fulfillmentType == 'DELIVERY' ? 'fa-truck' : 'fa-store'} text-brand-gold mr-1.5"></i>${order.fulfillmentLabel}
                <c:if test="${order.fulfillmentType == 'DELIVERY'}">: ${fn:escapeXml(order.shippingAddress)}</c:if></p>
            <c:if test="${not empty order.note}"><p class="mt-2 text-slate-400 italic">“${fn:escapeXml(order.note)}”</p></c:if>
        </section>

        <!-- Thanh toán -->
        <section class="glass-panel rounded-2xl border border-white/10 p-5 text-xs">
            <h2 class="text-[11px] uppercase tracking-wider font-semibold text-brand-gold mb-3">Thanh toán</h2>
            <p class="text-slate-100">${order.paymentMethodLabel}</p>
            <span class="inline-block mt-1.5 px-2.5 py-0.5 rounded-full border text-[10px] font-semibold ${order.paymentStatusClasses}">${order.paymentStatusLabel}</span>
            <c:if test="${not empty order.transactionCode}"><p class="mt-2 text-slate-400">Mã giao dịch: <span class="font-mono text-slate-200">${fn:escapeXml(order.transactionCode)}</span></p></c:if>
            <c:if test="${not empty order.formattedPaidAt}"><p class="text-slate-500 mt-0.5">Lúc ${order.formattedPaidAt}</p></c:if>

            <c:if test="${open && order.paymentMethod == 'BANK_TRANSFER' && order.paymentStatus == 'UNPAID'}">
                <form action="/admin/orders/${order.orderId}/payment" method="post" class="mt-4 flex gap-2">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                    <input type="text" name="transactionCode" maxlength="100" required placeholder="Mã giao dịch ngân hàng" class="glass-input flex-1 px-3 py-2 text-xs rounded-xl placeholder-slate-500"/>
                    <button type="submit" class="px-3 py-2 rounded-xl bg-emerald-500/15 hover:bg-emerald-500/25 border border-emerald-500/30 text-emerald-300 font-semibold transition whitespace-nowrap">Đã nhận tiền</button>
                </form>
                <p class="mt-2 text-[11px] text-slate-500">Khách chuyển khoản với nội dung <span class="font-mono text-slate-300">${fn:escapeXml(order.orderCode)}</span>.</p>
            </c:if>
            <c:if test="${open && order.paymentMethod == 'COD' && order.paymentStatus == 'UNPAID'}">
                <p class="mt-3 text-[11px] text-slate-500">Tiền mặt được ghi nhận tự động khi hoàn tất đơn.</p>
            </c:if>
        </section>

        <!-- Thao tác -->
        <c:if test="${open}">
            <section class="glass-panel rounded-2xl border border-white/10 p-5 text-xs">
                <h2 class="text-[11px] uppercase tracking-wider font-semibold text-brand-gold mb-3">Thao tác</h2>
                <div class="flex flex-col gap-2.5">
                    <c:choose>
                        <c:when test="${order.status == 'PENDING'}">
                            <form action="/admin/orders/${order.orderId}/confirm" method="post">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <button type="submit" class="w-full px-4 py-2.5 rounded-xl bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold hover:from-yellow-400 hover:to-amber-500 transition"><i class="fa-solid fa-check mr-1.5"></i>Xác nhận đơn hàng</button>
                            </form>
                        </c:when>
                        <c:when test="${order.status == 'CONFIRMED'}">
                            <form action="/admin/orders/${order.orderId}/ready" method="post">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <button type="submit" class="w-full px-4 py-2.5 rounded-xl bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold hover:from-yellow-400 hover:to-amber-500 transition">
                                    <i class="fa-solid ${order.fulfillmentType == 'DELIVERY' ? 'fa-truck' : 'fa-box-open'} mr-1.5"></i>${order.fulfillmentType == 'DELIVERY' ? 'Bắt đầu giao hàng' : 'Đã chuẩn bị xong, chờ khách đến nhận'}
                                </button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <form action="/admin/orders/${order.orderId}/complete" method="post" onsubmit="return confirm('Xác nhận đơn đã giao nhận xong? Các bản sách sẽ chuyển sang ĐÃ BÁN.')">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <button type="submit" class="w-full px-4 py-2.5 rounded-xl bg-gradient-to-r from-emerald-500 to-emerald-600 text-slate-950 font-bold hover:from-emerald-400 hover:to-emerald-500 transition"><i class="fa-solid fa-flag-checkered mr-1.5"></i>Hoàn tất đơn hàng</button>
                            </form>
                        </c:otherwise>
                    </c:choose>
                    <form action="/admin/orders/${order.orderId}/cancel" method="post" onsubmit="return confirm('Huỷ đơn ${fn:escapeXml(order.orderCode)}? Các bản sách sẽ được mở bán trở lại.')">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <button type="submit" class="w-full px-4 py-2.5 rounded-xl border border-rose-500/40 text-rose-300 hover:bg-rose-500/10 font-semibold transition"><i class="fa-solid fa-ban mr-1.5"></i>Huỷ đơn hàng</button>
                    </form>
                </div>
            </section>
        </c:if>
    </aside>
</div>

<jsp:include page="/WEB-INF/views/admin/common/page-bottom.jsp"/>
