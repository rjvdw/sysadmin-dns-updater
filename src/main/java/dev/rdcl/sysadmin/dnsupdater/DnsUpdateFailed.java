package dev.rdcl.sysadmin.dnsupdater;

public class DnsUpdateFailed extends RuntimeException {
    public DnsUpdateFailed(Throwable cause) {
        super("failed to update dns: %s".formatted(cause.getLocalizedMessage()), cause);
    }
}
