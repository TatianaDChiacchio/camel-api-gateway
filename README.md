Camel API Gateway
API Gateway desenvolvido com Java, Spring Boot e Apache Camel, como projeto prático de estudo de integração de sistemas, segurança e padrões de resiliência.
O gateway recebe requisições HTTP, valida a autenticação e as permissões do usuário, encaminha as chamadas ao serviço de pedidos e padroniza o tratamento de falhas. Também registra informações de auditoria e propaga um identificador de correlação entre os serviços.
Arquitetura
```text
Cliente / Postman
       |
       | HTTP + Bearer JWT
       v
Camel API Gateway (:8081)
       |
       +-- Spring Security: validação JWT e perfis
       +-- Apache Camel: REST DSL e roteamento
       +-- Correlation ID e auditoria
       +-- Throttler e Circuit Breaker
       |
       | HTTP + X-Correlation-ID
       v
Backend de Pedidos (:8080)
       |
       v
   PostgreSQL
```
O gateway e o backend são aplicações separadas. O backend utilizado nos exercícios é o projeto `camel-aula1`.
Tecnologias
Java 21
Spring Boot
Apache Camel (REST DSL, `platform-http` e roteamento dinâmico)
Spring Security / OAuth2 Resource Server
JWT assinado com RSA (RS256)
Resilience4j Circuit Breaker integrado ao Camel
Maven
Funcionalidades
Recurso	Implementação
Proxy HTTP	Encaminhamento de GET, POST, PUT e DELETE ao backend de pedidos
Autenticação	Validação de JWT RS256 pelo Spring Security
Autorização	`USER` e `ADMIN` podem consultar; operações de alteração exigem `ADMIN`
Limitação de requisições	Throttler configurado para 5 requisições por segundo, com rejeição HTTP 429
Resiliência	Circuit Breaker com fallback HTTP 503
Tratamento de falhas	Respostas para erros de comunicação, incluindo HTTP 502 e 504
Rastreabilidade	Propagação do header `X-Correlation-ID`
Auditoria	Logs com usuário, perfis, método, serviço, status e duração
Endpoints
URL base local:
```text
http://localhost:8081/camel-gateway
```
Método	Endpoint	Permissão
GET	`/pedidos/api/pedidos`	`USER` ou `ADMIN`
POST	`/pedidos/api/pedidos`	`ADMIN`
PUT	`/pedidos/api/pedidos/{id}`	`ADMIN`
DELETE	`/pedidos/api/pedidos/{id}`	`ADMIN`
> Confirme os caminhos com identificador (`{id}`) no código das rotas REST antes de publicar: eles podem variar conforme a versão do projeto.
Pré-requisitos
JDK 21 instalado.
Maven ou Maven Wrapper disponível.
Backend `camel-aula1` em execução na porta `8080`.
PostgreSQL configurado e disponível para o backend.
Chave pública RSA configurada no gateway para validar os tokens.
Executando localmente
Primeiro, inicie o backend `camel-aula1`. Depois, na pasta do gateway:
```bash
./mvnw spring-boot:run
```
No Windows, pelo Prompt de Comando, você também pode usar:
```bat
mvnw.cmd spring-boot:run
```
O gateway ficará disponível, na configuração local utilizada no curso, em `http://localhost:8081`.
Autenticação e autorização
O gateway utiliza JWT assinado com RS256. A aplicação valida a assinatura usando uma chave pública RSA. A chave privada deve permanecer fora do controle de versão.
No ambiente de estudo, há um utilitário local para geração de tokens:
```bash
java GerarJwt.java ADMIN
```
O token gerado pode ser usado no Postman:
```http
Authorization: Bearer SEU_TOKEN_JWT
```
Perfis utilizados:
`USER`: acesso às operações de consulta (GET).
`ADMIN`: acesso às operações de consulta e alteração (GET, POST, PUT e DELETE).
> O gerador de JWT é uma ferramenta de desenvolvimento. Em produção, a emissão de tokens deve ficar a cargo de um provedor de identidade confiável.
Exemplos de teste
Consulta de pedidos
```bash
curl -i \
  -H "Authorization: Bearer SEU_TOKEN_JWT" \
  -H "X-Correlation-ID: teste-pedidos-001" \
  http://localhost:8081/camel-gateway/pedidos/api/pedidos
```
Comportamentos esperados
Cenário	Resultado esperado
Requisição sem token	HTTP 401
Token válido, mas perfil sem permissão	HTTP 403
Requisição autorizada com backend disponível	Resposta do backend
Limite de requisições excedido	HTTP 429
Circuit Breaker acionando fallback	HTTP 503
Falha de comunicação tratada pelo gateway	HTTP 502 ou 504, conforme o erro
Os resultados exatos dependem da configuração ativa e do estado do backend.
Circuit Breaker
A rota de execução utiliza o Resilience4j com os parâmetros configurados durante o curso:
Parâmetro	Valor
`failureRateThreshold`	`50` (%)
`minimumNumberOfCalls`	`4`
`slidingWindowSize`	`4`
`waitDurationInOpenState`	`10` segundos
`automaticTransitionFromOpenToHalfOpenEnabled`	`true`
A chamada HTTP ao backend utiliza timeout de conexão de 3 segundos e timeout de resposta de 5 segundos. O fallback configurado retorna HTTP 503.
Correlation ID e auditoria
O gateway aceita o header `X-Correlation-ID` quando ele é válido; caso contrário, gera um UUID. Esse identificador é encaminhado ao backend e permite relacionar os logs das duas aplicações.
Exemplo de header:
```http
X-Correlation-ID: teste-pedidos-001
```
Os logs de auditoria incluem informações como:
```text
AUDITORIA | CorrelationId=teste-pedidos-001 | Usuario=usuario-teste | Perfis=ROLE_ADMIN | Metodo=GET | Servico=pedidos | Status=200 | Duracao=479ms
```
O `ExchangeId` do Apache Camel é próprio de cada aplicação; o `X-Correlation-ID` é o identificador compartilhado entre gateway e backend.
Organização do código
As responsabilidades principais estão distribuídas entre:
Rotas REST: expõem os endpoints HTTP e iniciam o processamento.
Rotas de proxy: carregam a configuração do serviço e executam as chamadas ao backend.
Processadores de segurança: recuperam informações do usuário autenticado.
Processadores de auditoria: registram início, fim e duração das requisições.
Processador de Correlation ID: valida ou gera o identificador de rastreamento.
Processador de erros: prepara respostas padronizadas para falhas.
Segurança do repositório
Não versionar:
Chaves privadas RSA, especialmente `private.pem`.
Tokens JWT gerados para testes.
Segredos e credenciais de ambientes.
Arquivos `.env` contendo informações sensíveis.
A chave pública necessária à validação pode fazer parte dos recursos da aplicação, conforme a configuração do projeto.
Contexto
Projeto desenvolvido para praticar os conceitos de Enterprise Integration Patterns (EIP) com Apache Camel, construção de um API Gateway, autenticação/autorização e integração entre microsserviços.