# Spec Delta

## Purpose

Centralizar el registro y seguimiento de solicitudes de Soluciones, con consultas delimitadas por el actor seleccionado en la demostración.

## ADDED Requirements

### Requirement: Registro obligatorio
El sistema SHALL permitir al cliente registrar solicitudes con cliente, categoría, asunto y descripción obligatorios, asociadas al cliente seleccionado. Cada solicitud SHALL disponer de identificador único y fecha de creación para su seguimiento y ordenación.

#### Scenario: Registro válido
- **WHEN** un cliente seleccionado registra categoría válida, asunto y descripción no vacíos
- **THEN** se registra la solicitud para ese cliente con identificador y fecha de creación y se intenta su asignación automática.

#### Scenario: Datos incompletos
- **WHEN** falta cualquiera de los cuatro campos obligatorios, o asunto o descripción contienen solo espacios
- **THEN** se informa el campo inválido y no se registra la solicitud ni se crea tarea.

#### Scenario: Referencias inválidas
- **WHEN** el cliente o la categoría no pertenecen a los datos disponibles de demostración
- **THEN** se rechaza el registro sin crear solicitud ni tarea.

### Requirement: Contexto de demostración y visibilidad
El sistema SHALL ofrecer selección de rol y persona precargada sin autenticación real. El cliente SHALL consultar únicamente sus solicitudes; el empleado, sus tareas y la solicitud asociada; el coordinador, todas las solicitudes. Los filtros SHALL conservar estos límites.

#### Scenario: Consulta del cliente
- **WHEN** el cliente A consulta solicitudes, incluso filtrando o solicitando el detalle de una solicitud de B
- **THEN** solo obtiene solicitudes de A y no obtiene el detalle de B.

#### Scenario: Consulta del coordinador
- **WHEN** se selecciona el rol coordinador
- **THEN** se pueden consultar las solicitudes de todos los clientes y acceder al reintento de pendientes.

#### Scenario: Cambio de persona
- **WHEN** se cambia del cliente A al cliente B mediante el selector
- **THEN** la consulta corresponde a B y no conserva resultados de A como propios de B.

### Requirement: Seguimiento de asignación
El sistema SHALL mostrar los datos de la solicitud, su estado de asignación y el empleado responsable cuando exista tarea. Una solicitud sin tarea por falta de disponibilidad SHALL mostrarse como PENDIENTE_ASIGNACION y sin responsable. El estado de la tarea SHALL distinguirse del estado de asignación.

#### Scenario: Solicitud asignada
- **WHEN** se consulta una solicitud cuya asignación tuvo éxito
- **THEN** se identifica como asignada y se muestra su empleado responsable y el estado de la tarea.

#### Scenario: Solicitud pendiente
- **WHEN** se consulta una solicitud que no encontró empleado elegible
- **THEN** se muestra PENDIENTE_ASIGNACION, sin responsable y sin tarea asociada.

### Requirement: Filtrado de solicitudes
El sistema SHALL permitir filtrar las solicitudes visibles por estado de asignación, categoría y empleado responsable. Los filtros aplicados conjuntamente SHALL cumplirse simultáneamente; quitar los filtros SHALL restaurar la consulta del actor seleccionado.

#### Scenario: Filtros combinados
- **WHEN** el coordinador filtra por asignadas, categoría C y empleado E
- **THEN** obtiene únicamente las solicitudes asignadas de C cuyo responsable es E.

#### Scenario: Identificación de pendientes
- **WHEN** se filtra por PENDIENTE_ASIGNACION
- **THEN** se muestran únicamente solicitudes pendientes sin tarea ni responsable.

#### Scenario: Sin coincidencias
- **WHEN** ninguna solicitud visible satisface los filtros, incluido pendiente junto con un responsable concreto
- **THEN** se muestra un resultado vacío sin incorporar solicitudes ajenas al contexto seleccionado.
