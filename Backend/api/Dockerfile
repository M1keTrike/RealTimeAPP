# --- Etapa 1: Build ---
FROM node:20-alpine AS builder

# Instalamos pnpm y el CLI de Nest globalmente
RUN npm install -g pnpm @nestjs/cli

WORKDIR /usr/src/app

COPY package.json pnpm-lock.yaml ./

# 🔥 EL TRUCO DE ORO PARA PNPM EN DOCKER:
# Fuerza a pnpm a usar una estructura plana para que TypeScript y Nest no se pierdan.
RUN echo "node-linker=hoisted" > .npmrc

# Instalamos dependencias
RUN pnpm install

COPY . .

# Compilamos usando el estándar de Nest
RUN nest build

# --- Etapa 2: Producción ---
FROM node:20-alpine

RUN npm install -g pnpm

WORKDIR /usr/src/app

COPY package.json pnpm-lock.yaml ./

# Aplicamos el mismo truco para producción
RUN echo "node-linker=hoisted" > .npmrc

# Instalamos SOLO dependencias de producción
RUN pnpm install --prod

# Copiamos la carpeta dist compilada
COPY --from=builder /usr/src/app/dist ./dist

EXPOSE 3000

# Iniciamos la API
CMD ["node", "dist/main.js"]