document.addEventListener('DOMContentLoaded', () => {
    console.log("BIDLIFE Account Page Initialized.");

    const btnToggleEdit = document.getElementById('btnToggleEdit');
    const editSection = document.getElementById('editSection');

    if (btnToggleEdit && editSection) {
        btnToggleEdit.addEventListener('click', () => {
            editSection.classList.toggle('active');
            if (editSection.classList.contains('active')) {
                btnToggleEdit.textContent = '닫기';
                editSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
            } else {
                btnToggleEdit.textContent = '정보 수정';
            }
        });

        if (editSection.classList.contains('active')) {
            btnToggleEdit.textContent = '닫기';
        }
    }

    const profileImageInput = document.getElementById('profileImageInput');
    const btnSelectImage = document.getElementById('btnSelectImage');
    const profilePreview = document.getElementById('profilePreview');
    const btnUploadProfile = document.getElementById('btnUploadProfile');

    if (btnSelectImage && profileImageInput) {
        btnSelectImage.addEventListener('click', () => {
            profileImageInput.click();
        });

        profileImageInput.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (file && profilePreview) {
                const reader = new FileReader();
                reader.onload = (event) => {
                    profilePreview.innerHTML = `<img src="${event.target.result}" alt="미리보기" class="preview-img">`;
                    if (btnUploadProfile) {
                        btnUploadProfile.disabled = false;
                    }
                };
                reader.readAsDataURL(file);
            }
        });
    }

    const passwordForm = document.getElementById('passwordForm');

    if (passwordForm) {
        passwordForm.addEventListener('submit', (e) => {
            const currentPassword = document.getElementById('currentPassword').value;
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            if (!currentPassword) {
                e.preventDefault();
                showAlert('현재 비밀번호를 입력해주세요.', '🔒');
                return;
            }

            if (!newPassword || newPassword.length < 6) {
                e.preventDefault();
                showAlert('새 비밀번호는 6자 이상이어야 합니다.', '🔒');
                return;
            }

            if (newPassword !== confirmPassword) {
                e.preventDefault();
                showAlert('비밀번호가 일치하지 않습니다.', '🔒');
                return;
            }
        });
    }
});
