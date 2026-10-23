<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="adminTitle" value="${empty editingId ? 'Tạo Mã Giảm Giá' : 'Sửa Mã Giảm Giá'}" scope="request"/>
<c:set var="adminActive" value="promotions" scope="request"/>
<jsp:include page="/WEB-INF/views/admin/common/page-top.jsp"/>

<div class="pb-6 border-b border-white/5 mb-6">
    <a href="/admin/promotions" class="text-[11px] text-slate-400 hover:text-brand-gold transition"><i class="fa-solid fa-arrow-left mr-1"></i>Danh sách mã giảm giá</a>
    <h1 class="text-2xl sm:text-3xl font-serif font-bold text-white tracking-tight mt-1">${empty editingId ? 'Tạo Mã Giảm Giá' : 'Sửa Mã Giảm Giá'}</h1>
</div>

<c:if test="${not empty formErrors}">
    <div class="mb-5 p-3.5 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-300 text-xs">
        <ul class="list-disc pl-5 space-y-0.5"><c:forEach var="err" items="${formErrors}"><li>${err}</li></c:forEach></ul>
    </div>
</c:if>

<c:set var="formAction" value="${empty editingId ? '/admin/promotions/new' : ('/admin/promotions/' += editingId += '/edit')}"/>
<form action="${formAction}" method="post" class="grid grid-cols-1 lg:grid-cols-2 gap-6">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

    <section class="glass-panel rounded-2xl border border-white/10 p-5 flex flex-col gap-4">
        <h2 class="text-[11px] uppercase tracking-wider font-semibold text-brand-gold">Thông tin mã</h2>
        <label class="block text-xs text-slate-300">Mã giảm giá
            <input type="text" name="code" value="${form.code}" maxlength="30" ${empty editingId ? 'required' : 'readonly'} placeholder="VD: WELCOME10"
                   class="glass-input mt-1.5 w-full px-3 py-2 rounded-xl text-sm uppercase font-mono ${empty editingId ? '' : 'opacity-60 cursor-not-allowed'}"/>
            <c:if test="${not empty editingId}"><span class="text-[11px] text-slate-500">Không đổi được chuỗi mã sau khi tạo.</span></c:if>
        </label>
        <label class="block text-xs text-slate-300">Mô tả
            <input type="text" name="description" value="${form.description}" maxlength="255" placeholder="Hiển thị cho khách khi chọn mã" class="glass-input mt-1.5 w-full px-3 py-2 rounded-xl text-sm"/>
        </label>
        <div class="grid grid-cols-2 gap-4">
            <label class="block text-xs text-slate-300">Loại giảm giá
                <select name="type" id="type" class="glass-input mt-1.5 w-full px-3 py-2 rounded-xl text-sm bg-[#161a22]">
                    <c:forEach var="t" items="${types}">
                        <option value="${t}" ${form.type == t ? 'selected' : ''}>${t.label}</option>
                    </c:forEach>
                </select>
            </label>
            <label class="block text-xs text-slate-300"><span id="value-label">Mức giảm</span>
                <input type="number" name="value" value="${form.value}" min="0.01" step="any" required class="glass-input mt-1.5 w-full px-3 py-2 rounded-xl text-sm"/>
            </label>
        </div>
        <div class="grid grid-cols-2 gap-4">
            <label class="block text-xs text-slate-300">Đơn tối thiểu (đ)
                <input type="number" name="minOrderAmount" value="${form.minOrderAmount}" min="0" step="1000" class="glass-input mt-1.5 w-full px-3 py-2 rounded-xl text-sm"/>
            </label>
            <label class="block text-xs text-slate-300" id="max-field">Giảm tối đa (đ)
                <input type="number" name="maxDiscountAmount" value="${form.maxDiscountAmount}" min="1" step="1000" placeholder="Không giới hạn" class="glass-input mt-1.5 w-full px-3 py-2 rounded-xl text-sm"/>
            </label>
        </div>
    </section>

    <section class="glass-panel rounded-2xl border border-white/10 p-5 flex flex-col gap-4">
        <h2 class="text-[11px] uppercase tracking-wider font-semibold text-brand-gold">Thời hạn và phạm vi</h2>
        <div class="grid grid-cols-2 gap-4">
            <label class="block text-xs text-slate-300">Bắt đầu
                <input type="datetime-local" name="startsAt" value="${form.startsAtInput}" required class="glass-input mt-1.5 w-full px-3 py-2 rounded-xl text-sm [color-scheme:dark]"/>
            </label>
            <label class="block text-xs text-slate-300">Kết thúc
                <input type="datetime-local" name="endsAt" value="${form.endsAtInput}" required class="glass-input mt-1.5 w-full px-3 py-2 rounded-xl text-sm [color-scheme:dark]"/>
            </label>
        </div>
        <label class="block text-xs text-slate-300">Tổng số lượt dùng (số đơn)
            <input type="number" name="usageLimit" value="${form.usageLimit}" min="1" placeholder="Để trống nếu không giới hạn" class="glass-input mt-1.5 w-full px-3 py-2 rounded-xl text-sm"/>
        </label>
        <label class="block text-xs text-slate-300">Chi nhánh áp dụng
            <select name="storeId" class="glass-input mt-1.5 w-full px-3 py-2 rounded-xl text-sm bg-[#161a22]">
                <option value="">Toàn chuỗi</option>
                <c:forEach var="store" items="${stores}">
                    <option value="${store.id}" ${form.storeId == store.id ? 'selected' : ''}>${store.name}</option>
                </c:forEach>
            </select>
        </label>
        <label class="flex items-center gap-2.5 text-xs text-slate-200">
            <input type="checkbox" name="onePerCustomer" value="true" ${form.onePerCustomer ? 'checked' : ''} class="accent-yellow-500 w-4 h-4"/> Mỗi khách chỉ dùng một lần
        </label>
        <label class="flex items-center gap-2.5 text-xs text-slate-200">
            <input type="checkbox" name="active" value="true" ${form.active ? 'checked' : ''} class="accent-yellow-500 w-4 h-4"/> Kích hoạt ngay
        </label>
        <div class="flex gap-3 pt-2 mt-auto">
            <button type="submit" class="px-5 py-2.5 rounded-xl bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold text-xs hover:from-yellow-400 hover:to-amber-500 transition"><i class="fa-solid fa-floppy-disk mr-1.5"></i>${empty editingId ? 'Tạo mã' : 'Lưu thay đổi'}</button>
            <a href="/admin/promotions" class="px-5 py-2.5 rounded-xl bg-white/5 hover:bg-white/10 text-slate-300 text-xs font-semibold transition">Huỷ</a>
        </div>
    </section>
</form>

<script>
    (function () {
        var type = document.getElementById('type');
        var maxField = document.getElementById('max-field');
        var valueLabel = document.getElementById('value-label');
        function sync() {
            var percent = type.value === 'PERCENT';
            maxField.classList.toggle('hidden', !percent);
            valueLabel.textContent = percent ? 'Mức giảm (%)' : 'Số tiền giảm (đ)';
        }
        type.addEventListener('change', sync);
        sync();
    })();
</script>

<jsp:include page="/WEB-INF/views/admin/common/page-bottom.jsp"/>
