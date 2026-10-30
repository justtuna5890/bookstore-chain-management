<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
    Đóng thẻ main, body, html mà shipper/common/page-top.jsp đã mở.
--%>
</main>
<div id="live-toast-root" class="fixed top-16 left-0 right-0 z-40 px-4 space-y-2 pointer-events-none [&>*]:pointer-events-auto max-w-xl mx-auto"></div>
<script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
<script src="/js/shipper-live.js"></script>
</body>
</html>
