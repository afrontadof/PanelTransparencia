# Panel de Transparencia de Contrataciones del Estado

Prototipo del artículo *"Panel de Transparencia de Contrataciones del Estado: Diseño y Planificación de un Módulo de Ingesta, Validación y Análisis de Órdenes de Servicio"* (Universidad La Salle, Calidad de Software).

Ingesta órdenes de servicio de los Catálogos Electrónicos de Acuerdos Marco (Perú Compras), valida el RUC (11 dígitos + módulo 11 SUNAT), calcula métricas y las expone en una API REST y un panel web.

## Ejecutar
Requisitos: JDK 21 y Maven.
```bash
mvn test                 # pruebas unitarias (JUnit 5 + Mockito)
mvn compile exec:java    # inicia en http://localhost:8080
```
En el panel: **Cargar dataset → Usar dataset de muestra** (o sube `data/muestra.csv`).

## Arquitectura (ADR-001)
```
Main (raíz de composición) ─► OrdenServicioProcessor ─► IDatasetParser ◄─ CsvDatasetParser
                                                      ├► IRucValidator ◄─ RucValidator
                                                      ├► IOrdenServicioRepository ◄─ InMemoryOrdenServicioRepository
                                                      └► IMetricasService ◄─ MetricasGastoService
```
Agregar un formato nuevo (p. ej. Excel o API externa) = crear una clase que implemente `IDatasetParser`; el procesador no cambia.

## API
| Método | Endpoint | Descripción | RF |
|---|---|---|---|
| POST | `/api/datasets/cargar` | Cuerpo = archivo CSV; devuelve leídas/válidas/descartadas y log | RF-01..07 |
| GET | `/api/ordenes?acuerdo&estado&q&page&size` | Órdenes paginadas | RF-10,11,14 |
| GET | `/api/metricas/resumen` | Gasto total y gasto por estado | RF-10,14 |
| GET | `/api/rankings?top=5` | Top N proveedores y entidades | RF-08,09 |
| GET | `/api/reportes/exportar?formato=csv` | Exportación CSV | RF-12 |
| GET | `/api/auditoria/rucs-csv` | Log de RUC descartados | RF-06 |

## Estado de requisitos
Implementado: RF-02..RF-12, RF-14, RNF-01, 02, 05, 07 (sin credenciales en el repo), 08 (Java 21).
Pendiente (hoja de ruta): RF-01 Excel (`ExcelDatasetParser` con Apache POI), RF-13 PDF, `RepositorioJdbc` (PostgreSQL), carga multipart, despliegue en nube (Docker + Cloud Run/Cloud SQL), medición de Fan-Out/complejidad (RNF-03) y rendimiento (RNF-04).

## Estructura
`src/main` código · `src/test` pruebas · `docs/` ADR · `data/muestra.csv` datos de ejemplo (RUC sintéticos, algunos inválidos a propósito).
