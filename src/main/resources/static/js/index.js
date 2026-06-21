document.addEventListener('DOMContentLoaded', () => {
    console.log("⚡ BIDLIFE System: Black & Orange Neon Theme Initialized.");

    // 실시간 경매 카운트다운 가동
    initCountdowns();
});

/**
 * 전역 카운트다운 타이머 구동 함수
 */
function initCountdowns() {
    const countdownElements = document.querySelectorAll('.time-countdown');

    if (countdownElements.length === 0) return;

    const timer = setInterval(() => {
        countdownElements.forEach(el => {
            const targetDateStr = el.getAttribute('data-time');
            if (!targetDateStr) return;

            const targetDate = new Date(targetDateStr).getTime();
            const now = new Date().getTime();
            const difference = targetDate - now;

            // 경매 종료 시
            if (difference <= 0) {
                el.textContent = "CLOSED";
                el.style.color = "#444444";
                
                // LIVE 배지 찾아서 완료 상태로 변경
                const card = el.closest('.item-card');
                if (card) {
                    const badge = card.querySelector('.status-badge');
                    if (badge) {
                        badge.textContent = "END";
                        badge.style.backgroundColor = "#444444";
                        badge.style.animation = "none";
                    }
                }
                return;
            }

            // 일, 시, 분, 초 포맷팅
            const days = Math.floor(difference / (1000 * 60 * 60 * 24));
            const hours = Math.floor((difference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
            const minutes = Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60));
            const seconds = Math.floor((difference % (1000 * 60)) / 1000);

            const formattedHours = String(hours).padStart(2, '0');
            const formattedMinutes = String(minutes).padStart(2, '0');
            const formattedSeconds = String(seconds).padStart(2, '0');

            if (days > 0) {
                el.textContent = `${days}일 ${formattedHours}:${formattedMinutes}:${formattedSeconds}`;
            } else {
                el.textContent = `${formattedHours}:${formattedMinutes}:${formattedSeconds}`;
            }
        });
    }, 1000);
}