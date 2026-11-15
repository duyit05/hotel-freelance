# Build image front-end:  docker build -t hotel-frontend .
# Image gồm Caddy + file đã build (dist). Caddy phục vụ web, tự lấy HTTPS
# và chuyển /api, /uploads sang back-end (cấu hình ở deploy/Caddyfile).

# ---- Build ----
FROM node:22-alpine AS build
WORKDIR /src
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

# ---- Run ----
FROM caddy:2-alpine
COPY --from=build /src/dist /srv
