package br.com.robodegaragem.camel_api_gateway.routes;

import br.com.robodegaragem.camel_api_gateway.processors.AuditEndProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class RestGatewayRoutes extends RouteBuilder {

    private final AuditEndProcessor auditEndProcessor;

    public RestGatewayRoutes(AuditEndProcessor auditEndProcessor) {
        this.auditEndProcessor = auditEndProcessor;
    }


    @Override
    public void configure() throws Exception {

        // Configuração do componente REST
        restConfiguration()
                // O platform-http utiliza o servidor HTTP integrado ao Spring Boot.
                .component("platform-http")
                // O contextPath define o prefixo das URLs.
                .contextPath("/camel-gateway");

        // Endpoint de entrada do Gateway
        // O Camel extrai os parâmetros da URL e disponibiliza seus valores como headers.

        // /camel-gateway/pedidos/api/pedidos
        // /camel-gateway/{service}/api/pedidos

        // http://localhost:8080/camel-gateway/pedidos/api/pedidos
        // o Camel combina o contextPath com o caminho no rest():
        //      /camel-gateway        /pedidos/api/pedidos
        //        contextPath             Caminho REST
        //   http://localhost:8081/camel-gateway/pedidos/api/pedidos
        rest("/{service}/{*path}")
                .get().to("direct:gatewayGet")
                .post().to("direct:gatewayPost")
                .put().to("direct:gatewayPut")
                .delete().to("direct:gatewayDelete");

        from("direct:gatewayGet")
                .routeId("gateway-get")
                .to("direct:processRequest")
                .process(auditEndProcessor);

        from("direct:gatewayPost")
                .routeId("gateway-post")
                .to("direct:processRequest")
                .process(auditEndProcessor);

        from("direct:gatewayPut")
                .routeId("gateway-put")
                .to("direct:processRequest")
                .process(auditEndProcessor);

        from("direct:gatewayDelete")
                .routeId("gateway-delete")
                .to("direct:processRequest")
                .process(auditEndProcessor);

    }
}