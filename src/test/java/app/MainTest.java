package app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

// Integration tests drive startup, loading, controller, use case, presenter, and console together.
class MainTest {
    @TempDir Path temporaryDirectory;

    @Test
    void consoleSupportsRepeatedSearchesValidationAndQuit() {
        Result result = run(" csc108h1 \nUNKNOWN\n\nCSC207H1\n:quit\nCSC108H1\n", "--sample", "--console");
        assertEquals(0, result.status());
        assertTrue(result.errors().isEmpty());
        assertTrue(result.output().contains("SAMPLE DATA"));
        assertTrue(result.output().contains("Loaded 3 offerings"));
        assertTrue(result.output().contains("2 offerings for CSC108H1"));
        assertTrue(result.output().contains("No matching offerings"));
        assertTrue(result.output().contains("cannot be blank"));
        assertTrue(result.output().contains("1 offering for CSC207H1"));
        assertEquals(1, result.output().split("2 offerings for CSC108H1", -1).length - 1);
    }

    @Test
    void endOfInputExitsCleanly() {
        assertEquals(0, run("", "--sample", "--console").status());
    }

    @Test
    void helpDoesNotLoadAnyCatalogue() {
        Result result = run("", "--help");
        assertEquals(0, result.status());
        assertTrue(result.output().contains("--catalog"));
        assertFalse(result.output().contains("Loading"));
    }

    @Test
    void missingCatalogueFailsStartupInsteadOfShowingNoResults() {
        Result result = run("", "--console", "--catalog", temporaryDirectory.resolve("missing.json").toString());
        assertEquals(1, result.status());
        assertTrue(result.errors().contains("Could not start course search"));
        assertFalse(result.output().contains("Course code >"));
    }

    @Test
    void malformedCatalogueReportsAnActionableError() throws Exception {
        Path file = temporaryDirectory.resolve("broken.json");
        Files.writeString(file, "[{]");
        Result result = run("", "--console", "--catalog", file.toString());
        assertEquals(1, result.status());
        assertTrue(result.errors().contains("record 1"));
        assertTrue(result.errors().contains("--sample"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"--unknown", "--catalog"})
    void invalidOptionsReportUsageFailure(String option) {
        Result result = run("", option);
        assertEquals(2, result.status());
        assertTrue(result.errors().contains("--help"));
    }

    @Test
    void conflictingSourcesAreRejected() {
        assertEquals(2, run("", "--sample", "--catalog", "anything.json").status());
    }

    // Inject streams to make tests deterministic without replacing System.in/out globally.
    private Result run(String input, String... args) {
        StringWriter output = new StringWriter();
        StringWriter errors = new StringWriter();
        int status = Main.run(args, new StringReader(input), new PrintWriter(output), new PrintWriter(errors));
        return new Result(status, output.toString(), errors.toString());
    }

    private record Result(int status, String output, String errors) { }
}
