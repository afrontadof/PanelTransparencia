package pe.lasalle.transparencia.parser;
import java.io.InputStream;
import java.util.*;
/** Contrato: convierte un archivo de entrada en filas crudas (columna -> valor). */
public interface IDatasetParser { List<Map<String, String>> parse(InputStream in) throws Exception; }
