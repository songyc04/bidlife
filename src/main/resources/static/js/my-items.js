document.addEventListener('DOMContentLoaded', () => {
    console.log("BIDLIFE My Items Page Initialized.");

    const filterBtns = document.querySelectorAll('.filter-btn');
    const itemsGrid = document.getElementById('itemsGrid');
    const itemCards = document.querySelectorAll('.my-item-card');

    if (!itemsGrid || itemCards.length === 0) return;

    let currentFilter = 'all';

    function filterCards() {
        let visibleCount = 0;

        itemCards.forEach(card => {
            const cardStatus = card.dataset.status;
            let showCard = true;

            if (currentFilter !== 'all' && cardStatus !== currentFilter) {
                showCard = false;
            }

            if (showCard) {
                card.style.display = 'flex';
                visibleCount++;
            } else {
                card.style.display = 'none';
            }
        });

        if (visibleCount === 0) {
            itemsGrid.style.display = 'none';
        } else {
            itemsGrid.style.display = 'grid';
        }
    }

    filterBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            filterBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            currentFilter = btn.dataset.filter;
            filterCards();
        });
    });

    function updateCountdowns() {
        const countdowns = document.querySelectorAll('.time-countdown');
        countdowns.forEach(el => {
            const targetTime = new Date(el.dataset.time).getTime();
            const now = new Date().getTime();
            const diff = targetTime - now;

            if (diff <= 0) {
                el.textContent = '종료';
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
