package zzik2.soop4j.exception;

/** The live API requires a signed-in account or the signed-in account lacks access. */
public final class AuthenticationRequiredException extends SoopApiException {
    private final String reason;

    public AuthenticationRequiredException(String streamerId, int resultCode, String reason) {
        super(streamerId, resultCode);
        this.reason = reason == null ? "" : reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String getMessage() {
        return reason.isBlank() ? super.getMessage() : super.getMessage() + ": " + reason;
    }
}
