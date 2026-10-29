<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
        </main>
    </div>

    <!-- Minimalist Footer (với khoảng đệm cho mobile dock) -->
    <footer class="relative z-10 max-w-[1520px] w-full mx-auto mt-6 px-4 text-center text-[11px] text-slate-500 pb-20 lg:pb-2">
        &copy; OldBook — Hệ thống chuỗi cửa hàng sách cũ tuyển chọn
    </footer>

    <!-- ======================================================== -->
    <!-- FLOATING MOBILE & TABLET BOTTOM DOCK (App Navigation Bar) -->
    <!-- Hiển thị trên màn hình nhỏ < xl (Điện thoại & Tablet)    -->
    <!-- ======================================================== -->
    <nav class="lg:hidden fixed bottom-3 inset-x-3 sm:inset-x-auto sm:left-1/2 sm:-translate-x-1/2 z-40 max-w-md w-[calc(100%-1.5rem)] sm:w-auto glass-panel rounded-full py-1.5 px-3.5 border border-white/15 shadow-glass-elevated flex items-center justify-around sm:gap-6 backdrop-blur-2xl" aria-label="Điều hướng di động">
        <!-- 1. Trang chủ / Khám phá -->
        <a href="/" class="flex flex-col items-center gap-0.5 py-1 px-2 text-slate-400 hover:text-brand-gold active:scale-95 transition" title="Khám phá">
            <i class="fa-solid fa-house text-sm"></i>
            <span class="text-[9px] font-medium leading-none">Khám phá</span>
        </a>

        <!-- 2. Kho sách -->
        <a href="/#catalog" class="flex flex-col items-center gap-0.5 py-1 px-2 text-slate-400 hover:text-brand-gold active:scale-95 transition" title="Kho sách">
            <i class="fa-solid fa-book-bookmark text-sm"></i>
            <span class="text-[9px] font-medium leading-none">Kho sách</span>
        </a>

        <!-- 3. Giỏ hàng -->
        <a href="/cart" class="relative flex flex-col items-center gap-0.5 py-1 px-2 text-slate-400 hover:text-brand-gold active:scale-95 transition" title="Giỏ hàng">
            <i class="fa-solid fa-cart-shopping text-sm"></i>
            <span data-cart-count class="absolute top-0 right-0 min-w-[15px] h-[15px] px-0.5 rounded-full bg-brand-gold text-slate-950 text-[9px] font-black flex items-center justify-center shadow ${empty cartCount || cartCount == 0 ? 'hidden' : ''}">${empty cartCount ? 0 : cartCount}</span>
            <span class="text-[9px] font-medium leading-none">Giỏ hàng</span>
        </a>

        <!-- 4. Thông báo (Nổi bật nhất) -->
        <c:if test="${pageContext.request.userPrincipal != null}">
            <button type="button" data-notification-toggle aria-haspopup="true" aria-expanded="false" 
                    class="relative flex flex-col items-center gap-0.5 py-1 px-3 rounded-full bg-gradient-to-br from-amber-500/25 via-brand-gold/35 to-amber-600/25 border border-brand-gold text-brand-gold shadow-[0_0_12px_rgba(212,175,55,0.4)] active:scale-95 transition" title="Trung tâm Thông báo">
                <span class="relative flex items-center justify-center">
                    <i class="fa-solid fa-bell text-sm"></i>
                    <span class="absolute -top-0.5 -right-0.5 flex h-1.5 w-1.5">
                        <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75"></span>
                        <span class="relative inline-flex rounded-full h-1.5 w-1.5 bg-amber-500"></span>
                    </span>
                </span>
                <span data-notification-badge class="hidden absolute top-0 right-1 min-w-[15px] h-[15px] px-0.5 rounded-full bg-rose-500 text-white text-[9px] font-black flex items-center justify-center shadow border border-rose-300 animate-pulse">0</span>
                <span class="text-[9px] font-bold leading-none">Thông báo</span>
            </button>
        </c:if>
        <c:if test="${pageContext.request.userPrincipal == null}">
            <a href="/login" class="flex flex-col items-center gap-0.5 py-1 px-2 text-slate-400 hover:text-brand-gold active:scale-95 transition" title="Thông báo">
                <i class="fa-solid fa-bell text-sm text-brand-gold"></i>
                <span class="text-[9px] font-medium leading-none">Thông báo</span>
            </a>
        </c:if>

        <!-- 5. Đơn hàng (hoặc Thu mua) -->
        <c:if test="${pageContext.request.userPrincipal != null}">
            <a href="/orders" class="flex flex-col items-center gap-0.5 py-1 px-2 text-slate-400 hover:text-brand-gold active:scale-95 transition" title="Đơn hàng">
                <i class="fa-solid fa-receipt text-sm"></i>
                <span class="text-[9px] font-medium leading-none">Đơn hàng</span>
            </a>
        </c:if>
        <c:if test="${pageContext.request.userPrincipal == null}">
            <a href="/#buyback" class="flex flex-col items-center gap-0.5 py-1 px-2 text-slate-400 hover:text-brand-gold active:scale-95 transition" title="Thu mua">
                <i class="fa-solid fa-hand-holding-dollar text-sm"></i>
                <span class="text-[9px] font-medium leading-none">Thu mua</span>
            </a>
        </c:if>

        <!-- 6. Tài khoản / Đăng nhập -->
        <c:if test="${pageContext.request.userPrincipal != null}">
            <a href="/account" class="flex flex-col items-center gap-0.5 py-1 px-2 text-slate-400 hover:text-brand-gold active:scale-95 transition" title="Tài khoản">
                <i class="fa-solid fa-user text-sm"></i>
                <span class="text-[9px] font-medium leading-none">Tài khoản</span>
            </a>
        </c:if>
        <c:if test="${pageContext.request.userPrincipal == null}">
            <a href="/login" class="flex flex-col items-center gap-0.5 py-1 px-2 text-brand-gold hover:text-white active:scale-95 transition" title="Đăng nhập">
                <i class="fa-solid fa-arrow-right-to-bracket text-sm"></i>
                <span class="text-[9px] font-semibold leading-none">Đăng nhập</span>
            </a>
        </c:if>
    </nav>

    <script src="/js/cart.js"></script>
    <script src="/js/wishlist.js"></script>
    <script src="/js/notifications.js"></script>
</body>
</html>
