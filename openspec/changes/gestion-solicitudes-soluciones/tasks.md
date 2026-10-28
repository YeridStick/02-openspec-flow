# Tasks

Plan incremental pendiente de revisión. Ninguna tarea se ha ejecutado. Las tareas de implementación dependen de la aprobación funcional y de la definición técnica reservada al desarrollador; este documento no elige tecnologías ni autoriza implementar.

## 1. Definición técnica por el desarrollador

- [ ] 1.1 Registrar la aprobación de propuesta, especificaciones y diseño conceptual; verificar que la revisión cubra las tres capacidades y sus escenarios.
- [ ] 1.2 Incorporar las decisiones del desarrollador sobre frontend, backend, persistencia, arquitectura y concurrencia, sin asumirlas; verificar que el diseño actualizado explique cómo satisface cada invariante y cuente con su aprobación antes de iniciar código.
- [ ] 1.3 Concretar contratos de operaciones, orden de identificadores y fechas, y recuperación ante fallos técnicos según las decisiones aprobadas; verificar que no cambien comportamiento sin consulta y ajustar el desglose técnico de estas tareas.

## 2. Modelo y contexto de demostración

- [ ] 2.1 Implementar el modelo conceptual y datos precargados de clientes, categorías, empleados y habilitaciones, con capacidad inicial 3 y configurable por empleado; verificar relaciones y preparar casos reproducibles de inactividad, categoría no habilitada, empate y capacidad distinta.
- [ ] 2.2 Implementar selector de rol y persona y delimitar la consulta por actor; verificar los escenarios de visibilidad de `solicitudes` y `gestion-tareas`, incluido cambio de persona y acceso ajeno.
- [ ] 2.3 Documentar cómo iniciar la demostración y qué personas/categorías incluye; verificar que las instrucciones permitan seleccionar cada actor sin autenticación ni administración de empleados.

## 3. Registro y asignación inicial

- [ ] 3.1 Implementar el cálculo de carga global, elegibilidad y selección por carga/identificador; verificar con pruebas los escenarios de capacidad, categorías distintas, tareas resueltas y desempate de `asignacion-solicitudes`.
- [ ] 3.2 Implementar la confirmación de asignación con el mecanismo de concurrencia aprobado y la unicidad de tarea por solicitud; verificar que dos registros compitiendo por el último cupo no excedan capacidad y que un fallo no deje asignación parcial.
- [ ] 3.3 Implementar el formulario y la operación de registro con validación de campos y referencias; verificar los escenarios de registro de `solicitudes` y los resultados con y sin disponibilidad de `asignacion-solicitudes`.
- [ ] 3.4 Documentar las garantías de asignación y su mecanismo aprobado; verificar que la documentación explique la diferencia entre falta de disponibilidad y fallo técnico sin prometer reintentos automáticos.

## 4. Consulta y seguimiento

- [ ] 4.1 Implementar listado y consulta de solicitudes con datos, situación de asignación, responsable y estado de tarea; verificar que una pendiente no muestre tarea ni responsable y que una asignada muestre los correctos.
- [ ] 4.2 Implementar filtros combinables por estado de asignación, categoría y responsable; verificar coincidencias, resultado vacío, retirada de filtros y conservación de los límites del actor en `solicitudes`.

## 5. Trabajo del empleado y liberación de capacidad

- [ ] 5.1 Implementar consulta de tareas propias y cambios PENDIENTE → EN_PROGRESO → RESUELTA; verificar todos los escenarios de transiciones de `gestion-tareas`, incluida tarea ajena, salto, retroceso y reapertura rechazados.
- [ ] 5.2 Integrar la resolución con la carga efectiva; verificar liberación de un único cupo, resolución repetida/concurrente y resolución simultánea con asignación, conservando la tarea y el responsable.
- [ ] 5.3 Documentar el recorrido del empleado en la demostración; verificar que el ejemplo permita observar carga constante al iniciar y menor al resolver, sin asignación automática de pendientes.

## 6. Reintento manual del coordinador

- [ ] 6.1 Implementar la acción para todas las pendientes y su orden por fecha/identificador; verificar antigüedad, empate, continuación tras falta de disponibilidad y carga actualizada con los escenarios de `asignacion-solicitudes`.
- [ ] 6.2 Integrar reintentos con las garantías de unicidad y capacidad; verificar lotes superpuestos, registro simultáneo, ejecución repetida y lote vacío, sin modificar tareas ya asignadas, incluso resueltas.
- [ ] 6.3 Habilitar la acción únicamente en el contexto coordinador y documentar el recorrido manual; verificar que cliente y empleado no puedan ejecutarla y que liberar cupos no la dispare automáticamente.

## 7. Verificación integrada de la demostración

- [ ] 7.1 Recorrer registro → consulta del responsable → inicio → resolución → reintento manual con distintos actores; verificar correspondencia entre solicitud, tarea, responsable, filtros y carga en cada paso, registrando evidencia.
- [ ] 7.2 Ejecutar conjuntamente los escenarios de concurrencia con datos reproducibles y el mecanismo técnico aprobado; verificar al final unicidad de tarea, capacidad por empleado, carga global y ausencia de asignaciones parciales.
- [ ] 7.3 Revisar la cobertura de los escenarios de las tres especificaciones y documentar resultados y limitaciones de la demostración; verificar que no se hayan añadido autenticación real, administración de empleados, notificaciones, reapertura ni reintentos automáticos.
