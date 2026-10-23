<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- Chuông thông báo của người dùng đã đăng nhập. Nội dung và số chưa đọc do /js/notifications.js nạp và cập nhật realtime. --%>
<div class="relative" data-notification-bell>
    <button type="button" data-notification-toggle aria-haspopup="true" aria-expanded="false" title="Thông báo"
            class="relative p-2 rounded-full glass-panel-subtle hover:bg-white/10 transition text-slate-300">
        <i class="fa-regular fa-bell text-sm"></i>
        <span data-notification-badge class="hidden absolute -top-1 -right-1 min-w-[18px] h-[18px] px-1 rounded-full bg-rose-500 text-white text-[10px] font-bold flex items-center justify-center">0</span>
    </button>
    <div data-notification-panel class="hidden absolute right-0 mt-2 w-[22rem] max-w-[92vw] z-50 rounded-2xl border border-white/10 bg-[#12151c]/95 backdrop-blur-xl shadow-2xl overflow-hidden">
        <div class="flex items-center justify-between px-4 py-3 border-b border-white/10">
            <span class="text-xs font-semibold text-white">Thông báo</span>
            <button type="button" data-notification-read-all class="text-[11px] text-brand-gold hover:text-white transition">Đánh dấu đã đọc tất cả</button>
        </div>
        <ul data-notification-list class="max-h-96 overflow-y-auto divide-y divide-white/5">
            <li class="px-4 py-6 text-center text-xs text-slate-500">Đang tải...</li>
        </ul>
        <a href="/notifications" class="block px-4 py-2.5 text-center text-[11px] text-slate-300 hover:text-brand-gold border-t border-white/10 transition">Xem tất cả thông báo</a>
    </div>
</div>
