<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Chat với chi nhánh" scope="request"/>
<jsp:include page="/WEB-INF/views/customer/common/layout-top.jsp"/>

<div class="mt-6">
    <h1 class="text-2xl md:text-3xl font-serif font-bold text-white">Chat với chi nhánh</h1>
    <p class="text-xs text-slate-400 mt-1">Hỏi nhân viên về sách, đơn hàng hoặc thu mua. Mỗi chi nhánh có một cuộc trò chuyện riêng.</p>
</div>

<div id="chat-app" data-mode="customer" data-api="/chat/api" data-topics='["/user/queue/chat"]'
     data-initial="${initialConversationId}" data-initial-store="${initialStoreId}"
     class="mt-6 grid grid-cols-1 lg:grid-cols-12 gap-4">

    <!-- Danh sách cuộc trò chuyện và chọn chi nhánh để bắt đầu -->
    <aside class="lg:col-span-4 rounded-2xl glass-panel-subtle border border-white/10 overflow-hidden flex flex-col max-h-[70vh]">
        <div class="p-4 border-b border-white/10">
            <label class="block text-[11px] uppercase tracking-wider font-semibold text-brand-gold mb-1.5" for="chat-store">Nhắn cho chi nhánh</label>
            <select id="chat-store" data-chat-store class="form-field w-full px-3 py-2 rounded-xl text-xs text-slate-100">
                <option value="">Chọn chi nhánh...</option>
                <c:forEach var="store" items="${stores}">
                    <option value="${store.id}">${fn:escapeXml(store.name)}</option>
                </c:forEach>
            </select>
        </div>
        <ul data-chat-list class="flex-1 overflow-y-auto divide-y divide-white/5"></ul>
        <p data-chat-list-empty class="hidden px-4 py-8 text-center text-xs text-slate-500">Bạn chưa có cuộc trò chuyện nào. Chọn một chi nhánh để bắt đầu.</p>
    </aside>

    <!-- Luồng tin nhắn -->
    <section class="lg:col-span-8 rounded-2xl glass-panel-subtle border border-white/10 overflow-hidden flex flex-col h-[70vh]">
        <header class="px-5 py-3.5 border-b border-white/10">
            <h2 data-chat-title class="text-sm font-semibold text-white">Chat với chi nhánh</h2>
            <p data-chat-subtitle class="text-[11px] text-slate-400 mt-0.5"></p>
        </header>
        <div class="relative flex-1 min-h-0">
            <div data-chat-thread class="absolute inset-0 overflow-y-auto px-5 py-4 flex flex-col gap-2.5"></div>
            <p data-chat-placeholder class="absolute inset-0 flex items-center justify-center text-center text-xs text-slate-500 px-8 pointer-events-none">
                <span><i class="fa-regular fa-comments text-3xl text-slate-600 block mb-2"></i>Chọn chi nhánh ở bên trái hoặc mở một cuộc trò chuyện đã có.</span>
            </p>
        </div>
        <p data-chat-error class="hidden mx-5 mb-2 p-2.5 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-200 text-xs"></p>
        <form data-chat-form class="p-3 border-t border-white/10 flex items-end gap-2">
            <textarea data-chat-input rows="2" maxlength="1000" disabled placeholder="Nhập tin nhắn... (Enter để gửi, Shift+Enter xuống dòng)"
                      class="form-field flex-1 px-3 py-2 rounded-xl text-xs text-slate-100 resize-none"></textarea>
            <div class="flex flex-col items-end gap-1">
                <span data-chat-counter class="text-[10px] text-slate-500">0/1000</span>
                <button type="submit" data-chat-send disabled class="px-4 py-2 rounded-full bg-gradient-to-r from-brand-gold to-yellow-500 text-slate-950 font-bold text-xs hover:from-yellow-400 hover:to-amber-500 transition disabled:opacity-40 disabled:cursor-not-allowed">
                    <i class="fa-solid fa-paper-plane mr-1"></i>Gửi
                </button>
            </div>
        </form>
    </section>
</div>

<script src="/js/chat.js"></script>

<jsp:include page="/WEB-INF/views/customer/common/layout-bottom.jsp"/>
