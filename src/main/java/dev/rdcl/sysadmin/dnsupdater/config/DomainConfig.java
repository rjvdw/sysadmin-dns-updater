package dev.rdcl.sysadmin.dnsupdater.config;

import dev.rdcl.sysadmin.dnsupdater.dns.IpVersion;

public sealed interface DomainConfig permits RootDomainConfig, SubDomainConfig {
    IpVersion ipVersion();

    String fullDomain();

    String topDomain();

    String name();
}
