package net.madz.download.service.exception;

public class ErrorException extends Exception {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ErrorException(String message) {
        setMessage(message);
    }
}
