package dataaccess.catalog;

import com.google.gson.JsonParser;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import domain.CourseOffering;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

// Reads plain JSON or the supplied ZIP. Only one offering's JSON tree is held at a time.
public final class JsonCatalogLoader {
    private final JsonOfferingMapper mapper = new JsonOfferingMapper();

    // Opens/closes the source; ZIP input is read directly without extracting the 130 MB file.
    public List<CourseOffering> load(Path path) throws CatalogLoadException {
        if (path.toString().toLowerCase(Locale.ROOT).endsWith(".zip")) {
            return loadZip(path);
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return load(reader);
        } catch (IOException exception) {
            throw new CatalogLoadException("Cannot read catalogue: " + path, exception);
        }
    }

    // Read an array from a caller-owned reader. Fail the whole load if any record is invalid.
    public List<CourseOffering> load(Reader source) throws CatalogLoadException {
        JsonReader reader = new JsonReader(source);
        reader.setStrictness(Strictness.STRICT);
        List<CourseOffering> offerings = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        try {
            reader.beginArray();
            while (reader.hasNext()) {
                CourseOffering offering = mapper.map(JsonParser.parseReader(reader).getAsJsonObject());
                if (!ids.add(offering.id())) {
                    throw new IllegalArgumentException("Duplicate offering ID: " + offering.id());
                }
                offerings.add(offering);
            }
            reader.endArray();
            if (reader.peek() != JsonToken.END_DOCUMENT) {
                throw new IllegalArgumentException("Unexpected data after the catalogue array.");
            }
            return List.copyOf(offerings);
        } catch (IOException | RuntimeException exception) {
            throw new CatalogLoadException("Invalid catalogue near record " + (offerings.size() + 1)
                    + " at " + reader.getPath() + ": " + exception.getMessage(), exception);
        }
    }

    // The repository archive has a single root entry named courses.json.
    private List<CourseOffering> loadZip(Path path) throws CatalogLoadException {
        try (ZipFile archive = new ZipFile(path.toFile())) {
            ZipEntry entry = archive.getEntry("courses.json");
            if (entry == null || entry.isDirectory()) {
                throw new IOException("ZIP must contain a root courses.json file.");
            }
            try (Reader reader = new InputStreamReader(archive.getInputStream(entry), StandardCharsets.UTF_8)) {
                return load(reader);
            }
        } catch (IOException exception) {
            throw new CatalogLoadException("Cannot read catalogue ZIP: " + path, exception);
        }
    }
}
