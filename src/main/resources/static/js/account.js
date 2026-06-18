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

    document.querySelectorAll('.profile-icon[data-seed]').forEach(el => {
        const seed = el.getAttribute('data-seed') || '';
        const gradient = generateGradient(seed);
        el.style.background = gradient;
    });

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
                showAlert('새 비밀번호가 일치하지 않습니다.', '🔒');
                return;
            }
        });
    }
});

function generateGradient(seed) {
    const colors = [
        ['#ff5a00', '#ff0055'],
        ['#00c6ff', '#0072ff'],
        ['#f857a6', '#ff5858'],
        ['#11998e', '#38ef7d'],
        ['#fc466b', '#3f5efb'],
        ['#8360c3', '#2ebf91'],
        ['#f7971e', '#ffd200'],
        ['#00b09b', '#96c93d'],
        ['#667eea', '#764ba2'],
        ['#f093fb', '#f5576c'],
        ['#4facfe', '#00f2fe'],
        ['#43e97b', '#38f9d7'],
        ['#fa709a', '#fee140'],
        ['#30cfd0', '#330867'],
        ['#a8edea', '#fed6e3']
    ];
    
    let hash = 0;
    for (let i = 0; i < seed.length; i++) {
        hash = ((hash << 5) - hash) + seed.charCodeAt(i);
        hash = hash & hash;
    }
    
    const index = Math.abs(hash) % colors.length;
    const [color1, color2] = colors[index];
    
    return `linear-gradient(135deg, ${color1}, ${color2})`;
}
