<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authorize access="hasRole('ADMIN')">
    <c:set var="userRole" value="ADMIN" />
    <c:set var="isAdmin" value="true" />
</sec:authorize>
<sec:authorize access="hasRole('MANAGER')">
    <c:set var="userRole" value="MANAGER" />
    <c:set var="isManager" value="true" />
</sec:authorize>
<sec:authorize access="hasRole('STAFF')">
    <c:set var="userRole" value="STAFF" />
    <c:set var="isStaff" value="true" />
</sec:authorize>

<!-- ========================================== -->
<!-- UNIFIED ADMIN NAVIGATION DOCK (OLDBOOK)   -->
<!-- Support: Desktop Dock + Mobile Header Bar  -->
<!-- ========================================== -->

<!-- 1. Mobile & Tablet Top Bar (xl:hidden) -->
<div class="xl:hidden w-full mb-6 glass-panel rounded-2xl p-3 border border-white/10 shadow-glass-elevated">
    <div class="flex items-center justify-between gap-3 mb-2.5 pb-2.5 border-b border-white/10">
        <a href="/" class="flex items-center gap-2 text-brand-gold font-serif font-bold text-sm">
            <div class="w-8 h-8 rounded-full bg-brand-gold/15 border border-brand-gold/30 flex items-center justify-center">
                <i class="fa-solid fa-feather-pointed text-xs"></i>
            </div>
            <span>OldBook Admin</span>
        </a>
        <div class="flex items-center gap-2">
            <c:choose>
                <c:when test="${isAdmin}">
                    <span class="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-rose-600/20 border border-rose-500/40 text-rose-300">Quản trị hệ thống</span>
                </c:when>
                <c:when test="${isManager}">
                    <span class="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-purple-600/20 border border-purple-500/40 text-purple-300">Quản lý vận hành</span>
                </c:when>
                <c:when test="${isStaff}">
                    <span class="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-sky-600/20 border border-sky-500/40 text-sky-300">Nhân viên chi nhánh</span>
                </c:when>
            </c:choose>

            <!-- Mobile Notification Bell -->
            <div class="relative">
                <button type="button" onclick="toggleNotifDropdown()" class="w-7 h-7 rounded-lg bg-brand-gold/10 hover:bg-brand-gold/20 text-brand-gold flex items-center justify-center transition relative" title="Thông báo thời gian thực">
                    <i class="fa-solid fa-bell text-xs"></i>
                    <span id="mobileNotifBadge" class="hidden absolute -top-1 -right-1 min-w-[15px] h-[15px] px-1 rounded-full bg-rose-500 text-white text-[9px] font-bold flex items-center justify-center animate-pulse">0</span>
                </button>
            </div>

            <form action="/logout" method="post" class="m-0 inline">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <button type="submit" class="w-7 h-7 rounded-lg bg-rose-500/10 hover:bg-rose-500/20 text-rose-300 flex items-center justify-center transition" title="Đăng xuất">
                    <i class="fa-solid fa-power-off text-xs"></i>
                </button>
            </form>
        </div>
    </div>
    <!-- Horizontal Scrollable Nav Pills for Mobile -->
    <div class="flex items-center gap-2 overflow-x-auto pb-1 text-xs no-scrollbar">
        <a href="/admin/dashboard" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'dashboard' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">
            <i class="fa-solid fa-gauge-high text-xs"></i>
            <span>Dashboard</span>
        </a>
        <c:if test="${isAdmin}">
            <a href="/admin/users" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'users' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">
                <i class="fa-solid fa-users-gear text-xs"></i>
                <span>Nhân Sự</span>
            </a>
        </c:if>
        <c:if test="${isAdmin || isManager}">
            <a href="/admin/stores" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'stores' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">
                <i class="fa-solid fa-store text-xs"></i>
                <span>Chi Nhánh</span>
            </a>
        </c:if>
        <a href="/admin/books" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'books' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">
            <i class="fa-solid fa-book text-xs"></i>
            <span>Đầu Sách</span>
        </a>
        <a href="/admin/book-copies" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'copies' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">
            <i class="fa-solid fa-boxes-stacked text-xs"></i>
            <span>Kho Bản Sách</span>
        </a>
        <a href="/admin/orders" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'orders' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">
            <i class="fa-solid fa-bag-shopping text-xs"></i>
            <span>Đơn Hàng</span>
        </a>
        <a href="/admin/buybacks" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'buybacks' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">
            <i class="fa-solid fa-hand-holding-dollar text-xs"></i>
            <span>Thu Mua</span>
        </a>
        <a href="/admin/transfers" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'transfers' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">
            <i class="fa-solid fa-truck-ramp-box text-xs"></i>
            <span>Chuyển Kho</span>
        </a>
        <c:if test="${isAdmin || isManager}">
            <a href="/admin/reports" class="px-3 py-1.5 rounded-xl whitespace-nowrap transition flex items-center gap-1.5 ${param.active == 'reports' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300'}">
                <i class="fa-solid fa-chart-line text-xs"></i>
                <span>Báo Cáo Kỳ</span>
            </a>
        </c:if>
    </div>
</div>

<!-- 2. Desktop Floating Left Dock (hidden xl:flex) -->
<aside class="hidden xl:flex flex-col items-center justify-between py-6 px-3 rounded-full glass-panel shadow-glass-elevated sticky top-10 h-[670px] w-[64px] border border-white/10 shrink-0">
    <a class="w-10 h-10 rounded-full bg-brand-gold/15 border border-brand-gold/30 flex items-center justify-center text-brand-gold hover:bg-brand-gold/25 transition-all" href="/" title="Về Sàn Khám Phá Trang Chủ">
        <i class="fa-solid fa-feather-pointed text-brand-gold"></i>
    </a>

    <nav class="flex flex-col items-center gap-4 my-auto">
        <!-- Dashboard -->
        <a class="p-2.5 rounded-full transition group relative ${param.active == 'dashboard' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
           href="/admin/dashboard" title="Tổng quan Dashboard">
            <i class="fa-solid fa-gauge-high text-base"></i>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Tổng Quan Dashboard</span>
        </a>

        <!-- Nhân sự (Chỉ ADMIN) -->
        <c:if test="${isAdmin}">
            <a class="p-2.5 rounded-full transition group relative ${param.active == 'users' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
               href="/admin/users" title="Quản lý nhân sự">
                <i class="fa-solid fa-users-gear text-base"></i>
                <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Tài Khoản & Quyền</span>
            </a>
        </c:if>

        <!-- Chi nhánh (ADMIN & MANAGER) -->
        <c:if test="${isAdmin || isManager}">
            <a class="p-2.5 rounded-full transition group relative ${param.active == 'stores' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
               href="/admin/stores" title="Mạng lưới chi nhánh">
                <i class="fa-solid fa-store text-base"></i>
                <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Chi Nhánh</span>
            </a>
        </c:if>

        <!-- Đầu sách -->
        <a class="p-2.5 rounded-full transition group relative ${param.active == 'books' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
           href="/admin/books" title="Danh mục đầu sách">
            <i class="fa-solid fa-book text-base"></i>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Đầu Sách</span>
        </a>

        <!-- Kho bản sách -->
        <a class="p-2.5 rounded-full transition group relative ${param.active == 'copies' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
           href="/admin/book-copies" title="Quản lý bản sao tồn kho">
            <i class="fa-solid fa-boxes-stacked text-base"></i>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Kho Bản Sách</span>
        </a>

        <!-- Đơn hàng của khách -->
        <a class="p-2.5 rounded-full transition group relative ${param.active == 'orders' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
           href="/admin/orders" title="Đơn hàng của khách">
            <i class="fa-solid fa-bag-shopping text-base"></i>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Đơn Hàng</span>
        </a>

        <!-- Thu mua -->
        <a class="p-2.5 rounded-full transition group relative ${param.active == 'buybacks' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
           href="/admin/buybacks" title="Duyệt thu mua sách cũ">
            <i class="fa-solid fa-hand-holding-dollar text-base"></i>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Duyệt Thu Mua</span>
        </a>

        <!-- Chuyển kho -->
        <a class="p-2.5 rounded-full transition group relative ${param.active == 'transfers' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
           href="/admin/transfers" title="Điều chuyển kho chi nhánh">
            <i class="fa-solid fa-truck-ramp-box text-base"></i>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Chuyển Kho</span>
        </a>

        <!-- Báo cáo kỳ chuyên sâu (ADMIN & MANAGER) -->
        <c:if test="${isAdmin || isManager}">
            <a class="p-2.5 rounded-full transition group relative ${param.active == 'reports' ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'text-slate-400 hover:text-brand-gold hover:bg-white/5'}" 
               href="/admin/reports" title="Báo cáo chuyên sâu theo kỳ">
                <i class="fa-solid fa-chart-line text-base"></i>
                <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Báo Cáo Kỳ</span>
            </a>
        </c:if>
    </nav>

    <!-- Desktop User Role Pill -->
    <div class="relative mb-3 group">
        <div class="w-9 h-9 rounded-full flex items-center justify-center font-bold text-xs border cursor-pointer transition shadow-sm ${isAdmin ? 'bg-rose-500/20 border-rose-500/40 text-rose-300' : isManager ? 'bg-purple-500/20 border-purple-500/40 text-purple-300' : 'bg-sky-500/20 border-sky-500/40 text-sky-300'}">
            ${pageContext.request.userPrincipal != null ? pageContext.request.userPrincipal.name.substring(0, 1).toUpperCase() : 'U'}
        </div>
        <div class="absolute left-14 bottom-0 ml-2 p-3 rounded-2xl glass-panel shadow-glass-elevated border border-white/10 opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap z-50 text-left">
            <div class="text-xs font-bold text-white">${pageContext.request.userPrincipal.principal.user.fullName}</div>
            <div class="text-[10px] ${isAdmin ? 'text-rose-400' : isManager ? 'text-purple-400' : 'text-sky-400'} font-semibold mt-0.5">
                ${isAdmin ? 'Quản trị hệ thống (ADMIN)' : isManager ? 'Quản lý vận hành (MANAGER)' : 'Nhân viên chi nhánh (STAFF)'}
            </div>
            <c:if test="${not empty pageContext.request.userPrincipal.principal.user.store}">
                <div class="text-[10px] text-brand-gold mt-1 flex items-center gap-1">
                    <i class="fa-solid fa-store text-[9px]"></i>
                    <span>${pageContext.request.userPrincipal.principal.user.store.name}</span>
                </div>
            </c:if>
        </div>
    </div>

    <!-- Desktop Notification Bell -->
    <div class="relative mb-2">
        <button type="button" onclick="toggleNotifDropdown()" class="p-2.5 rounded-full text-slate-400 hover:text-brand-gold hover:bg-white/5 transition group relative" title="Thông báo thời gian thực">
            <i class="fa-solid fa-bell text-base"></i>
            <span id="desktopNotifBadge" class="hidden absolute top-1 right-1 min-w-[15px] h-[15px] px-1 rounded-full bg-rose-500 text-white text-[9px] font-bold flex items-center justify-center animate-pulse">0</span>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-white opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Chuông Thông Báo</span>
        </button>
    </div>

    <!-- Logout -->
    <form action="/logout" method="post" class="m-0">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <button type="submit" class="p-2.5 rounded-full text-rose-400 hover:text-rose-300 hover:bg-rose-500/10 transition group relative" title="Đăng xuất">
            <i class="fa-solid fa-power-off text-base"></i>
            <span class="absolute left-14 ml-2 px-2.5 py-1 rounded bg-black/90 text-xs text-rose-300 opacity-0 group-hover:opacity-100 pointer-events-none transition whitespace-nowrap shadow-lg z-50">Đăng Xuất</span>
        </button>
    </form>
</aside>

<!-- ============================================== -->
<!-- 3. REAL-TIME NOTIFICATION DRAWER & TOAST POPUP -->
<!-- ============================================== -->

<!-- Notification Dropdown Panel -->
<div id="notifDropdown" class="hidden fixed top-16 right-4 sm:right-12 xl:left-24 xl:top-20 z-50 w-80 sm:w-96 rounded-2xl glass-panel p-4 shadow-glass-elevated border border-white/15 backdrop-blur-2xl">
    <div class="flex items-center justify-between pb-3 border-b border-white/10 mb-3">
        <div class="flex items-center gap-2">
            <i class="fa-solid fa-bell text-brand-gold text-xs"></i>
            <span class="text-xs font-bold text-white uppercase tracking-wider">Thông Báo Thời Gian Thực</span>
        </div>
        <button type="button" onclick="toggleNotifDropdown()" class="text-slate-400 hover:text-white text-xs p-1">
            <i class="fa-solid fa-xmark"></i>
        </button>
    </div>
    <div id="notifList" class="space-y-2.5 max-h-72 overflow-y-auto no-scrollbar text-xs">
        <div id="emptyNotifMsg" class="text-center py-6 text-slate-400">
            <i class="fa-regular fa-bell-slash text-xl mb-1.5 block opacity-40"></i>
            <span>Chưa có thông báo mới nào</span>
        </div>
    </div>
</div>

<!-- Floating Toast Container (Bottom-Right) -->
<div id="toastContainer" class="fixed bottom-6 right-6 z-50 flex flex-col gap-3 max-w-sm w-full pointer-events-none"></div>

<!-- ============================================== -->
<!-- 4. WEBSOCKET & AUDIO SYNTHESIZER SCRIPT        -->
<!-- ============================================== -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>

<script>
    let stompClient = null;
    let unreadCount = 0;

    // 1. Âm thanh chuông báo (Web Audio API Synthesizer - không phụ thuộc file ngoài, không lo 404)
    function playChimeSound() {
        try {
            const AudioCtx = window.AudioContext || window.webkitAudioContext;
            if (!AudioCtx) return;
            const ctx = new AudioCtx();

            // Nốt 1: 587.33Hz (D5)
            const osc1 = ctx.createOscillator();
            const gain1 = ctx.createGain();
            osc1.type = 'sine';
            osc1.frequency.setValueAtTime(587.33, ctx.currentTime);
            gain1.gain.setValueAtTime(0.12, ctx.currentTime);
            gain1.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.3);
            osc1.connect(gain1);
            gain1.connect(ctx.destination);
            osc1.start();
            osc1.stop(ctx.currentTime + 0.3);

            // Nốt 2: 880Hz (A5) ngân vang sau 100ms
            setTimeout(() => {
                const osc2 = ctx.createOscillator();
                const gain2 = ctx.createGain();
                osc2.type = 'triangle';
                osc2.frequency.setValueAtTime(880, ctx.currentTime);
                gain2.gain.setValueAtTime(0.18, ctx.currentTime);
                gain2.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + 0.55);
                osc2.connect(gain2);
                gain2.connect(ctx.destination);
                osc2.start();
                osc2.stop(ctx.currentTime + 0.55);
            }, 100);
        } catch (e) {
            console.warn("Audio autoplay blocked or unsupported:", e);
        }
    }

    // 2. Bật / Tắt danh sách thông báo
    function toggleNotifDropdown() {
        const dd = document.getElementById('notifDropdown');
        if (!dd) return;
        dd.classList.toggle('hidden');
        if (!dd.classList.contains('hidden')) {
            // Đặt lại số đếm khi mở xem
            unreadCount = 0;
            updateBadgeUI();
        }
    }

    // 3. Cập nhật Badge số lượng thông báo
    function updateBadgeUI() {
        const mob = document.getElementById('mobileNotifBadge');
        const desk = document.getElementById('desktopNotifBadge');
        [mob, desk].forEach(badge => {
            if (!badge) return;
            if (unreadCount > 0) {
                badge.innerText = unreadCount > 9 ? '9+' : unreadCount;
                badge.classList.remove('hidden');
            } else {
                badge.classList.add('hidden');
            }
        });
    }

    // 4. Xử lý nhận thông báo mới từ WebSocket
    function handleIncomingNotification(data) {
        // Phát âm thanh chuông "ting"
        playChimeSound();

        // Tăng đếm chưa đọc
        unreadCount++;
        updateBadgeUI();

        // Ẩn thông báo rỗng nếu có
        const emptyMsg = document.getElementById('emptyNotifMsg');
        if (emptyMsg) emptyMsg.style.display = 'none';

        // Thêm vào danh sách Dropdown
        const notifList = document.getElementById('notifList');
        if (notifList) {
            const isBuyback = data.type === 'BUYBACK';
            const icon = isBuyback ? 'fa-hand-holding-dollar text-amber-400' : 'fa-truck-ramp-box text-blue-400';
            const borderCol = isBuyback ? 'border-amber-500/30' : 'border-blue-500/30';

            const item = document.createElement('a');
            item.href = data.targetUrl || '#';
            item.className = 'block p-3 rounded-xl bg-white/[0.03] hover:bg-white/[0.08] border ' + borderCol + ' transition';
            item.innerHTML = '<div class="flex items-start gap-2.5">' +
                '<i class="fa-solid ' + icon + ' text-sm mt-0.5 shrink-0"></i>' +
                '<div class="flex-1">' +
                    '<div class="font-semibold text-white leading-tight">' + (data.title || 'Thông báo mới') + '</div>' +
                    '<p class="text-slate-300 text-[11px] mt-0.5 leading-snug">' + (data.message || '') + '</p>' +
                    '<span class="text-[10px] text-brand-gold block mt-1"><i class="fa-regular fa-clock mr-1"></i>Vừa xong</span>' +
                '</div>' +
            '</div>';

            notifList.prepend(item);
        }

        // Tạo Toast Popup góc màn hình
        showToast(data);
    }

    // 5. Hiển thị Toast Popup nổi góc màn hình
    function showToast(data) {
        const container = document.getElementById('toastContainer');
        if (!container) return;

        const isBuyback = data.type === 'BUYBACK';
        const icon = isBuyback ? 'fa-hand-holding-dollar text-amber-400' : 'fa-truck-ramp-box text-blue-400';
        const borderColor = isBuyback ? 'border-amber-500/40' : 'border-blue-500/40';

        const toast = document.createElement('div');
        toast.className = 'pointer-events-auto p-4 rounded-2xl glass-panel shadow-glass-elevated border ' + borderColor + ' transition-all duration-300 transform translate-y-3 opacity-0';
        toast.innerHTML = '<div class="flex items-start gap-3">' +
            '<div class="w-8 h-8 rounded-xl bg-white/10 flex items-center justify-center shrink-0">' +
                '<i class="fa-solid ' + icon + ' text-sm"></i>' +
            '</div>' +
            '<div class="flex-1">' +
                '<div class="flex items-center justify-between">' +
                    '<h4 class="text-xs font-bold text-white uppercase tracking-wider">' + (isBuyback ? 'Thu Mua Mới' : 'Điều Chuyển Kho') + '</h4>' +
                    '<span class="text-[10px] text-slate-400">Vừa xong</span>' +
                '</div>' +
                '<p class="text-xs text-slate-200 font-medium mt-1 leading-snug">' + (data.message || '') + '</p>' +
                '<div class="mt-2.5 flex items-center gap-2">' +
                    '<a href="' + (data.targetUrl || '#') + '" class="px-2.5 py-1 rounded-lg bg-brand-gold text-slate-950 font-bold text-[10px] hover:bg-yellow-400 transition">Xem Chi Tiết</a>' +
                    '<button type="button" onclick="this.closest(\'.pointer-events-auto\').remove()" class="px-2 py-1 rounded-lg text-slate-400 hover:text-white text-[10px]">Đóng</button>' +
                '</div>' +
            '</div>' +
        '</div>';

        container.appendChild(toast);

        // Slide-in animation
        requestAnimationFrame(() => {
            toast.classList.remove('translate-y-3', 'opacity-0');
            toast.classList.add('translate-y-0', 'opacity-100');
        });

        // Tự động biến mất sau 6 giây
        setTimeout(() => {
            toast.classList.remove('translate-y-0', 'opacity-100');
            toast.classList.add('translate-y-3', 'opacity-0');
            setTimeout(() => toast.remove(), 300);
        }, 6000);
    }

    // 6. Kết nối WebSocket STOMP
    function initWebSocket() {
        if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
            console.warn("SockJS or Stomp not loaded");
            return;
        }

        try {
            const socket = new SockJS('/ws');
            stompClient = Stomp.over(socket);
            stompClient.debug = null; // Tắt log debug

            stompClient.connect({}, function (frame) {
                // Topic chung: Toàn bộ Admin / Manager
                stompClient.subscribe('/topic/notifications', function (res) {
                    try {
                        const notif = JSON.parse(res.body);
                        handleIncomingNotification(notif);
                    } catch (e) {
                        console.error("Lỗi parse thông báo:", e);
                    }
                });

                // Topic riêng: Theo chi nhánh của nhân viên phụ trách (nếu có)
                <c:set var="uStoreId" value="${pageContext.request.userPrincipal != null && pageContext.request.userPrincipal.principal.user.store != null ? pageContext.request.userPrincipal.principal.user.store.id : ''}" />
                <c:if test="${not empty uStoreId}">
                    stompClient.subscribe('/topic/store/' + '${uStoreId}', function (res) {
                        try {
                            const notif = JSON.parse(res.body);
                            handleIncomingNotification(notif);
                        } catch (e) {
                            console.error("Lỗi parse thông báo chi nhánh:", e);
                        }
                    });
                </c:if>
            }, function (err) {
                console.warn("WebSocket disconnect, retrying in 5s...", err);
                setTimeout(initWebSocket, 5000);
            });
        } catch (err) {
            console.error("Lỗi khởi tạo WebSocket:", err);
        }
    }

    // Kích hoạt khi trang đã sẵn sàng
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initWebSocket);
    } else {
        initWebSocket();
    }
</script>
