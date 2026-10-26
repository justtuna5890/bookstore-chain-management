<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="adminTitle" value="Kiểm Duyệt Đánh Giá" scope="request"/>
<c:set var="adminActive" value="reviews" scope="request"/>
<jsp:include page="/WEB-INF/views/admin/common/page-top.jsp"/>

<div class="pb-6 border-b border-white/5 mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
    <div>
        <span class="px-2.5 py-0.5 rounded-full bg-brand-gold/15 text-brand-gold text-[10px] font-semibold border border-brand-gold/30">NGHIỆP VỤ BÁN HÀNG</span>
        <h1 class="text-2xl sm:text-3xl font-serif font-bold text-white tracking-tight mt-1">Kiểm Duyệt Đánh Giá</h1>
        <p class="text-xs text-slate-400 mt-0.5">Ẩn các đánh giá spam hoặc vi phạm; đánh giá bị ẩn không hiển thị và không tính vào điểm sao của sách</p>
    </div>
    <div class="flex items-center gap-2 text-xs">
        <span class="px-3 py-1.5 rounded-xl bg-white/5 border border-white/10 text-slate-300">Tổng <strong class="text-white">${totalCount}</strong></span>
        <span class="px-3 py-1.5 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300">Đang ẩn <strong>${hiddenCount}</strong></span>
    </div>
</div>

<c:if test="${not empty successMessage}">
    <div class="mb-5 p-3.5 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-300 text-xs flex items-center gap-2"><i class="fa-solid fa-circle-check"></i><span>${fn:escapeXml(successMessage)}</span></div>
</c:if>

<form action="/admin/reviews" method="get" class="mb-5 flex flex-col md:flex-row gap-3">
    <input type="text" name="keyword" value="${fn:escapeXml(keyword)}" placeholder="Tìm theo tên sách, người đánh giá hoặc nội dung..."
           class="glass-input flex-1 px-3.5 py-2.5 text-xs rounded-xl text-slate-100 placeholder-slate-500"/>
    <select name="status" class="glass-input px-3.5 py-2.5 text-xs rounded-xl text-slate-100">
        <option value="all" ${status == 'all' ? 'selected' : ''}>Mọi trạng thái</option>
        <option value="visible" ${status == 'visible' ? 'selected' : ''}>Đang hiển thị</option>
        <option value="hidden" ${status == 'hidden' ? 'selected' : ''}>Đang ẩn</option>
    </select>
    <select name="rating" class="glass-input px-3.5 py-2.5 text-xs rounded-xl text-slate-100">
        <option value="">Mọi số sao</option>
        <c:forEach var="s" begin="1" end="5"><option value="${s}" ${rating == s ? 'selected' : ''}>${s} sao</option></c:forEach>
    </select>
    <button type="submit" class="px-5 py-2.5 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-slate-200 text-xs font-semibold transition"><i class="fa-solid fa-magnifying-glass mr-1"></i>Lọc</button>
</form>

<div class="glass-panel rounded-2xl border border-white/10 overflow-hidden shadow-lg">
    <div class="overflow-x-auto">
        <table class="w-full text-left text-xs text-slate-300">
            <thead class="bg-white/5 text-[11px] text-brand-gold uppercase tracking-wider border-b border-white/10">
                <tr>
                    <th class="py-3.5 px-4">Sách</th>
                    <th class="py-3.5 px-4">Người Đánh Giá</th>
                    <th class="py-3.5 px-4">Số Sao</th>
                    <th class="py-3.5 px-4">Nhận Xét</th>
                    <th class="py-3.5 px-4">Ngày</th>
                    <th class="py-3.5 px-4">Trạng Thái</th>
                    <th class="py-3.5 px-4 text-right">Thao Tác</th>
                </tr>
            </thead>
            <tbody class="divide-y divide-white/5">
                <c:forEach var="rv" items="${reviewPage.content}">
                    <tr class="hover:bg-white/[0.03] transition ${rv.hidden ? 'opacity-70' : ''}">
                        <td class="py-3.5 px-4 max-w-[200px]"><a href="/books/${rv.bookId}" target="_blank" class="font-semibold text-slate-100 hover:text-brand-gold transition block truncate" title="${fn:escapeXml(rv.bookTitle)}">${fn:escapeXml(rv.bookTitle)}</a></td>
                        <td class="py-3.5 px-4 whitespace-nowrap">${fn:escapeXml(rv.reviewerName)}</td>
                        <td class="py-3.5 px-4 whitespace-nowrap text-amber-400 font-semibold">${rv.rating} ★</td>
                        <td class="py-3.5 px-4 max-w-[340px]">
                            <c:choose>
                                <c:when test="${not empty rv.comment}"><p class="whitespace-pre-line break-words line-clamp-3">${fn:escapeXml(rv.comment)}</p></c:when>
                                <c:otherwise><span class="text-slate-500">(không có nhận xét)</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td class="py-3.5 px-4 whitespace-nowrap text-slate-400">${rv.formattedCreatedAt}</td>
                        <td class="py-3.5 px-4 whitespace-nowrap">
                            <c:choose>
                                <c:when test="${rv.hidden}"><span class="inline-block px-2.5 py-0.5 rounded-full border text-[10px] font-semibold bg-rose-500/10 border-rose-500/30 text-rose-300">Đang ẩn</span></c:when>
                                <c:otherwise><span class="inline-block px-2.5 py-0.5 rounded-full border text-[10px] font-semibold bg-emerald-500/10 border-emerald-500/30 text-emerald-300">Hiển thị</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td class="py-3.5 px-4 text-right whitespace-nowrap">
                            <form action="/admin/reviews/${rv.id}/${rv.hidden ? 'show' : 'hide'}" method="post" class="inline">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <input type="hidden" name="keyword" value="${fn:escapeXml(keyword)}"/>
                                <input type="hidden" name="status" value="${fn:escapeXml(status)}"/>
                                <input type="hidden" name="rating" value="${rating}"/>
                                <input type="hidden" name="page" value="${reviewPage.number}"/>
                                <button type="submit" class="inline-flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg border font-semibold transition ${rv.hidden ? 'border-emerald-500/30 text-emerald-300 hover:bg-emerald-500/10' : 'border-rose-500/30 text-rose-300 hover:bg-rose-500/10'}">
                                    <i class="fa-solid ${rv.hidden ? 'fa-eye' : 'fa-eye-slash'} text-[10px]"></i> ${rv.hidden ? 'Hiện lại' : 'Ẩn'}
                                </button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty reviewPage.content}">
                    <tr><td colspan="7" class="py-12 text-center text-slate-500"><i class="fa-regular fa-comments text-3xl mb-3 text-slate-600 block"></i>Không có đánh giá nào phù hợp.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>

    <c:if test="${reviewPage.totalPages > 1}">
        <div class="p-4 border-t border-white/10 flex items-center justify-between text-xs text-slate-400">
            <div>Trang <span class="text-white font-medium">${reviewPage.number + 1}</span> / <span class="text-white font-medium">${reviewPage.totalPages}</span>
                (<span class="text-brand-gold font-medium">${reviewPage.totalElements}</span> đánh giá)</div>
            <div class="flex items-center gap-1.5">
                <c:forEach begin="0" end="${reviewPage.totalPages - 1}" var="i">
                    <c:url var="pageUrl" value="/admin/reviews">
                        <c:param name="page" value="${i}"/>
                        <c:param name="keyword" value="${keyword}"/>
                        <c:param name="status" value="${status}"/>
                        <c:param name="rating" value="${rating}"/>
                    </c:url>
                    <a href="${fn:escapeXml(pageUrl)}" class="w-8 h-8 rounded-lg flex items-center justify-center font-medium transition ${reviewPage.number == i ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">${i + 1}</a>
                </c:forEach>
            </div>
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/views/admin/common/page-bottom.jsp"/>
