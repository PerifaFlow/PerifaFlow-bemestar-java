package com.perifaflow.bemestar.config;

import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer addAcceptLanguageHeader() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(op -> {
                    HeaderParameter lang = new HeaderParameter();
                    lang.setName("Accept-Language");
                    lang.setDescription("Idioma da resposta (ex.: pt-BR | en). Padrão: pt-BR");
                    lang.setSchema(new StringSchema()._default("pt-BR"));
                    op.addParametersItem(lang);
                })
        );
    }
}
