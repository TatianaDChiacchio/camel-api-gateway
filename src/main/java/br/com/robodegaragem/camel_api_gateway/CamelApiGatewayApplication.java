package br.com.robodegaragem.camel_api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class CamelApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(CamelApiGatewayApplication.class, args);
	}

}
