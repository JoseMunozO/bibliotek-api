package se.josecarlos.bibliotek;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BibliotekApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BibliotekApiApplication.class, args);
    }
}
