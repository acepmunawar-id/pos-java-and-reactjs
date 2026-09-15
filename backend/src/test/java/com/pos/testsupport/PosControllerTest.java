package com.pos.testsupport;

import com.pos.config.SecurityConfig;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

/**
 * Composed {@code @WebMvcTest} for this project's controllers: every slice test needs the
 * same two things wired in beyond the controller itself - our custom {@link SecurityConfig}
 * (which @WebMvcTest's component scan excludes by default, since it's a plain
 * {@code @Configuration} class, not a controller/filter/converter) and an exclusion of Boot's
 * default {@code UserDetailsServiceAutoConfiguration} (which otherwise registers a second,
 * conflicting {@code UserDetailsService} bean alongside our own).
 *
 * <p>Extend {@link AbstractControllerTest} in the concrete test class to also get the
 * {@code MockMvc}/{@code ObjectMapper} fields and the {@code JwtService}/
 * {@code CustomUserDetailsService} mocks that {@link SecurityConfig} needs to wire up.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@WebMvcTest(excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import(SecurityConfig.class)
public @interface PosControllerTest {

    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] value() default {};
}
