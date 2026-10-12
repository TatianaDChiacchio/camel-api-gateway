package br.com.robodegaragem.camel_api_gateway.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
public class GatewayRequestProcessor implements Processor {

    private static final String CONTEXT_PATH = "/camel-gateway";

    @Override
    public void process(Exchange exchange) {

        // aqui pega o service da rota que está no header e coloca na variável service
        String service = exchange.getIn()
                .getHeader("service", String.class);
        // na variável originalPath vai ser colocado o caminho da reuisição
        // exemplo: /camel-gateway/pedidos/api/pedidos/10
        String originalPath = exchange.getIn()
                .getHeader(Exchange.HTTP_PATH, String.class);

        if (service == null || service.isBlank()) {
            throw new IllegalArgumentException(
                    "Serviço não informado"
            );
        }

        // aqui junta os dois:
        // /camel-gateway/pedido
        String prefix = CONTEXT_PATH + "/" + service;

        if (originalPath == null ||
                !originalPath.startsWith(prefix + "/")) {

            throw new IllegalArgumentException(
                    "Caminho inválido: " + originalPath
            );
        }
        // String originalPath = "/camel-gateway/pedidos/api/pedidos/10";
        // String prefix = "/camel-gateway/pedidos";
        // Queremos remover /camel-gateway/pedidos e manter apenas /api/pedidos/10.
        String backendPath = originalPath.substring(prefix.length());

        // então aqui foi colocado no header a variável BackendPath com o conteúdo /api/pedidos/10 (por exemplo)
        exchange.getIn().setHeader("BackendPath", backendPath);
    }
}