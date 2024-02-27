package io.meeds.gamification.crowdin.exception;

public class CrowdinConnectionException extends Exception {

    private static final long serialVersionUID = -2437500058122638710L;

    public CrowdinConnectionException(String e) {
        super(e);
    }

    public CrowdinConnectionException(String message, Exception e) {
        super(message, e);
    }

}
