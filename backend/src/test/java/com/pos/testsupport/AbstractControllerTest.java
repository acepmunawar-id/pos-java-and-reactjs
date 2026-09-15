package com.pos.testsupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pos.security.CustomUserDetailsService;
import com.pos.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base class for every {@code @PosControllerTest}: pulls in {@code MockMvc} + {@code
 * ObjectMapper} and mocks the two beans {@link com.pos.config.SecurityConfig} needs
 * (real {@link com.pos.security.JwtAuthenticationFilter} is left in place so requests with no
 * {@code Authorization} header behave exactly as they do in production - see the note on
 * {@link com.pos.auth.AuthControllerTest}).
 */
public abstract class AbstractControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected JwtService jwtService;

    @MockBean
    protected CustomUserDetailsService customUserDetailsService;
}
