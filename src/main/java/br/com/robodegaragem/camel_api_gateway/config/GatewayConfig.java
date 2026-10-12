package br.com.robodegaragem.camel_api_gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

// faz com que o Spring leia as propriedades iniciadas por app.gateway e preencha nosso Map.
// É esse mecanismo que permitirá ao Gateway escolher o backend sem termos que criar uma rota Camel específica para cada serviço.
// estrutura responsável por armazenar e consultar os endereços dos serviços.
// Essa classe é responsável por organizar e disponibilizar as configurações de todos os serviços registrados.
// @ConfigurationProperties(prefix = "app.gateway") Informa ao Spring:
// Leia as propriedades que começam com app.gateway e utilize seus valores para preencher esta classe.
@Component("gatewayConfig")
@ConfigurationProperties(prefix = "app.gateway")
public class GatewayConfig {

    private Map<String, ServiceConfig> services = new HashMap<>();

    public Map<String, ServiceConfig> getServices() {
        return services;
    }

    public void setServices(Map<String, ServiceConfig> services) {
        this.services = services;
    }

    // Esse método recebe o nome de um serviço e devolve sua configuração.
    public ServiceConfig getServiceConfigByName(String serviceName) {
        return services.get(serviceName);
    }
}

//GatewayConfig
//    │
//    └── services
//          │
//          ├── "pedidos"
//          │     └── ServiceConfig
//          │           └── baseUrl = http://localhost:8081
//          │
//          └── "pokemons"
//                └── ServiceConfig
//                     └── baseUrl = https://pokeapi.co/api/v2

// Ao executar o Spring Boot:
//1. O Spring encontra a classe GatewayConfig porque ela possui @Component.
//2. Cria uma instância dela.
//3. Lê as propriedades iniciadas por app.gateway.
//4. Cria e preenche os objetos ServiceConfig.
//5. Coloca esses objetos dentro do Map da classe GatewayConfig.
//6. Deixa o bean gatewayConfig disponível para utilização pelas rotas Camel.