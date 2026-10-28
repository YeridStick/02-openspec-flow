# Spec Delta

## Purpose

Distribuir solicitudes de forma determinista entre empleados elegibles, respetando la capacidad y garantizando una única tarea por solicitud incluso bajo concurrencia.

## ADDED Requirements

### Requirement: Asignación al registrar
El sistema SHALL intentar asignar cada solicitud válida al registrarla, usando empleados activos, habilitados para su categoría y con carga abierta inferior a su capacidad máxima. No SHALL considerar horarios, ausencias ni vencimientos de capacitaciones.

#### Scenario: Elegibilidad conjunta
- **WHEN** se registra una solicitud de categoría C y existen un empleado inactivo habilitado, otro activo no habilitado, otro activo habilitado lleno y otro activo habilitado con cupo
- **THEN** únicamente el último es elegible y recibe la tarea.

### Requirement: Capacidad y carga global
Cada empleado SHALL tener una capacidad máxima configurable, inicialmente 3 en la demostración. La carga SHALL contar sus tareas PENDIENTE y EN_PROGRESO de todas las categorías, excluyendo RESUELTA. Ninguna asignación SHALL superar la capacidad configurada.

#### Scenario: Carga de categorías distintas
- **WHEN** un empleado con capacidad 3 tiene dos tareas PENDIENTE de A y una EN_PROGRESO de B
- **THEN** su carga es 3 y no puede recibir una solicitud adicional de ninguna categoría.

#### Scenario: Tareas resueltas excluidas
- **WHEN** un empleado con capacidad 3 tiene dos tareas abiertas y cinco RESUELTA
- **THEN** su carga es 2 y puede recibir una tarea adicional si cumple los demás criterios.

#### Scenario: Capacidad distinta de la inicial
- **WHEN** los datos configurados dan capacidad 1 a un empleado con una tarea abierta
- **THEN** no es elegible aunque el valor inicial de demostración sea 3.

### Requirement: Selección por carga e identificador
El sistema SHALL seleccionar al empleado elegible con menor carga abierta global y, en caso de empate, el de identificador menor.

#### Scenario: Menor carga antes que identificador
- **WHEN** los empleados elegibles 10 y 20 tienen respectivamente dos y una tareas abiertas
- **THEN** la solicitud se asigna al empleado 20.

#### Scenario: Empate de carga
- **WHEN** los empleados elegibles 10 y 20 tienen ambos una tarea abierta
- **THEN** la solicitud se asigna al empleado 10.

### Requirement: Resultado de la asignación
Una asignación exitosa SHALL crear exactamente una tarea PENDIENTE vinculada a la solicitud y al empleado seleccionado. Si no hay empleados elegibles, la solicitud SHALL permanecer registrada en PENDIENTE_ASIGNACION, sin tarea ni responsable.

#### Scenario: Asignación exitosa
- **WHEN** existe un empleado elegible y se completa la asignación
- **THEN** la solicitud tiene una única tarea PENDIENTE y muestra a ese empleado como responsable.

#### Scenario: Sin empleados habilitados activos
- **WHEN** no existe ningún empleado activo habilitado para la categoría
- **THEN** la solicitud queda PENDIENTE_ASIGNACION sin tarea ni responsable.

#### Scenario: Todos al límite
- **WHEN** todos los empleados activos habilitados tienen carga igual a su capacidad
- **THEN** la solicitud queda PENDIENTE_ASIGNACION sin tarea ni responsable.

### Requirement: Reintento manual ordenado
El sistema SHALL permitir exclusivamente al coordinador ejecutar el reintento de todas las solicitudes pendientes, por fecha de creación ascendente e identificador menor ante fechas iguales. SHALL continuar tras una solicitud sin disponibilidad y evaluar la carga actualizada para cada asignación. No SHALL iniciar reintentos automáticos.

#### Scenario: Antigüedad y desempate
- **WHEN** hay tres solicitudes pendientes elegibles: 30 de ayer y 10 y 20 con la misma fecha de hoy, y existe un solo cupo
- **THEN** recibe el cupo la 30; si se ejecuta otro reintento con un nuevo cupo, lo recibe la 10 antes que la 20.

#### Scenario: Continuación tras falta de disponibilidad
- **WHEN** la solicitud más antigua es de A sin empleado elegible y la siguiente es de B con un empleado elegible
- **THEN** la primera permanece pendiente y la segunda se asigna.

#### Scenario: Carga actualizada durante el lote
- **WHEN** dos solicitudes pendientes requieren un empleado que tiene un único cupo
- **THEN** solo se asigna la primera según el orden definido y la otra permanece pendiente.

#### Scenario: Solo acción manual del coordinador
- **WHEN** se libera capacidad sin que el coordinador ejecute el reintento
- **THEN** las solicitudes pendientes no se reasignan automáticamente y cliente y empleado no pueden ejecutar la acción.

#### Scenario: Lote vacío o repetido
- **WHEN** el coordinador ejecuta el reintento sin solicitudes pendientes o repite una ejecución ya completada
- **THEN** no se crean tareas para solicitudes ya asignadas ni se alteran sus responsables.

### Requirement: Consistencia ante concurrencia
El sistema SHALL conservar una sola tarea por solicitud y una carga no superior a la capacidad ante registros y reintentos concurrentes. La selección SHALL ser coherente con un orden de asignaciones que reevalúe elegibilidad y carga; no SHALL quedar una asignación parcial visible.

#### Scenario: Dos reintentos de la misma solicitud
- **WHEN** dos ejecuciones intentan asignar simultáneamente la misma solicitud pendiente
- **THEN** al terminar existe como máximo una tarea asociada y un único responsable, sin duplicación de carga.

#### Scenario: Competencia por el último cupo
- **WHEN** un registro y un reintento, o dos registros, compiten por el único cupo de un empleado sin otros elegibles
- **THEN** solo una solicitud obtiene tarea y la otra queda pendiente, sin superar la capacidad.

#### Scenario: Lotes superpuestos
- **WHEN** dos reintentos concurrentes procesan las mismas pendientes
- **THEN** las asignaciones respetan el orden de las pendientes, excluyen las ya asignadas y usan la carga actualizada sin duplicados.

#### Scenario: Fallo durante la asignación
- **WHEN** ocurre un fallo técnico antes de completar la vinculación entre solicitud, tarea y empleado
- **THEN** no se expone una tarea huérfana, una solicitud asignada sin tarea ni una carga consumida sin tarea; no se informa una asignación exitosa.
