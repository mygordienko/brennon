package com.mygordienko.brennon.core.common.di;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

@Configuration
public class ThymeleafCustomConfiguration {
    @Bean
    public SpringResourceTemplateResolver svgTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:/templates/svg/");
        resolver.setSuffix(".svg");
        // Use XML or XML-compatible mode to correctly handle strict SVG tags
        resolver.setTemplateMode(TemplateMode.XML); 
        resolver.setCharacterEncoding("UTF-8");
        
        // Ensure this resolver checks files first or skips safely if not found
        resolver.setCheckExistence(true); 
        
        // Lower numbers mean higher priority (Default HTML resolver defaults to Integer.MAX_VALUE)
        resolver.setOrder(1); 
        
        return resolver;
    }
}
