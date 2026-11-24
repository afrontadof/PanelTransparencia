package pe.lasalle.transparencia.web;
import pe.lasalle.transparencia.OrdenServicioProcessor;
import pe.lasalle.transparencia.domain.*;
import pe.lasalle.transparencia.metricas.*;
import pe.lasalle.transparencia.parser.CsvDatasetParser;
import pe.lasalle.transparencia.repositorio.*;
import pe.lasalle.transparencia.validacion.RucValidator;
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Raíz de composición: único lugar donde se hace 'new' de las implementaciones. API REST del documento. */
public class Main {
    public static void main(String[] args) throws Exception {
        var repo = new InMemoryOrdenServicioRepository();
        var proc = new OrdenServicioProcessor(new CsvDatasetParser(), new RucValidator(), repo, new MetricasGastoService());
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        var s = HttpServer.create(new InetSocketAddress(port), 0);
        s.createContext("/", ex -> estatico(ex));
        s.createContext("/api/datasets/cargar", ex -> safe(ex, () -> {
            if (!ex.getRequestMethod().equals("POST")) { send(ex, 405, "text/plain", "Use POST"); return; }
            send(ex, 200, "application/json", Json.to(proc.procesar(ex.getRequestBody()))); }));
        s.createContext("/api/ordenes", ex -> safe(ex, () -> {
            var q = query(ex); var l = repo.buscar(q.get("acuerdo"), q.get("estado"), q.get("q"));
            int page = Math.max(1, num(q.get("page"), 1)), size = Math.min(200, num(q.get("size"), 10));
            int from = Math.min(l.size(), (page - 1) * size);
            send(ex, 200, "application/json", Json.to(Map.of("total", l.size(), "page", page, "size", size,
                "items", l.subList(from, Math.min(l.size(), from + size))))); }));
        s.createContext("/api/metricas/resumen", ex -> safe(ex, () -> {
            var q = query(ex); var l = repo.buscar(q.get("acuerdo"), q.get("estado"), q.get("q"));
            var m = proc.metricas(); var r = proc.ultimoResultado();
            send(ex, 200, "application/json", Json.to(Map.of("gastoTotal", m.gastoTotal(l), "ordenes", l.size(),
                "gastoPorEstado", m.gastoPorEstado(l), "rucDescartados", r.descartadas(), "ordenesValidas", r.validas()))); }));
        s.createContext("/api/rankings", ex -> safe(ex, () -> {
            var q = query(ex); var l = repo.buscar(q.get("acuerdo"), q.get("estado"), q.get("q")); int n = num(q.get("top"), 5);
            send(ex, 200, "application/json", Json.to(Map.of("proveedores", proc.metricas().topProveedores(l, n),
                "entidades", proc.metricas().topEntidades(l, n)))); }));
        s.createContext("/api/auditoria/rucs-csv", ex -> safe(ex, () -> {
            var sb = new StringBuilder("fila,orden,ruc,motivo\n");
            proc.ultimoResultado().descartes().forEach(d -> sb.append(d.fila()).append(',').append(d.orden()).append(',')
                .append(d.ruc()).append(",\"").append(d.motivo()).append("\"\n"));
            download(ex, "rucs_descartados.csv", sb.toString()); }));
        s.createContext("/api/reportes/exportar", ex -> safe(ex, () -> {
            var q = query(ex); var l = repo.buscar(q.get("acuerdo"), q.get("estado"), q.get("q")); var m = proc.metricas();
            if ("pdf".equalsIgnoreCase(q.get("formato"))) { send(ex, 501, "text/plain", "RF-13 (PDF) pendiente de implementar"); return; }
            var sb = new StringBuilder("tipo,nombre,monto_soles\n");
            m.topProveedores(l, 10).forEach(r -> sb.append("proveedor,\"").append(r.nombre()).append("\",").append(r.monto().toPlainString()).append('\n'));
            m.topEntidades(l, 10).forEach(r -> sb.append("entidad,\"").append(r.nombre()).append("\",").append(r.monto().toPlainString()).append('\n'));
            m.gastoPorEstado(l).forEach((k, v) -> sb.append("estado,\"").append(k).append("\",").append(v.toPlainString()).append('\n'));
            download(ex, "reporte.csv", sb.toString()); }));
        s.start();
        System.out.println("Panel de Transparencia en http://localhost:" + port);
    }
    private static int num(String s, int d) { try { return Integer.parseInt(s); } catch (Exception e) { return d; } }
    private static Map<String, String> query(HttpExchange ex) {
        Map<String, String> m = new HashMap<>(); String s = ex.getRequestURI().getRawQuery();
        if (s != null) for (String p : s.split("&")) { String[] kv = p.split("=", 2);
            m.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : ""); }
        return m; }
    private static void download(HttpExchange ex, String name, String body) throws IOException {
        ex.getResponseHeaders().add("Content-Disposition", "attachment; filename=" + name);
        send(ex, 200, "text/csv; charset=utf-8", body); }
    private static void send(HttpExchange ex, int code, String type, String body) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type.contains("charset") ? type : type + "; charset=utf-8");
        ex.sendResponseHeaders(code, b.length); try (var o = ex.getResponseBody()) { o.write(b); } }
    private static void estatico(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath(); boolean csv = path.equals("/muestra.csv");
        try (var in = Main.class.getResourceAsStream(csv ? "/static/muestra.csv" : "/static/index.html")) {
            if (in == null) { send(ex, 404, "text/plain", "No encontrado"); return; }
            send(ex, 200, csv ? "text/csv" : "text/html", new String(in.readAllBytes(), StandardCharsets.UTF_8)); } }
    interface Op { void run() throws Exception; }
    private static void safe(HttpExchange ex, Op op) throws IOException {
        try { op.run(); } catch (Exception e) { send(ex, 500, "application/json", Json.to(Map.of("error", String.valueOf(e.getMessage())))); } }
}
