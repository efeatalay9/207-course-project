package architecture;

import app.SearchApplication;
import org.junit.jupiter.api.Test;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.spi.ToolProvider;
import static org.junit.jupiter.api.Assertions.*;

// Checks compiled dependencies so a future import cannot silently break the agreed layers.
class ArchitectureTest {
    @Test
    void compiledClassesRespectTheDependencyDirection() throws Exception {
        ToolProvider dependencyTool = ToolProvider.findFirst("jdeps")
                .orElseThrow(() -> new IllegalStateException("Run tests with a full JDK 17+ (jdeps is required)."));
        Path classes = Path.of(SearchApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        StringWriter report = new StringWriter();
        StringWriter errors = new StringWriter();
        int status = dependencyTool.run(new PrintWriter(report), new PrintWriter(errors),
                "--ignore-missing-deps", "-verbose:package", classes.toString());
        assertEquals(0, status, errors.toString());

        // Only the outer composition root may know about both concrete data and UI adapters.
        Map<String, List<String>> allowed = Map.of(
                "domain", List.of("domain", "java.lang", "java.util", "java.time", "java.math"),
                "usecase", List.of("usecase", "domain", "java.lang", "java.util", "java.time", "java.math"),
                "interfaceadapter", List.of("interfaceadapter", "usecase", "domain", "java.lang",
                        "java.util", "java.time", "java.math"),
                "dataaccess", List.of("dataaccess", "usecase", "domain", "java", "com.google.gson"),
                "view", List.of("view", "interfaceadapter", "java", "javax.swing", "javax.accessibility"));
        Pattern dependency = Pattern.compile("\\s*(\\S+)\\s+->\\s+(\\S+)\\s+.*");
        int checked = 0;
        for (String line : report.toString().split("\\R")) {
            Matcher matcher = dependency.matcher(line);
            if (!matcher.matches()) {
                continue;
            }
            String origin = matcher.group(1);
            String destination = matcher.group(2);
            String layer = origin.split("\\.")[0];
            if (allowed.containsKey(layer)) {
                checked++;
                assertTrue(allowed.get(layer).stream().anyMatch(prefix -> within(destination, prefix)),
                        "Forbidden layer dependency: " + origin + " -> " + destination);
            }
        }
        assertTrue(checked > 0, "No compiled dependencies were examined.");
    }

    // Package boundaries matter: an allowed prefix must not match an unrelated similarly named package.
    private boolean within(String packageName, String prefix) {
        return packageName.equals(prefix) || packageName.startsWith(prefix + ".");
    }
}
