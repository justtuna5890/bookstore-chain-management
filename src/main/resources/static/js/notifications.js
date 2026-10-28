(function () {
    var toggles = document.querySelectorAll('[data-notification-toggle]');
    var panel = document.querySelector('[data-notification-panel]');
    var pageList = document.querySelector('[data-notification-page-list]');
    if (!toggles.length && !panel && !pageList) {
        return;
    }
    var MAX_ITEMS = 8;
    var ICONS = { ORDER: 'fa-receipt', WISHLIST: 'fa-heart', BUYBACK: 'fa-hand-holding-dollar', REVIEW: 'fa-star', CHAT: 'fa-comments' };

    function csrfToken() {
        var meta = document.querySelector('meta[name="_csrf"]');
        if (meta) {
            return meta.content;
        }
        var input = document.querySelector('input[name="_csrf"]');
        return input ? input.value : '';
    }

    function post(url) {
        return fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Accept': 'application/json' },
            body: '_csrf=' + encodeURIComponent(csrfToken()),
            keepalive: true
        }).then(function (response) {
            return response.ok || response.status === 404 ? response.json() : null;
        });
    }

    function setBadge(count) {
        var n = Math.max(0, count || 0);
        document.querySelectorAll('[data-notification-badge]').forEach(function (badge) {
            badge.textContent = n > 99 ? '99+' : n;
            badge.classList.toggle('hidden', n === 0);
        });
        document.querySelectorAll('[data-notification-unread-total]').forEach(function (el) { el.textContent = n; });
    }

    function currentCount() {
        var badge = document.querySelector('[data-notification-badge]');
        return badge ? (parseInt(badge.textContent, 10) || 0) : 0;
    }

    function safeLink(link) {
        return typeof link === 'string' && link.charAt(0) === '/' && link.charAt(1) !== '/' && link.indexOf('\\') < 0 ? link : null;
    }

    // Dựng bằng DOM + textContent, không dùng innerHTML: nội dung thông báo có thể chứa tên sách do người dùng nhập
    function buildItem(n) {
        var li = document.createElement('li');
        var row = document.createElement(safeLink(n.link) ? 'a' : 'div');
        if (safeLink(n.link)) {
            row.href = n.link;
        }
        row.className = 'flex gap-3 px-4 py-3 hover:bg-white/5 transition cursor-pointer ' + (n.read ? 'opacity-60' : 'bg-brand-gold/5');
        row.dataset.notificationItem = n.id;
        row.dataset.unread = n.read ? 'false' : 'true';

        var icon = document.createElement('span');
        icon.className = 'w-8 h-8 shrink-0 rounded-full bg-brand-gold/15 text-brand-gold flex items-center justify-center';
        var i = document.createElement('i');
        i.className = 'fa-solid ' + (ICONS[n.type] || 'fa-bell') + ' text-xs';
        icon.appendChild(i);

        var body = document.createElement('span');
        body.className = 'min-w-0 flex-1';
        var title = document.createElement('span');
        title.className = 'block text-xs font-semibold text-slate-100';
        title.textContent = n.title;
        var message = document.createElement('span');
        message.className = 'block text-[11px] text-slate-400 mt-0.5 break-words';
        message.textContent = n.message;
        var time = document.createElement('span');
        time.className = 'block text-[10px] text-slate-500 mt-1';
        time.textContent = n.timeAgo || '';
        body.appendChild(title);
        body.appendChild(message);
        body.appendChild(time);

        row.appendChild(icon);
        row.appendChild(body);
        if (!n.read) {
            var dot = document.createElement('span');
            dot.className = 'w-2 h-2 mt-1.5 shrink-0 rounded-full bg-brand-gold';
            dot.setAttribute('data-notification-dot', '');
            row.appendChild(dot);
        }
        li.appendChild(row);
        return li;
    }

    function render(items) {
        document.querySelectorAll('[data-notification-list]').forEach(function (list) {
            list.textContent = '';
            if (!items.length) {
                var empty = document.createElement('li');
                empty.className = 'px-4 py-6 text-center text-xs text-slate-500';
                empty.textContent = 'Bạn chưa có thông báo nào.';
                list.appendChild(empty);
            } else {
                items.slice(0, MAX_ITEMS).forEach(function (n) { list.appendChild(buildItem(n)); });
            }
        });
    }

    function load() {
        fetch('/notifications/recent', { headers: { 'Accept': 'application/json' } })
            .then(function (response) {
                if (response.redirected || (response.headers.get('Content-Type') || '').indexOf('json') < 0) {
                    return null;
                }
                return response.json();
            })
            .then(function (data) {
                if (data) {
                    render(data.items);
                    setBadge(data.unread);
                }
            })
            .catch(function () {});
    }

    function showToast(n) {
        var toast = document.createElement('div');
        toast.className = 'fixed bottom-6 right-6 z-[100] max-w-xs px-4 py-3 rounded-xl text-xs shadow-2xl border backdrop-blur-md transition-opacity duration-300 bg-[#12151c]/95 border-brand-gold/40 text-slate-100';
        var title = document.createElement('strong');
        title.className = 'block text-brand-gold';
        title.textContent = n.title;
        var message = document.createElement('span');
        message.className = 'block text-slate-300 mt-0.5';
        message.textContent = n.message;
        toast.appendChild(title);
        toast.appendChild(message);
        document.body.appendChild(toast);
        setTimeout(function () { toast.style.opacity = '0'; }, 5000);
        setTimeout(function () { toast.remove(); }, 5400);
    }

    function onPush(n) {
        document.querySelectorAll('[data-notification-list]').forEach(function (list) {
            if (list.querySelector('[data-notification-item="' + n.id + '"]')) {
                return;
            }
            var first = list.firstElementChild;
            if (first && !first.querySelector('[data-notification-item]')) {
                first.remove();
            }
            list.insertBefore(buildItem(n), list.firstChild);
            while (list.children.length > MAX_ITEMS) {
                list.removeChild(list.lastChild);
            }
        });
        setBadge(currentCount() + 1);
        showToast(n);
        var pageList = document.querySelector('[data-notification-page-list]');
        if (pageList && window.location.search.indexOf('page=') < 0) {
            setTimeout(function () { window.location.reload(); }, 1500);
        }
    }

    function markRead(row) {
        if (row.dataset.unread !== 'true') {
            return;
        }
        row.dataset.unread = 'false';
        row.classList.remove('bg-brand-gold/5');
        row.classList.add('opacity-60');
        var dot = row.querySelector('[data-notification-dot]');
        if (dot) {
            dot.remove();
        }
        setBadge(currentCount() - 1);
        post('/notifications/' + encodeURIComponent(row.dataset.notificationItem) + '/read').then(function (data) {
            if (data && typeof data.unread === 'number') {
                setBadge(data.unread);
            }
        });
    }

    window.addEventListener('notifications:refresh', load);

    toggles.forEach(function (toggle) {
        toggle.addEventListener('click', function (event) {
            event.stopPropagation();
            if (!panel) return;
            var open = panel.classList.toggle('hidden') === false;
            toggles.forEach(function (t) { t.setAttribute('aria-expanded', open ? 'true' : 'false'); });
            if (open) {
                load();
            }
        });
    });

    document.addEventListener('click', function (event) {
        if (panel && !panel.classList.contains('hidden')) {
            var clickedToggle = false;
            toggles.forEach(function (t) {
                if (t.contains(event.target)) clickedToggle = true;
            });
            if (!panel.contains(event.target) && !clickedToggle) {
                panel.classList.add('hidden');
                toggles.forEach(function (t) { t.setAttribute('aria-expanded', 'false'); });
            }
        }
        var row = event.target.closest('[data-notification-item]');
        if (row) {
            markRead(row);
        }
        if (event.target.closest('[data-notification-read-all]')) {
            event.preventDefault();
            post('/notifications/read-all').then(function () {
                setBadge(0);
                document.querySelectorAll('[data-notification-item]').forEach(function (el) {
                    el.dataset.unread = 'false';
                    el.classList.remove('bg-brand-gold/5');
                    el.classList.add('opacity-60');
                });
                document.querySelectorAll('[data-notification-dot]').forEach(function (el) { el.remove(); });
            });
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && panel) {
            panel.classList.add('hidden');
            toggles.forEach(function (t) { t.setAttribute('aria-expanded', 'false'); });
        }
    });

    function loadScript(src) {
        return new Promise(function (resolve, reject) {
            var script = document.createElement('script');
            script.src = src;
            script.onload = resolve;
            script.onerror = reject;
            document.head.appendChild(script);
        });
    }

    var retryDelay = 2000;
    function connect() {
        var client = Stomp.over(new SockJS('/ws'));
        client.debug = null;
        client.connect({}, function () {
            retryDelay = 2000;
            client.subscribe('/user/queue/notifications', function (frame) {
                try {
                    onPush(JSON.parse(frame.body));
                } catch (e) {}
            });
        }, function () {
            setTimeout(connect, retryDelay);
            retryDelay = Math.min(retryDelay * 2, 30000);
        });
    }

    var ready = (typeof SockJS !== 'undefined' && typeof Stomp !== 'undefined')
        ? Promise.resolve()
        : loadScript('https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js')
            .then(function () { return loadScript('https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js'); });

    load();
    ready.then(connect).catch(function () {});
})();
