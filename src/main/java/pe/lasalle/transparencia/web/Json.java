package pe.lasalle.transparencia.web;
import java.lang.reflect.RecordComponent;
import java.util.*;
final class Json {
    static String to(Object o) {
        try {
            if (o == null) return "null";
            if (o instanceof Number || o instanceof Boolean) return o instanceof java.math.BigDecimal b ? b.toPlainString() : o.toString();
            if (o instanceof Map<?, ?> m) { var sj = new StringJoiner(",", "{", "}");
                m.forEach((k, v) -> sj.add(to(String.valueOf(k)) + ":" + to(v))); return sj.toString(); }
            if (o instanceof Iterable<?> it) { var sj = new StringJoiner(",", "[", "]"); it.forEach(x -> sj.add(to(x))); return sj.toString(); }
            if (o.getClass().isRecord()) { var sj = new StringJoiner(",", "{", "}");
                for (RecordComponent c : o.getClass().getRecordComponents())
                    sj.add(to(c.getName()) + ":" + to(c.getAccessor().invoke(o))); return sj.toString(); }
            return "\"" + o.toString().replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "") + "\"";
        } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
    }
}
