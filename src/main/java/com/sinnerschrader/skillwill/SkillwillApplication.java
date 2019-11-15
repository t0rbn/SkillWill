package com.sinnerschrader.skillwill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@SpringBootApplication
@EnableWebSecurity
@EnableSwagger2WebMvc
public class SkillwillApplication {

  public static void main(String[] args) {
    SpringApplication.run(SkillwillApplication.class, args);
  }

}
