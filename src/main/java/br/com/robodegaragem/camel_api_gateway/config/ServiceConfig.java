package br.com.robodegaragem.camel_api_gateway.config;

// Essa classe representa a configuração de um único serviço.
// é um POJO (Plain Old Java Object), ou seja, uma classe Java simples usada para representar dados.
public class ServiceConfig {

    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    // O Spring Boot utiliza esses métodos para preencher as propriedades da classe durante o carregamento da configuração.
    // Por exemplo, ao encontrar:app.gateway.services.pedidos.base-url=http://localhost:8081
    // app.gateway.services.pedidos.base-url=http://localhost:8081
}