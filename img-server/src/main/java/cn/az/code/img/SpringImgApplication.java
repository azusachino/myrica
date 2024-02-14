package cn.az.code.img;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring native image application
 *
 * @author az
 */
@RestController
@SpringBootApplication
public class SpringImgApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringImgApplication.class);
    }

    @RequestMapping("/")
    ResponseEntity<String> ok() {
        return ResponseEntity.ok("wow");
    }
}
