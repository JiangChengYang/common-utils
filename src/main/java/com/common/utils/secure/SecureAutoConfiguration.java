package com.common.utils.secure;


import com.common.utils.secure.filter.AbstractHandlerMethodInterceptor;
import com.common.utils.secure.filter.SecureHandlerMethodInterceptorAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collection;

@RefreshScope
@Configuration
@ConditionalOnWebApplication
//@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties({SecureProperties.class})
public class SecureAutoConfiguration implements WebMvcConfigurer {
    private final SecureProperties secureProperties;
    private final ObjectProvider<Collection<AbstractHandlerMethodInterceptor>> customizedHandlerMethodInterceptors;

    public SecureAutoConfiguration(SecureProperties secureProperties,
                                   StringRedisTemplate redisTemplate,
                                   ObjectProvider<AuthorizingService> authorizingServiceProvider,
                                   ObjectProvider<Collection<AbstractHandlerMethodInterceptor>> customizedHandlerMethodInterceptors,
                                   ObjectMapper objectMapper) {
        SecureManager.redisTemplate = redisTemplate;
        SecureManager.secureProperties = secureProperties;
        SecureManager.authorizingServiceProvider = authorizingServiceProvider;
        SecureManager.objectMapper = objectMapper;
        AES.secureProperties = secureProperties;
        AccessToken.secureProperties = secureProperties;
        AccessToken.objectMapper = objectMapper;
        this.secureProperties = secureProperties;
        this.customizedHandlerMethodInterceptors = customizedHandlerMethodInterceptors;
    }

    @Bean
    public static FilterRegistrationBean<SecureFilter> safeFilterRegistration(SecureFilter secureFilter) {
        FilterRegistrationBean<SecureFilter> registration = new FilterRegistrationBean<>(secureFilter);
        registration.setOrder(SecureFilter.DEFAULT_ORDER);
        return registration;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(
                new SecureHandlerMethodInterceptorAdapter(
                        secureProperties.isHideNotExposedHandler(),
                        customizedHandlerMethodInterceptors.getIfAvailable()
                )
        ).addPathPatterns("/**");
    }

    @Bean
    @ConditionalOnMissingBean(AuthorizingService.class)
    public DefaultAuthorizingService defaultAuthorizingService() {
        return new DefaultAuthorizingService();
    }

    @Bean
    @ConditionalOnMissingBean(SecureFilter.class)
    public SecureFilter safeFilter(SecureProperties secureProperties, ObjectMapper objectMapper) {
        return new SecureFilter(secureProperties, objectMapper);
    }
}
