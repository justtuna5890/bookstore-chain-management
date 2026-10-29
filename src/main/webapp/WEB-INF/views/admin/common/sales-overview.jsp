<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
    Khu vực "Doanh thu bán hàng online" của dashboard. Dữ liệu do DashboardController đặt vào request:
    salesOverview, salesReport (30 ngày gần nhất), salesScopeLabel, salesShowStores,
    salesDailyJson, salesPaymentJson, salesStoreJson. Chart.js đã được dashboard.jsp nạp ở phần head.
--%>
<c:if test="${not empty salesOverview}">
<section class="mt-6" id="sales-overview">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-3">
        <h2 class="text-sm font-semibold text-slate-200 flex items-center gap-2">
            <i class="fa-solid fa-cart-shopping text-emerald-400"></i>
            <span>Doanh Thu Bán Hàng Online</span>
            <span class="px-2 py-0.5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-[10px] font-medium"><c:out value="${salesScopeLabel}"/></span>
        </h2>
        <a href="/admin/orders" class="text-[11px] text-brand-gold hover:text-white transition"><i class="fa-solid fa-arrow-right mr-1"></i>Quản lý đơn hàng</a>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="glass-panel-subtle rounded-xl sm:rounded-2xl p-3 sm:p-5 border border-white/5 hover:border-emerald-500/30 transition">
            <div class="flex items-center justify-between mb-3">
                <span class="text-xs text-slate-400 font-medium">Doanh Thu Hôm Nay</span>
                <div class="w-8 h-8 rounded-lg bg-emerald-500/10 text-emerald-400 flex items-center justify-center"><i class="fa-solid fa-sack-dollar"></i></div>
            </div>
            <div class="text-xl sm:text-2xl font-bold font-serif text-white"><fmt:formatNumber value="${salesOverview.revenueToday}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></div>
            <span class="text-[11px] text-slate-500 mt-1 block">${salesOverview.ordersToday} đơn đã hoàn tất hôm nay</span>
        </div>

        <div class="glass-panel-subtle rounded-xl sm:rounded-2xl p-3 sm:p-5 border border-white/5 hover:border-emerald-500/30 transition">
            <div class="flex items-center justify-between mb-3">
                <span class="text-xs text-slate-400 font-medium">Doanh Thu Tháng Này</span>
                <div class="w-8 h-8 rounded-lg bg-brand-gold/10 text-brand-gold flex items-center justify-center"><i class="fa-solid fa-chart-line"></i></div>
            </div>
            <div class="text-xl sm:text-2xl font-bold font-serif text-white"><fmt:formatNumber value="${salesOverview.revenueMonth}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></div>
            <span class="text-[11px] mt-1 block">
                <c:choose>
                    <c:when test="${salesOverview.growthPercent == null}"><span class="text-slate-500">Chưa có cùng kỳ tháng trước để so sánh</span></c:when>
                    <c:when test="${salesOverview.growthPercent >= 0}"><span class="text-emerald-400"><i class="fa-solid fa-arrow-trend-up mr-1"></i>+<fmt:formatNumber value="${salesOverview.growthPercent}" maxFractionDigits="1"/>%</span><span class="text-slate-500"> so với cùng kỳ tháng trước</span></c:when>
                    <c:otherwise><span class="text-rose-400"><i class="fa-solid fa-arrow-trend-down mr-1"></i><fmt:formatNumber value="${salesOverview.growthPercent}" maxFractionDigits="1"/>%</span><span class="text-slate-500"> so với cùng kỳ tháng trước</span></c:otherwise>
                </c:choose>
            </span>
        </div>

        <a href="/admin/orders" class="glass-panel-subtle rounded-xl sm:rounded-2xl p-3 sm:p-5 border ${salesOverview.pendingOrders > 0 ? 'border-amber-500/40' : 'border-white/5'} hover:border-amber-500/40 transition block">
            <div class="flex items-center justify-between mb-3">
                <span class="text-xs text-slate-400 font-medium">Đơn Đang Xử Lý</span>
                <div class="w-8 h-8 rounded-lg bg-amber-500/10 text-amber-400 flex items-center justify-center"><i class="fa-solid fa-box-open"></i></div>
            </div>
            <div class="text-xl sm:text-2xl font-bold font-serif text-white">${salesOverview.openOrders}</div>
            <span class="text-[11px] mt-1 block ${salesOverview.pendingOrders > 0 ? 'text-amber-300' : 'text-slate-500'}">
                <c:choose>
                    <c:when test="${salesOverview.pendingOrders > 0}"><i class="fa-solid fa-bell mr-1"></i>${salesOverview.pendingOrders} đơn đang chờ xác nhận</c:when>
                    <c:otherwise>Không có đơn chờ xác nhận</c:otherwise>
                </c:choose>
            </span>
        </a>

        <div class="glass-panel-subtle rounded-xl sm:rounded-2xl p-3 sm:p-5 border border-white/5 hover:border-emerald-500/30 transition">
            <div class="flex items-center justify-between mb-3">
                <span class="text-xs text-slate-400 font-medium">Giá Trị Đơn Trung Bình</span>
                <div class="w-8 h-8 rounded-lg bg-sky-500/10 text-sky-400 flex items-center justify-center"><i class="fa-solid fa-receipt"></i></div>
            </div>
            <div class="text-xl sm:text-2xl font-bold font-serif text-white"><fmt:formatNumber value="${salesOverview.averageOrderValueMonth}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></div>
            <span class="text-[11px] text-slate-500 mt-1 block">Từ ${salesOverview.ordersMonth} đơn hoàn tất trong tháng</span>
        </div>
    </div>

    <!-- Số đơn theo trạng thái -->
    <div class="flex flex-wrap gap-2 mt-3">
        <c:forEach var="entry" items="${salesOverview.ordersByStatus}">
            <span class="px-2.5 py-1 rounded-full bg-white/5 border border-white/10 text-[11px] text-slate-300">
                <c:out value="${entry.key}"/>: <strong class="${entry.value > 0 ? 'text-white' : 'text-slate-500'}">${entry.value}</strong>
            </span>
        </c:forEach>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-4">
        <div class="lg:col-span-2 glass-panel-subtle rounded-xl sm:rounded-2xl p-3 sm:p-5 border border-white/5">
            <h3 class="text-sm font-semibold text-slate-200 mb-4 flex items-center gap-2">
                <i class="fa-solid fa-chart-column text-emerald-400"></i><span>Doanh Thu 30 Ngày Gần Nhất</span>
                <span class="ml-auto text-[11px] font-normal text-slate-500">
                    <fmt:formatNumber value="${salesReport.revenue}" type="currency" currencySymbol="₫" maxFractionDigits="0"/> · ${salesReport.orderCount} đơn · ${salesReport.booksSold} cuốn
                </span>
            </h3>
            <div class="h-64"><canvas id="salesDailyChart"></canvas></div>
        </div>
        <div class="glass-panel-subtle rounded-xl sm:rounded-2xl p-3 sm:p-5 border border-white/5">
            <h3 class="text-sm font-semibold text-slate-200 mb-4 flex items-center gap-2">
                <i class="fa-solid fa-wallet text-brand-gold"></i><span>Hình Thức Thanh Toán</span>
            </h3>
            <div class="h-64 flex items-center justify-center"><canvas id="salesPaymentChart"></canvas></div>
        </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-6">
        <div class="glass-panel-subtle rounded-xl sm:rounded-2xl p-3 sm:p-5 border border-white/5">
            <h3 class="text-sm font-semibold text-slate-200 mb-3 flex items-center gap-2">
                <i class="fa-solid fa-trophy text-brand-gold"></i><span>Sách Bán Chạy (30 Ngày)</span>
            </h3>
            <c:choose>
                <c:when test="${empty salesReport.topBooks}">
                    <p class="py-8 text-center text-xs text-slate-500">Chưa có đơn hoàn tất trong 30 ngày qua.</p>
                </c:when>
                <c:otherwise>
                    <ul class="divide-y divide-white/5">
                        <c:forEach var="book" items="${salesReport.topBooks}" end="4" varStatus="st">
                            <li class="py-2.5 flex items-center gap-3">
                                <span class="w-6 h-6 shrink-0 rounded-full ${st.index == 0 ? 'bg-brand-gold text-slate-950' : 'bg-white/10 text-slate-300'} text-[11px] font-bold flex items-center justify-center">${st.index + 1}</span>
                                <a href="/books/${book.bookId}" target="_blank" class="flex-1 min-w-0 text-xs text-slate-200 hover:text-brand-gold truncate transition"><c:out value="${book.title}"/></a>
                                <span class="text-[11px] text-slate-400 shrink-0">${book.copies} cuốn</span>
                                <span class="text-xs font-semibold text-emerald-400 shrink-0"><fmt:formatNumber value="${book.revenue}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></span>
                            </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </div>

        <c:choose>
            <c:when test="${salesShowStores}">
                <div class="glass-panel-subtle rounded-xl sm:rounded-2xl p-3 sm:p-5 border border-white/5">
                    <h3 class="text-sm font-semibold text-slate-200 mb-4 flex items-center gap-2">
                        <i class="fa-solid fa-store text-blue-400"></i><span>Doanh Thu Theo Chi Nhánh (30 Ngày)</span>
                    </h3>
                    <div class="h-56"><canvas id="salesStoreChart"></canvas></div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="glass-panel-subtle rounded-xl sm:rounded-2xl p-3 sm:p-5 border border-white/5">
                    <h3 class="text-sm font-semibold text-slate-200 mb-3 flex items-center gap-2">
                        <i class="fa-solid fa-circle-info text-blue-400"></i><span>Cách Tính Doanh Thu</span>
                    </h3>
                    <p class="text-xs text-slate-400 leading-relaxed">Chỉ tính các đơn đã <strong class="text-slate-200">hoàn tất</strong>, theo ngày hoàn tất. Doanh thu là số tiền khách thanh toán (đã trừ mã giảm giá, đã gồm phí giao hàng). Đơn đã huỷ hoặc đang xử lý chưa được tính.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<script src="/js/sales-charts.js"></script>
<script>
SalesCharts.render({
    daily: ${salesDailyJson}, payment: ${salesPaymentJson}, stores: ${salesStoreJson},
    dailyCanvas: 'salesDailyChart', paymentCanvas: 'salesPaymentChart', storeCanvas: 'salesStoreChart'
});
</script>
</c:if>
