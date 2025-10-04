package dev.nishisan.keycloak.admin.client.exception;

public class SSOIOException  extends Exception {
    public SSOIOException() {
    }

    public SSOIOException(String message) {
        super(message);
    }

    public SSOIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public SSOIOException(Throwable cause) {
        super(cause);
    }

    public SSOIOException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
