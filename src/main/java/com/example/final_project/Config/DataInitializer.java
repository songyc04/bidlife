package com.example.final_project.Config;

import com.example.final_project.Entity.FaqEntity;
import com.example.final_project.Entity.NoticeEntity;
import com.example.final_project.Entity.SignupEntity;
import com.example.final_project.Repository.FaqRepository;
import com.example.final_project.Repository.NoticeRepository;
import com.example.final_project.Repository.SignupRepository;
import com.example.final_project.Service.ProfileGradientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SignupRepository signupRepository;
    private final NoticeRepository noticeRepository;
    private final FaqRepository faqRepository;
    private final ProfileGradientService profileGradientService;

    @Override
    @Transactional
    public void run(String... args) {
        assignGradientsToExistingUsers();
        seedNoticesIfEmpty();
        seedFaqsIfEmpty();
    }

    private void assignGradientsToExistingUsers() {
        List<SignupEntity> users = signupRepository.findAll();
        int updated = 0;
        for (SignupEntity user : users) {
            if (user.getProfileGradient() == null || user.getProfileGradient().isEmpty()) {
                user.setProfileGradient(profileGradientService.generateGradientFromSeed(user.getNickname()));
                signupRepository.save(user);
                updated++;
            }
        }
        if (updated > 0) {
            log.info("프로필 그라데이션 할당: {}명의 사용자에게 적용됨", updated);
        }
    }

    private void seedNoticesIfEmpty() {
        if (noticeRepository.count() > 0) {
            return;
        }
        log.info("기본 공지사항 데이터 시드 시작");

        NoticeEntity n1 = new NoticeEntity();
        n1.setTitle("[중요] BIDLIFE 서비스 오픈 안내");
        n1.setContent("안녕하세요, BIDLIFE입니다.\n\n저희는 실시간 경매 플랫폼으로, 모든 회원분들이 안전하고 편리하게 경매에 참여하실 수 있도록 최선을 다하겠습니다.\n\n앞으로도 많은 관심과 사랑 부탁드립니다. 감사합니다.");
        n1.setAuthor("관리자");
        n1.setImportant(true);
        n1.setViewCount(0);
        n1.setCreatedAt(LocalDateTime.now().minusDays(7));
        noticeRepository.save(n1);

        NoticeEntity n2 = new NoticeEntity();
        n2.setTitle("안전한 거래를 위한 이용 수칙 안내");
        n2.setContent("BIDLIFE는 안전한 거래 환경을 위해 다음과 같은 이용 수칙을 운영합니다.\n\n1. 정확한 상품 정보 등록\n2. 정직한 입찰 및 거래\n3. 낙찰 후 7일 이내 결제 완료\n4. 판매자 입금 확인 후 거래 확정\n\n위 수칙을 준수하지 않을 경우 서비스 이용이 제한될 수 있습니다.");
        n2.setAuthor("관리자");
        n2.setImportant(true);
        n2.setViewCount(0);
        n2.setCreatedAt(LocalDateTime.now().minusDays(5));
        noticeRepository.save(n2);

        NoticeEntity n3 = new NoticeEntity();
        n3.setTitle("결제 시스템 업데이트 안내");
        n3.setContent("고객님들의 안전한 결제를 위해 결제 시스템이 업데이트되었습니다.\n\n- 토스페이먼츠 연동\n- 실시간 결제 상태 확인\n- 거래 확정 시 판매자 입금 알림\n\n이용에 참고 부탁드립니다.");
        n3.setAuthor("관리자");
        n3.setImportant(false);
        n3.setViewCount(0);
        n3.setCreatedAt(LocalDateTime.now().minusDays(3));
        noticeRepository.save(n3);

        NoticeEntity n4 = new NoticeEntity();
        n4.setTitle("서버 점검 안내 (6/25 02:00 - 04:00)");
        n4.setContent("보다 안정적인 서비스 제공을 위해 아래와 같이 서버 점검을 진행합니다.\n\n- 일시: 2026년 6월 25일 (목) 02:00 - 04:00 (2시간)\n- 점검 내용: DB 최적화 및 보안 패치\n- 점검 시간 동안 서비스 이용이 일시 중단됩니다.\n\n양해 부탁드립니다.");
        n4.setAuthor("관리자");
        n4.setImportant(false);
        n4.setViewCount(0);
        n4.setCreatedAt(LocalDateTime.now().minusDays(1));
        noticeRepository.save(n4);

        log.info("공지사항 {}건 시드 완료", noticeRepository.count());
    }

    private void seedFaqsIfEmpty() {
        if (faqRepository.count() > 0) {
            return;
        }
        log.info("기본 FAQ 데이터 시드 시작");

        saveFaq("회원", "회원가입은 어떻게 하나요?", "상단 우측의 '회원가입' 버튼을 클릭하신 후, 이메일 인증을 거쳐 간단한 정보 입력만으로 가입할 수 있습니다.");
        saveFaq("회원", "비밀번호를 잊어버렸어요.", "로그인 페이지의 '비밀번호 찾기'를 통해 가입한 이메일을 입력하시면, 임시 비밀번호를 발송해 드립니다. (@test.com 도메인은 팝업으로 즉시 확인 가능)");
        saveFaq("회원", "닉네임을 변경할 수 있나요?", "마이페이지 > 내 정보 > 정보 수정에서 닉네임과 프로필 이미지를 변경할 수 있습니다.");

        saveFaq("경매", "입찰은 어떻게 하나요?", "경매 상세 페이지에서 입찰 금액을 입력한 후 '비딩하기' 버튼을 클릭하면 됩니다. 최소 입찰 금액 이상부터 입찰 가능합니다.");
        saveFaq("경매", "즉시 구매는 무엇인가요?", "즉시 구매가 설정된 경매의 경우, 경매 종료 전이라도 설정된 가격으로 바로 구매할 수 있습니다.");
        saveFaq("경매", "입찰을 취소할 수 있나요?", "입찰 후에는 취소가 불가능합니다. 신중하게 입찰해 주세요.");

        saveFaq("결제", "결제는 어떻게 하나요?", "낙찰 또는 즉시 구매 후 마이페이지의 결제하기 버튼을 눌러 토스페이먼츠로 결제를 진행할 수 있습니다.");
        saveFaq("결제", "판매자 입금 확인은 언제 하나요?", "구매자가 결제를 완료하면 판매자에게 알림이 전송됩니다. 판매자는 마이페이지의 입금 확인 버튼을 눌러 거래를 확정할 수 있습니다.");
        saveFaq("결제", "거래 확정은 어떻게 하나요?", "구매자 결제와 판매자 입금 확인이 모두 완료되면 자동으로 거래가 확정됩니다.");

        saveFaq("기타", "문의사항은 어디로 연락하나요?", "상단 메뉴의 '문의하기' 페이지를 통해 1:1 문의를 남겨주시면 빠른 시일 내에 답변 드립니다.");
        saveFaq("기타", "프로필 이미지는 어떻게 변경하나요?", "마이페이지 > 내 정보 > 정보 수정 > 프로필 이미지 변경에서 새로운 이미지를 업로드할 수 있습니다.");

        log.info("FAQ {}건 시드 완료", faqRepository.count());
    }

    private void saveFaq(String category, String question, String answer) {
        FaqEntity faq = new FaqEntity();
        faq.setCategory(category);
        faq.setQuestion(question);
        faq.setAnswer(answer);
        faq.setDisplayOrder((int) faqRepository.count() + 1);
        faqRepository.save(faq);
    }
}
