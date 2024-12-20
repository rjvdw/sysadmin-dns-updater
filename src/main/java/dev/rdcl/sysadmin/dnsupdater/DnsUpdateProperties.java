package dev.rdcl.sysadmin.dnsupdater;

import io.smallrye.config.ConfigMapping;

import java.nio.file.Path;

@ConfigMapping(prefix = "dns-updater")
public interface DnsUpdateProperties {
    Path config();
}
