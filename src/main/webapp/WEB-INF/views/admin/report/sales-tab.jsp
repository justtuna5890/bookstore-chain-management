<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
    Tab "Doanh thu bán hàng" của trang báo cáo. Dùng summary.sales (đơn hoàn tất trong kỳ, theo ngày hoàn tất) và
    salesDailyJson / salesPaymentJson / salesStoreJson do ReportController đặt vào request.
--%>
<c:choose>
    <c:when test="${summary.sales == null}">
        <div class="p-6 rounded-2xl glass-panel-subtle border border-amber-500/30 text-xs text-amber-200">
            <i class="fa-solid fa-circle-info mr-1"></i>Không hiển thị doanh thu vì khoảng thời gian dài hơn 366 ngày. Hãy chọn khoảng ngắn hơn.
        </div>
    </c:when>
    <c:otherwise>
        <c:set var="sales" value="${summary.sales}"/>

        <p class="text-[11px] text-slate-500 mb-3">Chỉ tính đơn đã hoàn tất, theo ngày hoàn tất. Doanh thu là số tiền khách thanh toán (đã trừ mã giảm giá, đã gồm phí giao hàng).</p>

        <div class="grid grid-cols-2 lg:grid-cols-6 gap-3">
            <div class="glass-panel-subtle rounded-2xl p-4 border border-emerald-500/20">
                <span class="text-[11px] text-slate-400">Doanh thu</span>
                <div class="text-lg font-bold font-serif text-white mt-1"><fmt:formatNumber value="${sales.revenue}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></div>
            </div>
            <div class="glass-panel-subtle rounded-2xl p-4 border border-white/5">
                <span class="text-[11px] text-slate-400">Đơn hoàn tất</span>
                <div class="text-lg font-bold font-serif text-white mt-1">${sales.orderCount}</div>
            </div>
            <div class="glass-panel-subtle rounded-2xl p-4 border border-white/5">
                <span class="text-[11px] text-slate-400">Cuốn đã bán</span>
                <div class="text-lg font-bold font-serif text-white mt-1">${sales.booksSold}</div>
            </div>
            <div class="glass-panel-subtle rounded-2xl p-4 border border-white/5">
                <span class="text-[11px] text-slate-400">Giá trị đơn TB</span>
                <div class="text-lg font-bold font-serif text-white mt-1"><fmt:formatNumber value="${sales.averageOrderValue}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></div>
            </div>
            <div class="glass-panel-subtle rounded-2xl p-4 border border-white/5">
                <span class="text-[11px] text-slate-400">Giảm giá đã dùng</span>
                <div class="text-lg font-bold font-serif text-amber-300 mt-1"><fmt:formatNumber value="${sales.discountTotal}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></div>
            </div>
            <div class="glass-panel-subtle rounded-2xl p-4 border border-white/5">
                <span class="text-[11px] text-slate-400">Phí giao hàng thu</span>
                <div class="text-lg font-bold font-serif text-sky-300 mt-1"><fmt:formatNumber value="${sales.shippingTotal}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></div>
            </div>
        </div>

        <c:choose>
            <c:when test="${sales.withoutOrders}">
                <div class="mt-6 p-10 rounded-2xl glass-panel-subtle text-center text-xs text-slate-500">
                    <i class="fa-solid fa-cart-shopping text-3xl text-slate-600 mb-3 block"></i>Không có đơn hàng nào hoàn tất trong kỳ báo cáo.
                </div>
            </c:when>
            <c:otherwise>
                <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-6">
                    <div class="lg:col-span-2 glass-panel-subtle rounded-2xl p-5 border border-white/5">
                        <h3 class="text-sm font-bold text-white mb-4 flex items-center gap-2"><i class="fa-solid fa-chart-column text-emerald-400"></i><span>Doanh Thu Theo Ngày</span></h3>
                        <div style="height: 16rem;"><canvas id="reportSalesDailyChart"></canvas></div>
                    </div>
                    <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5">
                        <h3 class="text-sm font-bold text-white mb-4 flex items-center gap-2"><i class="fa-solid fa-wallet text-brand-gold"></i><span>Hình Thức Thanh Toán</span></h3>
                        <div style="height: 16rem;"><canvas id="reportSalesPaymentChart"></canvas></div>
                    </div>
                </div>

                <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-6">
                    <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5">
                        <h3 class="text-sm font-bold text-white mb-3 flex items-center gap-2"><i class="fa-solid fa-trophy text-brand-gold"></i><span>Sách Bán Chạy Trong Kỳ</span></h3>
                        <div class="overflow-x-auto rounded-xl border border-white/5">
                            <table class="w-full text-left text-xs">
                                <thead class="bg-white/5 text-slate-300 font-semibold uppercase tracking-wider text-[11px]">
                                    <tr><th class="py-2.5 px-3">#</th><th class="py-2.5 px-3">Tên sách</th><th class="py-2.5 px-3 text-right">Số cuốn</th><th class="py-2.5 px-3 text-right">Doanh thu</th></tr>
                                </thead>
                                <tbody class="divide-y divide-white/5 text-slate-300">
                                    <c:forEach var="book" items="${sales.topBooks}" varStatus="st">
                                        <tr>
                                            <td class="py-2 px-3 text-slate-500">${st.index + 1}</td>
                                            <td class="py-2 px-3 text-slate-100"><c:out value="${book.title}"/></td>
                                            <td class="py-2 px-3 text-right">${book.copies}</td>
                                            <td class="py-2 px-3 text-right font-semibold text-emerald-400"><fmt:formatNumber value="${book.revenue}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <c:choose>
                        <c:when test="${not isScopedToStore}">
                            <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5">
                                <h3 class="text-sm font-bold text-white mb-4 flex items-center gap-2"><i class="fa-solid fa-store text-blue-400"></i><span>Doanh Thu Theo Chi Nhánh</span></h3>
                                <div style="height: 14rem;"><canvas id="reportSalesStoreChart"></canvas></div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5">
                                <h3 class="text-sm font-bold text-white mb-3 flex items-center gap-2"><i class="fa-solid fa-calendar-days text-blue-400"></i><span>Doanh Thu Từng Ngày</span></h3>
                                <div class="overflow-y-auto rounded-xl border border-white/5" style="max-height: 14rem;">
                                    <table class="w-full text-left text-xs">
                                        <thead class="bg-white/5 text-slate-300 font-semibold text-[11px]"><tr><th class="py-2 px-3">Ngày</th><th class="py-2 px-3 text-right">Đơn</th><th class="py-2 px-3 text-right">Doanh thu</th></tr></thead>
                                        <tbody class="divide-y divide-white/5 text-slate-300">
                                            <c:forEach var="day" items="${sales.daily}">
                                                <c:if test="${day.orders > 0}">
                                                    <tr><td class="py-1.5 px-3">${day.date}</td><td class="py-1.5 px-3 text-right">${day.orders}</td><td class="py-1.5 px-3 text-right"><fmt:formatNumber value="${day.revenue}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></td></tr>
                                                </c:if>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
                <script src="/js/sales-charts.js"></script>
                <script>
                SalesCharts.render({
                    daily: ${salesDailyJson}, payment: ${salesPaymentJson}, stores: ${salesStoreJson},
                    dailyCanvas: 'reportSalesDailyChart', paymentCanvas: 'reportSalesPaymentChart', storeCanvas: 'reportSalesStoreChart'
                });
                </script>
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>
