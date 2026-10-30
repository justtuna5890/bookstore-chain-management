<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="shipperTitle" value="Chi tiết đơn giao" scope="request"/>
<jsp:include page="/WEB-INF/views/shipper/common/page-top.jsp"/>

<c:url var="mapUrl" value="https://www.google.com/maps/search/">
    <c:param name="api" value="1"/>
    <c:param name="query" value="${order.shippingAddress}"/><%-- c:param tự mã hoá URL, bọc escapeXml sẽ làm hỏng địa chỉ có & hay dấu nháy --%>
</c:url>
<c:set var="mine" value="${order.status == 'SHIPPING'}"/>

<a href="/shipper/orders${mine ? '?tab=mine' : ''}" class="text-xs text-slate-400 hover:text-brand-gold transition"><i class="fa-solid fa-arrow-left mr-1"></i>Danh sách đơn</a>

<div class="mt-2 flex items-start justify-between gap-3">
    <div>
        <h1 class="text-xl font-mono font-bold text-white">${fn:escapeXml(order.orderCode)}</h1>
        <p class="text-[11px] text-slate-400 mt-0.5">Đặt lúc ${order.formattedCreatedAt} · ${fn:escapeXml(order.storeName)}</p>
    </div>
    <span class="shrink-0 px-3 py-1 rounded-full border text-[11px] font-semibold ${order.statusClasses}">${order.statusLabel}</span>
</div>

<c:if test="${not empty successMessage}">
    <div class="mt-4 p-3.5 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-300 text-sm flex items-center gap-2"><i class="fa-solid fa-circle-check"></i><span>${fn:escapeXml(successMessage)}</span></div>
</c:if>
<c:if test="${not empty errorMessage}">
    <div class="mt-4 p-3.5 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-300 text-sm flex items-center gap-2"><i class="fa-solid fa-circle-exclamation"></i><span>${fn:escapeXml(errorMessage)}</span></div>
</c:if>

<!-- Người nhận và địa chỉ -->
<section class="card rounded-2xl p-4 mt-4 text-sm">
    <h2 class="text-[11px] uppercase tracking-wider font-semibold text-brand-gold mb-2">Giao đến</h2>
    <p class="text-slate-100 font-semibold text-base">${fn:escapeXml(order.customerName)}</p>
    <p class="mt-2 text-slate-300"><i class="fa-solid fa-location-dot text-brand-gold mr-1.5"></i>${fn:escapeXml(order.shippingAddress)}</p>
    <c:if test="${not empty order.note}"><p class="mt-2 text-xs text-slate-400 italic">“${fn:escapeXml(order.note)}”</p></c:if>
    <div class="mt-3 grid grid-cols-2 gap-2">
        <a href="tel:${fn:escapeXml(order.receiverPhone)}" class="text-center px-3 py-2.5 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-300 text-xs font-semibold"><i class="fa-solid fa-phone mr-1.5"></i>${fn:escapeXml(order.receiverPhone)}</a>
        <a href="${fn:escapeXml(mapUrl)}" target="_blank" rel="noopener noreferrer" class="text-center px-3 py-2.5 rounded-xl bg-sky-500/15 border border-sky-500/30 text-sky-300 text-xs font-semibold"><i class="fa-solid fa-map-location-dot mr-1.5"></i>Mở bản đồ</a>
    </div>
</section>

<!-- Tiền cần thu -->
<section class="card rounded-2xl p-4 mt-4 text-sm">
    <h2 class="text-[11px] uppercase tracking-wider font-semibold text-brand-gold mb-2">Thanh toán</h2>
    <c:choose>
        <c:when test="${order.amountToCollect.signum() > 0}">
            <p class="text-slate-300">Thu tiền mặt khi giao</p>
            <p class="mt-1 text-2xl font-bold text-amber-300"><fmt:formatNumber value="${order.amountToCollect}" pattern="#,###"/> đ</p>
        </c:when>
        <c:otherwise>
            <p class="text-emerald-400 font-semibold"><i class="fa-solid fa-circle-check mr-1.5"></i>Đã thanh toán, không thu thêm tiền</p>
        </c:otherwise>
    </c:choose>
</section>

<!-- Sách trong đơn -->
<section class="card rounded-2xl mt-4 overflow-hidden text-sm">
    <h2 class="px-4 pt-4 pb-2 text-[11px] uppercase tracking-wider font-semibold text-brand-gold">${order.itemCount} cuốn sách</h2>
    <ul class="divide-y divide-white/5">
        <c:forEach var="line" items="${order.lines}">
            <li class="px-4 py-3 flex items-center justify-between gap-3">
                <div class="min-w-0">
                    <p class="text-slate-100 font-medium">${fn:escapeXml(line.title)}</p>
                    <p class="text-[11px] font-mono text-amber-300 mt-0.5">${fn:escapeXml(line.copyCode)}</p>
                </div>
            </li>
        </c:forEach>
    </ul>
</section>

<c:if test="${order.status == 'DELIVERY_FAILED'}">
    <p class="mt-4 p-3 rounded-xl bg-orange-500/10 border border-orange-500/30 text-orange-200 text-xs leading-relaxed"><strong>Bạn đã báo giao không thành công:</strong> ${fn:escapeXml(order.deliveryFailureReason)}</p>
</c:if>

<!-- Thao tác -->
<c:choose>
    <c:when test="${order.status == 'READY_FOR_DELIVERY'}">
        <form action="/shipper/orders/${order.orderId}/claim" method="post" class="mt-5">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <button type="submit" class="w-full px-4 py-3.5 rounded-2xl bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold hover:from-yellow-400 hover:to-amber-500 transition"><i class="fa-solid fa-hand mr-1.5"></i>Nhận đơn này</button>
        </form>
    </c:when>
    <c:when test="${mine}">
        <form action="/shipper/orders/${order.orderId}/deliver" method="post" class="mt-5"
              onsubmit="return confirm('Xác nhận đã giao xong đơn ${fn:escapeXml(order.orderCode)}?')">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <button type="submit" class="w-full px-4 py-3.5 rounded-2xl bg-gradient-to-r from-emerald-500 to-emerald-600 text-slate-950 font-bold hover:from-emerald-400 hover:to-emerald-500 transition"><i class="fa-solid fa-flag-checkered mr-1.5"></i>Đã giao thành công</button>
        </form>

        <details class="mt-3 card rounded-2xl">
            <summary class="px-4 py-3.5 text-sm font-semibold text-rose-300 cursor-pointer select-none"><i class="fa-solid fa-triangle-exclamation mr-1.5"></i>Giao không thành công</summary>
            <form action="/shipper/orders/${order.orderId}/fail" method="post" class="px-4 pb-4"
                  onsubmit="return confirm('Báo giao không thành công? Đơn sẽ về lại chi nhánh.')">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <label for="reason" class="block text-xs text-slate-400 mb-1.5">Lý do (bắt buộc)</label>
                <textarea id="reason" name="reason" rows="3" maxlength="255" required placeholder="VD: Khách không nghe máy, khách từ chối nhận..." class="field w-full px-3 py-2.5 text-sm rounded-xl placeholder-slate-500"></textarea>
                <button type="submit" class="mt-3 w-full px-4 py-3 rounded-xl border border-rose-500/40 text-rose-300 hover:bg-rose-500/10 font-semibold transition">Xác nhận giao không thành công</button>
            </form>
        </details>
    </c:when>
</c:choose>

<jsp:include page="/WEB-INF/views/shipper/common/page-bottom.jsp"/>
