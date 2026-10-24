/* Chat hỗ trợ dùng chung cho khách (/chat) và nhân viên (/admin/chat). Chế độ lấy từ data-mode của #chat-app. */
(function () {
    var app = document.getElementById('chat-app');
    if (!app) {
        return;
    }
    var isStaff = app.dataset.mode === 'staff';
    var api = app.dataset.api;
    var topics = JSON.parse(app.dataset.topics || '[]');
    var MAX_LENGTH = 1000;

    function q(selector) { return app.querySelector(selector); }
    var els = {
        list: q('[data-chat-list]'),
        listEmpty: q('[data-chat-list-empty]'),
        thread: q('[data-chat-thread]'),
        title: q('[data-chat-title]'),
        subtitle: q('[data-chat-subtitle]'),
        placeholder: q('[data-chat-placeholder]'),
        form: q('[data-chat-form]'),
        input: q('[data-chat-input]'),
        send: q('[data-chat-send]'),
        counter: q('[data-chat-counter]'),
        error: q('[data-chat-error]'),
        closeBtn: q('[data-chat-close]'),
        storeSelect: q('[data-chat-store]'),
        statusFilter: q('[data-chat-filter-status]'),
        storeFilter: q('[data-chat-filter-store]'),
        closedHint: q('[data-chat-closed-hint]')
    };

    var conversations = [];
    var activeId = null;
    var rendered = {};
    var sending = false;

    function csrfToken() {
        var meta = document.querySelector('meta[name="_csrf"]');
        if (meta) {
            return meta.content;
        }
        var input = document.querySelector('input[name="_csrf"]');
        return input ? input.value : '';
    }

    function el(tag, className, text) {
        var node = document.createElement(tag);
        if (className) {
            node.className = className;
        }
        if (text !== undefined && text !== null) {
            node.textContent = text;
        }
        return node;
    }

    // Mọi nội dung hiển thị đều qua textContent: tên và tin nhắn do người dùng nhập
    function request(method, url, body) {
        var options = { method: method, headers: { 'Accept': 'application/json' } };
        if (method === 'POST') {
            options.headers['Content-Type'] = 'application/x-www-form-urlencoded';
            options.body = '_csrf=' + encodeURIComponent(csrfToken()) + (body ? '&' + body : '');
        }
        return fetch(url, options).then(function (response) {
            if (response.redirected || (response.headers.get('Content-Type') || '').indexOf('json') < 0) {
                window.location.href = '/login';
                return new Promise(function () { /* đang chuyển trang */ });
            }
            return response.json().then(function (data) { return { ok: response.ok, status: response.status, data: data }; });
        });
    }

    function showError(message) {
        els.error.textContent = message || '';
        els.error.classList.toggle('hidden', !message);
    }

    function find(id) {
        for (var i = 0; i < conversations.length; i++) {
            if (conversations[i].id === id) {
                return conversations[i];
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- danh sách

    function listTitle(c) {
        return isStaff ? c.customerName : c.storeName;
    }

    function renderList() {
        els.list.textContent = '';
        els.listEmpty.classList.toggle('hidden', conversations.length > 0);
        conversations.forEach(function (c) {
            var li = el('li');
            var button = el('button', 'w-full text-left px-4 py-3 flex gap-3 hover:bg-white/5 transition '
                + (c.id === activeId ? 'bg-brand-gold/10' : ''));
            button.type = 'button';
            button.dataset.conversationId = c.id;

            var avatar = el('span', 'w-9 h-9 shrink-0 rounded-full bg-brand-gold/15 text-brand-gold flex items-center justify-center text-xs font-bold',
                (listTitle(c) || '?').trim().charAt(0).toUpperCase());
            var body = el('span', 'min-w-0 flex-1');
            var top = el('span', 'flex items-center justify-between gap-2');
            top.appendChild(el('span', 'text-xs font-semibold text-slate-100 truncate', listTitle(c)));
            top.appendChild(el('span', 'text-[10px] text-slate-500 shrink-0', c.timeAgo));
            body.appendChild(top);
            if (isStaff && c.storeName && app.dataset.showStore === 'true') {
                body.appendChild(el('span', 'block text-[10px] text-brand-gold truncate', c.storeName));
            }
            var bottom = el('span', 'flex items-center justify-between gap-2 mt-0.5');
            bottom.appendChild(el('span', 'text-[11px] text-slate-400 truncate', c.lastMessagePreview || ''));
            if (c.unread > 0 && c.id !== activeId) {
                bottom.appendChild(el('span', 'shrink-0 min-w-[18px] h-[18px] px-1 rounded-full bg-rose-500 text-white text-[10px] font-bold flex items-center justify-center',
                    c.unread > 99 ? '99+' : c.unread));
            } else if (c.closed) {
                bottom.appendChild(el('span', 'shrink-0 px-1.5 py-0.5 rounded bg-white/10 text-slate-400 text-[9px]', 'Đã đóng'));
            }
            body.appendChild(bottom);
            button.appendChild(avatar);
            button.appendChild(body);
            li.appendChild(button);
            els.list.appendChild(li);
        });
    }

    function query() {
        var params = [];
        if (isStaff) {
            if (els.statusFilter && els.statusFilter.value) {
                params.push('status=' + encodeURIComponent(els.statusFilter.value));
            }
            if (els.storeFilter && els.storeFilter.value) {
                params.push('storeId=' + encodeURIComponent(els.storeFilter.value));
            }
        }
        return params.length ? '?' + params.join('&') : '';
    }

    function loadList() {
        return request('GET', api + '/conversations' + query()).then(function (result) {
            if (result.ok) {
                conversations = result.data.conversations;
                renderList();
            }
        });
    }

    function matchesFilters(c) {
        if (!isStaff) {
            return true;
        }
        if (els.statusFilter && els.statusFilter.value && c.status !== els.statusFilter.value) {
            return false;
        }
        return !(els.storeFilter && els.storeFilter.value && String(c.storeId) !== els.storeFilter.value);
    }

    function upsert(c) {
        var rest = conversations.filter(function (x) { return x.id !== c.id; });
        conversations = matchesFilters(c) ? [c].concat(rest) : rest;
    }

    // ------------------------------------------------------------------ luồng tin

    function bubble(m) {
        var mine = isStaff ? !m.fromCustomer : m.fromCustomer;
        var row = el('div', 'flex ' + (mine ? 'justify-end' : 'justify-start'));
        var box = el('div', 'max-w-[80%] rounded-2xl px-3.5 py-2 ' + (mine
            ? 'bg-gradient-to-r from-brand-gold to-yellow-500 text-slate-950 rounded-br-md'
            : 'bg-white/10 text-slate-100 rounded-bl-md'));
        if (!mine) {
            box.appendChild(el('div', 'text-[10px] font-semibold text-brand-gold mb-0.5', m.senderName));
        }
        box.appendChild(el('div', 'text-xs whitespace-pre-wrap break-words', m.content));
        box.appendChild(el('div', 'text-[10px] mt-1 text-right ' + (mine ? 'text-slate-800/70' : 'text-slate-500'), m.timeLabel));
        row.appendChild(box);
        return row;
    }

    function appendMessage(m) {
        if (rendered[m.id]) {
            return;
        }
        rendered[m.id] = true;
        var nearBottom = els.thread.scrollHeight - els.thread.scrollTop - els.thread.clientHeight < 120;
        els.thread.appendChild(bubble(m));
        if (nearBottom || (isStaff ? !m.fromCustomer : m.fromCustomer)) {
            els.thread.scrollTop = els.thread.scrollHeight;
        }
    }

    function updateHeader() {
        var c = find(activeId);
        els.placeholder.classList.toggle('hidden', activeId !== null || (els.storeSelect && els.storeSelect.value));
        if (!c) {
            if (!isStaff && els.storeSelect && els.storeSelect.value) {
                els.title.textContent = els.storeSelect.options[els.storeSelect.selectedIndex].text;
                els.subtitle.textContent = 'Cuộc trò chuyện mới - nhập tin nhắn đầu tiên bên dưới';
            } else {
                els.title.textContent = isStaff ? 'Chọn một cuộc trò chuyện' : 'Chat với chi nhánh';
                els.subtitle.textContent = '';
            }
            if (els.closeBtn) { els.closeBtn.classList.add('hidden'); }
            if (els.closedHint) { els.closedHint.classList.add('hidden'); }
            // Chưa mở cuộc nào: nhân viên chưa soạn được; khách soạn được khi đã chọn chi nhánh để bắt đầu
            var canCompose = !isStaff && !!(els.storeSelect && els.storeSelect.value);
            els.input.disabled = !canCompose;
            els.send.disabled = !canCompose;
            return;
        }
        els.title.textContent = listTitle(c);
        els.subtitle.textContent = isStaff ? c.storeName : (c.closed ? 'Đã đóng - nhắn lại để mở lại' : 'Nhân viên sẽ trả lời tại đây');
        if (els.closeBtn) { els.closeBtn.classList.toggle('hidden', c.closed); }
        if (els.closedHint) { els.closedHint.classList.toggle('hidden', !(isStaff && c.closed)); }
        els.input.disabled = isStaff && c.closed;
        els.send.disabled = isStaff && c.closed;
    }

    function openConversation(id) {
        activeId = id;
        els.thread.textContent = '';
        rendered = {};
        showError('');
        if (els.storeSelect && id !== null) { els.storeSelect.value = ''; }
        updateHeader();
        if (id === null) {
            renderList();
            return Promise.resolve();
        }
        var c = find(id);
        if (c) { c.unread = 0; }
        renderList();
        history.replaceState(null, '', '?conversation=' + id);
        return request('GET', api + '/conversations/' + id + '/messages').then(function (result) {
            if (id !== activeId) {
                return;
            }
            if (!result.ok) {
                showError(result.data.message || 'Không mở được cuộc trò chuyện.');
                return;
            }
            els.thread.textContent = '';
            rendered = {};
            result.data.forEach(appendMessage);
            els.thread.scrollTop = els.thread.scrollHeight;
            els.input.focus();
            refreshBell();
        });
    }

    function send() {
        var content = els.input.value.trim();
        if (!content || sending) {
            return;
        }
        var url;
        if (activeId !== null) {
            url = api + '/conversations/' + activeId + '/messages';
        } else if (!isStaff && els.storeSelect && els.storeSelect.value) {
            url = api + '/stores/' + els.storeSelect.value + '/messages';
        } else {
            showError('Hãy chọn chi nhánh bạn muốn nhắn.');
            return;
        }
        sending = true;
        els.send.disabled = true;
        showError('');
        request('POST', url, 'content=' + encodeURIComponent(content)).then(function (result) {
            if (!result.ok) {
                showError(result.data.message || 'Không gửi được tin nhắn.');
                return;
            }
            els.input.value = '';
            updateCounter();
            var message = result.data;
            if (activeId === null) {
                // Cuộc trò chuyện vừa được tạo: tải lại danh sách rồi mở nó
                return loadList().then(function () { return openConversation(message.conversationId); });
            }
            appendMessage(message);
            return loadList();
        }).catch(function () {
            showError('Không thể kết nối máy chủ, vui lòng thử lại.');
        }).finally(function () {
            sending = false;
            els.send.disabled = isStaff && find(activeId) && find(activeId).closed;
            els.input.focus();
        });
    }

    function updateCounter() {
        els.counter.textContent = els.input.value.length + '/' + MAX_LENGTH;
    }

    // ------------------------------------------------------------------ realtime

    /** Mở cuộc chat đánh dấu đã đọc cả thông báo trên chuông; báo chuông tải lại số chưa đọc. */
    function refreshBell() {
        window.dispatchEvent(new Event('notifications:refresh'));
    }

    function onEvent(event) {
        var c = event.conversation;
        var active = c.id === activeId;
        if (active) {
            c.unread = 0;
        }
        upsert(c);
        renderList();
        if (active) {
            updateHeader();
            if (event.message) {
                appendMessage(event.message);
                var fromOtherSide = isStaff ? event.message.fromCustomer : !event.message.fromCustomer;
                if (fromOtherSide) {
                    // Đang xem cuộc này: đánh dấu đã đọc phía máy chủ để số chưa đọc không tăng
                    request('GET', api + '/conversations/' + c.id + '/messages').then(refreshBell);
                }
            }
        }
    }

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
            topics.forEach(function (topic) {
                client.subscribe(topic, function (frame) {
                    try { onEvent(JSON.parse(frame.body)); } catch (e) { /* khung hỏng: bỏ qua */ }
                });
            });
            // Có thể đã lỡ tin lúc mất kết nối: đồng bộ lại
            loadList().then(function () {
                if (activeId !== null) {
                    return request('GET', api + '/conversations/' + activeId + '/messages').then(function (result) {
                        if (result.ok) { result.data.forEach(appendMessage); }
                    });
                }
            });
        }, function () {
            setTimeout(connect, retryDelay);
            retryDelay = Math.min(retryDelay * 2, 30000);
        });
    }

    // ------------------------------------------------------------------ khởi tạo

    els.list.addEventListener('click', function (event) {
        var button = event.target.closest('[data-conversation-id]');
        if (button) {
            openConversation(parseInt(button.dataset.conversationId, 10));
        }
    });
    els.form.addEventListener('submit', function (event) { event.preventDefault(); send(); });
    els.input.addEventListener('input', updateCounter);
    els.input.addEventListener('keydown', function (event) {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            send();
        }
    });
    if (els.closeBtn) {
        els.closeBtn.addEventListener('click', function () {
            if (activeId === null || !window.confirm('Đóng cuộc trò chuyện này? Khách nhắn lại sẽ tự mở lại.')) {
                return;
            }
            request('POST', api + '/conversations/' + activeId + '/close').then(function (result) {
                if (result.ok) {
                    upsert(result.data);
                    renderList();
                    updateHeader();
                } else {
                    showError(result.data.message || 'Không đóng được cuộc trò chuyện.');
                }
            });
        });
    }
    if (els.statusFilter) { els.statusFilter.addEventListener('change', loadList); }
    if (els.storeFilter) { els.storeFilter.addEventListener('change', loadList); }
    if (els.storeSelect) {
        els.storeSelect.addEventListener('change', function () {
            var storeId = parseInt(els.storeSelect.value, 10);
            var existing = conversations.filter(function (c) { return c.storeId === storeId; })[0];
            if (existing) {
                openConversation(existing.id);
                return;
            }
            activeId = null;
            els.thread.textContent = '';
            rendered = {};
            updateHeader();
            renderList();
            els.input.disabled = !els.storeSelect.value;
            els.send.disabled = !els.storeSelect.value;
            if (els.storeSelect.value) { els.input.focus(); }
        });
    }

    updateCounter();
    updateHeader();
    loadList().then(function () {
        var initial = parseInt(app.dataset.initial, 10);
        var wantedStore = parseInt(app.dataset.initialStore, 10);
        if (initial && find(initial)) {
            return openConversation(initial);
        }
        if (!isStaff && wantedStore) {
            els.storeSelect.value = String(wantedStore);
            els.storeSelect.dispatchEvent(new Event('change'));
        }
    });

    var ready = (typeof SockJS !== 'undefined' && typeof Stomp !== 'undefined')
        ? Promise.resolve()
        : loadScript('https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js')
            .then(function () { return loadScript('https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js'); });
    ready.then(connect).catch(function () { /* không tải được thư viện: chat vẫn gửi nhận được khi tải lại */ });
})();
