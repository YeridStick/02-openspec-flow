# Soluciones

Base de desarrollo local para gestión de solicitudes. Esta entrega prepara Angular, Spring Boot, PostgreSQL, pruebas e infraestructura; las capacidades de registro, asignación y gestión de tareas siguen pendientes.

## Requisitos y versiones

| Componente | Versión |
| --- | --- |
| Java | 21 |
| Spring Boot | 4.1.2 |
| PostgreSQL | 17.11 |
| Angular | 22.0.8 |
| Angular CLI / build | 22.0.9 |
| Node | 24.18.0 (`frontend/.nvmrc`) |
| npm | 11.16.0 |
| Maven | 3.9.11 mediante Wrapper |

Para levantar todo se necesita Docker con Compose y acceso al registro de imágenes. Para compilar y probar fuera de contenedores se necesitan además Java 21, Node y npm. No se necesita Maven global. Las primeras ejecuciones descargan dependencias.

## Ejecutar con Docker Compose

Desde la raíz del repositorio:

```sh
cp .env.example .env
```

Editar `.env` y asignar una contraseña local a `DB_PASSWORD`. El archivo está excluido de Git. Los puertos pueden cambiarse en ese mismo archivo si están ocupados. No cambiar la contraseña de una base ya inicializada solamente mediante esta variable: PostgreSQL utiliza la contraseña establecida al crear el volumen.

```sh
docker compose config --quiet
docker compose up --build -d --wait
docker compose ps
curl --fail http://localhost:8080/actuator/health
curl --fail http://localhost:4200/actuator/health
```

Abrir <http://localhost:4200>. La pantalla inicial indica que el espacio de trabajo está en preparación. El segundo `curl` verifica el proxy Angular → backend → PostgreSQL. Si se cambiaron puertos, ajustar las URL. OpenAPI técnico está disponible en <http://localhost:8080/openapi.yaml>.

Los tres servicios tienen comprobaciones de salud. PostgreSQL escucha en el puerto local 5432, backend en 8080 y frontend en 4200, todos enlazados a localhost. Angular utiliza el servidor de desarrollo: este Compose no es un despliegue productivo.

```sh
docker compose logs --tail=100 backend frontend postgres
docker compose stop
docker compose start --wait
docker compose down
```

`stop`, `start` y `down` conservan el volumen. Para volver a levantar después de `down`, usar `docker compose up -d --wait`.

Solo para **reinicializar explícitamente y eliminar los datos locales**:

```sh
docker compose down --volumes
docker compose up -d --wait
```

## Compilar y probar

Con Docker en ejecución y Java 21:

```sh
./scripts/test-backend.sh
```

Ejecuta Maven Wrapper `verify` en todos los módulos. Testcontainers usa PostgreSQL 17.11 aislado del volumen de desarrollo y comprueba conexión mediante JdbcClient, migración Flyway, UTC y salud HTTP. El script utiliza el contexto Docker activo, incluido Rancher Desktop, sin desactivar la limpieza automática de contenedores. Se pueden proporcionar `DOCKER_HOST` y `TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE` para otro entorno.

Con Node 24.18.0 y npm:

```sh
cd frontend
npm ci
npm run build
npm test -- --watch=false
```

Angular CLI utiliza Vitest. Los parches transitivos están fijados en `package-lock.json`. La construcción Docker omite la ejecución de pruebas del backend porque no dispone del daemon para Testcontainers; ejecutar el comando de pruebas anterior antes de considerar validada la entrega.

Para revisar las dependencias interiores:

```sh
cd backend
./mvnw -pl domain,application -am dependency:tree
```

El dominio no tiene dependencias de runtime; aplicación depende únicamente del dominio. El parent de Spring Boot gestiona versiones y construcción, sin introducir dependencias Spring en esos módulos.

## Desarrollo fuera de contenedores

Puede mantenerse solo PostgreSQL en Compose:

```sh
docker compose up -d --wait postgres
```

En una terminal, desde `backend`, exportar `DB_PASSWORD` con el valor de `.env` y ejecutar:

```sh
./mvnw -DskipTests package
java -jar infrastructure/target/soluciones.jar
```

Si PostgreSQL usa otro puerto, exportar `DB_URL=jdbc:postgresql://localhost:PUERTO/soluciones`. En otra terminal, desde `frontend`, ejecutar `npm ci` y `npm start`. El proxy local espera backend en 8080; no ejecutar simultáneamente los contenedores de frontend/backend en los mismos puertos.

## Organización y alcance

- `backend/domain`: reglas y entidades futuras; independiente de frameworks.
- `backend/application`: futuros casos de uso y puertos; depende de dominio.
- `backend/infrastructure`: arranque Spring Boot, JDBC, Flyway, salud y adaptadores futuros.
- `frontend`: aplicación Angular inicial, sin flujos de negocio.
- `compose.yaml`: servicios reales y volumen PostgreSQL.
- `openspec/changes/gestion-solicitudes-soluciones`: reglas, decisiones y tareas.

Flyway prepara el esquema `soluciones` y su historial. Las tablas funcionales y los datos de actores pertenecen a la tarea 2.1. No hay usuarios ni autenticación en esta entrega. No están implementados todavía el bloqueo de coordinación ni los endpoints de negocio: el diseño describe su implementación futura.

El diseño aprobado serializa las escrituras mediante un bloqueo común y procesa cada lote en una transacción. Esto limita rendimiento y puede hacer esperar a registros y resoluciones durante lotes grandes. Aumentar réplicas del backend no elimina ese cuello de botella. Las pruebas iniciales no demuestran todavía las invariantes funcionales de concurrencia.

## Validar planificación

Con OpenSpec disponible en PATH:

```sh
openspec validate gestion-solicitudes-soluciones --strict
git diff --check
```

Consultar `AGENTS.md` antes de modificar el proyecto. La validación documental no sustituye las pruebas de aplicación.
