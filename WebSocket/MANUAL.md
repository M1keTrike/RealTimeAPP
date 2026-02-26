# MathDuel WebSocket — Manual de Uso

## Índice

1. [Descripción General](#descripción-general)
2. [Arquitectura](#arquitectura)
3. [Configuración](#configuración)
4. [Despliegue](#despliegue)
5. [Conexión WebSocket](#conexión-websocket)
6. [Protocolo de Mensajes](#protocolo-de-mensajes)
   - [Formato general](#formato-general)
   - [Mensajes del cliente → servidor](#mensajes-del-cliente--servidor)
   - [Mensajes del servidor → cliente](#mensajes-del-servidor--cliente)
7. [Flujo de Juego Completo](#flujo-de-juego-completo)
8. [Autenticación (JWT)](#autenticación-jwt)
9. [Matchmaking](#matchmaking)
10. [Sala de Juego (Room)](#sala-de-juego-room)
11. [Manejo de Desconexiones](#manejo-de-desconexiones)
12. [Health Check](#health-check)
13. [Ejemplos de Integración](#ejemplos-de-integración)

---

## Descripción General

**MathDuel WebSocket** es un servidor WebSocket escrito en **Go** que gestiona partidas en tiempo real de un juego de duelos matemáticos. Los jugadores se conectan, son emparejados automáticamente, y compiten respondiendo preguntas de opción múltiple ronda a ronda.

El servidor actúa como intermediario entre los clientes (apps móviles/web) y una **API REST NestJS** que almacena preguntas, sesiones de juego y resultados.

### Dependencias principales

| Librería | Uso |
| --- | --- |
| `github.com/gorilla/websocket` | Manejo de conexiones WebSocket |
| `github.com/golang-jwt/jwt/v5` | Validación y generación de tokens JWT |
| `github.com/joho/godotenv` | Carga de variables de entorno desde `.env` |

---

## Arquitectura

```
┌──────────────┐         WebSocket          ┌────────────────────┐
│  Cliente     │ ◄═══════════════════════►   │  MathDuel WS (Go)  │
│  (App/Web)   │    JSON sobre WS            │                    │
└──────────────┘                             │  ┌──────┐          │
                                             │  │ Hub  │          │
┌──────────────┐         HTTP/REST           │  │      ├─► Rooms  │
│  NestJS API  │ ◄═══════════════════════►   │  │      ├─► Queue  │
│  (Backend)   │    Bearer JWT (admin)       │  └──────┘          │
└──────────────┘                             └────────────────────┘
```

### Componentes internos

| Componente | Archivo | Responsabilidad |
| --- | --- | --- |
| **Hub** | `hub/hub.go` | Event loop central: registra/desregistra clientes, matchmaking, gestiona salas |
| **Client** | `hub/client.go` | Wrapper de conexión WebSocket por jugador (read/write pumps) |
| **Room** | `hub/room.go` | Ciclo de vida de una partida entre 2 jugadores (rondas, puntajes, timeouts) |
| **Messages** | `hub/messages.go` | Definición de tipos de mensajes y payloads JSON |
| **API Client** | `api/client.go` | Cliente HTTP para comunicarse con la API NestJS |
| **Auth** | `auth/jwt.go` | Validación de JWT de usuarios y generación de token de servicio |
| **Config** | `config/config.go` | Carga de configuración desde variables de entorno |

---

## Configuración

El servidor se configura mediante variables de entorno. Se puede usar un archivo `.env` en la raíz del proyecto.

| Variable | Requerida | Default | Descripción |
| --- | --- | --- | --- |
| `JWT_SECRET` | **Sí** | — | Clave secreta compartida con NestJS para firmar/verificar JWT (HS256) |
| `PORT` | No | `8080` | Puerto en el que escucha el servidor |
| `NESTJS_API_URL` | No | `http://localhost:3000` | URL base de la API NestJS |
| `GAME_ROUNDS` | No | `5` | Número de rondas por partida (mínimo 1) |
| `ROUND_TIMEOUT_SECONDS` | No | `30` | Tiempo límite por ronda en segundos (mínimo 5) |

### Ejemplo de `.env`

```env
JWT_SECRET=mi_super_secreto_compartido
PORT=8080
NESTJS_API_URL=http://api.mathduel.com:3000
GAME_ROUNDS=5
ROUND_TIMEOUT_SECONDS=30
```

---

## Despliegue

### Ejecución local

```bash
# Instalar dependencias
go mod download

# Ejecutar
go run main.go
```

### Docker

```bash
# Construir imagen
docker build -t mathduel-ws .

# Ejecutar contenedor
docker run -d \
  -p 8080:8080 \
  -e JWT_SECRET=mi_super_secreto \
  -e NESTJS_API_URL=http://host.docker.internal:3000 \
  --name mathduel-ws \
  mathduel-ws
```

El `Dockerfile` usa un **multi-stage build** (Go 1.23 Alpine → Alpine 3.20 runtime) que produce una imagen ligera.

---

## Conexión WebSocket

### Endpoint

```
ws://<host>:<port>/ws?token=<JWT>
```

### Parámetros

| Parámetro | Tipo | Descripción |
| --- | --- | --- |
| `token` | Query string | JWT válido emitido por la API NestJS |

### Proceso de conexión

1. El cliente obtiene un JWT haciendo login en la API NestJS.
2. El cliente abre una conexión WebSocket pasando el token como query param.
3. El servidor valida el JWT:
   - Si es **inválido o ausente** → responde con HTTP `401` y no se establece la conexión.
   - Si es **válido** → se hace el upgrade a WebSocket y el servidor envía un mensaje `authenticated`.

### Ejemplo de conexión (JavaScript)

```javascript
const token = "eyJhbGciOiJIUzI1NiIs...";
const ws = new WebSocket(`ws://localhost:8080/ws?token=${token}`);

ws.onopen = () => console.log("Conectado");
ws.onmessage = (event) => {
  const msg = JSON.parse(event.data);
  console.log(msg.type, msg.payload);
};
ws.onclose = () => console.log("Desconectado");
```

### Keep-alive

El servidor envía **pings** cada ~54 segundos. El cliente debe responder con **pong** (el navegador lo hace automáticamente). Si no se recibe pong en 60 segundos, la conexión se cierra.

Adicionalmente, el cliente puede enviar mensajes `ping` explícitos y recibirá un `pong` como respuesta.

---

## Protocolo de Mensajes

### Formato general

Todos los mensajes (entrantes y salientes) usan un **envelope JSON** con la siguiente estructura:

```json
{
  "type": "<tipo_de_mensaje>",
  "payload": { ... }
}
```

---

### Mensajes del cliente → servidor

#### `answer` — Enviar respuesta a una pregunta

```json
{
  "type": "answer",
  "payload": {
    "question_id": "uuid-de-la-pregunta",
    "option_id": "uuid-de-la-opcion-elegida"
  }
}
```

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `question_id` | `string` | ID de la pregunta actual (debe coincidir con la pregunta en curso) |
| `option_id` | `string` | ID de la opción seleccionada |

**Reglas:**
- Solo se acepta **una respuesta por jugador por ronda**.
- Si el `question_id` no corresponde a la ronda actual, se ignora.
- Si el jugador no está en una sala, se ignora.

#### `ping` — Keep-alive explícito

```json
{
  "type": "ping"
}
```

Respuesta del servidor:

```json
{
  "type": "pong"
}
```

---

### Mensajes del servidor → cliente

#### `authenticated` — Conexión autenticada exitosamente

Se envía inmediatamente al conectar. Confirma la identidad del usuario.

```json
{
  "type": "authenticated",
  "payload": {
    "user_id": "uuid-del-usuario",
    "username": "nombre_usuario"
  }
}
```

#### `waiting` — En cola de matchmaking

Se envía cuando no hay otro jugador disponible para emparejar.

```json
{
  "type": "waiting",
  "payload": {
    "message": "Buscando oponente..."
  }
}
```

#### `game_started` — Partida encontrada

Se envía a ambos jugadores cuando el matchmaking los empareja.

```json
{
  "type": "game_started",
  "payload": {
    "session_id": "uuid-de-la-sesion",
    "opponent": {
      "user_id": "uuid-del-oponente",
      "username": "nombre_oponente"
    },
    "total_rounds": 5
  }
}
```

#### `round_started` — Nueva ronda con pregunta

Se envía al inicio de cada ronda. La respuesta correcta **nunca** se incluye.

```json
{
  "type": "round_started",
  "payload": {
    "round_number": 1,
    "question": {
      "id": "uuid-de-la-pregunta",
      "statement": "¿Cuánto es 7 × 8?",
      "difficulty": "MEDIUM",
      "options": [
        { "id": "opt-1", "text": "54" },
        { "id": "opt-2", "text": "56" },
        { "id": "opt-3", "text": "58" },
        { "id": "opt-4", "text": "62" }
      ]
    },
    "time_limit_seconds": 30
  }
}
```

#### `round_result` — Resultado de la ronda

Se envía al finalizar cada ronda (cuando ambos respondieron o se agotó el tiempo).

```json
{
  "type": "round_result",
  "payload": {
    "round_number": 1,
    "winner_id": "uuid-del-ganador",
    "correct_option_id": "opt-2",
    "scores": {
      "uuid-jugador-1": 1,
      "uuid-jugador-2": 0
    }
  }
}
```

| Campo | Descripción |
| --- | --- |
| `winner_id` | ID del jugador que ganó la ronda. **Vacío** si ambos fallaron o hubo timeout. |
| `correct_option_id` | ID de la opción correcta (para feedback visual en el cliente). |
| `scores` | Puntaje acumulado de cada jugador. |

> **Nota:** El primer jugador que responde correctamente gana la ronda. Si ambos responden correctamente, gana el **más rápido**.

#### `game_over` — Fin de la partida

Se envía cuando terminan todas las rondas o un jugador se desconecta.

```json
{
  "type": "game_over",
  "payload": {
    "winner_id": "uuid-del-ganador",
    "reason": "rounds_completed",
    "scores": {
      "uuid-jugador-1": 3,
      "uuid-jugador-2": 2
    }
  }
}
```

| Valor de `reason` | Significado |
| --- | --- |
| `rounds_completed` | Se jugaron todas las rondas normalmente |
| `opponent_disconnected` | El oponente se desconectó; victoria por abandono |

#### `error` — Error del servidor

```json
{
  "type": "error",
  "payload": {
    "code": "INIT_FAILED",
    "message": "No se pudo crear la sesión en el servidor."
  }
}
```

| Código | Significado |
| --- | --- |
| `INIT_FAILED` | Error al crear la sesión de juego en NestJS |
| `QUESTION_UNAVAILABLE` | No se encontraron preguntas para la dificultad configurada |

#### `pong` — Respuesta a ping

```json
{
  "type": "pong"
}
```

---

## Flujo de Juego Completo

```
  Cliente A                    Servidor WS                    Cliente B
     │                             │                              │
     ├── ws://.../ws?token=JWT_A ──►                              │
     │                             │                              │
     ◄── authenticated ───────────┤                              │
     ◄── waiting ─────────────────┤                              │
     │                             │                              │
     │                             │   ◄── ws://.../ws?token=JWT_B
     │                             │                              │
     │                             ├── authenticated ────────────►
     │                             │                              │
     │          ┌──────────────────┤                              │
     │          │   MATCHMAKING    │                              │
     │          └──────────────────┤                              │
     │                             │                              │
     ◄── game_started ────────────┤── game_started ─────────────►
     │                             │                              │
     │     ╔══════════════════╗    │                              │
     │     ║   RONDA 1..N     ║    │                              │
     │     ╚══════════════════╝    │                              │
     │                             │                              │
     ◄── round_started ──────────┤── round_started ─────────────►
     │                             │                              │
     ├── answer ──────────────────►                              │
     │                             │   ◄── answer ───────────────┤
     │                             │                              │
     ◄── round_result ───────────┤── round_result ──────────────►
     │                             │                              │
     │       (pausa 3 seg)         │                              │
     │                             │                              │
     │     ... se repite N rondas ...                             │
     │                             │                              │
     ◄── game_over ──────────────┤── game_over ─────────────────►
     │                             │                              │
```

### Detalle paso a paso

1. **Conexión**: El cliente abre la conexión WebSocket con su JWT.
2. **Autenticación**: El servidor valida el JWT y envía `authenticated`.
3. **Cola de espera**: Si no hay oponente, envía `waiting` y encola al jugador.
4. **Matchmaking**: Cuando se conecta un segundo jugador, se emparejan automáticamente (FIFO).
5. **Inicio de partida**: Se crea una sesión en NestJS y se envía `game_started` a ambos.
6. **Rondas**: Por cada ronda:
   - Se obtiene una pregunta aleatoria de la API NestJS (dificultad `MEDIUM`).
   - Se envía `round_started` con la pregunta (sin respuesta correcta).
   - Los jugadores envían `answer`.
   - El primero en responder correctamente gana la ronda.
   - Se envía `round_result` con el ganador, la respuesta correcta y puntajes.
   - Pausa de 3 segundos antes de la siguiente ronda.
7. **Fin de partida**: Se envía `game_over` con el ganador y se persiste el resultado en NestJS.

---

## Autenticación (JWT)

### Estructura del JWT esperado

El servidor espera un token JWT firmado con **HS256** usando el `JWT_SECRET` compartido con NestJS.

```json
{
  "sub": "uuid-del-usuario",
  "email": "user@example.com",
  "username": "player1",
  "role": "USER",
  "iat": 1700000000,
  "exp": 1700086400
}
```

| Claim | Uso |
| --- | --- |
| `sub` | Identificador único del usuario (userID) |
| `username` | Nombre mostrado al oponente |
| `role` | Rol del usuario (`USER`, `ADMIN`) |
| `email` | Correo del usuario |

### Token de servicio

El servidor genera automáticamente un **token de administrador** de larga duración (365 días) para autenticarse contra los endpoints protegidos de NestJS (`GET /questions`, `POST /game-sessions`, etc.).

---

## Matchmaking

- Se usa una **cola FIFO** (first in, first out).
- Cuando un jugador se conecta y no hay nadie esperando, se le agrega a la cola.
- Cuando un segundo jugador se conecta, se empareja con el primero de la cola.
- Si un usuario se reconecta (mismo `user_id`), la conexión anterior se cierra automáticamente.
- No hay filtros por nivel, ELO ni región — el emparejamiento es completamente secuencial.

---

## Sala de Juego (Room)

Cada sala contiene exactamente **2 jugadores** y ejecuta su propio game loop en una goroutine independiente.

### Ciclo de vida

```
newRoom() → Start() → run()
  ├── CreateGameSession()       → Persistir sesión en NestJS
  ├── notifyGameStarted()       → Enviar game_started
  ├── playRound(1)              → Pregunta, esperar respuestas, resultado
  ├── playRound(2)              → ...
  ├── ...
  ├── playRound(N)              → Última ronda
  ├── determineWinner()         → El de mayor puntaje
  ├── broadcastGameOver()       → Enviar game_over
  ├── FinishGameSession()       → Persistir resultado en NestJS
  └── cleanup()                 → Desvincular jugadores de la sala
```

### Reglas del juego

- **Ganador de ronda**: El primer jugador que envía la respuesta correcta.
- **Sin ganador de ronda**: Si ambos fallan o se agota el timeout, `winner_id` queda vacío.
- **Ganador de partida**: El jugador con más rondas ganadas al final.
- **Empate**: Si los puntajes son iguales, gana el Player 1 (el que se conectó primero).
- **Abandono**: Si un jugador se desconecta, el otro gana automáticamente.

---

## Manejo de Desconexiones

| Escenario | Comportamiento |
| --- | --- |
| Desconexión en cola de espera | El jugador se elimina de la cola |
| Desconexión durante partida | El oponente gana por forfeit (`reason: "opponent_disconnected"`) y el resultado se persiste en NestJS |
| Reconexión del mismo usuario | La conexión anterior se cierra; el usuario vuelve a entrar en el flujo normal (cola → matchmaking) |

---

## Health Check

### Endpoint

```
GET /health
```

### Respuesta

```json
{
  "status": "ok",
  "service": "mathduel-ws"
}
```

Útil para probes de Docker, Kubernetes o balanceadores de carga.

---

## Ejemplos de Integración

### JavaScript (navegador / React Native)

```javascript
const WS_URL = "ws://localhost:8080/ws";

function connectToGame(jwtToken) {
  const ws = new WebSocket(`${WS_URL}?token=${jwtToken}`);

  ws.onmessage = (event) => {
    const { type, payload } = JSON.parse(event.data);

    switch (type) {
      case "authenticated":
        console.log(`Conectado como ${payload.username}`);
        break;

      case "waiting":
        console.log(payload.message);
        break;

      case "game_started":
        console.log(`Partida iniciada vs ${payload.opponent.username}`);
        console.log(`Rondas: ${payload.total_rounds}`);
        break;

      case "round_started":
        console.log(`Ronda ${payload.round_number}`);
        console.log(`Pregunta: ${payload.question.statement}`);
        console.log(`Tiempo: ${payload.time_limit_seconds}s`);
        payload.question.options.forEach((opt) => {
          console.log(`  [${opt.id}] ${opt.text}`);
        });
        break;

      case "round_result":
        console.log(`Ganador ronda: ${payload.winner_id || "Empate"}`);
        console.log(`Respuesta correcta: ${payload.correct_option_id}`);
        console.log(`Puntajes:`, payload.scores);
        break;

      case "game_over":
        console.log(`Ganador: ${payload.winner_id}`);
        console.log(`Razón: ${payload.reason}`);
        console.log(`Puntajes finales:`, payload.scores);
        break;

      case "error":
        console.error(`Error [${payload.code}]: ${payload.message}`);
        break;

      case "pong":
        console.log("Pong recibido");
        break;
    }
  };

  ws.onclose = () => console.log("Conexión cerrada");
  ws.onerror = (err) => console.error("Error WS:", err);

  // Función para enviar respuesta
  function sendAnswer(questionId, optionId) {
    ws.send(
      JSON.stringify({
        type: "answer",
        payload: {
          question_id: questionId,
          option_id: optionId,
        },
      })
    );
  }

  // Keep-alive manual (opcional, el ping/pong de WebSocket ya es automático)
  function sendPing() {
    ws.send(JSON.stringify({ type: "ping" }));
  }

  return { ws, sendAnswer, sendPing };
}
```

### Dart (Flutter)

```dart
import 'dart:convert';
import 'package:web_socket_channel/web_socket_channel.dart';

class MathDuelWS {
  late WebSocketChannel _channel;

  void connect(String token) {
    _channel = WebSocketChannel.connect(
      Uri.parse('ws://localhost:8080/ws?token=$token'),
    );

    _channel.stream.listen((data) {
      final msg = jsonDecode(data);
      final type = msg['type'];
      final payload = msg['payload'];

      switch (type) {
        case 'authenticated':
          print('Conectado como ${payload['username']}');
          break;
        case 'waiting':
          print(payload['message']);
          break;
        case 'game_started':
          print('Oponente: ${payload['opponent']['username']}');
          break;
        case 'round_started':
          print('Ronda ${payload['round_number']}: ${payload['question']['statement']}');
          break;
        case 'round_result':
          print('Puntajes: ${payload['scores']}');
          break;
        case 'game_over':
          print('Ganador: ${payload['winner_id']} — ${payload['reason']}');
          break;
        case 'error':
          print('Error: ${payload['message']}');
          break;
      }
    });
  }

  void sendAnswer(String questionId, String optionId) {
    _channel.sink.add(jsonEncode({
      'type': 'answer',
      'payload': {
        'question_id': questionId,
        'option_id': optionId,
      },
    }));
  }

  void dispose() {
    _channel.sink.close();
  }
}
```

### Python

```python
import asyncio
import json
import websockets

async def play(token: str):
    uri = f"ws://localhost:8080/ws?token={token}"
    async with websockets.connect(uri) as ws:
        async for raw in ws:
            msg = json.loads(raw)
            msg_type = msg["type"]
            payload = msg.get("payload", {})

            if msg_type == "round_started":
                # Responder automáticamente con la primera opción
                question = payload["question"]
                answer = {
                    "type": "answer",
                    "payload": {
                        "question_id": question["id"],
                        "option_id": question["options"][0]["id"],
                    },
                }
                await ws.send(json.dumps(answer))

            elif msg_type == "game_over":
                print(f"Partida terminada. Ganador: {payload['winner_id']}")
                break

asyncio.run(play("tu_jwt_aqui"))
```

---

## Constantes y Límites

| Parámetro | Valor | Descripción |
| --- | --- | --- |
| Tamaño máximo de mensaje | 2048 bytes | Mensajes mayores cierran la conexión |
| Buffer de envío por cliente | 256 mensajes | Si se llena, los mensajes se descartan |
| Timeout de escritura | 10 s | Tiempo máximo para escribir un frame |
| Timeout de pong | 60 s | Tiempo máximo sin recibir pong antes de desconectar |
| Intervalo de ping | ~54 s | Frecuencia de pings del servidor |
| Pausa entre rondas | 3 s | Tiempo de espera entre `round_result` y próximo `round_started` |
| Timeout de shutdown graceful | 10 s | Tiempo máximo para cerrar conexiones al apagar |
| Timeout del API client HTTP | 10 s | Tiempo máximo para llamadas a NestJS |

---

## Interacción con NestJS API

El servidor WS se comunica con NestJS usando un **token de servicio ADMIN** auto-generado.

| Operación | Método | Endpoint NestJS | Momento |
| --- | --- | --- | --- |
| Obtener preguntas | `GET` | `/questions` | Al inicio de cada ronda |
| Crear sesión | `POST` | `/game-sessions` | Al emparejar 2 jugadores |
| Finalizar sesión | `PATCH` | `/game-sessions/:id/finish` | Al terminar la partida o por abandono |

---

## Diagrama de Estados del Jugador

```
 ┌─────────────┐
 │ Desconectado│
 └──────┬──────┘
        │ ws://...?token=JWT
        ▼
 ┌─────────────┐
 │ Autenticado │ ◄── "authenticated"
 └──────┬──────┘
        │
        ▼
 ┌─────────────┐     matchmaking     ┌─────────────┐
 │  Esperando  │ ──────────────────► │  En partida  │
 │  (cola)     │  ◄── "waiting"      │              │
 └─────────────┘                     └──────┬───────┘
                                            │
                              ┌─────────────┼──────────────┐
                              ▼             ▼              ▼
                        round_started   round_result   game_over
                              │             │              │
                              └─────────────┘              │
                                (se repite N veces)        ▼
                                                    ┌─────────────┐
                                                    │ Desconectado│
                                                    └─────────────┘
```
