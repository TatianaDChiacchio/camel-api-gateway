package br.com.robodegaragem.camel_api_gateway.model;

public record ErrorResponse(
        String erro,
        String mensagem
) {
}