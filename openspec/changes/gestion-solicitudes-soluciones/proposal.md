# Proposal

## Why

Soluciones necesita centralizar las solicitudes de sus clientes para mejorar su seguimiento y distribuir la carga de trabajo entre empleados disponibles. La primera versión demostrará una asignación determinista que respete categorías, capacidad y ausencia de duplicados.

## What Changes

- Registrar solicitudes con cliente, categoría, asunto y descripción obligatorios.
- Consultar solicitudes y filtrar por estado, categoría y empleado responsable; distinguir las pendientes de asignación.
- Asignar al empleado activo, habilitado para la categoría y con capacidad que tenga menor carga abierta total; desempatar por identificador menor.
- Crear una única tarea por solicitud asignada y ninguna cuando no haya disponibilidad.
- Permitir al empleado avanzar sus tareas de PENDIENTE a EN_PROGRESO y luego a RESUELTA, liberando capacidad al resolver.
- Permitir al coordinador reintentar todas las pendientes por fecha de creación e identificador ascendentes, continuando aunque una no pueda asignarse.
- Demostrar los actores con datos precargados y selector de rol/persona, sin autenticación real.
- Excluir administración de empleados, notificaciones externas, reintentos automáticos, reapertura, horarios, ausencias y vencimientos de capacitaciones.

## Capabilities

### New Capabilities

- `solicitudes`: registro, consulta, filtros y visibilidad por actor de demostración.
- `asignacion-solicitudes`: elegibilidad, reparto, capacidad, reintento y garantías de concurrencia.
- `gestion-tareas`: consulta del empleado y transiciones de las tareas que determinan su carga activa.

### Modified Capabilities

Ninguna: no existen especificaciones previas.

## Impact

El cambio incorpora una aplicación nueva con Angular 22, Spring Boot 4.1.2 sobre Java 21, PostgreSQL 17.11 y arquitectura hexagonal. El acceso a datos será Spring JDBC con JdbcClient; Flyway gestionará migraciones SQL. Maven Wrapper y Angular CLI con npm permitirán construcciones reproducibles y Docker Compose el desarrollo local.

Los contratos REST se definirán mediante OpenAPI. Se usarán identificadores BIGINT, fechas UTC, carga calculada desde tareas y actores de demostración sin autenticación real. La coordinación transaccional serializará las operaciones de asignación/carga y cada reintento será una transacción para el lote completo. El diseño documenta las limitaciones de escalabilidad.

Se incluyen pruebas JUnit, Testcontainers y herramientas Angular. La autorización actual de implementación se limita a las bases de las tareas 1.4–1.6; las capacidades funcionales posteriores conservan su alcance, pero no se implementan en esta entrega. No se autoriza despliegue productivo.
