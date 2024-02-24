package dev.rdcl.tools.dnsupdater.config;

import dev.rdcl.tools.dns.IpVersion;

public record SubDomainConfig(
        IpVersion ipVersion,
        String topDomain,
        String name
) implements DomainConfig {

    @Override
    public String fullDomain() {
        return name + "." + topDomain;
    }
}
