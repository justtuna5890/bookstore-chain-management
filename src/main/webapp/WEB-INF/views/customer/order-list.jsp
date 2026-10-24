<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Đơn hàng của tôi" scope="request"/>
<jsp:include page="/WEB-INF/views/customer/common/layout-top.jsp"/>

<div class="mt-6">
    <h1 class="text-2xl md:text-3xl font-serif font-bold text-white">Đơn hàng của tôi</h1>
    <p class="text-xs text-slate-400 mt-1">${orders.size()} đơn hàng. Giỏ hàng nhiều chi nhánh được tách thành các đơn riêng.</p>
</div>

<c:if test="${empty orders}">
    <div class="mt-10 p-10 rounded-2xl glass-panel-subtle text-center">
        <i class="fa-solid fa-receipt text-3xl text-slate-600 mb-3"></i>
        <p class="text-sm text-slate-300">Bạn chưa có đơn hàng nào.</p>
        <a href="/#catalog" class="inline-flex items-center gap-2 mt-4 px-5 py-2 rounded-full bg-gradient-to-r from-brand-gold to-yellow-500 text-slate-950 font-bold text-xs hover:from-yellow-400 hover:to-amber-500 transition">
            <i class="fa-solid fa-book-open text-[11px]"></i> Khám phá kho sách
        </a>
    </div>
</c:if>

<div class="mt-6 flex flex-col gap-3">
    <c:forEach var="order" items="${orders}">
        <a href="/orders/${order.orderCode}" class="group rounded-2xl glass-panel-subtle border border-white/10 p-4 hover:border-brand-gold/40 hover:bg-white/5 transition flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div class="min-w-0">
                <div class="flex items-center gap-2.5 flex-wrap">
                    <span class="font-mono text-sm text-amber-300 font-semibold">${order.orderCode}</span>
                    <span class="px-2 py-0.5 rounded-full border text-[10px] font-semibold ${order.statusClasses}">${order.statusLabel}</span>
                </div>
                <p class="text-xs text-slate-400 mt-1.5">
                    <i class="fa-solid fa-store text-brand-gold mr-1"></i>${order.storeName}
                    <span class="mx-1.5 text-slate-600">·</span>${order.itemCount} cuốn
                    <span class="mx-1.5 text-slate-600">·</span>${order.formattedCreatedAt}
                </p>
                <p class="text-[11px] text-slate-500 mt-1 truncate">
                    <c:forEach var="line" items="${order.lines}" varStatus="st">${line.title}${st.last ? '' : ', '}</c:forEach>
                </p>
            </div>
            <div class="text-right shrink-0">
                <p class="text-sm font-bold text-emerald-400"><fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></p>
                <p class="text-[11px] text-slate-500 group-hover:text-brand-gold transition mt-0.5">Xem chi tiết <i class="fa-solid fa-arrow-right text-[9px]"></i></p>
            </div>
        </a>
    </c:forEach>
</div>

<jsp:include page="/WEB-INF/views/customer/common/layout-bottom.jsp"/>
