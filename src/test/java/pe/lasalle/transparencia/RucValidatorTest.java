package pe.lasalle.transparencia;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import pe.lasalle.transparencia.validacion.RucValidator;

class RucValidatorTest {
    private final RucValidator v = new RucValidator();
    @Test void rucValidoPasa() { assertTrue(v.motivoInvalido("20131312955").isEmpty()); } // SUNAT
    @Test void longitudIncorrectaSeRechaza() { assertTrue(v.motivoInvalido("2013131295").get().contains("Longitud")); }
    @Test void noNumericoSeRechaza() { assertTrue(v.motivoInvalido("2013131295A").isPresent()); }
    @Test void digitoVerificadorIncorrectoSeRechaza() { assertTrue(v.motivoInvalido("20131312956").get().contains("Módulo 11")); }
    @Test void nuloSeRechaza() { assertTrue(v.motivoInvalido(null).isPresent()); }
}
