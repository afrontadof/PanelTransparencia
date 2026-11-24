package pe.lasalle.transparencia.metricas;
import pe.lasalle.transparencia.domain.*;
import java.math.BigDecimal;
import java.util.*;
public interface IMetricasService {
    List<Ranking> topProveedores(List<OrdenServicio> o, int n);   // RF-08
    List<Ranking> topEntidades(List<OrdenServicio> o, int n);     // RF-09
    Map<String, BigDecimal> gastoPorEstado(List<OrdenServicio> o);// RF-10
    BigDecimal gastoTotal(List<OrdenServicio> o);
}
