# Salutis (Android / Java) – MVP
Abra a pasta no Android Studio (Hedgehog+), aguarde o Gradle Sync e execute (minSdk 24).
Telas: Login, Cadastro, Dashboard, Agendar, Minhas Consultas (cancelar/remarcar), Histórico.
Dados: o app consome a API REST do módulo salutis-web (Spring Boot); não usa mais SQLite local.

## Rodando no celular físico
1. No PC: `cd salutis-web && mvn spring-boot:run` (escuta em todas as interfaces, porta 8080).
2. PC e celular na mesma rede Wi-Fi. Descubra o IP do PC (`ipconfig` no Windows, `ip a` no Linux) e libere a porta 8080 no firewall.
3. Teste no navegador do celular: `http://IP_DO_PC:8080`.
4. No app, na tela de login, preencha "Servidor" com `http://IP_DO_PC:8080`, crie a conta e entre.
(Emulador: use `http://10.0.2.2:8080`.)
