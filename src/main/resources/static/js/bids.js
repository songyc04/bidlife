document.addEventListener('DOMContentLoaded', () => {
    console.log("BIDLIFE Bids Page Initialized.");

    const filterBtns = document.querySelectorAll('.filter-btn');
    const bidCards = document.querySelectorAll('.bid-card');
    const bidList = document.querySelector('.bid-list');
    const emptyState = document.querySelector('.empty-state');

    filterBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            filterBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            const filter = btn.dataset.filter;
            let visibleCount = 0;

            bidCards.forEach(card => {
                if (filter === 'all' || card.dataset.status === filter) {
                    card.style.display = 'flex';
                    visibleCount++;
                } else {
                    card.style.display = 'none';
                }
            });

            if (visibleCount === 0) {
                bidList.style.display = 'none';
                emptyState.style.display = 'block';
            } else {
                bidList.style.display = 'flex';
                emptyState.style.display = 'none';
            }
        });
    });
});
