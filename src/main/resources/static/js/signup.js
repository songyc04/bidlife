document.addEventListener('DOMContentLoaded', () => {
    console.log("⚡ BIDLIFE Signup: Initialized.");

    initFormValidation();
    initInputAnimations();
});

function initFormValidation() {
    const form = document.querySelector('.signup-form');
    if (!form) return;

    form.addEventListener('submit', (e) => {
        const username = document.querySelector('#username');
        const password = document.querySelector('#password');
        const passwordConfirm = document.querySelector('#password-confirm');
        const name = document.querySelector('#name');
        const email = document.querySelector('#email');

        if (!username.value.trim()) {
            e.preventDefault();
            shakeInput(username);
            username.focus();
            return;
        }

        if (!password.value.trim()) {
            e.preventDefault();
            shakeInput(password);
            password.focus();
            return;
        }

        if (password.value !== passwordConfirm.value) {
            e.preventDefault();
            shakeInput(passwordConfirm);
            passwordConfirm.style.borderColor = '#ff3333';
            showAlert('비밀번호가 일치하지 않습니다.', '🔒');
            passwordConfirm.focus();
            return;
        }

        if (!name.value.trim()) {
            e.preventDefault();
            shakeInput(name);
            name.focus();
            return;
        }

        if (!email.value.trim()) {
            e.preventDefault();
            shakeInput(email);
            email.focus();
            return;
        }

        console.log("📝 Signup attempt:", username.value);
    });
}

function initInputAnimations() {
    const inputs = document.querySelectorAll('.input-group input');

    inputs.forEach(input => {
        input.addEventListener('focus', () => {
            const label = input.parentElement.querySelector('label');
            if (label) {
                label.style.color = '#ff5a00';
            }
        });

        input.addEventListener('blur', () => {
            const label = input.parentElement.querySelector('label');
            if (label) {
                label.style.color = '#aaaaaa';
            }
        });
    });
}

function shakeInput(input) {
    input.style.animation = 'shake 0.4s';
    input.style.borderColor = '#ff3333';

    setTimeout(() => {
        input.style.animation = '';
        input.style.borderColor = '';
    }, 400);
}

const style = document.createElement('style');
style.textContent = `
    @keyframes shake {
        0%, 100% { transform: translateX(0); }
        25% { transform: translateX(-8px); }
        75% { transform: translateX(8px); }
    }
`;
document.head.appendChild(style);
