<div align="center">

# WatchuSee Android

**Cliente Android nativo para busca de produções cinematográficas, gerenciamento de watchlist e compartilhamento de filmes entre usuários.**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Android-Jetpack%20Compose-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![MVVM](https://img.shields.io/badge/Architecture-MVVM-blue?style=for-the-badge)](#arquitetura-de-software)
[![Retrofit](https://img.shields.io/badge/HTTP-Retrofit2-red?style=for-the-badge)](https://square.github.io/retrofit/)

[Telas e Recursos](#telas-e-recursos-do-aplicativo) •
[Arquitetura](#arquitetura-de-software) •
[Como Executar](#configuração-e-execução-local) •
[Roadmap](#roadmap-de-evolução)

</div>

---

## Sumário

- [Visão Geral](#visão-geral)
- [Arquitetura de Software](#arquitetura-de-software)
- [Tecnologias e Ferramentas](#tecnologias-e-ferramentas)
- [Telas e Recursos do Aplicativo](#telas-e-recursos-do-aplicativo)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Configuração e Execução Local](#configuração-e-execução-local)
- [Integração com o Backend](#integração-com-o-backend)
- [Roadmap de Evolução](#roadmap-de-evolução)
- [Projetos Relacionados](#projetos-relacionados)
- [Contribuindo](#contribuindo)
- [Licença](#licença)
- [Autor](#autor)

---

## Visão Geral

O **WatchuSee** é um aplicativo mobile desenvolvido para oferecer uma experiência moderna, fluida e reativa de descoberta de filmes, gerenciamento de acervo pessoal (*watchlist*) e compartilhamento de indicações entre usuários.

O aplicativo consome a [WatchuSee Backend REST API](https://github.com/jzmlucas/watchusee-backend) e permite que os usuários:

- **Se Autentiquem:** cadastro e login com token JWT persistido de forma criptografada no dispositivo.
- **Naveguem como Convidado:** explorem busca e detalhes de filmes sem precisar criar conta, sendo direcionados ao login apenas quando tentam uma ação que exige conta (ex.: salvar na watchlist).
- **Busquem Filmes:** consulta em tempo real de produções através de termos de pesquisa.
- **Visualizem Detalhes:** exibição detalhada de sinopse, nota de avaliação, data de lançamento e pôster oficial.
- **Gerenciem a Watchlist:** adição e remoção de títulos nas categorias `QUERO ASSISTIR` e `ASSISTIDO`.
- **Compartilhem Filmes:** indiquem filmes para outros usuários pelo nick, com fluxo de aceite/recusa da indicação.
- **Interajam de Forma Reativa:** interface baseada em estados (*UI State*) que reage automaticamente a mudanças nos dados.

> **Nota de Design:** o app é o cliente mobile oficial do ecossistema WatchuSee, consumindo diretamente a API REST em produção ou uma instância local do backend Spring Boot.

---

## Arquitetura de Software

O aplicativo foi construído seguindo a arquitetura **MVVM (Model-View-ViewModel)** recomendada pelo ecossistema Android moderno e pelos guias de *Android Architecture Components*, garantindo separação rigorosa de responsabilidades e testabilidade.

```
┌──────────────────────────────────────────────────────────┐
│                   Camada de Apresentação                  │
│      (UI / Jetpack Compose / Screens & Navigation)         │
└───────────────────────────┬────────────────────────────────┘
                             │ State Observability (StateFlow)
                             ▼
┌──────────────────────────────────────────────────────────┐
│                  Camada de Gerenciamento                   │
│   (ViewModels: AuthViewModel, SearchViewModel, ShareVM...)  │
└───────────────────────────┬────────────────────────────────┘
                             │ Coroutines / Suspend Functions
                             ▼
┌──────────────────────────────────────────────────────────┐
│                 Camada de Dados (Repository)                │
│   (AuthRepository, MovieRepository, TokenManager)            │
└───────────────────────────┬────────────────────────────────┘
                             │ Retrofit Interfaces & DTOs
                             ▼
┌──────────────────────────────────────────────────────────┐
│               Camada de Rede e Injeção (DI)                 │
│   (NetworkModule / MovieApi / OkHttp Auth Interceptor)       │
└───────────────────────────┬────────────────────────────────┘
                             │ HTTP / REST API (JSON + Bearer JWT)
                             ▼
┌──────────────────────────────────────────────────────────┐
│                WatchuSee Backend REST API                   │
│         (Java 21 / Spring Boot / PostgreSQL / JWT)           │
└──────────────────────────────────────────────────────────┘
```

### Autenticação e Persistência de Sessão

O `NetworkModule` injeta um **interceptor OkHttp** que anexa automaticamente o header `Authorization: Bearer <token>` em toda requisição, e limpa a sessão local caso o backend responda `401 Unauthorized`. O token JWT é armazenado através de `EncryptedSharedPreferences` (`androidx.security.crypto`), nunca em texto plano.

---

## Tecnologias e Ferramentas

### Core & UI

- **Kotlin** — linguagem nativa e moderna para desenvolvimento Android.
- **Jetpack Compose** — framework declarativo para construção de interfaces de usuário reativas.
- **Material Design 3** — componentes de UI modernos seguindo a especificação do Material You.
- **Coil 3** — carregamento e cache dinâmico e otimizado de pôsteres e imagens.
- **Navigation Compose** — navegação declarativa entre telas por rotas.

### Arquitetura & Estado

- **ViewModel** — sobrevivência de estado a mudanças de configuração (ex.: rotação de tela).
- **StateFlow & Kotlin Coroutines** — programação reativa assíncrona para manipulação de fluxos de dados e estados de tela (Loading, Success, Error).
- **Repository Pattern** — abstração da fonte de dados, isolando a camada de rede da camada de apresentação.

### Rede, Segurança & Comunicação

- **Retrofit 2** — cliente HTTP tipo-seguro para consumo das rotas REST da API.
- **OkHttp 3** — interceptação de requisições, injeção automática do token JWT, timeouts e logs de rede.
- **Gson** — parsing e serialização automatizada dos DTOs JSON.
- **AndroidX Security Crypto** — armazenamento criptografado (`EncryptedSharedPreferences`) do token JWT e dados de sessão.

### Testes

- **JUnit 4** — testes unitários locais.
- **Mockito Kotlin** — mocking de dependências em testes de ViewModel.
- **Kotlinx Coroutines Test** — testes de fluxos assíncronos (`StateFlow`, `suspend fun`).
- **AndroidX Test / JUnit Ext** — testes de instrumentação de UI.

---

## Telas e Recursos do Aplicativo

| Tela | Arquivo | Descrição |
| ---- | -------- | ----------- |
| **Login** | `LoginScreen.kt` | Autenticação via nick e senha, com opção de continuar como convidado. |
| **Cadastro** | `RegisterScreen.kt` | Criação de conta com validação de campos. |
| **Início** | `HomeScreen.kt` | Tela inicial com destaque de filme em tendência e navegação principal. |
| **Busca** | `SearchScreen.kt` | Pesquisa dinâmica de produções, com cards renderizados via `MovieComponents.kt` e estados de carregamento/vazio. |
| **Detalhes do Filme** | `MovieDetailScreen.kt` | Pôster em alta resolução, sinopse completa, métricas e ações contextuais (adicionar à lista, marcar como assistido, compartilhar). |
| **Minha Lista** | `WatchlistScreens.kt` | Abas "Para Assistir" e "Assistidos", com ações rápidas de remoção e transição entre listas. |
| **Compartilhamentos** | `SharesScreen.kt` | Indicações recebidas, pendentes e enviadas, com ações de aceitar/recusar. |
| **Navegação Centralizada** | `WatchuSeeNavigation.kt` | Controle de rotas entre telas via Navigation Compose, incluindo redirecionamento a Login quando uma ação exige autenticação. |

> Componentes visuais compartilhados (skeletons de carregamento, estados vazios, animações) ficam isolados em `ui/components` e `ui/animations`, reaproveitados por todas as telas acima.

---

## Estrutura do Projeto

```
watchusee-android/
├── app/
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/br/com/watchusee/android/
│       │   │   ├── data/
│       │   │   │   ├── api/
│       │   │   │   │   └── MovieApi.kt              # Interface Retrofit (auth, movies, watchlist, shares)
│       │   │   │   ├── dto/
│       │   │   │   │   ├── AuthModels.kt             # LoginRequest, RegisterRequest, UserResponse
│       │   │   │   │   ├── LoginResponse.kt
│       │   │   │   │   ├── MovieResponse.kt
│       │   │   │   │   ├── ShareModels.kt            # ShareRequest, ShareResponse, ShareStatus
│       │   │   │   │   └── WatchlistStatusResponse.kt
│       │   │   │   └── repository/
│       │   │   │       ├── AuthRepository.kt
│       │   │   │       ├── MovieRepository.kt
│       │   │   │       └── TokenManager.kt           # Persistência criptografada do token JWT
│       │   │   │
│       │   │   ├── di/
│       │   │   │   └── NetworkModule.kt              # Retrofit, OkHttp e interceptor de autenticação
│       │   │   │
│       │   │   ├── ui/
│       │   │   │   ├── animations/Animations.kt
│       │   │   │   ├── auth/
│       │   │   │   │   ├── LoginScreen.kt
│       │   │   │   │   └── RegisterScreen.kt
│       │   │   │   ├── components/
│       │   │   │   │   ├── EmptyState.kt
│       │   │   │   │   ├── MovieComponents.kt
│       │   │   │   │   └── SkeletonLoaders.kt
│       │   │   │   ├── detail/MovieDetailScreen.kt
│       │   │   │   ├── home/HomeScreen.kt
│       │   │   │   ├── navigation/WatchuSeeNavigation.kt
│       │   │   │   ├── search/SearchScreen.kt
│       │   │   │   ├── shares/SharesScreen.kt
│       │   │   │   ├── theme/
│       │   │   │   │   ├── Color.kt
│       │   │   │   │   ├── Theme.kt
│       │   │   │   │   └── Type.kt
│       │   │   │   └── watchlist/WatchlistScreens.kt
│       │   │   │
│       │   │   ├── util/TmdbImageUrl.kt
│       │   │   │
│       │   │   ├── viewmodel/
│       │   │   │   ├── AuthViewModel.kt
│       │   │   │   ├── DetailViewModel.kt
│       │   │   │   ├── HomeViewModel.kt
│       │   │   │   ├── SearchViewModel.kt
│       │   │   │   ├── ShareViewModel.kt
│       │   │   │   └── WatchlistViewModel.kt
│       │   │   │
│       │   │   ├── MainActivity.kt
│       │   │   └── WatchuSeeApp.kt
│       │   │
│       │   └── res/                                  # Recursos estáticos (drawables, values, mipmap)
│       │
│       ├── androidTest/                               # Testes de instrumentação de UI
│       └── test/                                       # Testes unitários locais
│           └── java/br/com/watchusee/android/viewmodel/SearchViewModelTest.kt
│
├── gradle/                                             # Wrapper do Gradle
├── build.gradle.kts                                    # Script de build raiz
├── settings.gradle.kts                                 # Módulos e repositórios do projeto
├── gradle.properties
└── README.md
```

---

## Configuração e Execução Local

### Pré-requisitos

- Android Studio (Koala ou superior).
- JDK 21 configurado no Android Studio.
- Android SDK com suporte a API 26+ (Android 8.0 Oreo ou superior).
- [WatchuSee Backend](https://github.com/jzmlucas/watchusee-backend) em execução — local ou apontando para a instância em produção.

### Conectando ao Backend

Por padrão, a `BASE_URL` em `di/NetworkModule.kt` aponta para um backend local:

```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/"
```

> No Android Emulator, `10.0.2.2` é o endereço reservado para acessar o `localhost` da máquina hospedeira. Em um dispositivo físico via USB, substitua pelo IP local da sua máquina na rede Wi-Fi (ex.: `http://192.168.1.X:8080/`).

Para testar contra a **API em produção**, aponte a `BASE_URL` para:

```kotlin
private const val BASE_URL = "https://watchusee-backend.onrender.com/"
```

### Passos para Execução

1. **Clone o repositório:**

   ```bash
   git clone https://github.com/jzmlucas/watchusee-android.git
   ```

2. **Abra o projeto no Android Studio** e aguarde o encerramento do Gradle Sync.
3. **Configure a `BASE_URL`** em `NetworkModule.kt` conforme o backend que deseja utilizar (local ou produção).
4. **Inicie o Emulador Android** ou conecte um dispositivo físico via USB (com Depuração USB ativa).
5. **Execute o projeto:** pressione `Shift + F10` ou clique em **Run 'app'**.

### Executando os Testes

```bash
./gradlew test
```

---

## Integração com o Backend

Este aplicativo atua como o cliente mobile oficial do ecossistema WatchuSee, consumindo integralmente a API — incluindo autenticação, watchlist e compartilhamentos.

- **Repositório Backend:** [jzmlucas/watchusee-backend](https://github.com/jzmlucas/watchusee-backend)
- **API em Produção:** [watchusee-backend.onrender.com](https://watchusee-backend.onrender.com/swagger-ui/index.html)
- **Tecnologias do Backend:** Java 21, Spring Boot, Spring Security + JWT, Spring Data JPA, PostgreSQL, OpenAPI/Swagger.

| Recurso do App | Endpoint consumido |
| ---------------- | --------------------- |
| Login | `POST /api/v1/auth/login` |
| Cadastro | `POST /api/v1/users` |
| Busca de filmes | `GET /api/v1/movies/search` |
| Filme em tendência (Home) | `GET /api/v1/movies/trending/random` |
| Detalhes do filme | `GET /api/v1/movies/{movieId}` |
| Watchlist (adicionar/remover/listar/status) | `/api/v1/watchlist/**` |
| Compartilhamentos (criar/listar/aceitar/recusar) | `/api/v1/shares/**` |

> ⚠️ O plano gratuito do Render "hiberna" o backend após 15 minutos de inatividade — a primeira requisição do app após esse período pode levar de 30 a 60 segundos para responder.

---

## Roadmap de Evolução

- [x] **MVP Mobile:** busca, detalhes, navegação por rotas e gerenciamento de watchlist em Jetpack Compose.
- [x] **Arquitetura Modular:** separação limpa de camadas de rede (`data`), injeção de dependência (`di`), estados (`viewmodel`) e UI (`ui`).
- [x] **Autenticação de Usuário:** login/cadastro integrados a tokens JWT expedidos pelo backend, com sessão criptografada no dispositivo.
- [x] **Modo Convidado:** navegação sem login, com redirecionamento contextual apenas quando uma ação exige conta.
- [x] **Compartilhamento de Filmes:** indicação de filmes entre usuários com fluxo de aceite/recusa.
- [ ] **Persistência Local (Offline First):** integração com Room para cache offline de filmes salvos.
- [ ] **Injeção de Dependência Avançada:** transição do módulo manual (`NetworkModule`) para Hilt / Dagger.
- [ ] **Suporte a Notificações:** lembretes para filmes marcados na lista "Para Assistir".
- [ ] **Refresh Token:** renovação de sessão sem exigir novo login quando o JWT expira.

---

## Projetos Relacionados

- **[watchusee-backend](https://github.com/jzmlucas/watchusee-backend)** — API REST em Java/Spring Boot consumida por este aplicativo.

---

## Contribuindo

Contribuições são bem-vindas! Para propor uma mudança:

1. Faça um fork do projeto.
2. Crie uma branch para a sua feature (`git checkout -b feature/minha-feature`).
3. Faça commit das suas alterações (`git commit -m 'feat: minha nova feature'`).
4. Envie para o seu fork (`git push origin feature/minha-feature`).
5. Abra um Pull Request descrevendo a mudança.

---

## Licença

> Este repositório ainda não possui um arquivo `LICENSE`. O [watchusee-backend](https://github.com/jzmlucas/watchusee-backend) já define a licença **MIT** — recomenda-se adicionar o mesmo arquivo aqui para manter a consistência entre os dois projetos do ecossistema.

---

## Autor

Desenvolvido por **Lucas Joly**.

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/jzmlucas)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/jzmlucas)