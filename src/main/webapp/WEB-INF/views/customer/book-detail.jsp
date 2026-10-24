<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="${book.title}" scope="request"/>
<jsp:include page="/WEB-INF/views/customer/common/layout-top.jsp"/>

<nav class="mt-5 text-xs text-slate-400 flex items-center gap-2 flex-wrap">
    <a href="/" class="hover:text-brand-gold transition">Trang chủ</a>
    <i class="fa-solid fa-chevron-right text-[9px] text-slate-600"></i>
    <c:if test="${not empty book.category}">
        <a href="/?category=${book.category}" class="hover:text-brand-gold transition">${book.category}</a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-600"></i>
    </c:if>
    <span class="text-slate-200">${book.title}</span>
</nav>

<div class="grid grid-cols-1 lg:grid-cols-12 gap-8 mt-6">
    <!-- Bìa sách -->
    <div class="lg:col-span-4 xl:col-span-3">
        <div class="relative aspect-[3/4.2] w-full max-w-xs mx-auto rounded-2xl overflow-hidden bg-gradient-to-b from-[#1b120c] to-[#0c0806] book-spine-effect">
            <c:choose>
                <c:when test="${not empty book.coverImage}">
                    <img src="${book.coverImage}" class="w-full h-full object-cover" alt="${book.title}"/>
                </c:when>
                <c:otherwise>
                    <div class="absolute inset-0 bg-gradient-to-t from-amber-950 via-amber-900/60 to-black/80 flex flex-col items-center justify-center p-4 text-center">
                        <span class="text-brand-gold text-2xl font-serif mb-3">✦</span>
                        <span class="text-base font-serif font-black tracking-widest text-amber-100 uppercase">${book.title}</span>
                        <span class="text-[11px] text-amber-300/80 tracking-wider mt-2">${book.author}</span>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <!-- Thông tin -->
    <div class="lg:col-span-8 xl:col-span-9">
        <c:if test="${not empty book.category}">
            <span class="text-[10px] px-2.5 py-0.5 rounded-full bg-brand-gold/15 text-brand-gold border border-brand-gold/30">${book.category}</span>
        </c:if>
        <h1 class="text-3xl md:text-4xl font-serif font-bold text-white mt-2 leading-tight">${book.title}</h1>
        <p class="text-sm text-brand-goldLight mt-1"><i class="fa-solid fa-pen-nib me-1"></i>${book.author}</p>

        <dl class="grid grid-cols-2 sm:grid-cols-4 gap-3 mt-5 text-xs">
            <div class="p-3 rounded-xl glass-panel-subtle">
                <dt class="text-slate-400">Nhà xuất bản</dt>
                <dd class="text-slate-100 font-semibold mt-0.5">${empty book.publisher ? 'Đang cập nhật' : book.publisher}</dd>
            </div>
            <div class="p-3 rounded-xl glass-panel-subtle">
                <dt class="text-slate-400">Năm xuất bản</dt>
                <dd class="text-slate-100 font-semibold mt-0.5">${empty book.publicationYear ? 'Đang cập nhật' : book.publicationYear}</dd>
            </div>
            <div class="p-3 rounded-xl glass-panel-subtle">
                <dt class="text-slate-400">Giá bìa</dt>
                <dd class="text-slate-100 font-semibold mt-0.5">
                    <c:choose>
                        <c:when test="${not empty book.coverPrice}"><fmt:formatNumber value="${book.coverPrice}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></c:when>
                        <c:otherwise>—</c:otherwise>
                    </c:choose>
                </dd>
            </div>
            <div class="p-3 rounded-xl glass-panel-subtle">
                <dt class="text-slate-400">Đang có</dt>
                <dd class="text-emerald-400 font-semibold mt-0.5">${book.availableCopiesCount} cuốn · ${book.storeCount} chi nhánh</dd>
            </div>
        </dl>

        <p class="text-sm text-slate-300 mt-5 leading-relaxed font-light">
            ${not empty book.description ? book.description : 'Bản in quý hiếm được lưu giữ tại hệ thống chuỗi cửa hàng OldBook.'}
        </p>

        <!-- Danh sách bản sao -->
        <div class="mt-8">
            <div class="flex items-end justify-between mb-3">
                <h2 class="text-xs uppercase tracking-wider font-semibold text-brand-gold">Các bản sách đang bán</h2>
                <c:if test="${book.availableCopiesCount > 0}">
                    <span class="text-xs text-slate-400">
                        Từ <strong class="text-emerald-400"><fmt:formatNumber value="${book.minPrice}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></strong>
                        <c:if test="${book.maxPrice != book.minPrice}"> đến <strong class="text-emerald-400"><fmt:formatNumber value="${book.maxPrice}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></strong></c:if>
                    </span>
                </c:if>
            </div>
            <div class="overflow-x-auto rounded-xl border border-white/5">
                <table class="w-full text-left text-xs text-slate-300">
                    <thead class="bg-white/5 text-[11px] text-slate-400 uppercase tracking-wider">
                        <tr>
                            <th class="p-3">Mã sách</th>
                            <th class="p-3">Chi nhánh</th>
                            <th class="p-3">Tình trạng</th>
                            <th class="p-3">Ghi chú</th>
                            <th class="p-3 text-right">Giá bán</th>
                            <th class="p-3"></th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-white/5">
                        <c:forEach var="cp" items="${book.copies}">
                            <tr class="hover:bg-white/5">
                                <td class="p-3 font-mono text-amber-300">${cp.copyCode}</td>
                                <td class="p-3">${cp.storeName}</td>
                                <td class="p-3">
                                    <c:choose>
                                        <c:when test="${cp.bookCondition == 'LIKE_NEW'}"><span class="px-2 py-0.5 rounded bg-emerald-500/15 text-emerald-300 text-[10px]">Như mới</span></c:when>
                                        <c:when test="${cp.bookCondition == 'GOOD'}"><span class="px-2 py-0.5 rounded bg-sky-500/15 text-sky-300 text-[10px]">Tốt</span></c:when>
                                        <c:when test="${cp.bookCondition == 'FAIR'}"><span class="px-2 py-0.5 rounded bg-amber-500/15 text-amber-300 text-[10px]">Khá</span></c:when>
                                        <c:otherwise><span class="px-2 py-0.5 rounded bg-rose-500/15 text-rose-300 text-[10px]">Trung bình</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="p-3 text-slate-400">${empty cp.note ? '—' : cp.note}</td>
                                <td class="p-3 text-right font-bold text-emerald-400"><fmt:formatNumber value="${cp.price}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></td>
                                <td class="p-3 text-right">
                                    <button type="button" data-add-to-cart="${cp.id}" class="whitespace-nowrap px-3 py-1.5 rounded-full bg-gradient-to-r from-brand-gold to-yellow-500 text-slate-950 font-bold text-[11px] hover:from-yellow-400 hover:to-amber-500 transition">
                                        <i class="fa-solid fa-cart-plus mr-1"></i>Thêm vào giỏ
                                    </button>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty book.copies}">
                            <tr><td colspan="6" class="p-6 text-center text-slate-500">Hiện tất cả bản sách đã được bán hoặc đang điều chuyển. Bạn có thể quay lại sau.</td></tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- Sách cùng thể loại -->
<c:if test="${not empty book.relatedBooks}">
    <section class="mt-10">
        <h2 class="text-xs uppercase tracking-wider font-semibold text-brand-gold mb-3">Sách cùng thể loại</h2>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-3.5">
            <c:forEach var="rb" items="${book.relatedBooks}">
                <a href="/books/${rb.id}" class="group rounded-2xl glass-panel-subtle border border-white/10 p-2.5 hover:border-brand-gold/40 hover:bg-white/5 transition">
                    <div class="relative aspect-[3/4.2] w-full rounded-xl overflow-hidden mb-2.5 bg-gradient-to-b from-[#1b120c] to-[#0c0806] book-spine-effect">
                        <c:choose>
                            <c:when test="${not empty rb.coverImage}"><img src="${rb.coverImage}" class="w-full h-full object-cover" alt="${rb.title}"/></c:when>
                            <c:otherwise>
                                <div class="absolute inset-0 flex items-center justify-center p-2 text-center text-xs font-serif font-black tracking-widest text-amber-100 uppercase">${rb.title}</div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <h3 class="text-xs font-semibold text-white truncate group-hover:text-brand-gold transition">${rb.title}</h3>
                    <p class="text-[11px] text-slate-400 truncate">${rb.author}</p>
                    <p class="text-[11px] font-bold text-emerald-400 mt-1.5">
                        <c:choose>
                            <c:when test="${rb.availableCopiesCount > 0}"><fmt:formatNumber value="${rb.minPrice}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></c:when>
                            <c:otherwise><span class="text-rose-300 font-medium">Hết bản sao</span></c:otherwise>
                        </c:choose>
                    </p>
                </a>
            </c:forEach>
        </div>
    </section>
</c:if>

<jsp:include page="/WEB-INF/views/customer/common/layout-bottom.jsp"/>
