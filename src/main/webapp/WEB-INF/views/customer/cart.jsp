<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Giỏ hàng" scope="request"/>
<jsp:include page="/WEB-INF/views/customer/common/layout-top.jsp"/>

<div class="mt-6 flex items-end justify-between gap-4 flex-wrap">
    <div>
        <h1 class="text-2xl md:text-3xl font-serif font-bold text-white">Giỏ hàng của bạn</h1>
        <p class="text-xs text-slate-400 mt-1">${cart.itemCount} cuốn sách. Mỗi bản sách cũ là duy nhất nên chỉ có một cuốn cho mỗi mã.</p>
    </div>
    <c:if test="${not cart.emptyCart}">
        <form action="/cart/clear" method="post" onsubmit="return confirm('Xoá toàn bộ sách trong giỏ hàng?')">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <button type="submit" class="text-xs text-slate-400 hover:text-rose-300 transition"><i class="fa-solid fa-trash-can mr-1"></i>Xoá giỏ hàng</button>
        </form>
    </c:if>
</div>

<c:if test="${cart.emptyCart}">
    <div class="mt-10 p-10 rounded-2xl glass-panel-subtle text-center">
        <i class="fa-solid fa-cart-shopping text-3xl text-slate-600 mb-3"></i>
        <p class="text-sm text-slate-300">Giỏ hàng của bạn đang trống.</p>
        <a href="/#catalog" class="inline-flex items-center gap-2 mt-4 px-5 py-2 rounded-full bg-gradient-to-r from-brand-gold to-yellow-500 text-slate-950 font-bold text-xs hover:from-yellow-400 hover:to-amber-500 transition">
            <i class="fa-solid fa-book-open text-[11px]"></i> Khám phá kho sách
        </a>
    </div>
</c:if>

<c:if test="${not cart.emptyCart}">
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 mt-6">
        <div class="lg:col-span-8 flex flex-col gap-5">
            <c:forEach var="group" items="${cart.groups}">
                <section class="rounded-2xl glass-panel-subtle border border-white/10 overflow-hidden">
                    <div class="px-4 py-3 bg-white/5 flex items-center justify-between">
                        <h2 class="text-xs font-semibold text-brand-gold"><i class="fa-solid fa-store mr-1.5"></i>${group.storeName}</h2>
                        <span class="text-xs text-slate-300">Tạm tính: <strong class="text-emerald-400"><fmt:formatNumber value="${group.subtotal}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></strong></span>
                    </div>
                    <ul class="divide-y divide-white/5">
                        <c:forEach var="item" items="${group.items}">
                            <li class="p-4 flex items-center gap-4 ${item.available ? '' : 'opacity-60'}">
                                <a href="/books/${item.bookId}" class="w-14 h-20 rounded-lg overflow-hidden shrink-0 book-spine-effect bg-[#15181E] flex items-center justify-center">
                                    <c:choose>
                                        <c:when test="${not empty item.coverImage}"><img src="${item.coverImage}" class="w-full h-full object-cover" alt="${item.title}"/></c:when>
                                        <c:otherwise><span class="text-[8px] font-serif text-amber-200 text-center px-1">${item.title}</span></c:otherwise>
                                    </c:choose>
                                </a>
                                <div class="flex-1 min-w-0">
                                    <a href="/books/${item.bookId}" class="text-sm font-semibold text-white hover:text-brand-gold transition line-clamp-1">${item.title}</a>
                                    <p class="text-[11px] text-slate-400">${item.author}</p>
                                    <p class="text-[11px] mt-1">
                                        <span class="font-mono text-amber-300">${item.copyCode}</span>
                                        <span class="mx-1.5 text-slate-600">·</span>
                                        <c:choose>
                                            <c:when test="${item.bookCondition == 'LIKE_NEW'}"><span class="text-emerald-300">Như mới</span></c:when>
                                            <c:when test="${item.bookCondition == 'GOOD'}"><span class="text-sky-300">Tốt</span></c:when>
                                            <c:when test="${item.bookCondition == 'FAIR'}"><span class="text-amber-300">Khá</span></c:when>
                                            <c:otherwise><span class="text-rose-300">Trung bình</span></c:otherwise>
                                        </c:choose>
                                    </p>
                                    <c:if test="${not item.available}">
                                        <p class="text-[11px] text-rose-300 mt-1"><i class="fa-solid fa-circle-exclamation mr-1"></i>Cuốn sách này không còn khả dụng, hãy xoá khỏi giỏ.</p>
                                    </c:if>
                                </div>
                                <div class="text-right shrink-0">
                                    <p class="text-sm font-bold text-emerald-400"><fmt:formatNumber value="${item.price}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></p>
                                    <form action="/cart/remove" method="post" class="mt-1.5">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                        <input type="hidden" name="copyId" value="${item.copyId}"/>
                                        <button type="submit" class="text-[11px] text-slate-400 hover:text-rose-300 transition"><i class="fa-solid fa-xmark mr-1"></i>Xoá</button>
                                    </form>
                                </div>
                            </li>
                        </c:forEach>
                    </ul>
                </section>
            </c:forEach>
        </div>

        <aside class="lg:col-span-4">
            <div class="rounded-2xl glass-panel-subtle border border-white/10 p-5 sticky top-6">
                <h2 class="text-xs uppercase tracking-wider font-semibold text-brand-gold mb-4">Tóm tắt đơn hàng</h2>
                <dl class="space-y-2.5 text-xs">
                    <div class="flex justify-between"><dt class="text-slate-400">Số sách khả dụng</dt><dd class="text-slate-100 font-semibold">${cart.availableCount} / ${cart.itemCount}</dd></div>
                    <div class="flex justify-between"><dt class="text-slate-400">Số đơn hàng sẽ tạo</dt><dd class="text-slate-100 font-semibold">${cart.orderCount}</dd></div>
                    <div class="flex justify-between pt-3 border-t border-white/10 text-sm">
                        <dt class="text-slate-200 font-semibold">Tạm tính</dt>
                        <dd class="text-emerald-400 font-bold"><fmt:formatNumber value="${cart.subtotal}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></dd>
                    </div>
                </dl>
                <p class="text-[11px] text-slate-500 mt-4 leading-relaxed">Sách thuộc các chi nhánh khác nhau sẽ được tách thành các đơn riêng để mỗi chi nhánh chuẩn bị hàng.</p>
            </div>
        </aside>
    </div>
</c:if>

<jsp:include page="/WEB-INF/views/customer/common/layout-bottom.jsp"/>
