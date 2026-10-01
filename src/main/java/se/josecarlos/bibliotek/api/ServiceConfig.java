package se.josecarlos.bibliotek.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.josecarlos.bibliotek.business.BookService;
import se.josecarlos.bibliotek.business.LoanService;
import se.josecarlos.bibliotek.business.MemberService;

@Configuration
public class ServiceConfig {

    @Bean
    public BookService bookService() {
        return new BookService();
    }

    @Bean
    public MemberService memberService() {
        return new MemberService();
    }

    @Bean
    public LoanService loanService() {
        return new LoanService();
    }
}
