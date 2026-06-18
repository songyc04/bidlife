// 커스텀 모달 시스템
function showModal(options) {
    const {
        type = 'alert', // alert, confirm, success, welcome
        icon = '',
        title = '',
        message = '',
        onConfirm = null,
        onCancel = null,
        confirmText = '확인',
        cancelText = '취소'
    } = options;

    // 기존 모달 제거
    const existingModal = document.querySelector('.modal-overlay');
    if (existingModal) {
        existingModal.remove();
    }

    // 모달 HTML 생성
    const modalHTML = `
        <div class="modal-overlay ${type === 'welcome' ? 'modal-welcome' : ''}">
            <div class="modal-container">
                ${icon ? `<div class="modal-icon">${icon}</div>` : ''}
                ${title ? `<div class="modal-title">${title}</div>` : ''}
                ${message ? `<div class="modal-message">${message}</div>` : ''}
                <div class="modal-buttons">
                    ${type === 'confirm' ? `<button class="modal-btn modal-btn-cancel" id="modalCancelBtn">${cancelText}</button>` : ''}
                    <button class="modal-btn modal-btn-primary" id="modalConfirmBtn">${confirmText}</button>
                </div>
            </div>
        </div>
    `;

    // body에 모달 추가
    document.body.insertAdjacentHTML('beforeend', modalHTML);

    // 모달 활성화
    const modal = document.querySelector('.modal-overlay');
    setTimeout(() => modal.classList.add('active'), 10);

    // 이벤트 리스너
    const confirmBtn = document.getElementById('modalConfirmBtn');
    const cancelBtn = document.getElementById('modalCancelBtn');

    const closeModal = () => {
        modal.classList.remove('active');
        setTimeout(() => modal.remove(), 300);
    };

    confirmBtn.addEventListener('click', () => {
        closeModal();
        if (onConfirm) onConfirm();
    });

    if (cancelBtn) {
        cancelBtn.addEventListener('click', () => {
            closeModal();
            if (onCancel) onCancel();
        });
    }

    // 배경 클릭으로 닫기
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeModal();
            if (type === 'confirm' && onCancel) onCancel();
        }
    });
}

// alert 대체
function showAlert(message, icon = '⚠️') {
    showModal({
        type: 'alert',
        icon: icon,
        title: '알림',
        message: message
    });
}

// confirm 대체
function showConfirm(message, onConfirm, onCancel = null) {
    showModal({
        type: 'confirm',
        icon: '❓',
        title: '확인',
        message: message,
        onConfirm: onConfirm,
        onCancel: onCancel,
        confirmText: '확인',
        cancelText: '취소'
    });
}

// 성공 모달
function showSuccess(message, icon = '✅') {
    showModal({
        type: 'success',
        icon: icon,
        title: '성공',
        message: message
    });
}

// 환영 모달
function showWelcome(message, icon = '🎉') {
    showModal({
        type: 'welcome',
        icon: icon,
        title: '환영합니다!',
        message: message
    });
}

// 에러 모달
function showError(message, icon = '❌') {
    showModal({
        type: 'alert',
        icon: icon,
        title: '오류',
        message: message
    });
}
