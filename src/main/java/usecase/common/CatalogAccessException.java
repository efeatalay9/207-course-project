package usecase.common;

// A storage-independent failure contract for any catalogue implementation.
public class CatalogAccessException extends Exception {
    // Retains the original cause for diagnostics without exposing it to the UI.
    public CatalogAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
