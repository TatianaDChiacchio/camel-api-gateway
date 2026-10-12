import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class GerarChavesRSA {

    public static void main(String[] args) throws Exception {

        // Gera um par de chaves RSA de 2048 bits
        KeyPairGenerator generator =
                KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);

        KeyPair keyPair = generator.generateKeyPair();

        // Cria o diretório das chaves
        Path diretorio = Path.of("jwt-keys");
        Files.createDirectories(diretorio);

        // Chave privada no formato PKCS#8
        String privateKey = converterParaPEM(
                "PRIVATE KEY",
                keyPair.getPrivate().getEncoded()
        );

        // Chave pública no formato X.509
        String publicKey = converterParaPEM(
                "PUBLIC KEY",
                keyPair.getPublic().getEncoded()
        );

        Files.writeString(
                diretorio.resolve("private.pem"),
                privateKey,
                StandardCharsets.US_ASCII
        );

        Files.writeString(
                diretorio.resolve("public.pem"),
                publicKey,
                StandardCharsets.US_ASCII
        );

        System.out.println("Chaves RSA geradas com sucesso!");
    }

    private static String converterParaPEM(
            String tipo, byte[] bytes) {

        String base64 = Base64.getMimeEncoder(
                64,
                new byte[]{'\n'}
        ).encodeToString(bytes);

        return "-----BEGIN " + tipo + "-----\n"
                + base64
                + "\n-----END " + tipo + "-----\n";
    }
}