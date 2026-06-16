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
