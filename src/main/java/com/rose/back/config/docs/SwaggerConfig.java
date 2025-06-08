package com.rose.back.config.docs;

import java.util.List;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {

        return new OpenAPI()
                .info(new Info()
                        .version("v1.0.0")
                        .title("CrystalRose API")
                        .description("빛나는 크리스퇄 가드닝 API")
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:4000")
                                .description("개발용 서버")
                        )
                );
    }
    
}