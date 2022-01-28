package cn.az.myrica.spring.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

/**
 * @author ycpang
 * @since 2022-01-28 14:23
 */
@RestController
@RequestMapping("/api/v1/longhorn/app")
public class AppController {

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(Collections.emptyList());
    }
}
