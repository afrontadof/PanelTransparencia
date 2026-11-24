package pe.lasalle.transparencia.domain;
import java.math.BigDecimal;
public record OrdenServicio(String orden, BigDecimal total, String estado, String acuerdoMarco,
                            String ruc, String proveedor, String entidad) {}
