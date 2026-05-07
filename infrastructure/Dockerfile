FROM swaggerapi/swagger-ui:v5.30.2 AS base

# BUILD stage #################################################################
FROM base AS build

COPY config/* /usr/share/nginx/html/
RUN apk add jq
COPY customize-nginx.sh /docker-entrypoint.d/50-customize-nginx.sh