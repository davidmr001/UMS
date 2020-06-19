package top.dcenter.security.core.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import top.dcenter.security.core.api.advice.SecurityControllerExceptionHandler;
import top.dcenter.security.core.api.authentication.handler.BaseAuthenticationFailureHandler;
import top.dcenter.security.core.api.authentication.handler.BaseAuthenticationSuccessHandler;
import top.dcenter.security.core.api.logout.DefaultLogoutSuccessHandler;
import top.dcenter.security.core.api.service.AbstractUserDetailsService;
import top.dcenter.security.core.api.service.CacheUserDetailsService;
import top.dcenter.security.core.auth.handler.ClientAuthenticationFailureHandler;
import top.dcenter.security.core.auth.handler.ClientAuthenticationSuccessHandler;
import top.dcenter.security.core.auth.provider.UsernamePasswordAuthenticationProvider;
import top.dcenter.security.core.properties.ClientProperties;

/**
 * security 配置
 * @author zhailiang
 * @author  zyw
 * @version V1.0  Created by 2020/5/3 19:59
 */
@Configuration
@AutoConfigureAfter({PropertiesConfiguration.class})
public class SecurityConfiguration {

    private final ClientProperties clientProperties;
    private final ObjectMapper objectMapper;

    @SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection"})
    @Autowired
    private AbstractUserDetailsService abstractUserDetailsService;
    @SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection"})
    @Autowired(required = false)
    private CacheUserDetailsService cacheUserDetailsService;

    public SecurityConfiguration(ClientProperties clientProperties, ObjectMapper objectMapper) {
        this.clientProperties = clientProperties;
        this.objectMapper = objectMapper;
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder 的实现了添加随机 salt 算法，并且能从hash后的字符串中获取 salt 进行原始密码与hash后的密码的对比
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean(type = "top.dcenter.security.core.api.authentication.handler.BaseAuthenticationSuccessHandler")
    public BaseAuthenticationSuccessHandler baseAuthenticationSuccessHandler() {
        return new ClientAuthenticationSuccessHandler(objectMapper, clientProperties);
    }

    @Bean
    @ConditionalOnMissingBean(type = "top.dcenter.security.core.api.authentication.handler.BaseAuthenticationFailureHandler")
    public BaseAuthenticationFailureHandler baseAuthenticationFailureHandler() {
        return new ClientAuthenticationFailureHandler(objectMapper, clientProperties);
    }

    @Bean
    @ConditionalOnMissingBean(type = "top.dcenter.security.core.api.advice.SecurityControllerExceptionHandler")
    public SecurityControllerExceptionHandler securityControllerExceptionHandler() {
        return new SecurityControllerExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean(type = "top.dcenter.security.core.auth.provider.UsernamePasswordAuthenticationProvider")
    public UsernamePasswordAuthenticationProvider usernamePasswordAuthenticationProvider(PasswordEncoder passwordEncoder) {
        return new UsernamePasswordAuthenticationProvider(passwordEncoder, abstractUserDetailsService);
    }

    @Bean
    @ConditionalOnMissingBean(type = "top.dcenter.security.core.api.logout.DefaultLogoutSuccessHandler")
    public DefaultLogoutSuccessHandler defaultLogoutSuccessHandler() {
        DefaultLogoutSuccessHandler defaultLogoutSuccessHandler = new DefaultLogoutSuccessHandler(clientProperties, objectMapper, cacheUserDetailsService);
        return defaultLogoutSuccessHandler;
    }

}
