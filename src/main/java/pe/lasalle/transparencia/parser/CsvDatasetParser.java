package pe.lasalle.transparencia.parser;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
/** RF-02: parser CSV intercambiable. Un nuevo formato = una clase nueva que implemente IDatasetParser. */
public class CsvDatasetParser implements IDatasetParser {
    private static final String SPLIT = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    @Override public List<Map<String, String>> parse(InputStream in) throws IOException {
        var br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String h = br.readLine();
        if (h == null) return List.of();
        String[] cols = cortar(h.replace("\uFEFF", ""));
        for (int i = 0; i < cols.length; i++) cols[i] = cols[i].trim().toUpperCase();
        List<Map<String, String>> filas = new ArrayList<>();
        for (String l; (l = br.readLine()) != null; ) {
            if (l.isBlank()) continue;
            String[] v = cortar(l);
            Map<String, String> m = new HashMap<>();
            for (int i = 0; i < cols.length && i < v.length; i++) m.put(cols[i], v[i].trim());
            filas.add(m);
        }
        return filas;
    }
    private static String[] cortar(String l) {
        String[] p = l.split(SPLIT, -1);
        for (int i = 0; i < p.length; i++) p[i] = p[i].replaceAll("^\"|\"$", "");
        return p;
    }
}
