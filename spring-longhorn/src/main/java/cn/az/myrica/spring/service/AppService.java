package cn.az.myrica.spring.service;

import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author ycpang
 * @since 2022-01-28 14:24
 */
@Service
public class AppService {


    public String abc() {
        var a = "12";
        return a.concat("3");
    }
}
