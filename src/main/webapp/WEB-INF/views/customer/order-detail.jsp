<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Đơn ${fn:escapeXml(order.orderCode)}" scope="request"/>
<jsp:include page="/WEB-INF/views/customer/common/layout-top.jsp"/>

<style>
    .stars { position: relative; display: inline-block; color: #475569; letter-spacing: 2px; line-height: 1; }
    .stars > span { position: absolute; top: 0; left: 0; bottom: 0; overflow: hidden; white-space: nowrap; color: #fbbf24; }
    .rate-input { display: inline-flex; flex-direction: row-reverse; gap: 4px; }
    .rate-input input { position: absolute; opacity: 0; pointer-events: none; }
    .rate-input label { font-size: 28px; line-height: 1; color: #475569; cursor: pointer; transition: color .15s; }
    .rate-input input:checked ~ label, .rate-input label:hover, .rate-input label:hover ~ label { color: #fbbf24; }
    .rate-input input:focus-visible + label { outline: 2px solid #D4AF37; border-radius: 4px; }
</style>

<nav class="mt-5 text-xs text-slate-400 flex items-center gap-2">
    <a href="/orders" class="hover:text-brand-gold transition">Đơn hàng của tôi</a>
    <i class="fa-solid fa-chevron-right text-[9px] text-slate-600"></i>
    <span class="text-slate-200 font-mono">${fn:escapeXml(order.orderCode)}</span>
</nav>

<c:if test="${not empty orderSuccess}">
    <div class="mt-4 p-3.5 rounded-2xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-200 text-xs flex items-start gap-2.5">
        <i class="fa-solid fa-circle-check text-emerald-400 text-base mt-0.5"></i><span>${fn:escapeXml(orderSuccess)}</span>
    </div>
</c:if>
<c:if test="${not empty orderError}">
    <div class="mt-4 p-3.5 rounded-2xl bg-rose-500/15 border border-rose-500/30 text-rose-200 text-xs flex items-start gap-2.5">
        <i class="fa-solid fa-circle-exclamation text-rose-400 text-base mt-0.5"></i><span>${fn:escapeXml(orderError)}</span>
    </div>
</c:if>

<div class="mt-5 flex items-center justify-between gap-4 flex-wrap">
    <div>
        <h1 class="text-2xl font-serif font-bold text-white font-mono">${fn:escapeXml(order.orderCode)}</h1>
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
        <div class="px-4 py-3 bg-white/5 text-xs font-semibold text-brand-gold flex items-center justify-between gap-3">
            <span><i class="fa-solid fa-store mr-1.5"></i>${fn:escapeXml(order.storeName)}</span>
            <a href="/chat?store=${order.storeId}" class="shrink-0 px-2.5 py-1 rounded-full border border-brand-gold/40 text-[11px] hover:bg-brand-gold/10 transition"><i class="fa-regular fa-comments mr-1"></i>Nhắn cho chi nhánh</a>
        </div>
        <ul class="divide-y divide-white/5">
            <c:forEach var="line" items="${order.lines}">
                <li class="p-4 flex items-center justify-between gap-4">
                    <div class="min-w-0">
                        <a href="/books/${line.bookId}" class="text-sm font-semibold text-white hover:text-brand-gold transition">${fn:escapeXml(line.title)}</a>
                        <p class="text-[11px] font-mono text-amber-300 mt-0.5">${fn:escapeXml(line.copyCode)}</p>
                    </div>
                    <span class="text-sm text-slate-100 shrink-0"><fmt:formatNumber value="${line.price}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></span>
                </li>
            </c:forEach>
        </ul>
        <dl class="p-4 border-t border-white/10 space-y-1.5 text-xs">
            <div class="flex justify-between"><dt class="text-slate-400">Tạm tính</dt><dd class="text-slate-100"><fmt:formatNumber value="${order.subtotal}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></dd></div>
            <c:if test="${order.discountAmount.signum() > 0}">
                <div class="flex justify-between"><dt class="text-slate-400">Giảm giá<c:if test="${not empty order.promotionCode}"> (${fn:escapeXml(order.promotionCode)})</c:if></dt><dd class="text-emerald-300">-<fmt:formatNumber value="${order.discountAmount}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></dd></div>
            </c:if>
            <div class="flex justify-between"><dt class="text-slate-400">Phí giao hàng</dt><dd class="text-slate-100">${order.shippingFee.signum() > 0 ? '' : 'Miễn phí'}<c:if test="${order.shippingFee.signum() > 0}"><fmt:formatNumber value="${order.shippingFee}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></c:if></dd></div>
            <div class="flex justify-between pt-2 border-t border-white/10 text-sm"><dt class="text-slate-200 font-semibold">Tổng cộng</dt><dd class="text-emerald-400 font-bold"><fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></dd></div>
        </dl>
    </section>

    <!-- Thông tin nhận hàng -->
    <aside class="lg:col-span-5 flex flex-col gap-4">
        <section class="rounded-2xl glass-panel-subtle border border-white/10 p-5 text-xs">
            <h2 class="text-xs uppercase tracking-wider font-semibold text-brand-gold mb-3">Nhận hàng</h2>
            <p class="text-slate-100 font-semibold">${fn:escapeXml(order.customerName)}</p>
            <p class="text-slate-400 mt-0.5">${fn:escapeXml(order.receiverPhone)}</p>
            <c:choose>
                <c:when test="${order.fulfillmentType == 'DELIVERY'}">
                    <p class="text-slate-300 mt-2"><i class="fa-solid fa-truck text-brand-gold mr-1.5"></i>Giao tận nơi: ${fn:escapeXml(order.shippingAddress)}</p>
                </c:when>
                <c:otherwise>
                    <p class="text-slate-300 mt-2"><i class="fa-solid fa-store text-brand-gold mr-1.5"></i>Nhận tại: ${fn:escapeXml(order.storeName)}</p>
                    <p class="text-slate-500 mt-0.5">${fn:escapeXml(order.storeAddress)} · ${fn:escapeXml(order.storePhone)}</p>
                </c:otherwise>
            </c:choose>
            <c:if test="${not empty order.note}"><p class="text-slate-400 mt-2 italic">“${fn:escapeXml(order.note)}”</p></c:if>
        </section>

        <section class="rounded-2xl glass-panel-subtle border border-white/10 p-5 text-xs">
            <h2 class="text-xs uppercase tracking-wider font-semibold text-brand-gold mb-3">Thanh toán</h2>
            <p class="text-slate-100">${order.paymentLabel}</p>
            <c:if test="${order.paymentMethod == 'BANK_TRANSFER' && order.paymentStatus == 'UNPAID' && order.status != 'CANCELLED'}">
                <p class="mt-2 p-2.5 rounded-lg bg-sky-500/10 border border-sky-500/30 text-sky-200 text-[11px] leading-relaxed">
                    Chuyển khoản đúng số tiền và ghi nội dung <strong class="font-mono">${fn:escapeXml(order.orderCode)}</strong>. Nhân viên sẽ xác nhận khi nhận được tiền.
                </p>
            </c:if>
        </section>

        <c:if test="${order.cancellable}">
            <form action="/orders/${fn:escapeXml(order.orderCode)}/cancel" method="post" onsubmit="return confirm('Huỷ đơn hàng ${fn:escapeXml(order.orderCode)}? Các cuốn sách sẽ được mở bán trở lại.')">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <button type="submit" class="w-full px-4 py-2.5 rounded-full border border-rose-500/40 text-rose-300 hover:bg-rose-500/10 text-xs font-semibold transition">
                    <i class="fa-solid fa-ban mr-1.5"></i>Huỷ đơn hàng
                </button>
            </form>
        </c:if>
    </aside>
</div>

<!-- Đánh giá sách sau khi nhận hàng -->
<c:if test="${order.status == 'COMPLETED' && not empty reviewLines}">
    <section id="danh-gia" class="mt-6 rounded-2xl glass-panel-subtle border border-white/10 overflow-hidden">
        <div class="px-4 py-3 bg-white/5 text-xs font-semibold text-brand-gold"><i class="fa-regular fa-star mr-1.5"></i>Đánh giá sách trong đơn</div>
        <c:if test="${not empty reviewSuccess}">
            <div class="m-4 p-3.5 rounded-2xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-200 text-xs flex items-start gap-2.5">
                <i class="fa-solid fa-circle-check text-emerald-400 text-base mt-0.5"></i><span>${fn:escapeXml(reviewSuccess)}</span>
            </div>
        </c:if>
        <c:if test="${not empty reviewError}">
            <div class="m-4 p-3.5 rounded-2xl bg-rose-500/15 border border-rose-500/30 text-rose-200 text-xs flex items-start gap-2.5">
                <i class="fa-solid fa-circle-exclamation text-rose-400 text-base mt-0.5"></i><span>${fn:escapeXml(reviewError)}</span>
            </div>
        </c:if>
        <ul class="divide-y divide-white/5">
            <c:forEach var="line" items="${reviewLines}">
                <c:set var="mine" value="${myReviews[line.bookId]}"/>
                <li class="p-4">
                    <div class="flex items-center justify-between gap-3 flex-wrap">
                        <a href="/books/${line.bookId}" class="text-sm font-semibold text-white hover:text-brand-gold transition">${fn:escapeXml(line.title)}</a>
                        <c:if test="${not empty mine}">
                            <span class="stars text-sm" aria-label="${mine.rating} sao">★★★★★<span style="width:${mine.starPercent}%">★★★★★</span></span>
                        </c:if>
                    </div>
                    <c:if test="${not empty mine.comment}">
                        <p class="text-xs text-slate-300 mt-1.5">“${fn:escapeXml(mine.comment)}”</p>
                    </c:if>
                    <c:if test="${mine.hidden}">
                        <p class="text-[11px] text-amber-300 mt-1.5"><i class="fa-solid fa-eye-slash mr-1"></i>Đánh giá của bạn đang bị quản trị viên ẩn vì vi phạm quy định.</p>
                    </c:if>
                    <details class="mt-2" ${empty mine ? 'open' : ''}>
                        <summary class="cursor-pointer text-[11px] text-brand-gold hover:underline select-none">${empty mine ? 'Viết đánh giá' : 'Sửa đánh giá'}</summary>
                        <form action="/orders/${fn:escapeXml(order.orderCode)}/reviews/${line.bookId}" method="post" class="mt-2">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <div class="rate-input relative" role="radiogroup" aria-label="Số sao">
                                <c:forEach var="star" begin="1" end="5">
                                    <c:set var="s" value="${6 - star}"/>
                                    <input type="radio" id="rate-${line.bookId}-${s}" name="rating" value="${s}" required ${mine.rating == s ? 'checked' : ''}/>
                                    <label for="rate-${line.bookId}-${s}" title="${s} sao">★</label>
                                </c:forEach>
                            </div>
                            <textarea name="comment" rows="3" maxlength="1000" placeholder="Chia sẻ tình trạng sách, giao nhận... (không bắt buộc)"
                                      class="form-field w-full mt-3 px-3 py-2 text-xs rounded-xl">${fn:escapeXml(mine.comment)}</textarea>
                            <button type="submit" class="mt-3 px-4 py-2 rounded-full bg-gradient-to-r from-brand-gold to-yellow-500 text-slate-950 font-bold text-xs hover:from-yellow-400 hover:to-amber-500 transition">
                                <i class="fa-solid fa-paper-plane mr-1"></i>${empty mine ? 'Gửi đánh giá' : 'Cập nhật'}
                            </button>
                        </form>
                    </details>
                </li>
            </c:forEach>
        </ul>
    </section>
</c:if>

<jsp:include page="/WEB-INF/views/customer/common/layout-bottom.jsp"/>
