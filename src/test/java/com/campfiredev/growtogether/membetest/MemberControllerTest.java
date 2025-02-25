package com.campfiredev.growtogether.membetest;

import com.campfiredev.growtogether.mail.service.EmailService;
import com.campfiredev.growtogether.member.controller.MemberController;
import com.campfiredev.growtogether.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = MemberController.class)
@AutoConfigureMockMvc(addFilters = false)


class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private MemberService memberService;

    @MockBean
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity()) // Spring Security 적용
                .build();
    }

    @Test
    @WithMockUser
    @DisplayName("이메일 인증 코드 검증 - 성공")
    void verifyEmail_Success() throws Exception {
        when(emailService.verifyCode(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/member/email/verify")
                        .param("email", "test@example.com")
                        .param("code", "123456")
                        .with(csrf())) // CSRF 보호 적용
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이메일 인증이 완료되었습니다."));
    }

    @Test
    @WithMockUser
    @DisplayName("이메일 인증 코드 검증 - 실패 (잘못된 코드)")
    void verifyEmail_InvalidCode() throws Exception {
        when(emailService.verifyCode(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/member/email/verify")
                        .param("email", "test@example.com")
                        .param("code", "wrongcode")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("인증 코드가 올바르지 않거나 만료되었습니다."));
    }


    @Test
    @WithMockUser
    @DisplayName("이메일 인증 코드 검증 - 실패 (코드 없음)")
    void verifyEmail_MissingCode() throws Exception {
        mockMvc.perform(post("/member/email/verify")
                        .param("email", "test@example.com") // 코드 누락
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
