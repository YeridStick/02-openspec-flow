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

El proyecto contiene únicamente la estructura inicial de OpenSpec; no se encontró aplicación, pruebas ni contratos existentes. Este cambio agrega planificación para una aplicación nueva, sin modificar código ni incorporar dependencias. El diseño será conceptual: frontend, backend, persistencia, arquitectura y mecanismos técnicos de concurrencia requieren definición y aprobación posterior del desarrollador antes de implementar.
