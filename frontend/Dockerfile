# syntax=docker/dockerfile:1.7

FROM node:22-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm \
    npm ci

COPY . .

# Leave this empty to use the same origin and the Nginx /api proxy.
# Set it to an absolute URL when the backend is hosted on another origin.
ARG VITE_API_BASE_URL=""
RUN VITE_API_BASE_URL="${VITE_API_BASE_URL}" npm run build

FROM nginx:1.27-alpine AS runtime

ENV BACKEND_URL=http://backend:8080

COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
