document.addEventListener('DOMContentLoaded', () => {
    console.log("BIDLIFE Account Page Initialized.");

    const passwordForm = document.getElementById('passwordForm');
    
    if (passwordForm) {
        passwordForm.addEventListener('submit', (e) => {
            const currentPassword = document.getElementById('currentPassword').value;
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            if (!currentPassword) {
                e.preventDefault();
                alert('현재 비밀번호를 입력해주세요.');
                return;
            }

            if (!newPassword || newPassword.length < 6) {
                e.preventDefault();
                alert('새 비밀번호는 6자 이상이어야 합니다.');
                return;
            }

            if (newPassword !== confirmPassword) {
                e.preventDefault();
                alert('새 비밀번호가 일치하지 않습니다.');
                return;
            }
        });
    }
});
