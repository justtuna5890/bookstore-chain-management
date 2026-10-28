<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- Chuông thông báo nổi bật dành cho người dùng đã đăng nhập --%>
<div class="relative inline-block" data-notification-bell>
    <button type="button" data-notification-toggle aria-haspopup="true" aria-expanded="false" title="Trung tâm Thông báo"
            class="relative p-2 rounded-full bg-gradient-to-br from-amber-500/20 via-brand-gold/30 to-amber-600/20 text-brand-gold border border-brand-gold/50 hover:bg-brand-gold/25 shadow-glow-gold transition flex items-center justify-center group">
        <i class="fa-solid fa-bell text-sm group-hover:animate-bounce"></i>
        <span data-notification-badge class="hidden absolute -top-1 -right-1 min-w-[16px] h-[16px] px-1 rounded-full bg-rose-500 text-white text-[9px] font-black flex items-center justify-center shadow-lg border border-rose-300 ring-2 ring-[#0b0c10] animate-pulse">0</span>
    </button>
</div>
