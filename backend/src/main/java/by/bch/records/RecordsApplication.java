package by.bch.records;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Точка входа. Наследование от SpringBootServletInitializer ОБЯЗАТЕЛЬНО:
 * именно оно позволяет разворачивать приложение как WAR во внешнем Tomcat.
 */
@SpringBootApplication
public class RecordsApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(RecordsApplication.class);   // для WAR-развёртывания
    }

    public static void main(String[] args) {                // для локальной разработки (embedded Tomcat)
        SpringApplication.run(RecordsApplication.class, args);
    }
}
