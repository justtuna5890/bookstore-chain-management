<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Đơn ${order.orderCode}" scope="request"/>
<jsp:include page="/WEB-INF/views/customer/common/layout-top.jsp"/>

<nav class="mt-5 text-xs text-slate-400 flex items-center gap-2">
    <a href="/orders" class="hover:text-brand-gold transition">Đơn hàng của tôi</a>
    <i class="fa-solid fa-chevron-right text-[9px] text-slate-600"></i>
    <span class="text-slate-200 font-mono">${order.orderCode}</span>
</nav>

<c:if test="${not empty orderSuccess}">
    <div class="mt-4 p-3.5 rounded-2xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-200 text-xs flex items-start gap-2.5">
        <i class="fa-solid fa-circle-check text-emerald-400 text-base mt-0.5"></i><span>${orderSuccess}</span>
    </div>
</c:if>
<c:if test="${not empty orderError}">
    <div class="mt-4 p-3.5 rounded-2xl bg-rose-500/15 border border-rose-500/30 text-rose-200 text-xs flex items-start gap-2.5">
        <i class="fa-solid fa-circle-exclamation text-rose-400 text-base mt-0.5"></i><span>${orderError}</span>
    </div>
</c:if>

<div class="mt-5 flex items-center justify-between gap-4 flex-wrap">
    <div>
        <h1 class="text-2xl font-serif font-bold text-white font-mono">${order.orderCode}</h1>
        <p class="text-xs text-slate-400 mt-1">Đặt lúc ${order.formattedCreatedAt}</p>
    </div>
    <span class="px-3 py-1 rounded-full border text-xs font-semibold ${order.statusClasses}">${order.statusLabel}</span>
</div>

<!-- Tiến trình -->
<c:choose>
    <c:when test="${order.status == 'CANCELLED'}">
        <div class="mt-5 p-3.5 rounded-2xl bg-rose-500/10 border border-rose-500/30 text-rose-200 text-xs"><i class="fa-solid fa-ban mr-1.5"></i>Đơn hàng này đã bị huỷ.</div>
    </c:when>
    <c:otherwise>
        <ol class="mt-5 grid grid-cols-4 gap-2">
            <c:forEach var="step" items="${order.timeline}">
                <li class="text-center">
                    <div class="h-1.5 rounded-full ${step.done ? 'bg-brand-gold' : 'bg-white/10'}"></div>
                    <p class="text-[11px] mt-1.5 ${step.current ? 'text-brand-gold font-semibold' : (step.done ? 'text-slate-200' : 'text-slate-500')}">${step.label}</p>
                </li>
            </c:forEach>
        </ol>
    </c:otherwise>
</c:choose>

<div class="grid grid-cols-1 lg:grid-cols-12 gap-6 mt-6">
    <!-- Sách trong đơn -->
    <section class="lg:col-span-7 rounded-2xl glass-panel-subtle border border-white/10 overflow-hidden self-start">
        <div class="px-4 py-3 bg-white/5 text-xs font-semibold text-brand-gold"><i class="fa-solid fa-store mr-1.5"></i>${order.storeName}</div>
        <ul class="divide-y divide-white/5">
            <c:forEach var="line" items="${order.lines}">
                <li class="p-4 flex items-center justify-between gap-4">
                    <div class="min-w-0">
                        <a href="/books/${line.bookId}" class="text-sm font-semibold text-white hover:text-brand-gold transition">${line.title}</a>
                        <p class="text-[11px] font-mono text-amber-300 mt-0.5">${line.copyCode}</p>
                    </div>
                    <span class="text-sm text-slate-100 shrink-0"><fmt:formatNumber value="${line.price}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></span>
                </li>
            </c:forEach>
        </ul>
        <dl class="p-4 border-t border-white/10 space-y-1.5 text-xs">
            <div class="flex justify-between"><dt class="text-slate-400">Tạm tính</dt><dd class="text-slate-100"><fmt:formatNumber value="${order.subtotal}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></dd></div>
            <c:if test="${order.discountAmount.signum() > 0}">
                <div class="flex justify-between"><dt class="text-slate-400">Giảm giá<c:if test="${not empty order.promotionCode}"> (${order.promotionCode})</c:if></dt><dd class="text-emerald-300">-<fmt:formatNumber value="${order.discountAmount}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></dd></div>
            </c:if>
            <div class="flex justify-between"><dt class="text-slate-400">Phí giao hàng</dt><dd class="text-slate-100">${order.shippingFee.signum() > 0 ? '' : 'Miễn phí'}<c:if test="${order.shippingFee.signum() > 0}"><fmt:formatNumber value="${order.shippingFee}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></c:if></dd></div>
            <div class="flex justify-between pt-2 border-t border-white/10 text-sm"><dt class="text-slate-200 font-semibold">Tổng cộng</dt><dd class="text-emerald-400 font-bold"><fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></dd></div>
        </dl>
    </section>

    <!-- Thông tin nhận hàng -->
    <aside class="lg:col-span-5 flex flex-col gap-4">
        <section class="rounded-2xl glass-panel-subtle border border-white/10 p-5 text-xs">
            <h2 class="text-xs uppercase tracking-wider font-semibold text-brand-gold mb-3">Nhận hàng</h2>
            <p class="text-slate-100 font-semibold">${order.customerName}</p>
            <p class="text-slate-400 mt-0.5">${order.receiverPhone}</p>
            <c:choose>
                <c:when test="${order.fulfillmentType == 'DELIVERY'}">
                    <p class="text-slate-300 mt-2"><i class="fa-solid fa-truck text-brand-gold mr-1.5"></i>Giao tận nơi: ${order.shippingAddress}</p>
                </c:when>
                <c:otherwise>
                    <p class="text-slate-300 mt-2"><i class="fa-solid fa-store text-brand-gold mr-1.5"></i>Nhận tại: ${order.storeName}</p>
                    <p class="text-slate-500 mt-0.5">${order.storeAddress} · ${order.storePhone}</p>
                </c:otherwise>
            </c:choose>
            <c:if test="${not empty order.note}"><p class="text-slate-400 mt-2 italic">“${order.note}”</p></c:if>
        </section>

        <section class="rounded-2xl glass-panel-subtle border border-white/10 p-5 text-xs">
            <h2 class="text-xs uppercase tracking-wider font-semibold text-brand-gold mb-3">Thanh toán</h2>
            <p class="text-slate-100">${order.paymentLabel}</p>
            <c:if test="${order.paymentMethod == 'BANK_TRANSFER' && order.paymentStatus == 'UNPAID' && order.status != 'CANCELLED'}">
                <p class="mt-2 p-2.5 rounded-lg bg-sky-500/10 border border-sky-500/30 text-sky-200 text-[11px] leading-relaxed">
                    Chuyển khoản đúng số tiền và ghi nội dung <strong class="font-mono">${order.orderCode}</strong>. Nhân viên sẽ xác nhận khi nhận được tiền.
                </p>
            </c:if>
        </section>

        <c:if test="${order.cancellable}">
            <form action="/orders/${order.orderCode}/cancel" method="post" onsubmit="return confirm('Huỷ đơn hàng ${order.orderCode}? Các cuốn sách sẽ được mở bán trở lại.')">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <button type="submit" class="w-full px-4 py-2.5 rounded-full border border-rose-500/40 text-rose-300 hover:bg-rose-500/10 text-xs font-semibold transition">
                    <i class="fa-solid fa-ban mr-1.5"></i>Huỷ đơn hàng
                </button>
            </form>
        </c:if>
    </aside>
</div>

<jsp:include page="/WEB-INF/views/customer/common/layout-bottom.jsp"/>
