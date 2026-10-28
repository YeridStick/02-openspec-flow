# Design

## Context

Ver [proposal.md](proposal.md) para la motivación. La inspección encontró únicamente OpenSpec inicializado, sin aplicación ni especificaciones previas. Las decisiones funcionales fueron confirmadas por el usuario. La configuración del proyecto aprueba Angular, Spring Boot, PostgreSQL, arquitectura hexagonal y desarrollo local con Docker Compose. Este documento incorpora esas decisiones; no autoriza iniciar implementación. La referencia a tecnologías pendientes en Impact de proposal.md corresponde al estado anterior a esta configuración y queda pendiente de reconciliar fuera del alcance de esta edición.

## Goals / Non-Goals

**Goals:** precisar relaciones de datos, estados, flujos y condiciones de consistencia suficientes para verificar las reglas de negocio, asignando responsabilidades a las capas de la arquitectura aprobada y definiendo la estrategia de verificación.

**Non-Goals:** cambiar las tecnologías aprobadas, seleccionar bibliotecas adicionales sin revisión, autorizar despliegue productivo, definir administración de empleados o modificar las reglas de negocio. Los mecanismos de concurrencia y contratos aún no definidos se mantienen como decisiones bloqueantes antes de implementar.

## Decisions

### Arquitectura y responsabilidades

Angular implementará la presentación: selector de rol/persona, formulario de registro, consultas, filtros y acciones del empleado y coordinador. Mostrará los resultados del backend; la validación visual no sustituye las reglas del dominio.

El backend Spring Boot se organizará con dependencias hacia el interior:

- **Dominio:** entidades y políticas de validación, visibilidad por actor, elegibilidad, carga global, desempate, capacidad y transiciones. No dependerá de Angular, Spring ni de detalles de PostgreSQL.
- **Aplicación:** casos de uso de registro, consulta, avance de tarea y reintento. Coordinará las políticas del dominio y los puertos necesarios para persistencia, consulta, identificación, tiempo y ejecución consistente.
- **Infraestructura:** adaptadores de entrada para las operaciones expuestas al frontend, adaptadores de persistencia PostgreSQL y configuración Spring Boot. Aquí se materializarán las garantías técnicas de consistencia; la política de reparto permanecerá en el dominio.

Los puertos pertenecen al interior y los adaptadores los implementan. No se colocará la selección del responsable en controladores, componentes Angular ni procedimientos de base de datos. Las consultas podrán filtrar en PostgreSQL, pero deberán respetar los límites de actor establecidos por el dominio y aplicados por los casos de uso.

Esta separación responde a la arquitectura elegida por el desarrollador y permite probar las reglas sin arrancar infraestructura. No se reconsideran otros frameworks ni bases de datos. Distribuir las reglas entre interfaz y persistencia dificultaría verificar una única política de negocio.

### Persistencia y entorno local

PostgreSQL persistirá las entidades y relaciones del modelo conceptual. El diseño físico deberá incluir integridad referencial y unicidad de la relación tarea-solicitud, además de la coordinación necesaria para capacidad y selección determinista. Una restricción de unicidad por sí sola no resuelve la competencia por cupos.

Docker Compose coordinará frontend, backend y PostgreSQL para la demostración local, con configuración de conexión, comprobaciones de disponibilidad y almacenamiento persistente de la base. Se documentarán arranque, parada y reinicialización explícita de los datos de demostración. El arranque ordinario no deberá borrar datos.

Quedan por concretar el adaptador de persistencia, las versiones y herramientas de construcción, y el mecanismo de evolución del esquema. No se incorporan ORM, bibliotecas de interfaz ni herramientas de migración como decisiones implícitas.

### Pruebas automatizadas

- Pruebas unitarias del dominio para registro, visibilidad, reparto, carga y transiciones, sin infraestructura.
- Pruebas de aplicación con puertos sustituidos para comprobar coordinación, permisos del contexto de demostración y manejo de errores.
- Pruebas de integración con PostgreSQL real para relaciones, unicidad, atomicidad y escenarios concurrentes; una base en memoria no sustituye estas verificaciones.
- Pruebas Angular de formularios, filtros, cambio de persona y presentación de resultados.
- Pruebas integradas del recorrido completo sobre el entorno local.

Las herramientas concretas se definirán antes de implementar. Cada grupo de tareas incorpora sus pruebas y documentación; la verificación final comprueba la integración.

### Modelo conceptual

Los campos siguientes describen información necesaria, no tablas físicas ni contratos de API.

| Entidad | Información mínima | Relaciones y restricciones |
| --- | --- | --- |
| Cliente | Identificador y nombre de demostración | Tiene múltiples solicitudes; datos precargados |
| Categoría | Identificador y nombre | Clasifica solicitudes y habilita empleados; datos precargados |
| Empleado | Identificador, nombre, activo, capacidad máxima | Habilitado en cero o más categorías; capacidad inicial 3, configurable en datos, sin pantalla administrativa |
| Habilitación | Empleado y categoría | Expresa capacitación vigente para esta versión, sin fechas ni vencimientos |
| Solicitud | Identificador, cliente, categoría, asunto, descripción, fecha de creación, situación de asignación | Exactamente un cliente y una categoría; cero o una tarea |
| Tarea | Identificador, solicitud, empleado responsable, estado | Exactamente una solicitud y un responsable; la solicitud no puede tener otra tarea durante este flujo |
| Contexto de demostración | Rol y persona seleccionada | No constituye una cuenta autenticada; coordinador es un rol de demostración |

La situación de asignación y la carga pueden almacenarse o derivarse: esa decisión técnica queda pendiente. En cualquier caso deben coincidir con las tareas efectivas. El responsable se obtiene de la tarea asociada, evitando discrepancias entre dos responsables conceptuales.

Convenciones documentales menores: se denomina ASIGNADA a la situación opuesta a PENDIENTE_ASIGNACION y se usan identificadores numéricos en los ejemplos. Su representación técnica está pendiente, pero debe preservar el orden de «identificador menor» acordado. No se agregan prioridades, plazos ni estados de cancelación.

### Estados y transiciones

| Objeto | Estado de origen | Evento | Resultado |
| --- | --- | --- | --- |
| Solicitud nueva válida | Sin resultado de asignación | Registro con empleado elegible | ASIGNADA, con una tarea PENDIENTE |
| Solicitud nueva válida | Sin resultado de asignación | Registro sin empleado elegible | PENDIENTE_ASIGNACION, sin tarea |
| Solicitud | PENDIENTE_ASIGNACION | Reintento manual exitoso | ASIGNADA, con una tarea PENDIENTE |
| Solicitud | PENDIENTE_ASIGNACION | Reintento sin disponibilidad | Permanece pendiente sin tarea |
| Tarea | PENDIENTE | Responsable inicia trabajo | EN_PROGRESO |
| Tarea | EN_PROGRESO | Responsable resuelve | RESUELTA |

ASIGNADA describe la existencia de una asignación, no el avance del trabajo. Resolver la tarea conserva solicitud, tarea y responsable; no vuelve la solicitud a pendiente ni permite una segunda tarea. Los filtros por estado de solicitud se refieren a su situación de asignación; el estado de tarea se presenta por separado. No hay saltos, retrocesos ni reapertura.

### Regla de selección

Para cada empleado E, carga(E) es el número de tareas de E en PENDIENTE o EN_PROGRESO, sumando todas las categorías. Es elegible si está activo, tiene habilitada la categoría de la solicitud y carga(E) < capacidad(E). Se elige el mínimo del par (carga(E), identificador(E)). Las tareas resueltas no contribuyen.

La alternativa de contar únicamente la categoría solicitada incumpliría la carga global confirmada. La selección aleatoria o rotativa incumpliría el desempate acordado. La capacidad inicial no se interpreta como un límite fijo universal: cada empleado conserva su configuración.

### Flujos principales

1. **Registrar:** tomar el cliente seleccionado, validar los campos y referencias, identificar y fechar la solicitud e intentar asignarla. Si hay elegible, vincular una sola tarea PENDIENTE al elegido; si no, conservar la solicitud pendiente sin tarea. Mostrar el resultado.
2. **Consultar:** determinar el contexto de demostración, delimitar los registros visibles y aplicar conjuntamente los filtros indicados. Mostrar datos, situación de asignación, responsable y estado de tarea cuando exista. La selección de persona permite demostrar distintos usuarios, sin prometer seguridad de autenticación.
3. **Gestionar tarea:** comprobar que corresponde al empleado seleccionado y que la transición es la siguiente permitida. Resolver elimina exactamente una contribución a la carga. No dispara asignaciones pendientes.
4. **Reintentar:** el coordinador solicita procesar todas las pendientes. Ordenarlas por (fecha de creación, identificador) ascendente, comprobar que cada una sigue pendiente y evaluar su asignación con la carga actualizada. Continuar si no encuentra elegible. Las ya asignadas conservan su tarea y responsable.

El orden de antigüedad gobierna los reintentos; no se introduce una cola global que impida asignar automáticamente solicitudes nuevas cuando existan pendientes antiguas. No se prescribe cuál operación simultánea gana entre registro y reintento: cada resultado debe corresponder a un orden consistente y respetar las invariantes.

### Invariantes de concurrencia

- Por solicitud, número de tareas entre cero y uno, incluso si la tarea ya está RESUELTA.
- PENDIENTE_ASIGNACION implica ausencia de tarea y responsable; ASIGNADA implica exactamente una tarea y su responsable.
- La carga coincide con el conteo efectivo de tareas abiertas, nunca con intentos fallidos o tareas resueltas.
- Cada asignación confirmada respeta actividad, habilitación, selección determinista y capacidad con respecto a las operaciones precedentes en un orden consistente.
- Seleccionar y confirmar una asignación no permite que otra operación use una lectura obsoleta para superar capacidad o duplicar tareas. Ante competencia debe reevaluarse el resultado antes de confirmarlo.
- Los lotes superpuestos conservan el orden de las pendientes y omiten las que ya obtuvieron tarea; cada éxito afecta la carga de la siguiente selección.
- Resolver repetidamente la misma tarea no libera cupos adicionales. Resolver y asignar concurrentemente conserva una carga válida.
- Una asignación parcialmente completada no se muestra como éxito ni deja relaciones huérfanas.

Estas garantías se implementarán mediante la coordinación de los casos de uso de Spring Boot y el adaptador PostgreSQL. La configuración no selecciona bloqueos, límites transaccionales ni aislamiento: el mecanismo deberá documentarse y aprobarse antes del código, explicando cada invariante y sus pruebas. Elegir PostgreSQL no demuestra por sí solo el cumplimiento.

### Casos excepcionales

| Caso | Resultado requerido |
| --- | --- |
| Datos obligatorios ausentes o referencias inválidas | Rechazar sin solicitud ni tarea nuevas |
| Ningún empleado activo habilitado, o todos llenos | Solicitud pendiente sin tarea |
| Pendiente antigua no asignable | Continuar el lote con la siguiente |
| Consulta sin coincidencias o reintento sin pendientes | Resultado vacío, sin crear datos |
| Reintento de una solicitud ya asignada | Conservar la tarea y el responsable existentes |
| Cambio de tarea ajena o transición inválida | No modificar estado ni carga |
| Fallo técnico durante asignación | No afirmar éxito ni exponer relaciones parciales; no equipararlo silenciosamente a falta de disponibilidad |

Los detalles de recuperación de fallos de persistencia y de respuesta al usuario requieren el diseño técnico posterior; no se introduce una política de reintentos automáticos. Si esa política técnica requiere modificar comportamiento observable, debe consultarse antes de incorporarla.

## Risks / Trade-offs

- [Selector sin autenticación] → Es una demostración; la visibilidad por actor se verifica funcionalmente, sin atribuirle garantías de identidad real.
- [Dos asignaciones consumen el último cupo] → Verificar las invariantes con escenarios concurrentes de registro, reintento y resolución antes de considerar completa la implementación.
- [Conteo duplicado o desactualizado] → Toda representación de carga debe concordar con las tareas abiertas; la resolución repetida no altera ese total otra vez.
- [Categorías sin cobertura] → Las solicitudes pueden permanecer pendientes indefinidamente; esta versión permite seguimiento y reintento manual, sin ampliar el alcance.
- [Diseño técnico pendiente] → No iniciar implementación hasta que el desarrollador defina y apruebe las decisiones reservadas; después ajustar el plan técnico sin alterar los contratos funcionales aprobados.

## Migration Plan

No existe aplicación ni datos previos que migrar. Se prevén datos de demostración para clientes, categorías, empleados, habilitaciones y capacidades, incluyendo casos de empate y falta de disponibilidad. La preparación local usará Docker Compose y PostgreSQL. Antes de implementar se documentarán creación y evolución del esquema, carga reproducible de datos, persistencia entre reinicios y reinicialización explícita; la herramienta concreta sigue pendiente. No se define publicación productiva. Este cambio no autoriza despliegues.

## Decisiones técnicas pendientes de aprobación

Angular, Spring Boot, PostgreSQL, arquitectura hexagonal y Docker Compose ya están aprobados. No deben volver a tratarse como elecciones pendientes.

Antes de iniciar código se deben concretar y revisar:
- Mecanismo de concurrencia, límites transaccionales, aislamiento y recuperación ante fallos, con trazabilidad a todas las invariantes.
- Contratos de operaciones y comunicación Angular/backend, representación y orden de identificadores y fechas.
- Representación de carga y situación de asignación, esquema físico y adaptador de persistencia.
- Versiones compatibles, herramientas de construcción, pruebas y evolución del esquema.

Estas decisiones no están aprobadas por inferencia. Las tareas de definición las mantienen como requisitos previos y deberán refinar las tareas dependientes cuando se resuelvan. No cambian las especificaciones funcionales.
