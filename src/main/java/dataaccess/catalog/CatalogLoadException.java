package dataaccess.catalog;

// A startup failure: no usable catalogue was loaded, so search must not start.
public final class CatalogLoadException extends Exception {
    // Keep the source failure available to startup diagnostics and tests.
    public CatalogLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
