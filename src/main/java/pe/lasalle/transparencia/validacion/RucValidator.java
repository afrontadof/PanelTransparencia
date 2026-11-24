package pe.lasalle.transparencia.validacion;
import java.util.Optional;
/** RF-04 / RF-05: 11 dígitos + dígito verificador módulo 11 (SUNAT). Instanciable e inyectable (sin métodos estáticos). */
public class RucValidator implements IRucValidator {
    private static final int[] F = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
    @Override public Optional<String> motivoInvalido(String ruc) {
        if (ruc == null || !ruc.matches("\\d{11}")) return Optional.of("Longitud inválida (debe tener 11 dígitos)");
        int s = 0;
        for (int i = 0; i < 10; i++) s += (ruc.charAt(i) - '0') * F[i];
        int d = 11 - (s % 11);
        if (d == 10) d = 0; else if (d == 11) d = 1;
        return d == ruc.charAt(10) - '0' ? Optional.empty() : Optional.of("Error en dígito verificador - Módulo 11");
    }
}
