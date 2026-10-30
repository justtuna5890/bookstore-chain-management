<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Đặt hàng thành công" scope="request"/>
<jsp:include page="/WEB-INF/views/customer/common/layout-top.jsp"/>

<div class="mt-8 text-center">
    <div class="w-14 h-14 mx-auto rounded-full bg-emerald-500/15 border border-emerald-500/40 flex items-center justify-center">
        <i class="fa-solid fa-check text-emerald-400 text-xl"></i>
    </div>
    <h1 class="text-2xl md:text-3xl font-serif font-bold text-white mt-4">Đặt hàng thành công</h1>
    <p class="text-xs text-slate-400 mt-1">Đã tạo ${placedOrders.size()} đơn hàng. Chi nhánh sẽ xác nhận và liên hệ với bạn sớm nhất.</p>
</div>

<div class="mt-8 grid grid-cols-1 md:grid-cols-2 gap-5 max-w-4xl mx-auto">
    <c:forEach var="order" items="${placedOrders}">
        <section class="rounded-2xl glass-panel-subtle border border-white/10 overflow-hidden">
            <div class="px-4 py-3 bg-white/5 flex items-center justify-between">
                <span class="font-mono text-sm text-amber-300 font-semibold">${fn:escapeXml(order.orderCode)}</span>
                <span class="text-[11px] text-slate-400">${order.formattedCreatedAt}</span>
            </div>
            <div class="p-4 text-xs">
                <p class="text-slate-200 font-semibold"><i class="fa-solid fa-store text-brand-gold mr-1.5"></i>${fn:escapeXml(order.storeName)}</p>
                <ul class="mt-3 space-y-1">
                    <c:forEach var="line" items="${order.lines}">
                        <li class="flex justify-between gap-3"><span class="text-slate-300 truncate">${fn:escapeXml(line.title)} <span class="font-mono text-slate-500">(${fn:escapeXml(line.copyCode)})</span></span><span class="text-slate-100 shrink-0"><fmt:formatNumber value="${line.price}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></span></li>
                    </c:forEach>
                </ul>
                <dl class="mt-3 pt-3 border-t border-white/10 space-y-1.5">
                    <c:if test="${order.discountAmount.signum() > 0}">
                        <div class="flex justify-between"><dt class="text-slate-400">Giảm giá (${fn:escapeXml(order.promotionCode)})</dt><dd class="text-emerald-300">-<fmt:formatNumber value="${order.discountAmount}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></dd></div>
                    </c:if>
                    <div class="flex justify-between"><dt class="text-slate-400">Nhận hàng</dt><dd class="text-slate-100">${order.fulfillmentType == 'DELIVERY' ? 'Giao tận nơi' : 'Nhận tại chi nhánh'}</dd></div>
                    <div class="flex justify-between"><dt class="text-slate-400">Thanh toán</dt><dd class="text-slate-100">${order.paymentMethod == 'COD' ? 'Tiền mặt khi nhận hàng' : 'Chuyển khoản'}</dd></div>
                    <div class="flex justify-between text-sm pt-1"><dt class="text-slate-200 font-semibold">Tổng cộng</dt><dd class="text-emerald-400 font-bold"><fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></dd></div>
                </dl>
                <c:if test="${order.paymentMethod == 'BANK_TRANSFER'}">
                    <p class="mt-3 p-2.5 rounded-lg bg-sky-500/10 border border-sky-500/30 text-sky-200 text-[11px] leading-relaxed">
                        Vui lòng chuyển khoản đúng số tiền và ghi nội dung <strong class="font-mono">${fn:escapeXml(order.orderCode)}</strong>. Nhân viên sẽ xác nhận khi nhận được tiền.
                    </p>
                    <jsp:include page="/WEB-INF/views/customer/common/transfer-qr.jsp"/>
                </c:if>
            </div>
        </section>
    </c:forEach>
</div>

<div class="mt-8 flex items-center justify-center gap-3 flex-wrap">
    <a href="/orders" class="inline-flex items-center gap-2 px-5 py-2 rounded-full border border-brand-gold/50 text-brand-gold hover:bg-brand-gold/10 font-bold text-xs transition">
        <i class="fa-solid fa-receipt text-[11px]"></i> Xem đơn hàng của tôi
    </a>
    <a href="/" class="inline-flex items-center gap-2 px-5 py-2 rounded-full bg-gradient-to-r from-brand-gold to-yellow-500 text-slate-950 font-bold text-xs hover:from-yellow-400 hover:to-amber-500 transition">
        <i class="fa-solid fa-book-open text-[11px]"></i> Tiếp tục khám phá sách
    </a>
</div>

<jsp:include page="/WEB-INF/views/customer/common/layout-bottom.jsp"/>
