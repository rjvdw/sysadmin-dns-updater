package dev.rdcl.tools.dns;

public enum IpVersion {
    V4, V6;

    public String toRecordType() {
        return switch (this) {
            case V4 -> "A";
            case V6 -> "AAAA";
        };
    }
}
