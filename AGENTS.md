# Guía para agentes de desarrollo

## Proyecto y alcance

Soluciones centraliza solicitudes de clientes y las distribuye entre empleados según categoría, carga y capacidad. Este repositorio contiene planificación OpenSpec; todavía no contiene una aplicación ejecutable.

Esta guía se aplica a todo el repositorio. La existencia de tareas de implementación no autoriza a ejecutarlas cuando el usuario solicita únicamente revisión o planificación. No cambiar tecnologías, reglas de negocio ni ampliar el alcance sin aprobación explícita.

## Fuentes de referencia

Antes de trabajar, leer:

1. `openspec/config.yaml`: tecnologías y restricciones aprobadas.
2. `openspec/changes/gestion-solicitudes-soluciones/proposal.md`: propósito y alcance.
3. `openspec/changes/gestion-solicitudes-soluciones/specs/*/spec.md`: requisitos y escenarios de aceptación.
4. `openspec/changes/gestion-solicitudes-soluciones/design.md`: responsabilidades y decisiones técnicas, incluidas las pendientes.
5. `openspec/changes/gestion-solicitudes-soluciones/tasks.md`: dependencias y verificaciones del trabajo.

Las especificaciones principales en `openspec/specs/` aún no contienen capacidades consolidadas. No confundir los artefactos de planificación completos con implementación terminada. Si las fuentes discrepan, informar la discrepancia y conservar las decisiones explícitas del usuario; no resolverla cambiando silenciosamente las reglas.

La sección Impact de la propuesta conserva una referencia anterior a tecnologías pendientes. Las tecnologías ya aprobadas están en la configuración y en el diseño actualizado. Los detalles técnicos que ambos dejan abiertos siguen pendientes.

## Tecnologías y arquitectura

- Frontend: Angular.
- Backend: Spring Boot.
- Persistencia: PostgreSQL.
- Arquitectura hexagonal, con separación de dominio, aplicación e infraestructura.
- Desarrollo local: Docker Compose.

El dominio contiene las reglas de validación, visibilidad, asignación, capacidad y transición. No depende de Spring, PostgreSQL ni del frontend. La aplicación coordina casos de uso mediante puertos; infraestructura implementa adaptadores de entrada, persistencia y configuración. Las dependencias apuntan hacia el interior.

Mantener las entidades del dominio independientes de los modelos de persistencia y de transporte. Los controladores y componentes de interfaz no deciden el reparto. Las validaciones del frontend mejoran la interacción, pero no sustituyen las comprobaciones del backend. La persistencia garantiza integridad y concurrencia sin trasladar la política de asignación a procedimientos de base de datos.

No seleccionar implícitamente ORM, herramienta de migraciones, librería visual, protocolo, versiones o herramientas de construcción. Resolver las decisiones pendientes del diseño y las tareas 1.1–1.3 antes de implementar la aplicación. Documentar la decisión, su justificación y cómo se verificará.

## Invariantes de negocio

Las especificaciones son la referencia completa; este resumen no las reemplaza:

- Registrar cliente, categoría, asunto y descripción válidos; rechazar textos vacíos y referencias inexistentes sin crear registros parciales.
- Un empleado es elegible si está activo, habilitado para la categoría y por debajo de su capacidad configurada. El valor inicial de demostración es 3, no un límite universal.
- La carga suma tareas PENDIENTE y EN_PROGRESO de todas las categorías. Seleccionar por menor carga y después menor identificador.
- Cada solicitud tiene como máximo una tarea, incluso después de resolverla. Sin disponibilidad queda pendiente, sin tarea ni responsable.
- Solo permitir PENDIENTE → EN_PROGRESO → RESUELTA. Resolver libera exactamente una contribución a la carga y conserva las asociaciones.
- El coordinador reintenta manualmente todas las pendientes por fecha e identificador ascendentes, usando carga actualizada y continuando si una no tiene disponibilidad.
- Conservar unicidad, capacidad y ausencia de asignaciones parciales ante registros, reintentos y resoluciones concurrentes.
- El cliente consulta sus solicitudes; el empleado gestiona sus tareas; el coordinador consulta todas y ejecuta reintentos. Aplicar estos límites también a detalles y filtros combinados.
- El selector de rol/persona es una demostración, no autenticación real. No añadir administración de empleados, notificaciones, reintentos automáticos, reapertura, horarios, ausencias ni vencimientos de capacitaciones.

## Convenciones de trabajo

- Comunicar y documentar en español; conservar los términos y estados definidos en las especificaciones.
- Revisar `git status --short` antes de editar y preservar los cambios existentes del usuario.
- Hacer cambios acotados al pedido y a la tarea; evitar refactorizaciones ajenas y dependencias innecesarias.
- No modificar artefactos OpenSpec cuando el usuario haya pedido solo validarlos. Para revisiones autorizadas, comprobar coherencia entre propuesta, especificaciones, diseño y tareas.
- Marcar tareas completadas únicamente después de implementar y verificar sus criterios. No marcar tareas por haber escrito documentación sobre trabajo futuro.
- No crear comandos de construcción o pruebas ficticios. Una vez creados los proyectos, documentar y usar sus scripts o wrappers reales.
- No incluir secretos ni credenciales reales en archivos versionados, ejemplos o salidas de herramientas.

## Pruebas y verificación

Cada cambio funcional debe incluir pruebas del comportamiento que modifica:

- Dominio: reglas y transiciones sin framework ni base de datos.
- Aplicación: coordinación, límites por actor y errores mediante sustitutos de los puertos.
- Integración: PostgreSQL real para relaciones, unicidad, atomicidad y concurrencia; una base en memoria no demuestra estas garantías.
- Frontend: formularios, filtros, cambio de persona y presentación de resultados.
- Integración completa: registro, asignación, inicio, resolución y reintento con los distintos actores.

Las pruebas concurrentes deben provocar competencia reproducible, no depender solamente de pausas temporales. Comprobar el estado persistido final: tareas únicas, carga efectiva, capacidad y asociaciones válidas. Distinguir fallos técnicos de falta de disponibilidad.

Para planificación, ejecutar desde la raíz cuando la CLI esté disponible:

```sh
openspec status --change gestion-solicitudes-soluciones --json
openspec validate gestion-solicitudes-soluciones --strict
git diff --check
```

La validación OpenSpec comprueba los artefactos, no demuestra corrección de una implementación ni cierra decisiones pendientes. Si la CLI no está en PATH, localizar la instalación existente antes de instalar o actualizar herramientas. Informar las comprobaciones que no pudieron ejecutarse.

## Infraestructura

Crear `compose.yaml` cuando existan componentes ejecutables y se hayan concretado versiones, comandos y conexiones. Debe ofrecer servicios reales, comprobaciones de disponibilidad y persistencia de PostgreSQL; no declarar rutas de construcción inexistentes ni servicios ficticios.

Documentar arranque, parada y reinicialización explícita. El arranque ordinario no debe eliminar datos. Verificar la configuración con `docker compose config --quiet` y comprobar funcionamiento y persistencia en ejecución antes de dar la infraestructura por terminada.

Terraform no forma parte del alcance local aprobado. Solo incorporarlo cuando se defina un destino de infraestructura que lo requiera. No inferir autorización de despliegue a partir del diseño o de la presencia de archivos de infraestructura.

## Entrega

Indicar archivos modificados, comportamiento o documentación resultante, verificaciones ejecutadas y decisiones pendientes. No afirmar que la aplicación está lista cuando solo se hayan validado artefactos. No crear commits, publicar o desplegar como parte de una revisión salvo que esté incluido en la autorización del usuario.
