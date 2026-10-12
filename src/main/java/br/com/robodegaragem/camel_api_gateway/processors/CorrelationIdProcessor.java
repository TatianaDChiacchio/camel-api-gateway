
package br.com.robodegaragem.camel_api_gateway.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CorrelationIdProcessor implements Processor {

    public static final String HEADER_NAME = "X-Correlation-ID";
    public static final String PROPERTY_NAME = "correlationId";

    @Override
    public void process(Exchange exchange) {

        String correlationId = exchange.getMessage()
                .getHeader(HEADER_NAME, String.class);

        // Se não existir um identificador válido, gera um novo.
        if (correlationId == null
                || correlationId.isBlank()
                || correlationId.length() > 128
                || !correlationId.matches("[a-zA-Z0-9._-]+")) {

            correlationId = UUID.randomUUID().toString();
        }

        // Guarda o identificador internamente no Exchange.
        exchange.setProperty(PROPERTY_NAME, correlationId);

        // Disponibiliza o identificador no header HTTP.
        exchange.getMessage().setHeader(HEADER_NAME, correlationId);
    }
}
