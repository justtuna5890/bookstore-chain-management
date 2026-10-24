<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="${fn:escapeXml(book.title)}" scope="request"/>
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

<nav class="mt-5 text-xs text-slate-400 flex items-center gap-2 flex-wrap">
    <a href="/" class="hover:text-brand-gold transition">Trang chủ</a>
    <i class="fa-solid fa-chevron-right text-[9px] text-slate-600"></i>
    <c:if test="${not empty book.category}">
        <a href="/?category=${fn:escapeXml(book.category)}" class="hover:text-brand-gold transition">${fn:escapeXml(book.category)}</a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-600"></i>
    </c:if>
    <span class="text-slate-200">${fn:escapeXml(book.title)}</span>
</nav>

<div class="grid grid-cols-1 lg:grid-cols-12 gap-8 mt-6">
    <!-- Bìa sách -->
    <div class="lg:col-span-4 xl:col-span-3">
        <div class="relative aspect-[3/4.2] w-full max-w-xs mx-auto rounded-2xl overflow-hidden bg-gradient-to-b from-[#1b120c] to-[#0c0806] book-spine-effect">
            <c:choose>
                <c:when test="${not empty book.coverImage}">
                    <img src="${fn:escapeXml(book.coverImage)}" class="w-full h-full object-cover" alt="${fn:escapeXml(book.title)}"/>
                </c:when>
                <c:otherwise>
                    <div class="absolute inset-0 bg-gradient-to-t from-amber-950 via-amber-900/60 to-black/80 flex flex-col items-center justify-center p-4 text-center">
                        <span class="text-brand-gold text-2xl font-serif mb-3">✦</span>
                        <span class="text-base font-serif font-black tracking-widest text-amber-100 uppercase">${fn:escapeXml(book.title)}</span>
                        <span class="text-[11px] text-amber-300/80 tracking-wider mt-2">${fn:escapeXml(book.author)}</span>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <!-- Thông tin -->
    <div class="lg:col-span-8 xl:col-span-9">
        <c:if test="${not empty book.category}">
            <span class="text-[10px] px-2.5 py-0.5 rounded-full bg-brand-gold/15 text-brand-gold border border-brand-gold/30">${fn:escapeXml(book.category)}</span>
        </c:if>
        <div class="flex items-start justify-between gap-3 mt-2">
            <h1 class="text-3xl md:text-4xl font-serif font-bold text-white leading-tight">${fn:escapeXml(book.title)}</h1>
            <button type="button" data-wishlist-toggle="${book.id}" data-wishlisted="${wishlisted}" aria-pressed="${wishlisted}" title="Yêu thích"
                    class="shrink-0 mt-1 w-9 h-9 rounded-full glass-panel-subtle hover:bg-white/10 flex items-center justify-center transition ${wishlisted ? 'text-rose-400' : 'text-slate-400'}">
                <i class="${wishlisted ? 'fa-solid' : 'fa-regular'} fa-heart text-sm"></i>
            </button>
        </div>
        <p class="text-sm text-brand-goldLight mt-1"><i class="fa-solid fa-pen-nib me-1"></i>${fn:escapeXml(book.author)}</p>
        <a href="#reviews" class="inline-flex items-center gap-2 mt-2 text-xs text-slate-300 hover:text-brand-gold transition">
            <span class="stars text-base" aria-hidden="true">★★★★★<span style="width:${ratingSummary.starPercent}%">★★★★★</span></span>
            <c:choose>
                <c:when test="${ratingSummary.rated}"><strong class="text-amber-400">${ratingSummary.averageLabel}</strong><span>(${ratingSummary.count} đánh giá)</span></c:when>
                <c:otherwise><span class="text-slate-500">Chưa có đánh giá</span></c:otherwise>
            </c:choose>
        </a>

        <dl class="grid grid-cols-2 sm:grid-cols-4 gap-3 mt-5 text-xs">
            <div class="p-3 rounded-xl glass-panel-subtle">
                <dt class="text-slate-400">Nhà xuất bản</dt>
                <dd class="text-slate-100 font-semibold mt-0.5">${empty book.publisher ? 'Đang cập nhật' : fn:escapeXml(book.publisher)}</dd>
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
            ${not empty book.description ? fn:escapeXml(book.description) : 'Bản in quý hiếm được lưu giữ tại hệ thống chuỗi cửa hàng OldBook.'}
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
                                <td class="p-3 font-mono text-amber-300">${fn:escapeXml(cp.copyCode)}</td>
                                <td class="p-3">${fn:escapeXml(cp.storeName)}</td>
                                <td class="p-3">
                                    <c:choose>
                                        <c:when test="${cp.bookCondition == 'LIKE_NEW'}"><span class="px-2 py-0.5 rounded bg-emerald-500/15 text-emerald-300 text-[10px]">Như mới</span></c:when>
                                        <c:when test="${cp.bookCondition == 'GOOD'}"><span class="px-2 py-0.5 rounded bg-sky-500/15 text-sky-300 text-[10px]">Tốt</span></c:when>
                                        <c:when test="${cp.bookCondition == 'FAIR'}"><span class="px-2 py-0.5 rounded bg-amber-500/15 text-amber-300 text-[10px]">Khá</span></c:when>
                                        <c:otherwise><span class="px-2 py-0.5 rounded bg-rose-500/15 text-rose-300 text-[10px]">Trung bình</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="p-3 text-slate-400">${empty cp.note ? '—' : fn:escapeXml(cp.note)}</td>
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

<!-- Đánh giá của khách -->
<section id="reviews" class="mt-10 scroll-mt-6">
    <h2 class="text-xs uppercase tracking-wider font-semibold text-brand-gold mb-3">Đánh giá của khách hàng</h2>

    <c:if test="${not empty reviewSuccess}">
        <div class="mb-3 p-3.5 rounded-2xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-200 text-xs flex items-start gap-2.5">
            <i class="fa-solid fa-circle-check text-emerald-400 text-base mt-0.5"></i><span>${fn:escapeXml(reviewSuccess)}</span>
        </div>
    </c:if>
    <c:if test="${not empty reviewError}">
        <div class="mb-3 p-3.5 rounded-2xl bg-rose-500/15 border border-rose-500/30 text-rose-200 text-xs flex items-start gap-2.5">
            <i class="fa-solid fa-circle-exclamation text-rose-400 text-base mt-0.5"></i><span>${fn:escapeXml(reviewError)}</span>
        </div>
    </c:if>

    <div class="grid grid-cols-1 md:grid-cols-12 gap-6">
        <!-- Tổng quan -->
        <div class="md:col-span-4 p-5 rounded-2xl glass-panel-subtle">
            <div class="flex items-end gap-3">
                <span class="text-4xl font-serif font-bold ${ratingSummary.rated ? 'text-white' : 'text-slate-600'}">${ratingSummary.averageLabel}</span>
                <span class="text-xs text-slate-400 pb-1">/ 5 · ${ratingSummary.count} đánh giá</span>
            </div>
            <span class="stars text-xl mt-1" aria-hidden="true">★★★★★<span style="width:${ratingSummary.starPercent}%">★★★★★</span></span>
            <ul class="mt-4 space-y-1.5">
                <c:forEach var="bar" items="${ratingSummary.bars}">
                    <li class="flex items-center gap-2 text-[11px] text-slate-400">
                        <span class="w-6 text-right">${bar.star}★</span>
                        <span class="flex-1 h-1.5 rounded-full bg-white/10 overflow-hidden"><span class="block h-full bg-amber-400" style="width:${bar.percent}%"></span></span>
                        <span class="w-6">${bar.count}</span>
                    </li>
                </c:forEach>
            </ul>
        </div>

        <div class="md:col-span-8">
            <!-- Biểu mẫu: chỉ khách đã nhận hàng cuốn này -->
            <c:choose>
                <c:when test="${canReview}">
                    <form action="/books/${book.id}/reviews" method="post" class="p-5 rounded-2xl glass-panel-subtle mb-4">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <p class="text-xs font-semibold text-slate-200 mb-2">${empty myReview ? 'Viết đánh giá của bạn' : 'Sửa đánh giá của bạn'}</p>
                        <div class="rate-input relative" role="radiogroup" aria-label="Số sao">
                            <c:forEach var="star" begin="1" end="5">
                                <c:set var="s" value="${6 - star}"/>
                                <input type="radio" id="rate-${s}" name="rating" value="${s}" required ${myReview.rating == s ? 'checked' : ''}/>
                                <label for="rate-${s}" title="${s} sao">★</label>
                            </c:forEach>
                        </div>
                        <textarea name="comment" rows="3" maxlength="1000" placeholder="Chia sẻ tình trạng sách, giao nhận... (không bắt buộc)"
                                  class="form-field w-full mt-3 px-3 py-2 text-xs rounded-xl">${fn:escapeXml(myReview.comment)}</textarea>
                        <div class="flex items-center justify-between mt-3">
                            <button type="submit" class="px-4 py-2 rounded-full bg-gradient-to-r from-brand-gold to-yellow-500 text-slate-950 font-bold text-xs hover:from-yellow-400 hover:to-amber-500 transition">
                                <i class="fa-solid fa-paper-plane mr-1"></i>${empty myReview ? 'Gửi đánh giá' : 'Cập nhật'}
                            </button>
                            <c:if test="${not empty myReview}">
                                <button type="submit" formaction="/books/${book.id}/reviews/delete" formnovalidate class="text-[11px] text-rose-300 hover:text-rose-200 transition">Xoá đánh giá</button>
                            </c:if>
                        </div>
                        <c:if test="${myReview.hidden}">
                            <p class="text-[11px] text-amber-300 mt-2"><i class="fa-solid fa-eye-slash mr-1"></i>Đánh giá của bạn đang bị quản trị viên ẩn vì vi phạm quy định.</p>
                        </c:if>
                    </form>
                </c:when>
                <c:otherwise>
                    <p class="p-3.5 rounded-2xl glass-panel-subtle mb-4 text-[11px] text-slate-400">
                        <i class="fa-solid fa-circle-info mr-1"></i>
                        <c:choose>
                            <c:when test="${empty pageContext.request.userPrincipal}">Hãy <a href="/login" class="text-brand-gold hover:underline">đăng nhập</a> để đánh giá sau khi mua và nhận sách.</c:when>
                            <c:otherwise>Bạn có thể đánh giá cuốn sách này sau khi đơn hàng chứa nó được giao thành công.</c:otherwise>
                        </c:choose>
                    </p>
                </c:otherwise>
            </c:choose>

            <!-- Danh sách đánh giá -->
            <ul class="space-y-3">
                <c:forEach var="rv" items="${reviews}">
                    <li class="p-4 rounded-2xl glass-panel-subtle">
                        <div class="flex items-center justify-between gap-3">
                            <div class="flex items-center gap-2 min-w-0">
                                <span class="w-7 h-7 shrink-0 rounded-full bg-brand-gold/15 text-brand-gold text-[11px] font-bold flex items-center justify-center">${fn:escapeXml(fn:toUpperCase(fn:substring(rv.reviewerName, 0, 1)))}</span>
                                <span class="text-xs font-semibold text-slate-100 truncate">${fn:escapeXml(rv.reviewerName)}</span>
                            </div>
                            <span class="text-[11px] text-slate-500 shrink-0">${rv.formattedCreatedAt}</span>
                        </div>
                        <span class="stars text-sm mt-1.5" aria-label="${rv.rating} sao">★★★★★<span style="width:${rv.starPercent}%">★★★★★</span></span>
                        <c:if test="${not empty rv.comment}">
                            <p class="text-xs text-slate-300 mt-2 leading-relaxed whitespace-pre-line">${fn:escapeXml(rv.comment)}</p>
                        </c:if>
                    </li>
                </c:forEach>
                <c:if test="${empty reviews}">
                    <li class="p-6 rounded-2xl glass-panel-subtle text-center text-xs text-slate-500">Chưa có đánh giá nào. Hãy là người đầu tiên!</li>
                </c:if>
            </ul>
        </div>
    </div>
</section>

<!-- Sách cùng thể loại -->
<c:if test="${not empty book.relatedBooks}">
    <section class="mt-10">
        <h2 class="text-xs uppercase tracking-wider font-semibold text-brand-gold mb-3">Sách cùng thể loại</h2>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-3.5">
            <c:forEach var="rb" items="${book.relatedBooks}">
                <a href="/books/${rb.id}" class="group rounded-2xl glass-panel-subtle border border-white/10 p-2.5 hover:border-brand-gold/40 hover:bg-white/5 transition">
                    <div class="relative aspect-[3/4.2] w-full rounded-xl overflow-hidden mb-2.5 bg-gradient-to-b from-[#1b120c] to-[#0c0806] book-spine-effect">
                        <c:choose>
                            <c:when test="${not empty rb.coverImage}"><img src="${fn:escapeXml(rb.coverImage)}" class="w-full h-full object-cover" alt="${fn:escapeXml(rb.title)}"/></c:when>
                            <c:otherwise>
                                <div class="absolute inset-0 flex items-center justify-center p-2 text-center text-xs font-serif font-black tracking-widest text-amber-100 uppercase">${fn:escapeXml(rb.title)}</div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <h3 class="text-xs font-semibold text-white truncate group-hover:text-brand-gold transition">${fn:escapeXml(rb.title)}</h3>
                    <p class="text-[11px] text-slate-400 truncate">${fn:escapeXml(rb.author)}</p>
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
