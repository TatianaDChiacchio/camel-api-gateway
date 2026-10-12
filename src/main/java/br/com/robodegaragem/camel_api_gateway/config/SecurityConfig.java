
package br.com.robodegaragem.camel_api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // USER e ADMIN podem consultar pedidos
                        .requestMatchers(HttpMethod.GET, "/camel-gateway/**")
                        .hasAnyRole("USER", "ADMIN")

                        // Somente ADMIN pode cadastrar pedidos
                        .requestMatchers(HttpMethod.POST, "/camel-gateway/**")
                        .hasRole("ADMIN")

                        // Somente ADMIN pode atualizar pedidos
                        .requestMatchers(HttpMethod.PUT, "/camel-gateway/**")
                        .hasRole("ADMIN")

                        // Somente ADMIN pode excluir pedidos
                        .requestMatchers(HttpMethod.DELETE, "/camel-gateway/**")
                        .hasRole("ADMIN")

                        // Outros métodos do Gateway exigem ADMIN
                        .requestMatchers("/camel-gateway/**")
                        .hasRole("ADMIN")

                        .anyRequest().permitAll()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        // Identifica o campo que contém os perfis no JWT
        authoritiesConverter.setAuthoritiesClaimName("roles");

        // Converte USER em ROLE_USER e ADMIN em ROLE_ADMIN
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        return converter;
    }
}


// Um JWT possui três partes: HEADER.PAYLOAD.SIGNATURE

// Por exemplo, nosso token terá informações semelhantes a estas:
// {
//  "sub": "usuario-teste",
//  "roles": ["USER"],
//  "iss": "camel-gateway-local",
//  "exp": 1790000000
//}

// sub	 -> Identifica o usuário
// roles ->	Informa as permissões
// iss	 -> Identifica quem emitiu o token
// exp	 -> Define quando o token expira

// Para nossa aula, utilizaremos RS256, com duas chaves:
// - Chave privada: utilizada para assinar o JWT.
// - Chave pública: utilizada pelo Gateway para verificar a assinatura.
// Essa separação é importante porque, em um sistema real, o Gateway não precisa conhecer a chave privada do servidor de autenticação.

// Nossa implementação terá três componentes:
// camel-api-gateway
//│
//├── config
//│   └── SecurityConfig.java
//│
//├── security
//│   └── JwtConfig.java
//│
//└── resources
//    └── keys
//        └── public.pem

// Criaremos também um pequeno gerador local de tokens, separado da aplicação, para produzir JWTs de teste com os perfis USER e ADMIN.
// A chave privada ficará fora do Gateway.

// GerarJwt.java
//     |
//     | Assina o JWT com private.pem
//     v
//Token JWT
//     |
//     | Authorization: Bearer <token>
//     v
//API Gateway (porta 8081)
//     |
//     | Spring Security verifica com public.pem
//     |
//     +-- Assinatura inválida → HTTP 401
//     |
//     +-- Token expirado → HTTP 401
//     |
//     +-- Token válido → Apache Camel
//                           |
//                           v
//                     Backend (8080)