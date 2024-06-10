package com.rose.back.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // @Bean
    // public OpenAPI openAPI() {
    //     Info info = new Info()
    //             .version("v1.0")
    //             .title("CristalRose API")
    //             .description("크리스퇄 API");
    //     return new OpenAPI()
    //             .info(info);
    // }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo());
    }
 
    private Info apiInfo() {
        return new Info()
                .title("CristalRose API")
                .description("빛나는 크리스퇄 가드닝 API")
                .version("1.0.0");
    }
}