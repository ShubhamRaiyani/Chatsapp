package com.shubham.chatsapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "com.shubham.chatsapp.repository")
@SpringBootApplication
public class ChatsappApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatsappApplication.class, args);
	}

}
