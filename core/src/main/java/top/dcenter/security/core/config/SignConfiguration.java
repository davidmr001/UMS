package top.dcenter.security.core.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import top.dcenter.security.core.properties.SignProperties;
import top.dcenter.security.core.api.sign.service.SignService;
import top.dcenter.security.core.sign.UserSignServiceImpl;

/**
 * @author zyw
 * @version V1.0  Created by 2020/9/15 12:23
 */
@Configuration
@AutoConfigureAfter({PropertiesConfiguration.class, SecurityConfiguration.class})
public class SignConfiguration {

    @Bean
    @ConditionalOnMissingBean(type = "top.dcenter.security.core.api.sign.service.SignService")
    public SignService signService(RedisConnectionFactory redisConnectionFactory, SignProperties signProperties) {
        return new UserSignServiceImpl(redisConnectionFactory, signProperties);
    }
}
