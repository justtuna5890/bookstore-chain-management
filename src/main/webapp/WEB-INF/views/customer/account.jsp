<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Tài khoản của tôi" scope="request"/>
<jsp:include page="/WEB-INF/views/customer/common/layout-top.jsp"/>

<div class="mt-6">
    <h1 class="text-2xl md:text-3xl font-serif font-bold text-white">Tài khoản của tôi</h1>
    <p class="text-xs text-slate-400 mt-1">Thông tin liên hệ và bảo mật của bạn.</p>
</div>

<div class="mt-6 grid grid-cols-1 lg:grid-cols-12 gap-6">
    <div class="lg:col-span-7 flex flex-col gap-5">
        <!-- Thông tin cá nhân -->
        <form action="/account/profile" method="post" class="rounded-2xl glass-panel-subtle border border-white/10 p-5">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <h2 class="text-xs uppercase tracking-wider font-semibold text-brand-gold mb-4">Thông tin cá nhân</h2>

            <c:if test="${not empty profileSuccess}">
                <div class="mb-4 p-3 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-200 text-xs flex items-start gap-2.5">
                    <i class="fa-solid fa-circle-check text-emerald-400 mt-0.5"></i><span>${fn:escapeXml(profileSuccess)}</span>
                </div>
            </c:if>
            <c:if test="${not empty profileError}">
                <div class="mb-4 p-3 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-200 text-xs flex items-start gap-2.5">
                    <i class="fa-solid fa-circle-exclamation text-rose-400 mt-0.5"></i><span>${fn:escapeXml(profileError)}</span>
                </div>
            </c:if>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <label class="block text-xs text-slate-300">Tên đăng nhập
                    <input type="text" value="${fn:escapeXml(profile.username)}" disabled class="form-field mt-1.5 w-full px-3 py-2 rounded-xl text-sm text-slate-400 opacity-70 cursor-not-allowed"/>
                </label>
                <label class="block text-xs text-slate-300">Email
                    <input type="text" value="${fn:escapeXml(profile.email)}" disabled class="form-field mt-1.5 w-full px-3 py-2 rounded-xl text-sm text-slate-400 opacity-70 cursor-not-allowed"/>
                </label>
                <label class="block text-xs text-slate-300">Họ và tên
                    <input type="text" name="fullName" value="${fn:escapeXml(profile.fullName)}" maxlength="100" required class="form-field mt-1.5 w-full px-3 py-2 rounded-xl text-sm text-slate-100"/>
                </label>
                <label class="block text-xs text-slate-300">Số điện thoại
                    <input type="tel" name="phone" value="${fn:escapeXml(profile.phone)}" maxlength="15" placeholder="VD: 0901234567" class="form-field mt-1.5 w-full px-3 py-2 rounded-xl text-sm text-slate-100"/>
                </label>
            </div>
            <div class="flex items-center justify-between mt-5">
                <span class="text-[11px] text-slate-500">Thành viên từ ${profile.formattedCreatedAt}</span>
                <button type="submit" class="px-5 py-2 rounded-full bg-gradient-to-r from-brand-gold to-yellow-500 text-slate-950 font-bold text-xs hover:from-yellow-400 hover:to-amber-500 transition">
                    <i class="fa-solid fa-floppy-disk mr-1"></i>Lưu thay đổi
                </button>
            </div>
        </form>

        <!-- Đổi mật khẩu -->
        <form action="/account/password" method="post" autocomplete="off" class="rounded-2xl glass-panel-subtle border border-white/10 p-5">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <h2 class="text-xs uppercase tracking-wider font-semibold text-brand-gold mb-4">Đổi mật khẩu</h2>

            <c:if test="${not empty passwordSuccess}">
                <div class="mb-4 p-3 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-200 text-xs flex items-start gap-2.5">
                    <i class="fa-solid fa-circle-check text-emerald-400 mt-0.5"></i><span>${fn:escapeXml(passwordSuccess)}</span>
                </div>
            </c:if>
            <c:if test="${not empty passwordError}">
                <div class="mb-4 p-3 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-200 text-xs flex items-start gap-2.5">
                    <i class="fa-solid fa-circle-exclamation text-rose-400 mt-0.5"></i><span>${fn:escapeXml(passwordError)}</span>
                </div>
            </c:if>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <label class="block text-xs text-slate-300 sm:col-span-2">Mật khẩu hiện tại
                    <input type="password" name="currentPassword" required autocomplete="current-password" class="form-field mt-1.5 w-full px-3 py-2 rounded-xl text-sm text-slate-100"/>
                </label>
                <label class="block text-xs text-slate-300">Mật khẩu mới
                    <input type="password" name="newPassword" minlength="6" maxlength="64" required autocomplete="new-password" class="form-field mt-1.5 w-full px-3 py-2 rounded-xl text-sm text-slate-100"/>
                </label>
                <label class="block text-xs text-slate-300">Nhập lại mật khẩu mới
                    <input type="password" name="confirmPassword" minlength="6" maxlength="64" required autocomplete="new-password" class="form-field mt-1.5 w-full px-3 py-2 rounded-xl text-sm text-slate-100"/>
                </label>
            </div>
            <p class="text-[11px] text-slate-500 mt-2">Từ 6 đến 64 ký tự và khác mật khẩu hiện tại.</p>
            <div class="flex justify-end mt-4">
                <button type="submit" class="px-5 py-2 rounded-full border border-brand-gold/40 text-brand-gold font-bold text-xs hover:bg-brand-gold/10 transition">
                    <i class="fa-solid fa-key mr-1"></i>Đổi mật khẩu
                </button>
            </div>
        </form>
    </div>

    <!-- Lối tắt -->
    <aside class="lg:col-span-5 flex flex-col gap-3 self-start">
        <a href="/orders" class="flex items-center gap-3 p-4 rounded-2xl glass-panel-subtle border border-white/10 hover:border-brand-gold/40 hover:bg-white/5 transition">
            <span class="w-9 h-9 rounded-full bg-brand-gold/15 text-brand-gold flex items-center justify-center"><i class="fa-solid fa-receipt text-sm"></i></span>
            <span class="text-sm text-slate-100 font-semibold">Đơn hàng của tôi<span class="block text-[11px] text-slate-400 font-normal">Theo dõi và huỷ đơn chưa xử lý</span></span>
        </a>
        <a href="/wishlist" class="flex items-center gap-3 p-4 rounded-2xl glass-panel-subtle border border-white/10 hover:border-brand-gold/40 hover:bg-white/5 transition">
            <span class="w-9 h-9 rounded-full bg-rose-500/15 text-rose-400 flex items-center justify-center"><i class="fa-solid fa-heart text-sm"></i></span>
            <span class="text-sm text-slate-100 font-semibold">Sách yêu thích<span class="block text-[11px] text-slate-400 font-normal">Những cuốn bạn đang theo dõi</span></span>
        </a>
        <a href="/cart" class="flex items-center gap-3 p-4 rounded-2xl glass-panel-subtle border border-white/10 hover:border-brand-gold/40 hover:bg-white/5 transition">
            <span class="w-9 h-9 rounded-full bg-emerald-500/15 text-emerald-400 flex items-center justify-center"><i class="fa-solid fa-cart-shopping text-sm"></i></span>
            <span class="text-sm text-slate-100 font-semibold">Giỏ hàng<span class="block text-[11px] text-slate-400 font-normal">Tiếp tục thanh toán</span></span>
        </a>
    </aside>
</div>

<jsp:include page="/WEB-INF/views/customer/common/layout-bottom.jsp"/>
