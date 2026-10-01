package se.josecarlos.bibliotek.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.josecarlos.bibliotek.business.BookService;

@Configuration
public class ServiceConfig {

    @Bean
    public BookService bookService() {
        return new BookService();
    }
}
