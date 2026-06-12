document.addEventListener('DOMContentLoaded', () => {
    console.log("BIDLIFE Items New Page Initialized.");

    const titleInput = document.getElementById('title');
    const titleCount = document.getElementById('titleCount');
    const descInput = document.getElementById('description');
    const descCount = document.getElementById('descCount');
    const imageUploadArea = document.getElementById('imageUploadArea');
    const imageInput = document.getElementById('imageInput');
    const imagePreview = document.getElementById('imagePreview');
    const auctionForm = document.querySelector('.auction-form');

    if (titleInput && titleCount) {
        titleInput.addEventListener('input', () => {
            titleCount.textContent = titleInput.value.length;
        });
    }

    if (descInput && descCount) {
        descInput.addEventListener('input', () => {
            descCount.textContent = descInput.value.length;
        });
    }

    let uploadedFiles = [];

    if (imageUploadArea && imageInput) {
        imageUploadArea.addEventListener('click', () => {
            imageInput.click();
        });

        imageUploadArea.addEventListener('dragover', (e) => {
            e.preventDefault();
            imageUploadArea.classList.add('dragover');
        });

        imageUploadArea.addEventListener('dragleave', () => {
            imageUploadArea.classList.remove('dragover');
        });

        imageUploadArea.addEventListener('drop', (e) => {
            e.preventDefault();
            imageUploadArea.classList.remove('dragover');
            handleFiles(e.dataTransfer.files);
        });

        imageInput.addEventListener('change', () => {
            handleFiles(imageInput.files);
        });
    }

    function handleFiles(files) {
        const remaining = 5 - uploadedFiles.length;
        if (remaining <= 0) {
            alert('최대 5장까지 업로드 가능합니다.');
            return;
        }

        const fileArray = Array.from(files).slice(0, remaining);

        fileArray.forEach(file => {
            if (!file.type.match('image/(jpeg|png)')) {
                alert('JPG, PNG 파일만 업로드 가능합니다.');
                return;
            }
            if (file.size > 10 * 1024 * 1024) {
                alert('파일당 10MB 이하만 업로드 가능합니다.');
                return;
            }

            uploadedFiles.push(file);
            const reader = new FileReader();
            reader.onload = (e) => {
                const previewItem = document.createElement('div');
                previewItem.className = 'preview-item';

                const img = document.createElement('img');
                img.src = e.target.result;

                const removeBtn = document.createElement('button');
                removeBtn.className = 'btn-remove';
                removeBtn.textContent = '×';
                removeBtn.type = 'button';
                removeBtn.addEventListener('click', () => {
                    const idx = uploadedFiles.indexOf(file);
                    if (idx > -1) uploadedFiles.splice(idx, 1);
                    previewItem.remove();
                });

                previewItem.appendChild(img);
                previewItem.appendChild(removeBtn);
                imagePreview.appendChild(previewItem);
            };
            reader.readAsDataURL(file);
        });
    }

    if (auctionForm) {
        auctionForm.addEventListener('submit', (e) => {
            const startPrice = document.getElementById('startPrice').value;
            const buyNowPrice = document.getElementById('buyNowPrice').value;
            const bidUnit = document.getElementById('bidUnit').value;
            const startTime = document.getElementById('startTime').value;
            const endTime = document.getElementById('endTime').value;

            if (!startPrice || parseInt(startPrice) < 1000) {
                e.preventDefault();
                alert('시작가는 1,000원 이상이어야 합니다.');
                return;
            }

            if (buyNowPrice && parseInt(buyNowPrice) > 0 && parseInt(buyNowPrice) <= parseInt(startPrice)) {
                e.preventDefault();
                alert('즉시 구매가는 시작가보다 높아야 합니다.');
                return;
            }

            if (!bidUnit || parseInt(bidUnit) < 1000) {
                e.preventDefault();
                alert('최소 입찰 단위는 1,000원 이상이어야 합니다.');
                return;
            }

            if (!startTime || !endTime) {
                e.preventDefault();
                alert('경매 시작 시간과 종료 시간을 모두 입력해주세요.');
                return;
            }

            if (new Date(endTime) <= new Date(startTime)) {
                e.preventDefault();
                alert('경매 종료 시간은 시작 시간 이후여야 합니다.');
                return;
            }
        });
    }
});
