package pe.lasalle.transparencia.repositorio;
import pe.lasalle.transparencia.domain.OrdenServicio;
import java.util.List;
public interface IOrdenServicioRepository {
    void limpiar();
    void guardarTodas(List<OrdenServicio> ordenes);
    List<OrdenServicio> buscar(String acuerdo, String estado, String q);
}
