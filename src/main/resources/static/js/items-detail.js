document.addEventListener('DOMContentLoaded', () => {
    console.log("BIDLIFE Item Detail Page Initialized.");

    const thumbnails = document.querySelectorAll('.thumbnail');
    const mainImage = document.getElementById('mainImage');
    const pickBtn = document.querySelector('.btn-pick');
    const bidBtn = document.querySelector('.btn-bid');
    const bidAmountInput = document.getElementById('bidAmount');

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

    if (pickBtn) {
        pickBtn.addEventListener('click', () => {
            if (pickBtn.classList.contains('active')) {
                pickBtn.classList.remove('active');
                pickBtn.textContent = '☆ 찜하기';
            } else {
                pickBtn.classList.add('active');
                pickBtn.textContent = '★ 찜완료';
            }
        });
    }

    if (bidBtn && bidAmountInput) {
        bidBtn.addEventListener('click', () => {
            const amount = parseInt(bidAmountInput.value);
            if (isNaN(amount) || amount <= 0) {
                alert('올바른 입찰 금액을 입력해주세요.');
                return;
            }
            alert('입찰 금액: ' + amount.toLocaleString() + '원\n(비딩 기능은 추후 구현됩니다)');
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
