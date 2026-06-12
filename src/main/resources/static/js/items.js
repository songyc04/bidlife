document.addEventListener('DOMContentLoaded', () => {
    console.log("BIDLIFE Items Page Initialized.");

    const filterBtns = document.querySelectorAll('.filter-btn');
    const itemCards = document.querySelectorAll('.my-item-card');
    const itemsGrid = document.querySelector('.items-grid');
    const emptyState = document.querySelector('.empty-state');

    filterBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            filterBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            const filter = btn.dataset.filter;
            let visibleCount = 0;

            itemCards.forEach(card => {
                if (filter === 'all' || card.dataset.status === filter) {
                    card.style.display = 'block';
                    visibleCount++;
                } else {
                    card.style.display = 'none';
                }
            });

            if (visibleCount === 0) {
                itemsGrid.style.display = 'none';
                emptyState.style.display = 'block';
            } else {
                itemsGrid.style.display = 'grid';
                emptyState.style.display = 'none';
            }
        });
    });
});
