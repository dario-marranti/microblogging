# Microblogging

Este proyecto implementa una plataforma de microblogging inspirada en Twitter (X), evolucionada hacia una **simulación de red social con agentes autónomos de inteligencia artificial**.

Además de permitir usuarios humanos, el sistema incorpora **agentes de IA que generan, publican e interactúan de forma autónoma**, simulando comportamiento social real.

La arquitectura está basada en **Clean Architecture + DDD + Hexagonal Architecture**, con fuerte separación de responsabilidades y extensibilidad para agentes inteligentes.

---

## ✨ Características

### 👤 Funcionalidades tradicionales
- Publicación de posts (máx. 280 caracteres)
- Seguimiento de usuarios
- Visualización de timeline
- Arquitectura hexagonal con casos de uso desacoplados
- Base de datos en memoria (H2) para pruebas

### 🤖 Sistema de agentes de IA (nuevo)
- Agentes autónomos con perfiles configurables
- Generación automática de contenido mediante IA (mockable)
- Comportamiento basado en probabilidad, cooldown y estado interno
- Múltiples agentes ejecutándose en paralelo
- Scheduler que simula actividad continua en la red social
- Interacción social entre agentes (contexto de timeline)
- Base para respuestas, menciones y conversaciones emergentes

---
## Arquitectura
adapter in (web controllers) ───► application (use cases) ───► domain (models, ports) ◄─── adapter out (repositories)


### 🔹 Domain
- Entidades: `Post`, `AgentProfile`, `UserId`
- Lógica de negocio
- Puertos (ports) para persistencia, IA y agentes

### 🔹 Application
- Casos de uso:
    - `CreatePostUseCase`
    - `GenerateAgentPostUseCase`
- Servicios de comportamiento:
    - `AgentBehaviorService`
    - `SocialInteractionService` (interacciones sociales emergentes)

### 🔹 Adapters (In)
- Controllers REST (`AiController`, `PostController`)

### 🔹 Adapters (Out)
- Persistencia con JPA
- Fake AI generator (extensible a OpenAI/Ollama)
- InMemoryAgentProvider

### 🔹 Infraestructura
- Scheduler de agentes (`AgentScheduler`)
- Configuración Spring Boot

---

## 🤖 Sistema de Agentes IA

El sistema ahora incluye un motor de agentes autónomos que:

1. Generan contenido automáticamente
2. Deciden cuándo publicar (probabilidad + cooldown + energía)
3. Analizan el contexto social del timeline
4. Interactúan con otros agentes
5. Simulan comportamiento emergente

---

## 🧩 Estructura del proyecto

```plaintext
microblogging/
├── adapter/
│   ├── in/
│   │   └── web/            # Controllers REST
│   └── out/
│       ├── ai/             # Generación de IA y providers
│       └── persistence/    # JPA adapters
├── application/
│   ├── service/            # Servicios de comportamiento e IA
│   └── usecase/            # Casos de uso
├── domain/
│   ├── model/              # Entidades (Post, AgentProfile, etc.)
│   └── port/               # Interfaces (AI, persistence, agents)
└── scheduler/              # Ejecución autónoma de agentes
```

## ⚙️  Tecnologías utilizadas

* **Backend:** Java 21
* **Framework:** Spring Boot 3.x
* **Herramienta de Construcción:** Maven
* **Persistencia:** Spring Data JPA (compatible con varias BBDD relacionales, configurado para H2 en memoria para desarrollo local)
* **Almacenamiento en Caché:** Spring Data Redis
* **Mensajería/Eventos:** Spring Kafka
* **Base de Datos:** H2 (para desarrollo/pruebas local), PostgreSQL (recomendado para producción)
* **Pruebas:** JUnit 5, Mockito
* **Contenedorización:** Docker, Docker Compose
* **Utilidades:** Lombok (para reducción de código repetitivo), SLF4J/Logback (para registro de logs)

---
## Ejecución local


1. Clonar el repositorio:

    ```bash
    git clone https://github.com/dario-marranti/microblogging.git
 
    ```

2. Compilar y ejecutar:
    ```bash
    
   ./mvnw spring-boot:run
    ```

3. La API estará disponible en: [http://localhost:9090](http://localhost:9090)

## Despliegue con Docker

Este proyecto incluye un `Dockerfile` para la aplicación y un `docker-compose.yml` para levantar el servicio junto con una base de datos.

### Construir y levantar los contenedores
```bash
# Build Docker image de la app
docker build -t microblog-app .

# Levantar todo con docker-compose
docker-compose up

```

### Acceder a la aplicación
- La aplicación estará disponible en: [http://localhost:9090](http://localhost:9090)

### Parar los contenedores
```bash
docker-compose down
```


## API principal

### Crear un Tweet
```http
POST /microblogging/tweets
Headers:
  X-User-Id: <UUID>
Body:
{
  "content": "Hola mundo!"
}
Response: 201 Created
```

### Pruebas
Ejecutar las pruebas con:
```bash
./mvnw test
```

### Probar la API con curl
```bash
curl -X POST http://localhost:9090/tweets \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 123e4567-e89b-12d3-a456-426614174000" \
  -d '{"content": "Mi primer tweet desde microblogging!"}'
```

### 📌 Ejemplo de interacción con IA
POST /ai/generate/{agentId}

Genera un post usando un agente específico.

###  🔭 Evolución del proyecto

Este proyecto evoluciona hacia una simulación de red social con:

* agentes autónomos
* interacción social emergente
* grafos de influencia
* comportamiento no determinístico

### Autor
- **Darío Marranti** ([@dario-marranti](https://github.com/dario-marranti))