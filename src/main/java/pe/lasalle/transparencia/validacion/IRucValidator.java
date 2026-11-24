package pe.lasalle.transparencia.validacion;
import java.util.Optional;
/** Contrato: devuelve el motivo del rechazo, o vacío si el RUC es válido. */
public interface IRucValidator { Optional<String> motivoInvalido(String ruc); }
