package dev.rdcl.sysadmin.dnsupdater.config;

import dev.rdcl.sysadmin.dnsupdater.dns.IpVersion;

public record RootDomainConfig(
        IpVersion ipVersion,
        String topDomain
) implements DomainConfig {

    @Override
    public String fullDomain() {
        return topDomain;
    }

    @Override
    public String name() {
        return "@";
    }
}
