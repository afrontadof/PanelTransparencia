package pe.lasalle.transparencia;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import java.util.*;
import org.junit.jupiter.api.Test;
import pe.lasalle.transparencia.metricas.IMetricasService;
import pe.lasalle.transparencia.parser.IDatasetParser;
import pe.lasalle.transparencia.repositorio.IOrdenServicioRepository;
import pe.lasalle.transparencia.validacion.IRucValidator;

/** RNF-02: el procesador se prueba con dobles, sin archivos ni base de datos. */
class OrdenServicioProcessorTest {
    private Map<String, String> fila(String ruc, String total) {
        return Map.of("ORDEN_ELECTRONICA", "OC-1", "TOTAL", total, "ESTADO_ORDEN_ELECTRONICA", "Pagada",
                      "ACUERDO_MARCO", "AM", "RUC_PROVEEDOR", ruc);
    }
    @Test void descartaRucInvalidoYPersisteSoloValidas() throws Exception {
        var parser = mock(IDatasetParser.class); var val = mock(IRucValidator.class); var repo = mock(IOrdenServicioRepository.class);
        when(parser.parse(any())).thenReturn(List.of(fila("OK", "100"), fila("MAL", "50"), fila("OK", "x")));
        when(val.motivoInvalido("OK")).thenReturn(Optional.empty());
        when(val.motivoInvalido("MAL")).thenReturn(Optional.of("Longitud inválida"));
        var p = new OrdenServicioProcessor(parser, val, repo, mock(IMetricasService.class));
        var r = p.procesar(new ByteArrayInputStream(new byte[0]));
        assertEquals(3, r.leidas()); assertEquals(1, r.validas()); assertEquals(2, r.descartadas());
        verify(repo).guardarTodas(argThat(l -> l.size() == 1));
    }
}
