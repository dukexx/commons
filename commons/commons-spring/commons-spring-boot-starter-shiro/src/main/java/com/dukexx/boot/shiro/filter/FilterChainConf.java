package com.dukexx.boot.shiro.filter;

import lombok.Data;

import java.io.Serializable;

/**
 * @author dukexx
 * @date 2017/4/17
 * @since 1.0.0
 */
@Data
public class FilterChainConf implements Serializable{
    private static final long serialVersionUID = 1L;

    private String url;
    private String filterName;
    private String filterConfString;
}
