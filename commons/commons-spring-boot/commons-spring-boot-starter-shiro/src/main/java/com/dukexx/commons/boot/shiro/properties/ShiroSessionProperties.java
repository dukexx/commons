package com.dukexx.commons.boot.shiro.properties;

import lombok.Data;
import org.apache.shiro.web.servlet.ShiroHttpSession;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by dukexx on 2017/4/11.
 * session相关配置。
 */
@Data
@ConfigurationProperties(prefix = ShiroSessionProperties.PREFIX)
public class ShiroSessionProperties {
    public static final String PREFIX = "shiro.session";
    //30munute
    private static final long DEFAULT_TIMEOUT = 1800000;
    private static final String DEFAULT_COOKIE = ShiroHttpSession.DEFAULT_SESSION_ID_NAME;
    private static final String DEFAULT_SESSION_PREFIX = "SHIRO_SESSION_KEY_";
    /*
    指定session类型，只支持default,http,cache,redis(默认redis实现，如果禁用了ShiroDefaultSupAutoConfigration，则失效)
    默认default
     */
    private SessionType type = SessionType.DEFAULT;
    //指定保存sessionId的cookie名称
    private String cookie = DEFAULT_COOKIE;
    //指定redis保存时的Session前缀
    private String prefix = DEFAULT_SESSION_PREFIX;
    //指定全局超时时间，默认30分
    private long timeout = DEFAULT_TIMEOUT;
    //是否设置cookie为httponly
    private boolean httpOnly = true;

    /**
     * session类型
     */
    public enum SessionType {
        DEFAULT, CACHE, HTTP, REDIS
    }
}
