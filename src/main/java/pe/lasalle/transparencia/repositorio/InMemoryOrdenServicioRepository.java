package pe.lasalle.transparencia.repositorio;
import pe.lasalle.transparencia.domain.OrdenServicio;
import java.util.*;
/** Implementación en memoria del contrato. RepositorioJdbc (PostgreSQL) se agrega como otra clase, sin tocar el procesador. */
public class InMemoryOrdenServicioRepository implements IOrdenServicioRepository {
    private volatile List<OrdenServicio> datos = List.of();
    @Override public void limpiar() { datos = List.of(); }
    @Override public void guardarTodas(List<OrdenServicio> o) { var n = new ArrayList<>(datos); n.addAll(o); datos = n; }
    @Override public List<OrdenServicio> buscar(String acuerdo, String estado, String q) {
        String t = q == null ? "" : q.toLowerCase();
        return datos.stream()
            .filter(o -> acuerdo == null || acuerdo.isBlank() || o.acuerdoMarco().equalsIgnoreCase(acuerdo))
            .filter(o -> estado == null || estado.isBlank() || o.estado().equalsIgnoreCase(estado))
            .filter(o -> t.isBlank() || (o.ruc() + o.entidad() + o.proveedor() + o.orden()).toLowerCase().contains(t))
            .toList();
    }
}
