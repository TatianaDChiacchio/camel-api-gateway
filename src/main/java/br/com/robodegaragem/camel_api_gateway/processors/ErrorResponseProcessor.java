package br.com.robodegaragem.camel_api_gateway.processors;

import br.com.robodegaragem.camel_api_gateway.model.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
public class ErrorResponseProcessor {

    private final ObjectMapper objectMapper;

    public ErrorResponseProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setError(
            Exchange exchange,
            int status,
            String erro,
            String mensagem
    ) throws Exception {

        ErrorResponse response = new ErrorResponse(erro, mensagem);

        exchange.getMessage().setHeader(
                Exchange.HTTP_RESPONSE_CODE,
                status
        );

        exchange.getMessage().setHeader(
                Exchange.CONTENT_TYPE,
                "application/json"
        );

        exchange.getMessage().setBody(
                objectMapper.writeValueAsString(response)
        );
    }
}