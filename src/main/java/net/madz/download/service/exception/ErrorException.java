package net.madz.download.service.exception;

public class ErrorException extends Exception {

    private static final long serialVersionUID = -3784777249170182306L;
    
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
