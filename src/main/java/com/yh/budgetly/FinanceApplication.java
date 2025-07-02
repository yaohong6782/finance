package com.yh.budgetly;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@SecurityScheme(
		type = SecuritySchemeType.HTTP,
		name = "bearerAuth",
		scheme = "bearer",
		bearerFormat = "JWT"
)
@EnableScheduling
public class FinanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceApplication.class, args);
	}

}
