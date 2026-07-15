package com.harshalkhade.signvault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class SignvaultApplication {

	public static void main(String[] args) {

		System.clearProperty("http.proxyHost");
		System.clearProperty("http.proxyPort");
		System.clearProperty("https.proxyHost");
		System.clearProperty("https.proxyPort");
		System.setProperty("java.net.useSystemProxies", "false");
		
		SpringApplication.run(SignvaultApplication.class, args);
	}

}
