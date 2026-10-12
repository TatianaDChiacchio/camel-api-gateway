
package br.com.robodegaragem.camel_api_gateway.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class AuditEndProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {

        Long inicio = exchange.getProperty("auditStartTime", Long.class);

        if (inicio == null) {
            return;
        }

        long duracaoMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicio);
        String usuario = exchange.getProperty( "usuarioAutenticado", String.class );
        String perfis = exchange.getProperty( "perfisUsuario", String.class);
        String metodo = exchange.getProperty("auditHttpMethod", String.class);
        String servico = exchange.getProperty("auditService", String.class);
        String requestId = exchange.getProperty("auditRequestId", String.class);
        String correlationId = exchange.getProperty("correlationId", String.class);
        Integer status = exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
        if (status == null) {
            status = exchange.getException() != null ? 500 : 200;
        }

        org.slf4j.LoggerFactory
                .getLogger(AuditEndProcessor.class)
                .info(
                        "AUDITORIA | CorrelationId={} | RequestId={} | Usuario={} | Perfis={} | Metodo={} | Servico={} | Status={} | Duracao={}ms",
                        correlationId,
                        requestId,
                        usuario,
                        perfis,
                        metodo,
                        servico,
                        status,
                        duracaoMs
                );
    }
}
