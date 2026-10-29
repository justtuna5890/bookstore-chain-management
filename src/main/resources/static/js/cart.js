(function () {
    var csrfToken = (document.querySelector('meta[name="_csrf"]') || {}).content;
    var csrfHeader = (document.querySelector('meta[name="_csrf_header"]') || {}).content;

    function showToast(message, ok) {
        var toast = document.createElement('div');
        toast.textContent = message;
        toast.className = 'fixed bottom-6 right-6 z-[100] px-4 py-3 rounded-xl text-xs font-semibold shadow-2xl border backdrop-blur-md transition-opacity duration-300 '
            + (ok ? 'bg-emerald-500/20 border-emerald-500/40 text-emerald-200' : 'bg-rose-500/20 border-rose-500/40 text-rose-200');
        document.body.appendChild(toast);
        setTimeout(function () { toast.style.opacity = '0'; }, 2200);
        setTimeout(function () { toast.remove(); }, 2600);
    }

    function updateCartBadge(count) {
        document.querySelectorAll('[data-cart-count]').forEach(function (el) {
            el.textContent = count;
            el.classList.toggle('hidden', !count);
        });
    }

    document.addEventListener('click', function (event) {
        var button = event.target.closest('[data-add-to-cart]');
        if (!button) {
            return;
        }
        if (document.body.dataset.loggedIn !== 'true') {
            window.location.href = '/login';
            return;
        }

        button.disabled = true;
        var headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
        headers[csrfHeader] = csrfToken;

        fetch('/cart/add', {
            method: 'POST',
            headers: headers,
            body: 'copyId=' + encodeURIComponent(button.dataset.addToCart)
        })
            .then(function (response) {
                return response.json().then(function (data) { return { ok: response.ok, data: data }; });
            })
            .then(function (result) {
                showToast(result.data.message, result.ok);
                if (result.ok) {
                    updateCartBadge(result.data.cartCount);
                    button.textContent = 'Đã trong giỏ';
                    button.classList.add('opacity-60', 'cursor-default');
                } else {
                    button.disabled = false;
                }
            })
            .catch(function () {
                showToast('Không thể kết nối máy chủ, vui lòng thử lại.', false);
                button.disabled = false;
            });
    });
})();
