package pe.lasalle.transparencia;
import pe.lasalle.transparencia.domain.*;
import pe.lasalle.transparencia.metricas.IMetricasService;
import pe.lasalle.transparencia.parser.IDatasetParser;
import pe.lasalle.transparencia.repositorio.IOrdenServicioRepository;
import pe.lasalle.transparencia.validacion.IRucValidator;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;
/** Coordina la ingesta. Solo conoce interfaces; las implementaciones se inyectan por constructor (ADR-001, RNF-02). */
public class OrdenServicioProcessor {
    private static final Logger LOG = Logger.getLogger(OrdenServicioProcessor.class.getName());
    private final IDatasetParser parser; private final IRucValidator validador;
    private final IOrdenServicioRepository repo; private final IMetricasService metricas;
    private volatile ResultadoCarga ultimo = new ResultadoCarga(0, 0, 0, List.of());

    public OrdenServicioProcessor(IDatasetParser p, IRucValidator v, IOrdenServicioRepository r, IMetricasService m) {
        this.parser = p; this.validador = v; this.repo = r; this.metricas = m;
    }
    public ResultadoCarga procesar(InputStream in) throws Exception {
        var filas = parser.parse(in);
        List<OrdenServicio> ok = new ArrayList<>(); List<Descarte> malas = new ArrayList<>(); int n = 0;
        for (var f : filas) {
            n++;
            String ruc = f.getOrDefault("RUC_PROVEEDOR", "").trim(), ord = f.getOrDefault("ORDEN_ELECTRONICA", "");
            var motivo = validador.motivoInvalido(ruc);                                  // RF-04, RF-05
            if (motivo.isPresent()) {                                                    // RF-06: descartar y registrar
                malas.add(new Descarte(n, ord, ruc, motivo.get()));
                LOG.warning("Fila " + n + " descartada (RUC " + ruc + "): " + motivo.get());
                continue;
            }
            try {
                ok.add(new OrdenServicio(ord, new BigDecimal(f.getOrDefault("TOTAL", "0").replace(",", "")),
                    f.getOrDefault("ESTADO_ORDEN_ELECTRONICA", ""), f.getOrDefault("ACUERDO_MARCO", ""), ruc,
                    f.getOrDefault("PROVEEDOR", ""), f.getOrDefault("ENTIDAD", "")));
            } catch (NumberFormatException e) {                                          // RNF-05: una fila mala no detiene el proceso
                malas.add(new Descarte(n, ord, ruc, "TOTAL no numérico"));
            }
        }
        repo.limpiar(); repo.guardarTodas(ok);                                           // RF-07
        return ultimo = new ResultadoCarga(n, ok.size(), malas.size(), malas);
    }
    public ResultadoCarga ultimoResultado() { return ultimo; }
    public IMetricasService metricas() { return metricas; }
}
