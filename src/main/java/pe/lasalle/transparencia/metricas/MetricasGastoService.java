package pe.lasalle.transparencia.metricas;
import pe.lasalle.transparencia.domain.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
public class MetricasGastoService implements IMetricasService {
    private List<Ranking> top(List<OrdenServicio> o, Function<OrdenServicio, String> k, int n) {
        return o.stream().collect(Collectors.groupingBy(k, Collectors.reducing(BigDecimal.ZERO, OrdenServicio::total, BigDecimal::add)))
            .entrySet().stream().map(e -> new Ranking(e.getKey(), e.getValue()))
            .sorted(Comparator.comparing(Ranking::monto).reversed()).limit(n).toList();
    }
    @Override public List<Ranking> topProveedores(List<OrdenServicio> o, int n) {
        return top(o, x -> x.proveedor().isBlank() ? x.ruc() : x.proveedor() + " (RUC " + x.ruc() + ")", n); }
    @Override public List<Ranking> topEntidades(List<OrdenServicio> o, int n) { return top(o, OrdenServicio::entidad, n); }
    @Override public Map<String, BigDecimal> gastoPorEstado(List<OrdenServicio> o) {
        return o.stream().collect(Collectors.groupingBy(OrdenServicio::estado, TreeMap::new,
            Collectors.reducing(BigDecimal.ZERO, OrdenServicio::total, BigDecimal::add))); }
    @Override public BigDecimal gastoTotal(List<OrdenServicio> o) {
        return o.stream().map(OrdenServicio::total).reduce(BigDecimal.ZERO, BigDecimal::add); }
}
