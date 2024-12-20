package dev.rdcl.sysadmin.dnsupdater.config;

import dev.rdcl.sysadmin.dnsupdater.dns.IpVersion;

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
