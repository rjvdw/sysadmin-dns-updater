package dev.rdcl.tools.dnsupdater.config;

import dev.rdcl.tools.dns.IpVersion;

public sealed interface DomainConfig permits RootDomainConfig, SubDomainConfig {
    IpVersion ipVersion();

    String fullDomain();

    String topDomain();

    String name();
}
