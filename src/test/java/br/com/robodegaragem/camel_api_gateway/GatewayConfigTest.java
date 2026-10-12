package br.com.robodegaragem.camel_api_gateway;

import br.com.robodegaragem.camel_api_gateway.config.GatewayConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GatewayConfigTest {

    @Autowired
    private GatewayConfig gatewayConfig;

    @Test
    void deveCarregarConfiguracaoDePedidos() {

        var config = gatewayConfig.getServiceConfigByName("pedidos");

        assertNotNull(config);
        assertEquals("http://localhost:8080", config.getBaseUrl());
    }

    @Test
    void deveRetornarNullParaServicoInexistente() {

        var config = gatewayConfig.getServiceConfigByName("inexistente");

        assertNull(config);
    }
}