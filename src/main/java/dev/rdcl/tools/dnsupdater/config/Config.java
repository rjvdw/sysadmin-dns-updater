package dev.rdcl.tools.dnsupdater.config;

import dev.rdcl.tools.dns.IpVersion;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record Config(
        boolean forceUpdate,
        List<DomainConfig> domains
) {
    public static Config fromJsonFile(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path);
             Reader reader = new InputStreamReader(is);
             JsonReader jsonReader = Json.createReader(reader)) {

            JsonObject configObj = jsonReader.readObject();

            boolean forceUpdate = configObj.containsKey("force_update")
                    ? configObj.getBoolean("force_update")
                    : false;

            List<DomainConfig> domains = configObj.getJsonArray("domains")
                    .stream()
                    .map(Config::parseDomainConfig)
                    .toList();

            return new Config(forceUpdate, domains);
        }
    }

    private static DomainConfig parseDomainConfig(JsonValue value) {
        JsonObject domainObj = value.asJsonObject();
        String type = domainObj.getString("type");
        IpVersion ipVersion = switch (type) {
            case "ipv4" -> IpVersion.V4;
            case "ipv6" -> IpVersion.V6;
            default -> throw new RuntimeException("invalid type: " + type);
        };
        String rootDomain = domainObj.getString("root");

        return domainObj.containsKey("sub")
                ? new SubDomainConfig(ipVersion, rootDomain, domainObj.getString("sub"))
                : new RootDomainConfig(ipVersion, rootDomain);
    }
}
