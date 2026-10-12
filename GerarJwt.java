
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

public class GerarJwt {

    public static void main(String[] args) throws Exception {

        // 1. Define o perfil do usuário
        // Se nenhum argumento for informado, utiliza USER.
        String perfil = args.length > 0
                ? args[0].toUpperCase(Locale.ROOT)
                : "USER";

        // Aceita somente os perfis USER e ADMIN
        if (!perfil.equals("USER") && !perfil.equals("ADMIN")) {
            throw new IllegalArgumentException(
                    "Perfil inválido. Utilize USER ou ADMIN."
            );
        }

        // 2. Carrega a chave privada RSA
        String pem = Files.readString(
                Path.of("jwt-keys/private.pem"),
                StandardCharsets.US_ASCII
        );

        // Remove cabeçalho, rodapé e espaços do arquivo PEM
        String privateKeyBase64 = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        // Decodifica a chave privada
        byte[] keyBytes = Base64.getDecoder()
                .decode(privateKeyBase64);

        PKCS8EncodedKeySpec keySpec =
                new PKCS8EncodedKeySpec(keyBytes);

        PrivateKey privateKey = KeyFactory
                .getInstance("RSA")
                .generatePrivate(keySpec);

        // 3. Cria o cabeçalho JWT
        String header = """
                {
                  "alg": "RS256",
                  "typ": "JWT"
                }
                """.trim();

        // 4. Define emissão e expiração do token
        long agora = Instant.now().getEpochSecond();

        // Token válido por 1 hora
        long expiracao = agora + 3600;

        // 5. Cria o payload JWT
        String payload = """
                {
                  "sub": "usuario-teste",
                  "iss": "camel-api-gateway-local",
                  "iat": %d,
                  "exp": %d,
                  "roles": ["%s"]
                }
                """.formatted(agora, expiracao, perfil);

        // 6. Codifica header e payload em Base64URL
        Base64.Encoder encoder =
                Base64.getUrlEncoder().withoutPadding();

        String headerCodificado = encoder.encodeToString(
                header.getBytes(StandardCharsets.UTF_8)
        );

        String payloadCodificado = encoder.encodeToString(
                payload.getBytes(StandardCharsets.UTF_8)
        );

        // 7. Monta o conteúdo que será assinado
        String conteudo = headerCodificado
                + "."
                + payloadCodificado;

        // 8. Assina o conteúdo com RSA SHA-256
        Signature assinatura =
                Signature.getInstance("SHA256withRSA");

        assinatura.initSign(privateKey);

        assinatura.update(
                conteudo.getBytes(StandardCharsets.US_ASCII)
        );

        byte[] assinaturaBytes = assinatura.sign();

        String assinaturaCodificada =
                encoder.encodeToString(assinaturaBytes);

        // 9. Monta o JWT completo
        String jwt = conteudo
                + "."
                + assinaturaCodificada;

        // 10. Exibe o resultado
        System.out.println();
        System.out.println("====================================");
        System.out.println(" JWT GERADO COM SUCESSO");
        System.out.println("====================================");
        System.out.println("Usuário: usuario-teste");
        System.out.println("Perfil: " + perfil);
        System.out.println("Validade: 1 hora");
        System.out.println();
        System.out.println("Token JWT:");
        System.out.println(jwt);
        System.out.println();
    }
}
