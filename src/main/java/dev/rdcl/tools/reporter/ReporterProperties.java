package dev.rdcl.tools.reporter;

import io.smallrye.config.ConfigMapping;

import java.util.List;

@ConfigMapping(prefix = "reporter")
public interface ReporterProperties {
    String sender();

    List<String> recipients();
}
