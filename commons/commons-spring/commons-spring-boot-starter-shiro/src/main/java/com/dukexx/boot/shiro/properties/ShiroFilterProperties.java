package com.dukexx.boot.shiro.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Created by dukexx on 2017/4/10.
 * 仅在使用自动配置默认的ShiroFilterFactoryBean生效
 */
@Data
@ConfigurationProperties(prefix = ShiroFilterProperties.PREFIX)
public class ShiroFilterProperties {
    public static final String PREFIX = "shiro.filter";
    public static final String DEFAULT = "default";

    private String loginUrl = "/login";
    private String unauthUrl = "/unauthorized";
    private String successUrl = "/success";
    //filterChain，设置过滤器映射
    private Map<String, String> filterChainMap;
    private String filterChain;
    private String chainDaoRef;
    //filters，设置自定义过滤器
    private Map<String, Class> filters;

}
