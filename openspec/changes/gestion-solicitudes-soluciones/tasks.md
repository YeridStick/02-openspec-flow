# Tasks

Plan para Angular, Spring Boot, PostgreSQL, arquitectura hexagonal y Docker Compose. Ninguna tarea se ha ejecutado. Las tareas 1.1–1.3 deben resolverse antes de cualquier código; este plan no autoriza implementar. Se conserva el alcance completo de las tres especificaciones.

## 1. Definición técnica por el desarrollador

- [ ] 1.1 Registrar la aprobación de propuesta, especificaciones y diseño actualizado; verificar que la revisión cubra las tres capacidades y sus escenarios.
- [ ] 1.2 Documentar y aprobar el mecanismo de concurrencia Spring Boot/PostgreSQL, sus límites transaccionales, aislamiento y recuperación, manteniendo las tecnologías y arquitectura ya aprobadas; verificar que el diseño actualizado explique cómo satisface cada invariante y cuente con su aprobación antes de iniciar código.
- [ ] 1.3 Concretar contratos Angular/backend, identificadores, fechas, representación de carga y asignación, esquema físico, adaptador de persistencia, versiones y herramientas de construcción, pruebas y evolución del esquema; verificar que no cambien comportamiento sin consulta y ajustar el desglose técnico de estas tareas.

- [ ] 1.4 Después de resolver 1.1–1.3, preparar Angular y Spring Boot con dominio, aplicación e infraestructura separados; verificar compilación y que el dominio no dependa de Spring ni PostgreSQL.
- [ ] 1.5 Preparar Docker Compose para frontend, backend y PostgreSQL con conexión, disponibilidad y volumen persistente; verificar arranque, parada y conservación de datos tras reinicio.
- [ ] 1.6 Documentar requisitos, versiones y comandos de construcción, pruebas y arranque local; verificar los comandos desde un entorno limpio y diferenciar reinicio de reinicialización explícita de datos.

## 2. Modelo y contexto de demostración

- [ ] 2.1 Implementar entidades de dominio, puertos y adaptadores PostgreSQL, esquema y datos precargados de clientes, categorías, empleados y habilitaciones, con capacidad inicial 3 y configurable por empleado; verificar relaciones y unicidad con pruebas de integración PostgreSQL y preparar casos reproducibles de inactividad, categoría no habilitada, empate y capacidad distinta.
- [ ] 2.2 Implementar selector Angular de rol/persona y políticas de visibilidad en dominio/aplicación; verificar con pruebas de interfaz y casos de uso los escenarios de visibilidad de `solicitudes` y `gestion-tareas`, incluido cambio de persona y acceso ajeno.
- [ ] 2.3 Documentar cómo iniciar la demostración y qué personas/categorías incluye; verificar que las instrucciones permitan seleccionar cada actor sin autenticación ni administración de empleados.

## 3. Registro y asignación inicial

- [ ] 3.1 Implementar en el dominio el cálculo de carga global, elegibilidad y selección por carga/identificador; verificar con pruebas unitarias sin infraestructura los escenarios de capacidad, categorías distintas, tareas resueltas y desempate de `asignacion-solicitudes`.
- [ ] 3.2 Implementar el caso de uso Spring Boot y adaptador PostgreSQL para confirmar la asignación con el mecanismo de concurrencia aprobado y la unicidad de tarea por solicitud; verificar mediante pruebas concurrentes sobre PostgreSQL real que dos registros compitiendo por el último cupo no excedan capacidad y que un fallo no deje asignación parcial.
- [ ] 3.3 Implementar formulario Angular y operación Spring Boot, delegando la validación de negocio al dominio; verificar con pruebas de formulario, aplicación e integración los escenarios de registro de `solicitudes` y los resultados con y sin disponibilidad de `asignacion-solicitudes`.
- [ ] 3.4 Documentar las garantías de asignación y su mecanismo aprobado; verificar que la documentación explique la diferencia entre falta de disponibilidad y fallo técnico sin prometer reintentos automáticos.

## 4. Consulta y seguimiento

- [ ] 4.1 Implementar listado Angular y consulta de solicitudes mediante puertos y adaptadores PostgreSQL con datos, situación de asignación, responsable y estado de tarea; verificar mediante pruebas de consulta e interfaz que una pendiente no muestre tarea ni responsable y que una asignada muestre los correctos.
- [ ] 4.2 Implementar en Angular y consultas del backend filtros combinables por estado de asignación, categoría y responsable; verificar con pruebas automatizadas coincidencias, resultado vacío, retirada de filtros y conservación de los límites del actor en `solicitudes`.

## 5. Trabajo del empleado y liberación de capacidad

- [ ] 5.1 Implementar consulta y acciones Angular, casos de uso Spring Boot y transiciones del dominio para tareas propias con cambios PENDIENTE → EN_PROGRESO → RESUELTA; verificar con pruebas de dominio, aplicación e interfaz todos los escenarios de transiciones de `gestion-tareas`, incluida tarea ajena, salto, retroceso y reapertura rechazados.
- [ ] 5.2 Integrar la resolución con la carga efectiva en PostgreSQL; verificar con pruebas concurrentes de integración liberación de un único cupo, resolución repetida/concurrente y resolución simultánea con asignación, conservando la tarea y el responsable.
- [ ] 5.3 Documentar el recorrido del empleado en la demostración; verificar que el ejemplo permita observar carga constante al iniciar y menor al resolver, sin asignación automática de pendientes.

## 6. Reintento manual del coordinador

- [ ] 6.1 Implementar el caso de uso de reintento con puertos PostgreSQL y políticas del dominio para todas las pendientes y su orden por fecha/identificador; verificar con pruebas de aplicación e integración antigüedad, empate, continuación tras falta de disponibilidad y carga actualizada con los escenarios de `asignacion-solicitudes`.
- [ ] 6.2 Integrar reintentos con las garantías de unicidad y capacidad; verificar con pruebas concurrentes sobre PostgreSQL lotes superpuestos, registro simultáneo, ejecución repetida y lote vacío, sin modificar tareas ya asignadas, incluso resueltas.
- [ ] 6.3 Habilitar la acción Angular únicamente en el contexto coordinador y comprobar esa restricción también en dominio/aplicación y documentar el recorrido manual; verificar con pruebas de interfaz y casos de uso que cliente y empleado no puedan ejecutarla y que liberar cupos no la dispare automáticamente.

## 7. Verificación integrada de la demostración

- [ ] 7.1 Automatizar en Docker Compose el recorrido registro → consulta del responsable → inicio → resolución → reintento manual con distintos actores; verificar correspondencia entre solicitud, tarea, responsable, filtros y carga en cada paso, registrando evidencia.
- [ ] 7.2 Ejecutar conjuntamente los escenarios de concurrencia sobre PostgreSQL real con datos reproducibles y el mecanismo técnico aprobado; verificar al final unicidad de tarea, capacidad por empleado, carga global y ausencia de asignaciones parciales.
- [ ] 7.3 Revisar la cobertura de los escenarios de las tres especificaciones y documentar resultados y limitaciones de la demostración; verificar que no se hayan añadido autenticación real, administración de empleados, notificaciones, reapertura ni reintentos automáticos.
