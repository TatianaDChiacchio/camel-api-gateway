package br.com.robodegaragem.camel_api_gateway.routes;

import br.com.robodegaragem.camel_api_gateway.processors.*;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.ThrottlerRejectedExecutionException;
import org.springframework.stereotype.Component;
import org.apache.hc.client5.http.HttpHostConnectException;
import java.net.SocketTimeoutException;

@Component
public class ProxyRoutes extends RouteBuilder {

    private final GatewayRequestProcessor gatewayRequestProcessor;
    private final ErrorResponseProcessor errorResponseProcessor;
    private final SecurityContextProcessor securityContextProcessor;
    private final AuditStartProcessor auditStartProcessor;
    private final AuditEndProcessor auditEndProcessor;
    private final CorrelationIdProcessor correlationIdProcessor;


    public ProxyRoutes(
            GatewayRequestProcessor gatewayRequestProcessor,
            ErrorResponseProcessor errorResponseProcessor,
            SecurityContextProcessor securityContextProcessor,
            AuditStartProcessor auditStartProcessor,
            AuditEndProcessor auditEndProcessor,
            CorrelationIdProcessor correlationIdProcessor) {

        this.gatewayRequestProcessor = gatewayRequestProcessor;
        this.errorResponseProcessor = errorResponseProcessor;
        this.securityContextProcessor = securityContextProcessor;
        this.auditStartProcessor = auditStartProcessor;
        this.auditEndProcessor = auditEndProcessor;
        this.correlationIdProcessor = correlationIdProcessor;
    }

    @Override
    public void configure() throws Exception {

        onException(ThrottlerRejectedExecutionException.class)
                .handled(true)
                .process(exchange -> {
                    errorResponseProcessor.setError(
                            exchange,
                            429,
                            "Too Many Requests",
                            "Limite de requisições excedido. Tente novamente mais tarde."
                    );

                    exchange.getMessage().setHeader("Retry-After", "1");
                });

        onException(HttpHostConnectException.class)
                .handled(true)
                .log("Backend indisponível: ${exception.message}")
                .process(exchange -> errorResponseProcessor.setError(
                        exchange, 502, "Bad Gateway", "O serviço de destino está indisponível" ))
                .to("direct:prepareResponse");

        onException(SocketTimeoutException.class)
                .handled(true)
                .log("Timeout na comunicação com o backend: ${exception.message}")
                .process(exchange -> errorResponseProcessor.setError(
                    exchange,504, "Gateway Timeout", "O serviço de destino demorou demais para responder" ))
                .to("direct:prepareResponse");

        from("direct:processRequest")
                .routeId("request-processor")
                .process(correlationIdProcessor)
                .process(securityContextProcessor)
                .process(auditStartProcessor)
                .log("Correlation ID: ${exchangeProperty.correlationId}")
                .log("Usuário autenticado: ${exchangeProperty.usuarioAutenticado}")
                .log("Perfis: ${exchangeProperty.perfisUsuario}")
                .process(gatewayRequestProcessor)
                .throttle(5)
                    .timePeriodMillis(1000)
                    .rejectExecution(true)
                .log("THROTTLER: requisição liberada às ${date:now:HH:mm:ss.SSS}")
                .log("Serviço: ${header.service}")
                .log("Caminho do backend: ${header.BackendPath}")
                .to("direct:loadServiceConfig");

        from("direct:loadServiceConfig")
                .routeId("service-config-loader")
                // é responsável por consultar a configuração do serviço solicitado e armazenar o resultado em um header chamado ServiceConfig.
                // Localiza o bean Spring chamado gatewayConfig.
                // Obtém o valor do header service.
                // Executa o método getServiceConfigByName(), passando esse valor como argumento.
                // gatewayConfig.getServiceConfigByName("pedidos");
                // // executa um método de um objeto Java (bean) e utiliza seu retorno.
                .setHeader("ServiceConfig", method( "gatewayConfig","getServiceConfigByName(${header.service})" ))
                // é devolvido e salvado em ServiceConfig baseUrl = http://localhost:8080

                .choice()
                    .when(header("ServiceConfig").isNull())
                    .process(exchange -> errorResponseProcessor.setError(
                            exchange, 404, "Service Not Found", "O serviço '" + exchange.getMessage().getHeader("service")
                                + "' não está cadastrado no Gateway" ))
                        .to("direct:prepareResponse")
                    .otherwise()
                        .log("Configuração encontrada para ${header.service}")
                        .to("direct:executeRequest")
                .end();

        from("direct:executeRequest")
                .routeId("execute-request")
                .setHeader("BackendUrl", simple( "${header.ServiceConfig.baseUrl}${header.BackendPath}" ))
                .log("Encaminhando ${header.CamelHttpMethod} para ${header.BackendUrl}")
                // Essa limpeza ajuda a evitar conflitos entre a URL de entrada e a URL de saída. É uma etapa importante na construção de um proxy HTTP com Apache Camel.
                .removeHeader(Exchange.HTTP_PATH)
                .removeHeader(Exchange.HTTP_URI)
                .removeHeader(Exchange.HTTP_URL)
                // após pelo menos quatro chamadas, o Circuit Breaker poderá abrir se a taxa de falhas atingir 50%.
                // Quando o circuito estiver aberto, novas requisições serão bloqueadas temporariamente, sem tentar acessar o backend.
                .circuitBreaker()
                    .resilience4jConfiguration()
                        .failureRateThreshold(50)
                        .minimumNumberOfCalls(4)
                        .slidingWindowSize(4)
                        .waitDurationInOpenState(10)
                        .automaticTransitionFromOpenToHalfOpenEnabled(true)
                    .end()
                    .log("CIRCUIT BREAKER: executando chamada HTTP ao backend")
                    .log("Query recebida: ${header.CamelHttpQuery}")
                    .toD("${header.BackendUrl}"
                        + "?throwExceptionOnFailure=false"
                        + "&connectTimeout=3000"
                        + "&responseTimeout=5000")
                .onFallback()
                    .log("CIRCUIT BREAKER: fallback acionado")
                    .process(exchange -> errorResponseProcessor.setError(
                        exchange, 503,"Service Unavailable", "O serviço está temporariamente indisponível" ))
                .end()

                .log("Circuito aberto: ${exchangeProperty.CamelResponseShortCircuited}")
                .log("Falha na execução: ${exchangeProperty.CamelResponseFailed}")
                .log("Resposta do fallback: ${exchangeProperty.CamelResponseFromFallback}")
                .to("direct:prepareResponse");

        from("direct:prepareResponse")
                .routeId("prepare-response")
                .removeHeaders("*",
                        Exchange.HTTP_RESPONSE_CODE,
                        Exchange.CONTENT_TYPE)
                .setHeader("X-Correlation-ID", exchangeProperty("correlationId"))
                .log("Resposta preparada: HTTP ${header.CamelHttpResponseCode}")
                .log("Correlation ID: ${header.X-Correlation-ID}");
    }
}