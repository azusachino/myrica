package cn.az.myrica.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ycpang
 * @since 2022-01-28 15:00
 */
@Configuration
public class CustomConfig {

    @Bean
    public Object myBean() {
        return "123";
    }
}
