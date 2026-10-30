<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="shipperTitle" value="Đơn giao hàng" scope="request"/>
<c:set var="liveReload" value="${true}" scope="request"/>
<jsp:include page="/WEB-INF/views/shipper/common/page-top.jsp"/>

<c:if test="${not empty successMessage}">
    <div class="mb-4 p-3.5 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-300 text-sm flex items-center gap-2"><i class="fa-solid fa-circle-check"></i><span>${fn:escapeXml(successMessage)}</span></div>
</c:if>
<c:if test="${not empty errorMessage}">
    <div class="mb-4 p-3.5 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-300 text-sm flex items-center gap-2"><i class="fa-solid fa-circle-exclamation"></i><span>${fn:escapeXml(errorMessage)}</span></div>
</c:if>

<nav class="grid grid-cols-3 gap-1.5 p-1 rounded-2xl card mb-5" aria-label="Danh mục đơn">
    <a href="/shipper/orders?tab=available" class="text-center py-2.5 rounded-xl text-xs font-semibold transition ${tab == 'available' ? 'bg-brand-gold text-slate-950' : 'text-slate-300 hover:bg-white/5'}">
        Chờ nhận<c:if test="${availableCount > 0}"> <span class="ml-1 px-1.5 py-0.5 rounded-full text-[10px] ${tab == 'available' ? 'bg-slate-950/20' : 'bg-brand-gold/20 text-brand-gold'}">${availableCount}</span></c:if>
    </a>
    <a href="/shipper/orders?tab=mine" class="text-center py-2.5 rounded-xl text-xs font-semibold transition ${tab == 'mine' ? 'bg-brand-gold text-slate-950' : 'text-slate-300 hover:bg-white/5'}">
        Đang giao<c:if test="${mineCount > 0}"> <span class="ml-1 px-1.5 py-0.5 rounded-full text-[10px] ${tab == 'mine' ? 'bg-slate-950/20' : 'bg-brand-gold/20 text-brand-gold'}">${mineCount}</span></c:if>
    </a>
    <a href="/shipper/orders?tab=history" class="text-center py-2.5 rounded-xl text-xs font-semibold transition ${tab == 'history' ? 'bg-brand-gold text-slate-950' : 'text-slate-300 hover:bg-white/5'}">Lịch sử</a>
</nav>

<c:choose>
    <c:when test="${empty orders}">
        <div class="card rounded-2xl p-8 text-center text-slate-400 text-sm">
            <i class="fa-solid ${tab == 'available' ? 'fa-box-open' : (tab == 'mine' ? 'fa-motorcycle' : 'fa-clock-rotate-left')} text-3xl text-slate-600 mb-3"></i>
            <p>
                <c:choose>
                    <c:when test="${tab == 'available'}">Hiện chưa có đơn nào chờ giao tại chi nhánh.</c:when>
                    <c:when test="${tab == 'mine'}">Bạn chưa nhận đơn nào.</c:when>
                    <c:otherwise>Bạn chưa có đơn giao nào gần đây.</c:otherwise>
                </c:choose>
            </p>
            <a href="/shipper/orders?tab=${tab}" class="inline-block mt-4 px-4 py-2 rounded-xl border border-white/15 text-xs text-slate-200 hover:bg-white/5 transition"><i class="fa-solid fa-rotate mr-1.5"></i>Tải lại</a>
        </div>
    </c:when>
    <c:otherwise>
        <ul class="space-y-3">
            <c:forEach var="order" items="${orders}">
                <li class="card rounded-2xl p-4">
                    <div class="flex items-start justify-between gap-3">
                        <a href="/shipper/orders/${order.orderId}" class="font-mono text-sm font-bold text-white">${fn:escapeXml(order.orderCode)}</a>
                        <span class="shrink-0 px-2.5 py-0.5 rounded-full border text-[10px] font-semibold ${order.statusClasses}">${order.statusLabel}</span>
                    </div>
                    <p class="mt-2 text-sm text-slate-100 font-semibold">${fn:escapeXml(order.customerName)}</p>
                    <p class="mt-0.5 text-xs text-slate-400"><i class="fa-solid fa-location-dot text-brand-gold mr-1.5"></i>${fn:escapeXml(order.shippingAddress)}</p>
                    <div class="mt-3 flex items-center justify-between text-xs">
                        <span class="text-slate-400">${order.itemCount} cuốn · ${order.formattedCreatedAt}</span>
                        <c:choose>
                            <c:when test="${order.amountToCollect.signum() > 0}">
                                <span class="px-2.5 py-1 rounded-lg bg-amber-500/15 border border-amber-500/30 text-amber-300 font-bold">Thu <fmt:formatNumber value="${order.amountToCollect}" pattern="#,###"/> đ</span>
                            </c:when>
                            <c:otherwise>
                                <span class="text-emerald-400 font-semibold">Đã thanh toán</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <c:if test="${tab == 'history' && order.status == 'DELIVERY_FAILED'}">
                        <p class="mt-2 text-[11px] text-orange-300">Lý do: ${fn:escapeXml(order.deliveryFailureReason)}</p>
                    </c:if>
                    <div class="mt-3 flex gap-2">
                        <a href="/shipper/orders/${order.orderId}" class="flex-1 text-center px-3 py-2.5 rounded-xl border border-white/15 text-xs font-semibold text-slate-200 hover:bg-white/5 transition">Chi tiết</a>
                        <c:if test="${tab == 'available'}">
                            <form action="/shipper/orders/${order.orderId}/claim" method="post" class="flex-1 m-0">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <button type="submit" class="w-full px-3 py-2.5 rounded-xl bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 text-xs font-bold hover:from-yellow-400 hover:to-amber-500 transition"><i class="fa-solid fa-hand mr-1.5"></i>Nhận đơn</button>
                            </form>
                        </c:if>
                    </div>
                </li>
            </c:forEach>
        </ul>
    </c:otherwise>
</c:choose>

<jsp:include page="/WEB-INF/views/shipper/common/page-bottom.jsp"/>
