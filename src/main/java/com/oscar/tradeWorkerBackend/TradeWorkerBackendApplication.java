package com.oscar.tradeWorkerBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TradeWorkerBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradeWorkerBackendApplication.class, args);
	}

}
