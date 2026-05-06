package com.example.task_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TaskSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskSystemApplication.class, args);
	}

}
