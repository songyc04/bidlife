document.addEventListener('DOMContentLoaded', () => {
    console.log("BIDLIFE Picklist Page Initialized.");

    const pickBtns = document.querySelectorAll('.btn-pick');
    const picklistGrid = document.querySelector('.picklist-grid');
    const emptyState = document.querySelector('.empty-state');

    pickBtns.forEach(btn => {
        btn.addEventListener('click', async (e) => {
            e.preventDefault();
            e.stopPropagation();

            const itemId = btn.dataset.itemId;
            if (!itemId) return;

            showConfirm('찜 목록에서 제거하시겠습니까?', async () => {
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
                        window.location.href = '/login?redirect=/account/picklist';
                        return;
                    }

                    const data = await response.json();

                    if (data.success && !data.isFavorite) {
                        const card = btn.closest('.pick-card');
                        if (card) {
                            card.style.transition = 'opacity 0.3s';
                            card.style.opacity = '0';
                            setTimeout(() => {
                                card.remove();
                                const remaining = document.querySelectorAll('.pick-card');
                                if (remaining.length === 0) {
                                    if (picklistGrid) picklistGrid.style.display = 'none';
                                    if (emptyState) emptyState.style.display = 'block';
                                }
                            }, 300);
                        }
                        showSuccess('찜 목록에서 제거되었습니다.', '⭐');
                    } else {
                        showAlert(data.message || '처리 중 오류가 발생했습니다.', '❌');
                    }
                } catch (error) {
                    console.error('Favorite remove error:', error);
                    showAlert('서버 연결에 실패했습니다.', '❌');
                }
            });
        });
    });

    function updateCountdowns() {
        const countdowns = document.querySelectorAll('.remaining-time:not(.ended-text)');
        countdowns.forEach(el => {
            const targetTime = el.dataset.time;
            if (!targetTime) return;

            const target = new Date(targetTime).getTime();
            const now = new Date().getTime();
            const diff = target - now;

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
