
package br.com.robodegaragem.camel_api_gateway.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class SecurityContextProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {

        // recupera a autenticação
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)
                || !authentication.isAuthenticated()) {
            throw new SecurityException(
                    "Não foi possível identificar o usuário autenticado"
            );
        }

        String usuario = jwtAuth.getName();

        String perfis = jwtAuth.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.joining(","));

        // Armazena no Exchange para uso nas rotas Camel
        exchange.setProperty("usuarioAutenticado", usuario);
        exchange.setProperty("perfisUsuario", perfis);
    }
}
