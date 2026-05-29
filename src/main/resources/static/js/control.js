// 1. id가 sel인 버튼 요소 가져오기
const selectBtn = document.getElementById('sel');

// 2. HTML을 삽입할 대상 부모 요소 가져오기 (예: id가 target인 div)
const targetContainer = document.getElementById('target');

// 3. 버튼에 클릭 이벤트 리스너 추가
selectBtn.addEventListener('click', () => {
    // 삽입하고 싶은 HTML 태그 문자열
    const newHtml = `
        <div class="added-item">
            <h3>새로운 제목</h3>
            <p>버튼을 눌러 추가된 본문 내용입니다.</p>
        </div>
    `;
    
    // targetContainer의 안쪽 맨 뒤(beforeend)에 HTML 삽입
    targetContainer.insertAdjacentHTML('beforeend', newHtml);
});