# Salutis Web (Spring Boot + Bootstrap)
Versão web do Salutis com as mesmas regras do app Android (ver documento de regras de negócio).
Stack: Spring Boot 3.3 (Web, Thymeleaf, Data JPA), banco H2 em arquivo (./data/salutis), Bootstrap 5.3 (CDN).
Requisitos: JDK 17 e Maven. Executar: `mvn spring-boot:run` e abrir http://localhost:8080
Telas: login, cadastro, painel, agendar, minhas consultas (remarcar/cancelar) e histórico.

## API REST (`/api`)
Autenticação por token: `POST /api/auth/login` retorna `{token}`; enviar `Authorization: Bearer <token>`. Erros: `{"erro": "..."}` (400/401). Datas `AAAA-MM-DD`, horas `HH:mm`. Tokens ficam em memória (reiniciar o servidor invalida).

| Método | Rota | Corpo / parâmetros |
|---|---|---|
| POST | /api/auth/cadastro | nome, email, cpf, senha, senha2 |
| POST | /api/auth/login | email, senha |
| POST | /api/auth/logout | – |
| GET | /api/especialidades | – |
| GET | /api/medicos | ?especialidade= |
| GET | /api/medicos/{id}/horarios | ?data= (horários livres) |
| GET | /api/consultas | – |
| POST | /api/consultas | medicoId, data, hora |
| PUT | /api/consultas/{id}/remarcar | data, hora, motivo |
| PUT | /api/consultas/{id}/cancelar | motivo |
| GET | /api/historico | – |

Exemplo: `curl -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' -d '{"email":"a@b.com","senha":"Senha1234"}'`
