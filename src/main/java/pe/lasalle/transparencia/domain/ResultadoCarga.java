package pe.lasalle.transparencia.domain;
import java.util.List;
public record ResultadoCarga(int leidas, int validas, int descartadas, List<Descarte> descartes) {}
