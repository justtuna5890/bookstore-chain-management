// Thời gian thực cho trang shipper: nghe kênh /topic/shipper/store/{id} của chi nhánh mình.
//   DELIVERY_AVAILABLE: có đơn chờ giao -> hiện thông báo, kêu, rung, rồi làm mới danh sách
//   DELIVERY_TAKEN:     đơn đã có người nhận -> chỉ làm mới danh sách để khỏi bấm nhầm đơn đã mất
// Chỉ các trang danh sách (body[data-live-reload="true"]) tự làm mới; trang chi tiết không bị tải lại giữa chừng.
(function () {
    var body = document.body;
    var storeId = body.dataset.storeId;
    if (!storeId || typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
        return;
    }
    var autoReload = body.dataset.liveReload === 'true';
    var reloadTimer = null;
    var statusDot = document.querySelector('[data-live-status]');

    function setStatus(online) {
        if (!statusDot) {
            return;
        }
        statusDot.classList.toggle('bg-emerald-400', online);
        statusDot.classList.toggle('bg-slate-600', !online);
        statusDot.title = online ? 'Đang nhận đơn mới theo thời gian thực' : 'Mất kết nối, đang thử lại...';
    }

    function safeLink(link) {
        return typeof link === 'string' && link.charAt(0) === '/' && link.charAt(1) !== '/' && link.indexOf('\\') < 0 ? link : null;
    }

    function beep() {
        try {
            var Ctx = window.AudioContext || window.webkitAudioContext;
            if (!Ctx) {
                return;
            }
            var ctx = new Ctx();
            [880, 1175].forEach(function (freq, i) {
                var start = ctx.currentTime + i * 0.18;
                var osc = ctx.createOscillator();
                var gain = ctx.createGain();
                osc.frequency.value = freq;
                osc.connect(gain);
                gain.connect(ctx.destination);
                gain.gain.setValueAtTime(0.15, start);
                gain.gain.exponentialRampToValueAtTime(0.001, start + 0.16);
                osc.start(start);
                osc.stop(start + 0.17);
            });
        } catch (e) {
            // Trình duyệt chặn âm thanh khi chưa chạm vào trang: bỏ qua
        }
    }

    // Dựng bằng DOM + textContent, không dùng innerHTML
    function showToast(n) {
        var root = document.getElementById('live-toast-root');
        if (!root) {
            return;
        }
        var link = safeLink(n.targetUrl);
        var toast = document.createElement(link ? 'a' : 'div');
        if (link) {
            toast.href = link;
        }
        toast.className = 'block p-3.5 rounded-2xl bg-brand-gold text-slate-950 shadow-2xl';
        var title = document.createElement('p');
        title.className = 'text-sm font-bold';
        title.textContent = n.title || 'Có đơn chờ giao';
        var message = document.createElement('p');
        message.className = 'text-xs mt-0.5';
        message.textContent = n.message || '';
        toast.appendChild(title);
        toast.appendChild(message);
        root.appendChild(toast);
        setTimeout(function () { toast.remove(); }, 8000);
    }

    function scheduleReload(delayMs) {
        if (!autoReload) {
            return;
        }
        clearTimeout(reloadTimer);
        reloadTimer = setTimeout(function () { window.location.reload(); }, delayMs);
    }

    function onMessage(frame) {
        var n;
        try {
            n = JSON.parse(frame.body);
        } catch (e) {
            return;
        }
        if (n.type === 'DELIVERY_AVAILABLE') {
            showToast(n);
            beep();
            if (navigator.vibrate) {
                navigator.vibrate([200, 100, 200]);
            }
            scheduleReload(3500);
        } else if (n.type === 'DELIVERY_TAKEN') {
            scheduleReload(800);
        }
    }

    function connect() {
        var client = Stomp.over(new SockJS('/ws'));
        client.debug = null;
        client.connect({}, function () {
            setStatus(true);
            client.subscribe('/topic/shipper/store/' + storeId, onMessage);
        }, function () {
            setStatus(false);
            setTimeout(connect, 5000);
        });
    }

    setStatus(false);
    connect();
})();
