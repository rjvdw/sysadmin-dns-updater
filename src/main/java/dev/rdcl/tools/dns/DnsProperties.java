package dev.rdcl.tools.dns;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "dns-resolver")
public interface DnsProperties {
    @WithDefault("53")
    int port();

    String host();
}
