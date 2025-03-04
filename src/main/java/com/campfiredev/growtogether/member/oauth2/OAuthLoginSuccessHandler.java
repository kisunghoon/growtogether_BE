package com.campfiredev.growtogether.member.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 로그 출력
        System.out.println("로그인 성공!");
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = token.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String nickname = oAuth2User.getAttribute("nickname");
        System.out.println("이메일: " + email);
        System.out.println("닉네임: " + nickname);
        response.sendRedirect("/home");
    }


//    @Value("${jwt.redirect}")
//    private String REDIRECT_URI; // 프론트엔드로 Jwt 토큰을 리다이렉트할 URI
//
//    @Value("${jwt.access-token.expiration-time}")
//    private long ACCESS_TOKEN_EXPIRATION_TIME; // 액세스 토큰 유효기간
//
//    @Value("${jwt.refresh-token.expiration-time}")
//    private long REFRESH_TOKEN_EXPIRATION_TIME; // 리프레쉬 토큰 유효기간
//
//    private OAuth2UserInfo oAuth2UserInfo = null;
//
//    private final JwtUtil jwtUtil;
//    private final SocialProviderRepository socialProviderRepository;
//    private final MemberRepository memberRepository;
//    private final RefreshTokenRepository refreshTokenRepository;
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
//        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication; // 토큰
//        final String provider = token.getAuthorizedClientRegistrationId(); // provider 추출
//
//        if (provider.equals("kakao")) {
//            log.info("카카오 로그인 요청");
//            oAuth2UserInfo = new KakaoUserInfo(token.getPrincipal()
//                    .getAttributes());
//        }
//
//        // 정보 추출
//        String providerId = oAuth2UserInfo.getProviderId();
//
//        SocialProvider existUser = socialProviderRepository.findByProviderId(providerId);
//        SocialProvider socialProvider;
//        WebtyUser user;
//
//        if (existUser == null) {
//            // 신규 유저인 경우
//            log.info("신규 유저입니다. 등록을 진행합니다.");
//
//            socialProvider = SocialProvider.builder()
//                    .provider(oAuth2UserInfo.getProvider())
//                    .providerId(providerId)
//                    .build();
//            socialProviderRepository.save(socialProvider);
//
//            user = WebtyUser.builder()
//                    .nickname("nickname")
//                    .socialProvider(socialProvider)
//                    .build();
//            MemberRepository.save(user);
//
//            socialProvider.setUser(user);
//            socialProviderRepository.save(socialProvider);
//        } else {
//            // 기존 유저인 경우
//            log.info("기존 유저입니다.");
//            socialProvider = existUser;
//            user = existUser.getUser();
//            refreshTokenRepository.deleteBymemberId(user.getMemberId());
//        }
//
//        log.info("PROVIDER : {}", provider);
//        log.info("PROVIDER_ID : {}", providerId);
//
//        // 리프레쉬 토큰 발급 후 저장
//        String refreshToken = jwtUtil.generateRefreshToken(user.getMemberId(), REFRESH_TOKEN_EXPIRATION_TIME);
//
//        RefreshToken newRefreshToken = RefreshToken.builder()
//                .memberId(user.getMemberId())
//                .token(refreshToken)
//                .build();
//        refreshTokenRepository.save(newRefreshToken);
//
//        // 액세스 토큰 발급
//        String accessToken = jwtUtil.generateAccessToken(user.getMemberId(), ACCESS_TOKEN_EXPIRATION_TIME);
//
//        // 이름, 액세스 토큰, 리프레쉬 토큰을 쿠키에 담아 리다이렉트
//        String redirectUri = String.format(REDIRECT_URI);
//        response.addCookie(createCookie(accessToken, "accessToken"));
//        response.addCookie(createCookie(refreshToken, "refreshToken"));
//        getRedirectStrategy().sendRedirect(request, response, redirectUri);
//
//    }
//
//    private Cookie createCookie(String token, String key) {
//        Cookie cookie = new Cookie(key, token);
//        cookie.setHttpOnly(false);
//        cookie.setMaxAge((int) ACCESS_TOKEN_EXPIRATION_TIME); // 만료 시간 설정
//        cookie.setPath("/"); // 전체 경로에서 접근 가능하게 설정
//        cookie.setSecure(false);
//
//        return cookie;
//    }
//
//}
//
}
