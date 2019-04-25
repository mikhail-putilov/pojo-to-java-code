package com.github.mikhail_putilov.pojo_to_code.config;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MustacheConfig {
    @Bean
    public Template bootstrap(Mustache.Compiler mustache, Mustache.TemplateLoader templateLoader) throws Exception {
        return mustache.compile(templateLoader.getTemplate("bootstrap"));
    }
}
