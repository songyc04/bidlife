document.addEventListener('DOMContentLoaded', () => {
    console.log("BIDLIFE Items Page Initialized.");

    const searchInput = document.getElementById('searchInput');
    const searchBtn = document.getElementById('searchBtn');
    const categoryFilter = document.getElementById('categoryFilter');
    const sortSelect = document.getElementById('sortSelect');
    const statusTabs = document.querySelectorAll('.status-tab');
    const cardGrid = document.getElementById('cardGrid');
    const itemCards = document.querySelectorAll('.item-card');
    const emptyState = document.getElementById('emptyState');
    const pickBtns = document.querySelectorAll('.btn-pick');
    const pageBtns = document.querySelectorAll('.page-btn');

    let currentStatus = 'all';
    let currentCategory = 'all';
    let currentSort = 'recent';
    let currentSearch = '';

    if (!cardGrid || itemCards.length === 0) return;

    function filterAndSortCards() {
        let visibleCount = 0;

        const cardsArray = Array.from(itemCards);

        cardsArray.forEach(card => {
            const cardStatus = card.dataset.status;
            const cardCategory = card.dataset.category;
            const cardTitle = card.querySelector('h3').textContent.toLowerCase();

            let showCard = true;

            if (currentStatus !== 'all' && cardStatus !== currentStatus) {
                showCard = false;
            }

            if (currentCategory !== 'all' && cardCategory !== currentCategory) {
                showCard = false;
            }

            if (currentSearch && !cardTitle.includes(currentSearch.toLowerCase())) {
                showCard = false;
            }

            if (showCard) {
                card.style.display = 'block';
                visibleCount++;
            } else {
                card.style.display = 'none';
            }
        });

        if (currentSort !== 'recent') {
            const sortedCards = cardsArray.sort((a, b) => {
                switch (currentSort) {
                    case 'price-low':
                        return parseInt(a.dataset.price) - parseInt(b.dataset.price);
                    case 'price-high':
                        return parseInt(b.dataset.price) - parseInt(a.dataset.price);
                    case 'bids':
                        return parseInt(b.dataset.bids) - parseInt(a.dataset.bids);
                    case 'ending':
                        return new Date(a.dataset.time) - new Date(b.dataset.time);
                    default:
                        return 0;
                }
            });

            sortedCards.forEach(card => {
                cardGrid.appendChild(card);
            });
        }

        if (visibleCount === 0) {
            cardGrid.style.display = 'none';
            emptyState.style.display = 'block';
        } else {
            cardGrid.style.display = 'grid';
            emptyState.style.display = 'none';
        }
    }

    searchBtn.addEventListener('click', () => {
        currentSearch = searchInput.value.trim();
        filterAndSortCards();
    });

    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            currentSearch = searchInput.value.trim();
            filterAndSortCards();
        }
    });

    searchInput.addEventListener('input', () => {
        currentSearch = searchInput.value.trim();
        filterAndSortCards();
    });

    categoryFilter.addEventListener('change', () => {
        currentCategory = categoryFilter.value;
        filterAndSortCards();
    });

    sortSelect.addEventListener('change', () => {
        currentSort = sortSelect.value;
        filterAndSortCards();
    });

    statusTabs.forEach(tab => {
        tab.addEventListener('click', () => {
            statusTabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            currentStatus = tab.dataset.status;
            filterAndSortCards();
        });
    });

    pickBtns.forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();

            if (btn.classList.contains('active')) {
                btn.classList.remove('active');
                btn.textContent = '☆';
            } else {
                btn.classList.add('active');
                btn.textContent = '⭐';
            }
        });
    });

    pageBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            pageBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    });

    function updateCountdowns() {
        const countdowns = document.querySelectorAll('.time-countdown:not(.ended)');
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
