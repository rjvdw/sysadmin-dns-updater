package dev.rdcl.tools.dnsupdater.config;

import dev.rdcl.tools.dns.IpVersion;

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
