FROM node:24.18.0-bookworm-slim
WORKDIR /app
RUN chown node:node /app
USER node
COPY --chown=node:node package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY --chown=node:node . .
RUN npm run build
EXPOSE 4200
CMD ["npm", "start", "--", "--proxy-config", "proxy.docker.json"]
