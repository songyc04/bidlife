document.addEventListener('DOMContentLoaded', () => {
    console.log("⚡ BIDLIFE Join Complete: Initialized.");

    initButtonAnimations();
});

function initButtonAnimations() {
    const buttons = document.querySelectorAll('.btn-primary, .btn-secondary');

    buttons.forEach(button => {
        button.addEventListener('mouseenter', () => {
            button.style.transform = 'translateY(-2px)';
        });

        button.addEventListener('mouseleave', () => {
            button.style.transform = 'translateY(0)';
        });
    });
}
