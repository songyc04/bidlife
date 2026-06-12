document.addEventListener('DOMContentLoaded', () => {
    console.log("BIDLIFE Picklist Page Initialized.");

    const filterBtns = document.querySelectorAll('.filter-btn');
    const pickCards = document.querySelectorAll('.pick-card');
    const picklistGrid = document.querySelector('.picklist-grid');
    const emptyState = document.querySelector('.empty-state');
    const pickBtns = document.querySelectorAll('.btn-pick');

    filterBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            filterBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            const filter = btn.dataset.filter;
            let visibleCount = 0;

            pickCards.forEach(card => {
                if (filter === 'all' || card.dataset.status === filter) {
                    card.style.display = 'block';
                    visibleCount++;
                } else {
                    card.style.display = 'none';
                }
            });

            if (visibleCount === 0) {
                picklistGrid.style.display = 'none';
                emptyState.style.display = 'block';
            } else {
                picklistGrid.style.display = 'grid';
                emptyState.style.display = 'none';
            }
        });
    });

    pickBtns.forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const card = btn.closest('.pick-card');

            if (btn.classList.contains('active')) {
                btn.classList.remove('active');
                btn.textContent = '☆';
                if (confirm('찜 목록에서 제거하시겠습니까?')) {
                    card.style.transition = 'opacity 0.3s';
                    card.style.opacity = '0';
                    setTimeout(() => {
                        card.remove();
                        const remaining = document.querySelectorAll('.pick-card');
                        if (remaining.length === 0) {
                            picklistGrid.style.display = 'none';
                            emptyState.style.display = 'block';
                        }
                    }, 300);
                } else {
                    btn.classList.add('active');
                    btn.textContent = '⭐';
                }
            }
        });
    });
});
