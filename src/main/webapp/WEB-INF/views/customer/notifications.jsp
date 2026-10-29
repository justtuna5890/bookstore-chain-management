<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Thông báo" scope="request"/>
<jsp:include page="/WEB-INF/views/customer/common/layout-top.jsp"/>

<div class="mt-6 flex flex-col sm:flex-row sm:items-end justify-between gap-3">
    <div>
        <h1 class="text-2xl md:text-3xl font-serif font-bold text-white">Thông báo</h1>
        <p class="text-xs text-slate-400 mt-1"><span data-notification-unread-total>${unreadCount}</span> chưa đọc · ${notificationPage.totalElements} thông báo</p>
    </div>
    <c:if test="${not empty notificationPage.content}">
        <button type="button" data-notification-read-all class="self-start sm:self-auto px-4 py-2 rounded-full border border-brand-gold/40 text-brand-gold text-xs font-bold hover:bg-brand-gold/10 transition">
            <i class="fa-solid fa-check-double mr-1"></i>Đánh dấu đã đọc tất cả
        </button>
    </c:if>
</div>

<c:if test="${empty notificationPage.content}">
    <div class="mt-10 p-10 rounded-2xl glass-panel-subtle text-center">
        <i class="fa-regular fa-bell text-3xl text-slate-600 mb-3"></i>
        <p class="text-sm text-slate-300">Bạn chưa có thông báo nào.</p>
        <p class="text-xs text-slate-500 mt-1">Chúng tôi sẽ báo khi đơn hàng đổi trạng thái hoặc sách yêu thích có hàng lại.</p>
    </div>
</c:if>

<ul data-notification-page-list class="mt-6 rounded-2xl glass-panel-subtle border border-white/10 divide-y divide-white/5 overflow-hidden">
    <c:forEach var="n" items="${notificationPage.content}">
        <li>
            <c:choose>
                <c:when test="${not empty n.link}"><a href="${fn:escapeXml(n.link)}" data-notification-item="${n.id}" data-unread="${not n.read}" class="flex gap-3 px-4 py-3.5 hover:bg-white/5 transition ${n.read ? 'opacity-60' : 'bg-brand-gold/5'}"></c:when>
                <c:otherwise><div data-notification-item="${n.id}" data-unread="${not n.read}" class="flex gap-3 px-4 py-3.5 ${n.read ? 'opacity-60' : 'bg-brand-gold/5'}"></c:otherwise>
            </c:choose>
                <span class="w-9 h-9 shrink-0 rounded-full bg-brand-gold/15 text-brand-gold flex items-center justify-center">
                    <c:choose>
                        <c:when test="${n.type == 'ORDER'}"><i class="fa-solid fa-receipt text-sm"></i></c:when>
                        <c:when test="${n.type == 'WISHLIST'}"><i class="fa-solid fa-heart text-sm"></i></c:when>
                        <c:when test="${n.type == 'BUYBACK'}"><i class="fa-solid fa-hand-holding-dollar text-sm"></i></c:when>
                        <c:when test="${n.type == 'CHAT'}"><i class="fa-solid fa-comments text-sm"></i></c:when>
                        <c:otherwise><i class="fa-solid fa-star text-sm"></i></c:otherwise>
                    </c:choose>
                </span>
                <span class="min-w-0 flex-1">
                    <span class="block text-sm font-semibold text-slate-100">${fn:escapeXml(n.title)}</span>
                    <span class="block text-xs text-slate-400 mt-0.5 break-words">${fn:escapeXml(n.message)}</span>
                    <span class="block text-[11px] text-slate-500 mt-1">${fn:escapeXml(n.timeAgo)}</span>
                </span>
                <c:if test="${not n.read}"><span data-notification-dot class="w-2 h-2 mt-2 shrink-0 rounded-full bg-brand-gold"></span></c:if>
            <c:choose>
                <c:when test="${not empty n.link}"></a></c:when>
                <c:otherwise></div></c:otherwise>
            </c:choose>
        </li>
    </c:forEach>
</ul>

<c:if test="${notificationPage.totalPages > 1}">
    <div class="mt-5 flex items-center justify-between text-xs text-slate-400">
        <div>Trang <span class="text-white font-medium">${notificationPage.number + 1}</span> / <span class="text-white font-medium">${notificationPage.totalPages}</span></div>
        <div class="flex items-center gap-1.5">
            <c:forEach begin="0" end="${notificationPage.totalPages - 1}" var="i">
                <a href="/notifications?page=${i}" class="w-8 h-8 rounded-lg flex items-center justify-center font-medium transition ${notificationPage.number == i ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">${i + 1}</a>
            </c:forEach>
        </div>
    </div>
</c:if>

<jsp:include page="/WEB-INF/views/customer/common/layout-bottom.jsp"/>
