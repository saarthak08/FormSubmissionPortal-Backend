package com.sg.fsp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FormSubmissionPortalApplication {

	public static void main(String[] args) {
		SpringApplication.run(FormSubmissionPortalApplication.class, args);
	}

}
