package com.campfiredev.growtogether.member.service;

import com.campfiredev.growtogether.mail.service.EmailService;
import com.campfiredev.growtogether.skill.entity.SkillEntity;
import com.campfiredev.growtogether.skill.repository.SkillRepository;
import com.campfiredev.growtogether.member.dto.KakaoUserDto;
import com.campfiredev.growtogether.member.dto.MemberLoginDto;
import com.campfiredev.growtogether.member.dto.MemberRegisterDto;
import com.campfiredev.growtogether.member.entity.MemberEntity;
import com.campfiredev.growtogether.member.repository.MemberRepository;
import com.campfiredev.growtogether.member.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final SkillRepository skillRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final S3Service s3Service;

    private final JwtUtil jwtUtil;

    @Transactional
    public MemberEntity register(MemberRegisterDto request, MultipartFile profileImage) {
        // 이메일 인증 여부 확인
        if (!emailService.verifyCode(request.getEmail(), request.getVerificationCode())) {
            throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
        }
        // 중복 검사
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (memberRepository.existsByNickName(request.getNickName())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        if (memberRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("이미 사용 중인 전화번호입니다.");
        }

        // 프로필 이미지 업로드 후 파일 키 저장
        String profileImageKey = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            profileImageKey = s3Service.uploadFile(profileImage);
        }

        // 회원 저장
        MemberEntity user = memberRepository.save(MemberEntity.builder()
                .nickName(request.getNickName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .githubUrl(request.getGithubUrl())
                .profileImageKey(profileImageKey)
                .build());

        // 선택한 기술 스택 저장
        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            List<SkillEntity> skills = skillRepository.findAllById(request.getSkills());
            for (SkillEntity skill : skills) {
         //       skillRepository.save(new SkillEntity(user, skill));
            }
        }

        return user;
    }

    public String userLogin(MemberLoginDto memberLoginDto) {
        MemberEntity user = memberRepository.findByEmail(memberLoginDto.email())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(memberLoginDto.password(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 틀립니다.");
        }

        return jwtUtil.generateAccessToken(user.getMemberId());
    }

    // 프로필 이미지 삭제
    @Transactional
    public void deleteProfileImage(Long memberId) {
        // member 찾기
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        //  기존 프로필 이미지가 있는지 확인
        if (member.getProfileImageKey() == null) {
            throw new IllegalArgumentException("이미 프로필 이미지가 없습니다.");
        }

        // S3에서 이미지 삭제
        s3Service.deleteFile(member.getProfileImageKey());

        //DB에서 프로필 이미지 Key 제거
        member.setProfileImageKey(null);
        memberRepository.save(member);
    }

    @Transactional
    public String updateProfileImage(Long memberId, MultipartFile profileImage) {
        // 사용자 찾기
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        //  기존 프로필 이미지 삭제 (있다면)
        if (member.getProfileImageKey() != null) {
            s3Service.deleteFile(member.getProfileImageKey());
        }

        // 새로운 이미지 업로드
        String newImageKey = s3Service.uploadFile(profileImage);

        //  DB에 새로운 Key 저장
        member.setProfileImageKey(newImageKey);
        memberRepository.save(member);

        return newImageKey;
    }

    public String getProfileImageUrl(Long memberId) {
        // 사용자 찾기
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다.")); // 예외 담당자가 수정 예정

        // 프로필 이미지 존재 여부 확인
        if (member.getProfileImageKey() == null) {
            throw new IllegalArgumentException("사용자의 프로필 이미지가 없습니다.");
        }

        // S3에서 파일 URL 반환
        return s3Service.getFileUrl(member.getProfileImageKey());
    }

    public MemberEntity kakaoLogin(KakaoUserDto kakaoUserDto) {
        // 카카오 고유 ID 확인
        String kakaoId = kakaoUserDto.getId();

        // DB 조회: 우리 서비스에 가입된 사용자인지 체크
        Optional<MemberEntity> user = memberRepository.findByKakaoId(kakaoId);

        // 가입된 사용자가 있다면 로그인 처리, 없으면 신규 가입 처리
        return user.orElseGet(() -> memberRepository.save(MemberEntity.builder()
                .kakaoId(kakaoId)
                .nickName(kakaoUserDto.getProperties().getNickname())
                .build()));

    }

}
