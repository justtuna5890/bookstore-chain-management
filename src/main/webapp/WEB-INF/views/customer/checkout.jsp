<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Đặt hàng" scope="request"/>
<jsp:include page="/WEB-INF/views/customer/common/layout-top.jsp"/>

<div class="mt-6">
    <h1 class="text-2xl md:text-3xl font-serif font-bold text-white">Đặt hàng</h1>
    <p class="text-xs text-slate-400 mt-1">Kiểm tra thông tin nhận hàng và chọn hình thức thanh toán.</p>
</div>

<c:if test="${not empty checkoutError}">
    <div class="mt-4 p-3.5 rounded-2xl bg-rose-500/15 border border-rose-500/30 text-rose-200 text-xs flex items-start gap-2.5">
        <i class="fa-solid fa-circle-exclamation text-rose-400 text-base mt-0.5"></i><span>${checkoutError}</span>
    </div>
</c:if>
<c:if test="${not empty formErrors}">
    <div class="mt-4 p-3.5 rounded-2xl bg-rose-500/15 border border-rose-500/30 text-rose-200 text-xs">
        <ul class="list-disc pl-5 space-y-0.5"><c:forEach var="err" items="${formErrors}"><li>${err}</li></c:forEach></ul>
    </div>
</c:if>

<form action="/checkout" method="post" id="checkout-form" class="grid grid-cols-1 lg:grid-cols-12 gap-6 mt-6">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

    <div class="lg:col-span-7 flex flex-col gap-5">
        <!-- Người nhận -->
        <section class="rounded-2xl glass-panel-subtle border border-white/10 p-5">
            <h2 class="text-xs uppercase tracking-wider font-semibold text-brand-gold mb-4">Thông tin người nhận</h2>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <label class="block text-xs text-slate-300">Họ và tên
                    <input type="text" name="receiverName" value="${form.receiverName}" maxlength="100" required class="form-field mt-1.5 w-full px-3 py-2 rounded-xl text-sm text-slate-100"/>
                </label>
                <label class="block text-xs text-slate-300">Số điện thoại
                    <input type="tel" name="receiverPhone" value="${form.receiverPhone}" maxlength="15" required class="form-field mt-1.5 w-full px-3 py-2 rounded-xl text-sm text-slate-100"/>
                </label>
            </div>
        </section>

        <!-- Hình thức nhận hàng -->
        <section class="rounded-2xl glass-panel-subtle border border-white/10 p-5">
            <h2 class="text-xs uppercase tracking-wider font-semibold text-brand-gold mb-4">Hình thức nhận hàng</h2>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <label class="flex items-start gap-3 p-3.5 rounded-xl border border-white/10 hover:border-brand-gold/40 cursor-pointer transition has-[:checked]:border-brand-gold has-[:checked]:bg-brand-gold/10">
                    <input type="radio" name="fulfillmentType" value="PICKUP" ${form.fulfillmentType == 'PICKUP' ? 'checked' : ''} class="mt-1 accent-yellow-500"/>
                    <span class="text-xs"><strong class="text-slate-100 text-sm block">Nhận tại chi nhánh</strong><span class="text-slate-400">Miễn phí, đến lấy khi đơn sẵn sàng</span></span>
                </label>
                <label class="flex items-start gap-3 p-3.5 rounded-xl border border-white/10 hover:border-brand-gold/40 cursor-pointer transition has-[:checked]:border-brand-gold has-[:checked]:bg-brand-gold/10">
                    <input type="radio" name="fulfillmentType" value="DELIVERY" ${form.fulfillmentType == 'DELIVERY' ? 'checked' : ''} class="mt-1 accent-yellow-500"/>
                    <span class="text-xs"><strong class="text-slate-100 text-sm block">Giao tận nơi</strong><span class="text-slate-400">+<fmt:formatNumber value="${deliveryFee}" type="currency" currencySymbol="₫" maxFractionDigits="0"/> mỗi đơn</span></span>
                </label>
            </div>
            <label id="address-field" class="block text-xs text-slate-300 mt-4 ${form.fulfillmentType == 'DELIVERY' ? '' : 'hidden'}">Địa chỉ giao hàng
                <textarea name="shippingAddress" rows="2" maxlength="255" class="form-field mt-1.5 w-full px-3 py-2 rounded-xl text-sm text-slate-100">${form.shippingAddress}</textarea>
            </label>
        </section>

        <!-- Thanh toán -->
        <section class="rounded-2xl glass-panel-subtle border border-white/10 p-5">
            <h2 class="text-xs uppercase tracking-wider font-semibold text-brand-gold mb-4">Phương thức thanh toán</h2>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <label class="flex items-start gap-3 p-3.5 rounded-xl border border-white/10 hover:border-brand-gold/40 cursor-pointer transition has-[:checked]:border-brand-gold has-[:checked]:bg-brand-gold/10">
                    <input type="radio" name="paymentMethod" value="COD" ${form.paymentMethod == 'COD' ? 'checked' : ''} class="mt-1 accent-yellow-500"/>
                    <span class="text-xs"><strong class="text-slate-100 text-sm block">Tiền mặt khi nhận hàng</strong><span class="text-slate-400">Thanh toán trực tiếp khi nhận sách</span></span>
                </label>
                <label class="flex items-start gap-3 p-3.5 rounded-xl border border-white/10 hover:border-brand-gold/40 cursor-pointer transition has-[:checked]:border-brand-gold has-[:checked]:bg-brand-gold/10">
                    <input type="radio" name="paymentMethod" value="BANK_TRANSFER" ${form.paymentMethod == 'BANK_TRANSFER' ? 'checked' : ''} class="mt-1 accent-yellow-500"/>
                    <span class="text-xs"><strong class="text-slate-100 text-sm block">Chuyển khoản</strong><span class="text-slate-400">Nhân viên xác nhận khi nhận được tiền</span></span>
                </label>
            </div>
            <label class="block text-xs text-slate-300 mt-4">Ghi chú cho cửa hàng (không bắt buộc)
                <textarea name="note" rows="2" maxlength="500" class="form-field mt-1.5 w-full px-3 py-2 rounded-xl text-sm text-slate-100">${form.note}</textarea>
            </label>
        </section>
    </div>

    <!-- Tóm tắt -->
    <aside class="lg:col-span-5">
        <div class="rounded-2xl glass-panel-subtle border border-white/10 p-5 sticky top-6"
             id="order-summary" data-subtotal="${cart.subtotal}" data-orders="${cart.orderCount}" data-delivery-fee="${deliveryFee}">
            <h2 class="text-xs uppercase tracking-wider font-semibold text-brand-gold mb-4">Đơn hàng của bạn</h2>
            <c:forEach var="group" items="${cart.groups}">
                <c:set var="hasAvailable" value="false"/>
                <c:forEach var="item" items="${group.items}"><c:if test="${item.available}"><c:set var="hasAvailable" value="true"/></c:if></c:forEach>
                <c:if test="${hasAvailable}">
                    <div class="mb-4">
                        <p class="text-[11px] font-semibold text-slate-200 mb-1.5"><i class="fa-solid fa-store text-brand-gold mr-1.5"></i>${group.storeName}</p>
                        <ul class="space-y-1 text-xs">
                            <c:forEach var="item" items="${group.items}">
                                <c:if test="${item.available}">
                                    <li class="flex justify-between gap-3"><span class="text-slate-300 truncate">${item.title}</span><span class="text-slate-100 shrink-0"><fmt:formatNumber value="${item.price}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></span></li>
                                </c:if>
                            </c:forEach>
                        </ul>
                    </div>
                </c:if>
            </c:forEach>
            <c:if test="${cart.hasUnavailable}">
                <p class="text-[11px] text-amber-300 mb-3"><i class="fa-solid fa-triangle-exclamation mr-1"></i>Sách không còn khả dụng sẽ được giữ lại trong giỏ, không đưa vào đơn.</p>
            </c:if>
            <dl class="space-y-2 text-xs pt-3 border-t border-white/10">
                <div class="flex justify-between"><dt class="text-slate-400">Tạm tính</dt><dd class="text-slate-100" id="sum-subtotal"></dd></div>
                <div class="flex justify-between"><dt class="text-slate-400">Phí giao hàng (<span id="sum-orders">${cart.orderCount}</span> đơn)</dt><dd class="text-slate-100" id="sum-shipping"></dd></div>
                <div class="flex justify-between pt-2 border-t border-white/10 text-sm"><dt class="text-slate-200 font-semibold">Tổng cộng</dt><dd class="text-emerald-400 font-bold" id="sum-total"></dd></div>
            </dl>
            <button type="submit" id="place-order" class="w-full mt-5 px-5 py-2.5 rounded-full bg-gradient-to-r from-brand-gold to-yellow-500 text-slate-950 font-bold text-sm hover:from-yellow-400 hover:to-amber-500 transition disabled:opacity-60 disabled:cursor-wait">
                <i class="fa-solid fa-bag-shopping mr-1.5"></i>Xác nhận đặt hàng
            </button>
            <a href="/cart" class="block text-center text-[11px] text-slate-400 hover:text-white mt-3 transition">Quay lại giỏ hàng</a>
        </div>
    </aside>
</form>

<script>
    (function () {
        var summary = document.getElementById('order-summary');
        var subtotal = parseFloat(summary.dataset.subtotal);
        var orders = parseInt(summary.dataset.orders, 10);
        var fee = parseFloat(summary.dataset.deliveryFee);
        var addressField = document.getElementById('address-field');
        var form = document.getElementById('checkout-form');

        function money(n) { return '₫' + Math.round(n).toLocaleString('en-US'); }

        function refresh() {
            var delivery = form.querySelector('input[name="fulfillmentType"]:checked').value === 'DELIVERY';
            var shipping = delivery ? fee * orders : 0;
            addressField.classList.toggle('hidden', !delivery);
            addressField.querySelector('textarea').required = delivery;
            document.getElementById('sum-subtotal').textContent = money(subtotal);
            document.getElementById('sum-shipping').textContent = delivery ? money(shipping) : 'Miễn phí';
            document.getElementById('sum-total').textContent = money(subtotal + shipping);
        }

        form.querySelectorAll('input[name="fulfillmentType"]').forEach(function (r) { r.addEventListener('change', refresh); });
        form.addEventListener('submit', function () { document.getElementById('place-order').disabled = true; });
        refresh();
    })();
</script>

<jsp:include page="/WEB-INF/views/customer/common/layout-bottom.jsp"/>
