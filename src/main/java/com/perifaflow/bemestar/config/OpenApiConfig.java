package com.perifaflow.bemestar.config;

import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer addAcceptLanguageHeader() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(op -> {
                    Parameter lang = new HeaderParameter()
                        .name("Accept-Language")
                        .description("Idioma da resposta (ex.: pt-BR | en). Padrão: pt-BR")
                        .schema(new StringSchema()._default("pt-BR"));
                    op.addParametersItem(lang);

                })
        );
    }
}
