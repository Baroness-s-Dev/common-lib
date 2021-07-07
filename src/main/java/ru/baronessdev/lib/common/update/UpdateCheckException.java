package ru.baronessdev.lib.common.update;

public class UpdateCheckException extends Exception {

    private final String rootCause;

    public UpdateCheckException(String cause) {
        rootCause = cause;
    }

    public String getRootCause() {
        return rootCause;
    }
}
