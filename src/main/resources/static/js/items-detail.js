document.addEventListener('DOMContentLoaded', () => {
    console.log("BIDLIFE Item Detail Page Initialized.");

    const thumbnails = document.querySelectorAll('.thumbnail');
    const mainImage = document.getElementById('mainImage');

    thumbnails.forEach(thumb => {
        thumb.addEventListener('click', () => {
            const img = thumb.querySelector('img');
            if (img && mainImage) {
                mainImage.src = img.src;
                thumbnails.forEach(t => t.classList.remove('active'));
                thumb.classList.add('active');
            }
        });
    });

    const bidInput = document.getElementById('bidAmount');
    const btnIncrease = document.getElementById('btnIncrease');
    const btnDecrease = document.getElementById('btnDecrease');

    if (bidInput && btnIncrease && btnDecrease) {
        const step = parseInt(bidInput.step) || 10000;
        const min = parseInt(bidInput.min) || 0;

        btnIncrease.addEventListener('click', () => {
            const currentValue = parseInt(bidInput.value) || 0;
            bidInput.value = currentValue + step;
        });

        btnDecrease.addEventListener('click', () => {
            const currentValue = parseInt(bidInput.value) || 0;
            const newValue = currentValue - step;
            if (newValue >= min) {
                bidInput.value = newValue;
            }
        });
    }

    const detailPickBtn = document.getElementById('detailPickBtn');
    if (detailPickBtn) {
        detailPickBtn.addEventListener('click', async () => {
            const itemId = detailPickBtn.dataset.itemId;
            if (!itemId) return;

            try {
                const formData = new URLSearchParams();
                formData.append('itemId', itemId);

                const response = await fetch('/account/favorites/toggle', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: formData.toString()
                });

                if (response.status === 401) {
                    window.location.href = '/login?redirect=/items/' + itemId;
                    return;
                }

                const data = await response.json();

                if (data.success) {
                    const starSpan = detailPickBtn.querySelector('span');
                    if (data.isFavorite) {
                        detailPickBtn.classList.add('active');
                        if (starSpan) starSpan.textContent = '★ 찜완료';
                        showSuccess(data.message || '찜 목록에 추가되었습니다.', '⭐');
                    } else {
                        detailPickBtn.classList.remove('active');
                        if (starSpan) starSpan.textContent = '☆ 찜하기';
                        showAlert(data.message || '찜 목록에서 제거되었습니다.', '⭐');
                    }
                } else {
                    showAlert(data.message || '처리 중 오류가 발생했습니다.', '❌');
                }
            } catch (error) {
                console.error('Favorite toggle error:', error);
                showAlert('서버 연결에 실패했습니다.', '❌');
            }
        });
    }

    // 비딩하기/즉시 구매 확인 팝업
    const bidForm = document.querySelector('.bid-confirm-form');
    if (bidForm) {
        const bidInput = document.getElementById('bidAmount');

        bidForm.addEventListener('submit', function(e) {
            e.preventDefault();

            const submitter = e.submitter;
            let message = '';

            if (submitter && submitter.classList.contains('btn-buy-now')) {
                const formattedPrice = submitter.dataset.buyNowPrice || '0';
                message = `⚡ 즉시 구매를 진행하시겠습니까?<br><br>` +
                          `<span style="color:#ff5a00;font-weight:700;font-size:18px;">${formattedPrice}원</span>에 구매하며,<br>` +
                          `구매 후에는 취소할 수 없습니다.`;
            } else {
                const bidAmount = bidInput ? (parseInt(bidInput.value) || 0) : 0;
                const formattedAmount = bidAmount.toLocaleString('ko-KR');
                message = `💰 입찰을 진행하시겠습니까?<br><br>` +
                          `입찰 금액: <span style="color:#ff5a00;font-weight:700;font-size:18px;">${formattedAmount}원</span><br>` +
                          `입찰 후에는 취소할 수 없습니다.`;
            }

            const formToSubmit = this;
            showConfirm(message, function() {
                formToSubmit.submit();
            });
        });
    }

    function updateCountdowns() {
        const countdowns = document.querySelectorAll('.countdown');
        countdowns.forEach(el => {
            const targetTime = new Date(el.dataset.time).getTime();
            const now = new Date().getTime();
            const diff = targetTime - now;

            if (diff <= 0) {
                el.textContent = '종료';
                el.classList.add('ended');
                return;
            }

            const days = Math.floor(diff / (1000 * 60 * 60 * 24));
            const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
            const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
            const seconds = Math.floor((diff % (1000 * 60)) / 1000);

            if (days > 0) {
                el.textContent = `${days}일 ${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
            } else {
                el.textContent = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
            }
        });
    }

    updateCountdowns();
    setInterval(updateCountdowns, 1000);
});
