# ADR-001: Inversión de dependencias mediante interfaces

- **Estado:** Aceptado
- **Contexto:** El módulo base (Práctica 2) tenía una God Class (`OrdenServicioProcessor`), acoplamiento con `new`, métodos estáticos en `RucValidator` y filtros por cadenas (H-01 a H-05).
- **Alternativas:** (1) modularizar en submétodos conservando clases concretas; (2) invertir dependencias con interfaces inyectadas por constructor; (3) arquitectura reactiva por eventos con colas.
- **Decisión:** Alternativa 2. `OrdenServicioProcessor` depende de `IDatasetParser`, `IRucValidator`, `IOrdenServicioRepository` e `IMetricasService`; las implementaciones se crean solo en la raíz de composición (`web/Main.java`).
- **Consecuencias (+):** un nuevo formato de entrada = 1 clase nueva, 0 modificadas (RNF-01); pruebas unitarias con mocks sin archivos ni BD (RNF-02).
- **Consecuencias (−):** más interfaces que mantener y gestión manual de la inyección y del ciclo de vida.
