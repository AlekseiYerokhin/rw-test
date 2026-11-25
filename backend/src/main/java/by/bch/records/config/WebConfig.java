package by.bch.records.config;

import by.bch.records.security.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/** Регистрация интерсептора и SPA-fallback (ТЗ В.10). */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** Всё /api/** под защитой, кроме эндпоинта входа. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }

    /**
     * SPA-fallback: неизвестные не-API пути (/records, /login, ...) отдают index.html.
     * Благодаря этому работают F5 и прямые ссылки (критерий приёмки №2).
     * Пути /api/** в fallback НЕ уходят — для них остаётся 404 (см. ниже).
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location)
                            throws IOException {
                        Resource requested = location.createRelative(resourcePath);
                        if (resourcePath.startsWith("api/")) {
                            // API-запросы не подменяем index.html; null -> 404,
                            // а не 500 (lastModified несуществующего ресурса)
                            return null;
                        }
                        return requested.exists() && requested.isReadable()
                                ? requested
                                : new ClassPathResource("/static/index.html");
                    }
                });
    }
}
