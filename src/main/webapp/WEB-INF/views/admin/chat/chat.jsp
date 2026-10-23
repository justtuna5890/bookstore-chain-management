<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="adminTitle" value="Chat Hỗ Trợ" scope="request"/>
<c:set var="adminActive" value="chat" scope="request"/>
<jsp:include page="/WEB-INF/views/admin/common/page-top.jsp"/>
<meta name="_csrf" content="${_csrf.token}"/>

<div class="pb-6 border-b border-white/5 mb-6">
    <span class="px-2.5 py-0.5 rounded-full bg-brand-gold/15 text-brand-gold text-[10px] font-semibold border border-brand-gold/30">NGHIỆP VỤ BÁN HÀNG</span>
    <h1 class="text-2xl sm:text-3xl font-serif font-bold text-white tracking-tight mt-1">Chat Hỗ Trợ Khách Hàng</h1>
    <p class="text-xs text-slate-400 mt-0.5">
        <c:choose>
            <c:when test="${isStaffScoped}">Tin nhắn khách gửi tới <strong class="text-slate-200">${fn:escapeXml(currentStoreName)}</strong>. Bạn chỉ thấy chat của chi nhánh mình.</c:when>
            <c:otherwise>Tin nhắn khách gửi tới các chi nhánh; có thể lọc theo chi nhánh và trạng thái.</c:otherwise>
        </c:choose>
    </p>
</div>

<c:if test="${unassignedStoreWarning}">
    <div class="p-4 rounded-xl bg-amber-500/15 border border-amber-500/30 text-amber-200 text-xs">
        <i class="fa-solid fa-triangle-exclamation mr-1"></i>Tài khoản của bạn chưa được gán chi nhánh nên chưa xử lý được chat. Hãy nhờ quản trị viên gán chi nhánh.
    </div>
</c:if>

<c:if test="${not unassignedStoreWarning}">
<div id="chat-app" data-mode="staff" data-api="/admin/chat/api" data-topics='${chatTopics}'
     data-show-store="${isStaffScoped ? 'false' : 'true'}" data-initial="${initialConversationId}"
     class="grid grid-cols-1 lg:grid-cols-12 gap-4">

    <aside class="lg:col-span-4 rounded-2xl bg-white/[0.03] border border-white/10 overflow-hidden flex flex-col max-h-[72vh]">
        <div class="p-4 border-b border-white/10 flex flex-col gap-2">
            <select data-chat-filter-status class="glass-input px-3 py-2 text-xs rounded-xl text-slate-100">
                <option value="">Mọi trạng thái</option>
                <option value="OPEN">Đang mở</option>
                <option value="CLOSED">Đã đóng</option>
            </select>
            <c:if test="${not isStaffScoped}">
                <select data-chat-filter-store class="glass-input px-3 py-2 text-xs rounded-xl text-slate-100">
                    <option value="">Mọi chi nhánh</option>
                    <c:forEach var="store" items="${stores}"><option value="${store.id}">${fn:escapeXml(store.name)}</option></c:forEach>
                </select>
            </c:if>
        </div>
        <ul data-chat-list class="flex-1 overflow-y-auto divide-y divide-white/5"></ul>
        <p data-chat-list-empty class="hidden px-4 py-8 text-center text-xs text-slate-500">Chưa có cuộc trò chuyện nào.</p>
    </aside>

    <section class="lg:col-span-8 rounded-2xl bg-white/[0.03] border border-white/10 overflow-hidden flex flex-col h-[72vh]">
        <header class="px-5 py-3.5 border-b border-white/10 flex items-center justify-between gap-3">
            <div class="min-w-0">
                <h2 data-chat-title class="text-sm font-semibold text-white truncate">Chọn một cuộc trò chuyện</h2>
                <p data-chat-subtitle class="text-[11px] text-slate-400 mt-0.5 truncate"></p>
            </div>
            <button type="button" data-chat-close class="hidden shrink-0 px-3 py-1.5 rounded-lg border border-rose-500/30 text-rose-300 hover:bg-rose-500/10 text-[11px] font-semibold transition">
                <i class="fa-solid fa-circle-check mr-1"></i>Đóng cuộc trò chuyện
            </button>
        </header>
        <div class="relative flex-1 min-h-0">
            <div data-chat-thread class="absolute inset-0 overflow-y-auto px-5 py-4 flex flex-col gap-2.5"></div>
            <p data-chat-placeholder class="absolute inset-0 flex items-center justify-center text-center text-xs text-slate-500 px-8 pointer-events-none">
                <span><i class="fa-regular fa-comments text-3xl text-slate-600 block mb-2"></i>Chọn một cuộc trò chuyện ở bên trái để xem và trả lời.</span>
            </p>
        </div>
        <p data-chat-closed-hint class="hidden mx-5 mb-2 text-[11px] text-slate-400"><i class="fa-solid fa-lock mr-1"></i>Cuộc trò chuyện đã đóng. Khi khách nhắn lại, cuộc trò chuyện sẽ tự mở.</p>
        <p data-chat-error class="hidden mx-5 mb-2 p-2.5 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-200 text-xs"></p>
        <form data-chat-form class="p-3 border-t border-white/10 flex items-end gap-2">
            <textarea data-chat-input rows="2" maxlength="1000" disabled placeholder="Nhập câu trả lời... (Enter để gửi, Shift+Enter xuống dòng)"
                      class="glass-input flex-1 px-3 py-2 text-xs rounded-xl text-slate-100 resize-none"></textarea>
            <div class="flex flex-col items-end gap-1">
                <span data-chat-counter class="text-[10px] text-slate-500">0/1000</span>
                <button type="submit" data-chat-send disabled class="px-4 py-2 rounded-full bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold text-xs hover:from-yellow-400 hover:to-amber-500 transition disabled:opacity-40 disabled:cursor-not-allowed">
                    <i class="fa-solid fa-paper-plane mr-1"></i>Gửi
                </button>
            </div>
        </form>
    </section>
</div>
<script src="/js/chat.js"></script>
</c:if>

<jsp:include page="/WEB-INF/views/admin/common/page-bottom.jsp"/>
