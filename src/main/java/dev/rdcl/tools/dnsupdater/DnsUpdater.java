package dev.rdcl.tools.dnsupdater;

import dev.rdcl.tools.digitalocean.domains.DODomainCreateRecordRequest;
import dev.rdcl.tools.digitalocean.domains.DODomainRecord;
import dev.rdcl.tools.digitalocean.domains.DODomainUpdateRecordRequest;
import dev.rdcl.tools.digitalocean.domains.DODomainsService;
import dev.rdcl.tools.dns.DnsService;
import dev.rdcl.tools.dnsupdater.config.Config;
import dev.rdcl.tools.dnsupdater.config.DomainConfig;
import dev.rdcl.tools.ipcheck.Ipv4CheckService;
import dev.rdcl.tools.ipcheck.Ipv6CheckService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

@ApplicationScoped
@RequiredArgsConstructor
@Log
public class DnsUpdater {

    private final MeterRegistry registry;

    private final DnsUpdateProperties properties;

    @RestClient
    private final Ipv4CheckService ipv4CheckService;

    @RestClient
    private final Ipv6CheckService ipv6CheckService;

    private final DnsService dnsService;

    @RestClient
    private final DODomainsService doDomainsService;

    @Scheduled(every = "5m")
    @Counted("tools.sysadmin.update-dns.runs")
    public void updateDns() {
        Config updateDnsConfig;
        try {
            updateDnsConfig = Config.fromJsonFile(properties.config());
        } catch (IOException ex) {
            log.log(Level.SEVERE, ex, () -> "failed to read config file '%s': %s".formatted(
                    properties.config(),
                    ex.getLocalizedMessage()
            ));
            registry.counter("tools.sysadmin.update-dns.fails");

            return;
        }

        log.info(() -> "checking %s records".formatted(updateDnsConfig.domains().size()));
        log.finer(updateDnsConfig.toString());

        try {
            String ipv4 = ipv4CheckService.get();
            String ipv6 = ipv6CheckService.get();

            log.info(() -> "resolved ip addresses: ipv4=%s, ipv6=%s".formatted(ipv4, ipv6));

            Map<String, List<DomainConfig>> toUpdate = updateDnsConfig
                    .domains()
                    .stream()
                    .filter(config -> updateDnsConfig.forceUpdate() || !hasCorrectIp(config, ipv4, ipv6))
                    .collect(Collectors.groupingBy(DomainConfig::topDomain));

            log.info(() -> "updating %s records".formatted(count(toUpdate)));

            for (var entry : toUpdate.entrySet()) {
                String topDomain = entry.getKey();
                List<DomainConfig> configs = entry.getValue();

                update(topDomain, configs, ipv4, ipv6);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, ex, () -> "failed to update dns: %s".formatted(ex.getLocalizedMessage()));
            registry.counter("tools.sysadmin.update-dns.failed");
        }
    }

    private boolean hasCorrectIp(DomainConfig config, String ipv4, String ipv6) {
        List<String> resolved = dnsService.resolve(config.ipVersion(), config.fullDomain());

        log.fine(() -> "%s resolved to %s".formatted(config.fullDomain(), String.join(", ", resolved)));

        return switch (config.ipVersion()) {
            case V4 -> resolved.contains(ipv4);
            case V6 -> resolved.contains(ipv6);
        };
    }

    private void update(String topDomain, List<DomainConfig> configs, String ipv4, String ipv6) {
        // TODO: perPage hardcoded, should add mechanism to handle pagination
        int perPage = 200;

        Map<String, List<DODomainRecord>> existingRecords = doDomainsService.getDnsRecords(topDomain, perPage)
                .domain_records()
                .stream()
                .collect(Collectors.groupingBy(DODomainRecord::name));

        log.info(() -> "found %s records for %s".formatted(count(existingRecords), topDomain));

        for (DomainConfig config : configs) {
            Optional<Long> optionalId = existingRecords.getOrDefault(config.name(), List.of())
                    .stream()
                    .filter(record -> config.ipVersion().toRecordType().equals(record.type()))
                    .map(DODomainRecord::id)
                    .findAny();

            String ip = switch (config.ipVersion()) {
                case V4 -> ipv4;
                case V6 -> ipv6;
            };

            if (optionalId.isPresent()) {
                long id = optionalId.get();
                log.info(() -> "update record %s with ip %s: %s".formatted(id, ip, config));
                DODomainUpdateRecordRequest body = new DODomainUpdateRecordRequest(ip);
                doDomainsService.updateDnsRecord(topDomain, id, body);
                registry.counter("tools.sysadmin.update-dns.records-updated");
            } else {
                log.info(() -> "create record with ip %s: %s".formatted(ip, config));
                DODomainCreateRecordRequest body = new DODomainCreateRecordRequest(config.ipVersion().toRecordType(), config.name(), ip);
                doDomainsService.createDnsRecord(topDomain, body);
                registry.counter("tools.sysadmin.update-dns.records-created");
            }
        }
    }

    private int count(Map<?, ? extends Collection<?>> map) {
        return map.values()
                .stream()
                .mapToInt(Collection::size)
                .sum();
    }

}
