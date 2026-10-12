
package br.com.robodegaragem.camel_api_gateway.security;

import java.security.interfaces.RSAPublicKey;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtConfig {

    // Registra o JwtDecoder no contexto Spring
    @Bean
    public JwtDecoder jwtDecoder() throws Exception {

        // Localiza a chave pública no classpath: Localiza o arquivo public.pem
        var resource = new ClassPathResource("keys/public.pem");

        // Converte o arquivo PEM para uma chave pública RSA
        RSAPublicKey publicKey;

        try (var inputStream = resource.getInputStream()) {
            publicKey = RsaKeyConverters.x509().convert(inputStream);
        }

        // Configura o decodificador JWT / Valida a assinatura e decodifica o JWT
        return NimbusJwtDecoder
                .withPublicKey(publicKey)
                // Define o algoritmo de assinatura aceito
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
    }
}
