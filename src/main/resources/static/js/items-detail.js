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
                    } else {
                        detailPickBtn.classList.remove('active');
                        if (starSpan) starSpan.textContent = '☆ 찜하기';
                    }
                } else {
                    alert(data.message || '처리 중 오류가 발생했습니다.');
                }
            } catch (error) {
                console.error('Favorite toggle error:', error);
                alert('서버 연결에 실패했습니다.');
            }
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
