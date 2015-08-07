package fr.sirs.core;


@SuppressWarnings("serial")
public class SirsCoreRuntimeException extends RuntimeException {

    public SirsCoreRuntimeException(Throwable e) {
        super(e);
    }

    public SirsCoreRuntimeException(String message) {
        super(message);
    }

    public SirsCoreRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
