# Spec Delta

## Purpose

Permitir a cada empleado consultar y avanzar sus tareas asignadas, haciendo que la resolución libere capacidad para nuevas asignaciones.

## ADDED Requirements

### Requirement: Consulta y gestión del responsable
El sistema SHALL permitir al empleado seleccionado consultar y gestionar únicamente sus tareas, identificando la solicitud asociada y su estado. El selector de demostración no constituye autenticación real.

#### Scenario: Tareas propias
- **WHEN** se selecciona al empleado A
- **THEN** se muestran sus tareas con la solicitud asociada y su estado, sin tareas de B.

#### Scenario: Gestión de una tarea ajena
- **WHEN** el empleado A intenta cambiar el estado de una tarea asignada a B
- **THEN** no se modifica esa tarea ni su carga.

### Requirement: Transiciones secuenciales sin reapertura
Una tarea creada por asignación SHALL comenzar en PENDIENTE y permitir únicamente PENDIENTE → EN_PROGRESO → RESUELTA como cambios de estado. El sistema SHALL rechazar saltos, retrocesos y reaperturas sin modificar la tarea.

#### Scenario: Inicio de trabajo
- **WHEN** el empleado responsable inicia una tarea PENDIENTE
- **THEN** pasa a EN_PROGRESO y sigue contando como una tarea abierta.

#### Scenario: Resolución
- **WHEN** el empleado responsable resuelve una tarea EN_PROGRESO
- **THEN** pasa a RESUELTA y deja de contar como carga activa, conservando su asociación con solicitud y empleado.

#### Scenario: Salto inválido
- **WHEN** se intenta pasar directamente de PENDIENTE a RESUELTA
- **THEN** se rechaza el cambio y la tarea permanece PENDIENTE.

#### Scenario: Retroceso o reapertura
- **WHEN** se intenta pasar de EN_PROGRESO a PENDIENTE o de RESUELTA a un estado abierto
- **THEN** se rechaza el cambio y se conserva el estado anterior.

### Requirement: Liberación consistente de capacidad
Resolver una tarea SHALL liberar exactamente su contribución a la carga abierta. Operaciones repetidas o concurrentes SHALL preservar esa carga, la capacidad máxima y las transiciones válidas. La resolución no SHALL crear otra tarea ni iniciar reintentos automáticos.

#### Scenario: Cupo disponible después de resolver
- **WHEN** un empleado con capacidad 3 y tres tareas abiertas resuelve una EN_PROGRESO
- **THEN** su carga pasa a 2 y queda elegible para una nueva asignación de categoría habilitada; las solicitudes pendientes esperan el reintento manual.

#### Scenario: Resolución repetida o concurrente
- **WHEN** se reciben dos operaciones para resolver la misma tarea EN_PROGRESO
- **THEN** queda RESUELTA, su contribución se elimina solo una vez y no se generan cupos adicionales ficticios.

#### Scenario: Resolución concurrente con asignación
- **WHEN** una resolución y una asignación se ejecutan simultáneamente sobre la carga de un empleado
- **THEN** el resultado respeta las transiciones y la capacidad; la asignación solo utiliza el cupo liberado si la resolución ya es efectiva en el orden consistente de operaciones.
