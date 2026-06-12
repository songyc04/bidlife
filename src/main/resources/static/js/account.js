document.addEventListener('DOMContentLoaded', () => {
    console.log("BIDLIFE Account Page Initialized.");

    const editProfileBtn = document.getElementById('editProfileBtn');
    
    if (editProfileBtn) {
        editProfileBtn.addEventListener('click', () => {
            alert('프로필 수정 기능은 준비 중입니다.');
        });
    }
});
