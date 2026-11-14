package com.youguocang;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 优果仓干果电商平台 - 启动类。
 *
 * <p>三端接口通过路径前缀隔离：
 * <ul>
 *   <li>{@code /api/user/**}      用户端</li>
 *   <li>{@code /api/merchant/**}  商家端</li>
 *   <li>{@code /api/admin/**}     管理端</li>
 *   <li>{@code /api/public/**}    免登录接口（登录、注册、公告列表）</li>
 * </ul>
 */
@SpringBootApplication
@MapperScan("com.youguocang.mapper")
@EnableScheduling
public class YouGuoCangApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouGuoCangApplication.class, args);
    }
}
