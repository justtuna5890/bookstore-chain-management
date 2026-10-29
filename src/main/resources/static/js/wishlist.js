(function () {
    function csrfToken() {
        var meta = document.querySelector('meta[name="_csrf"]');
        if (meta) {
            return meta.content;
        }
        var input = document.querySelector('input[name="_csrf"]');
        return input ? input.value : '';
    }

    function showToast(message, ok) {
        var toast = document.createElement('div');
        toast.textContent = message;
        toast.className = 'fixed bottom-6 right-6 z-[100] px-4 py-3 rounded-xl text-xs font-semibold shadow-2xl border backdrop-blur-md transition-opacity duration-300 '
            + (ok ? 'bg-emerald-500/20 border-emerald-500/40 text-emerald-200' : 'bg-rose-500/20 border-rose-500/40 text-rose-200');
        document.body.appendChild(toast);
        setTimeout(function () { toast.style.opacity = '0'; }, 2200);
        setTimeout(function () { toast.remove(); }, 2600);
    }

    function paint(bookId, wishlisted) {
        document.querySelectorAll('[data-wishlist-toggle="' + bookId + '"]').forEach(function (button) {
            button.dataset.wishlisted = wishlisted ? 'true' : 'false';
            button.setAttribute('aria-pressed', wishlisted ? 'true' : 'false');
            button.classList.toggle('text-rose-400', wishlisted);
            button.classList.toggle('text-slate-400', !wishlisted);
            var icon = button.querySelector('i');
            if (icon) {
                icon.classList.toggle('fa-solid', wishlisted);
                icon.classList.toggle('fa-regular', !wishlisted);
            }
        });
    }

    document.addEventListener('click', function (event) {
        var button = event.target.closest('[data-wishlist-toggle]');
        if (!button) {
            return;
        }
        event.preventDefault();
        event.stopPropagation();
        if (button.disabled) {
            return;
        }
        var bookId = button.dataset.wishlistToggle;
        button.disabled = true;

        fetch('/wishlist/toggle', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Accept': 'application/json' },
            body: 'bookId=' + encodeURIComponent(bookId) + '&_csrf=' + encodeURIComponent(csrfToken())
        })
            .then(function (response) {
                // Khách chưa đăng nhập bị chuyển sang trang đăng nhập (HTML)
                if (response.redirected || (response.headers.get('Content-Type') || '').indexOf('json') < 0) {
                    window.location.href = '/login';
                    return null;
                }
                return response.json().then(function (data) { return { ok: response.ok, data: data }; });
            })
            .then(function (result) {
                if (!result) {
                    return;
                }
                showToast(result.data.message, result.ok);
                if (!result.ok) {
                    return;
                }
                paint(bookId, result.data.wishlisted);
                var card = button.closest('[data-wishlist-card]');
                if (card && !result.data.wishlisted) {
                    card.remove();
                    var remaining = document.querySelectorAll('[data-wishlist-card]').length;
                    document.querySelectorAll('[data-wishlist-total]').forEach(function (el) { el.textContent = remaining; });
                    if (remaining === 0) {
                        window.location.reload();
                    }
                }
            })
            .catch(function () {
                showToast('Không thể kết nối máy chủ, vui lòng thử lại.', false);
            })
            .finally(function () {
                button.disabled = false;
            });
        // Pha capture: chặn sự kiện trước khi tới thẻ sách cha (mục "đang thịnh hành" ở trang chủ chuyển sang trang chi tiết khi bấm)
    }, true);
})();
