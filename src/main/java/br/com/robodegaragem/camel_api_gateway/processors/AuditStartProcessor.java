
package br.com.robodegaragem.camel_api_gateway.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
public class AuditStartProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {

        // Registra o instante inicial com relógio monotônico
        exchange.setProperty(
                "auditStartTime",
                System.nanoTime()
        );

        // Guarda informações da requisição
        exchange.setProperty(
                "auditHttpMethod",
                exchange.getMessage().getHeader(
                        Exchange.HTTP_METHOD, String.class
                )
        );

        exchange.setProperty(
                "auditService",
                exchange.getMessage().getHeader(
                        "service", String.class
                )
        );

        exchange.setProperty(
                "auditRequestId",
                exchange.getExchangeId()
        );
    }
}
