<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Sách yêu thích" scope="request"/>
<jsp:include page="/WEB-INF/views/customer/common/layout-top.jsp"/>

<div class="mt-6">
    <h1 class="text-2xl md:text-3xl font-serif font-bold text-white">Sách yêu thích</h1>
    <p class="text-xs text-slate-400 mt-1"><span data-wishlist-total>${items.size()}</span> đầu sách bạn đang theo dõi.</p>
</div>

<c:if test="${empty items}">
    <div class="mt-10 p-10 rounded-2xl glass-panel-subtle text-center">
        <i class="fa-regular fa-heart text-3xl text-slate-600 mb-3"></i>
        <p class="text-sm text-slate-300">Bạn chưa lưu cuốn sách nào.</p>
        <a href="/#catalog" class="inline-flex items-center gap-2 mt-4 px-5 py-2 rounded-full bg-gradient-to-r from-brand-gold to-yellow-500 text-slate-950 font-bold text-xs hover:from-yellow-400 hover:to-amber-500 transition">
            <i class="fa-solid fa-book-open text-[11px]"></i> Khám phá kho sách
        </a>
    </div>
</c:if>

<div class="mt-6 grid grid-cols-2 md:grid-cols-4 gap-3.5">
    <c:forEach var="item" items="${items}">
        <div data-wishlist-card class="relative group rounded-2xl glass-panel-subtle border border-white/10 p-2.5 hover:border-brand-gold/40 hover:bg-white/5 transition">
            <button type="button" data-wishlist-toggle="${item.id}" data-wishlisted="true" aria-pressed="true" title="Bỏ khỏi yêu thích"
                    class="absolute top-4 right-4 z-10 w-7 h-7 rounded-full bg-black/60 backdrop-blur-md flex items-center justify-center text-rose-400 hover:text-rose-300 transition">
                <i class="fa-solid fa-heart text-xs"></i>
            </button>
            <a href="/books/${item.id}">
                <div class="relative aspect-[3/4.2] w-full rounded-xl overflow-hidden mb-2.5 bg-gradient-to-b from-[#1b120c] to-[#0c0806] book-spine-effect">
                    <c:choose>
                        <c:when test="${not empty item.coverImage}"><img src="${fn:escapeXml(item.coverImage)}" class="w-full h-full object-cover" alt="${fn:escapeXml(item.title)}"/></c:when>
                        <c:otherwise>
                            <div class="absolute inset-0 flex items-center justify-center p-2 text-center text-xs font-serif font-black tracking-widest text-amber-100 uppercase">${fn:escapeXml(item.title)}</div>
                        </c:otherwise>
                    </c:choose>
                </div>
                <h3 class="text-xs font-semibold text-white truncate group-hover:text-brand-gold transition">${fn:escapeXml(item.title)}</h3>
                <p class="text-[11px] text-slate-400 truncate">${fn:escapeXml(item.author)}</p>
                <div class="flex items-center justify-between mt-1.5">
                    <p class="text-[11px] font-bold text-emerald-400">
                        <c:choose>
                            <c:when test="${item.availableCopiesCount > 0}"><fmt:formatNumber value="${item.minPrice}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></c:when>
                            <c:otherwise><span class="text-rose-300 font-medium">Hết bản sao</span></c:otherwise>
                        </c:choose>
                    </p>
                    <c:if test="${item.rating.count > 0}">
                        <span class="text-[10px] text-amber-400 font-semibold" title="${item.rating.count} đánh giá">★ ${item.rating.averageLabel}</span>
                    </c:if>
                </div>
            </a>
        </div>
    </c:forEach>
</div>

<jsp:include page="/WEB-INF/views/customer/common/layout-bottom.jsp"/>
