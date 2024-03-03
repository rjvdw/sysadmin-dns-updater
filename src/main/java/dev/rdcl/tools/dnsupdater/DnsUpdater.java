package dev.rdcl.tools.dnsupdater;

import dev.rdcl.tools.digitalocean.domains.DODomainCreateRecordRequest;
import dev.rdcl.tools.digitalocean.domains.DODomainRecord;
import dev.rdcl.tools.digitalocean.domains.DODomainUpdateRecordRequest;
import dev.rdcl.tools.digitalocean.domains.DODomainsService;
import dev.rdcl.tools.dns.DnsService;
import dev.rdcl.tools.dnsupdater.config.Config;
import dev.rdcl.tools.dnsupdater.config.DomainConfig;
import dev.rdcl.tools.health.HealthStatus;
import dev.rdcl.tools.ipcheck.Ipv4CheckService;
import dev.rdcl.tools.ipcheck.Ipv6CheckService;
import dev.rdcl.tools.reporter.ReporterService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.stream.Collectors;

@ApplicationScoped
public class DnsUpdater {

    private static final HealthStatus health = new HealthStatus(1, 5);

    @Inject Logger log;
    @Inject DnsUpdateProperties properties;
    @Inject DnsService dnsService;
    @Inject ReporterService reporterService;

    @Inject @RestClient Ipv4CheckService ipv4CheckService;
    @Inject @RestClient Ipv6CheckService ipv6CheckService;
    @Inject @RestClient DODomainsService doDomainsService;

    @Scheduled(every = "5m")
    public void updateDns() {
        Config updateDnsConfig = readConfig();

        log.infof("checking %s records", updateDnsConfig.domains().size());
        log.trace(updateDnsConfig.toString());

        try {
            String ipv4 = ipv4CheckService.get();
            String ipv6 = ipv6CheckService.get();

            log.infof("resolved ip addresses: ipv4=%s, ipv6=%s", ipv4, ipv6);

            Map<String, List<DomainConfig>> toUpdate = updateDnsConfig
                    .domains()
                    .stream()
                    .filter(config -> updateDnsConfig.forceUpdate() || !hasCorrectIp(config, ipv4, ipv6))
                    .collect(Collectors.groupingBy(DomainConfig::topDomain));

            log.infof("updating %s records", count(toUpdate));

            toUpdate.forEach((topDomain, configs) -> update(topDomain, configs, ipv4, ipv6));
            health.registerSuccess();
        } catch (Exception ex) {
            health.registerFailure();
            throw new DnsUpdateFailed(ex);
        }
    }

    @Scheduled(every = "15m")
    public void checkHealth() {
        health.checkIfChanged().ifPresent(healthy -> {
            reporterService.reportHealth(DnsUpdater.class, healthy);
        });
    }

    private Config readConfig() {
        try {
            return Config.fromJsonFile(properties.config());
        } catch (Exception ex) {
            log.errorf(ex, "failed to read config file '%s': %s",
                    properties.config(),
                    ex.getLocalizedMessage()
            );
            health.registerFailure();
            throw new DnsUpdateFailed(ex);
        }
    }

    private boolean hasCorrectIp(DomainConfig config, String ipv4, String ipv6) {
        List<String> resolved = dnsService.resolve(config.ipVersion(), config.fullDomain());

        log.debugf("%s resolved to %s", config.fullDomain(), String.join(", ", resolved));

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

        log.infof("found %s records for %s", count(existingRecords), topDomain);

        int failureCount = 0;
        for (DomainConfig config : configs) {
            OptionalLong optionalId = existingRecords.getOrDefault(config.name(), List.of())
                    .stream()
                    .filter(record -> config.ipVersion().toRecordType().equals(record.type()))
                    .mapToLong(DODomainRecord::id)
                    .findAny();

            String ip = switch (config.ipVersion()) {
                case V4 -> ipv4;
                case V6 -> ipv6;
            };

            if (optionalId.isPresent()) {
                long id = optionalId.getAsLong();
                try {
                    log.infof("update record %s with ip %s: %s", id, ip, config);
                    DODomainUpdateRecordRequest body = new DODomainUpdateRecordRequest(ip);
                    doDomainsService.updateDnsRecord(topDomain, id, body);
                } catch (Exception ex) {
                    log.errorf(ex, "failed to update record %s with ip %s: %s", id, ip, config);
                    failureCount += 1;
                }
            } else {
                try {
                    log.infof("create record with ip %s: %s", ip, config);
                    DODomainCreateRecordRequest body = new DODomainCreateRecordRequest(config.ipVersion().toRecordType(), config.name(), ip);
                    doDomainsService.createDnsRecord(topDomain, body);
                } catch (Exception ex) {
                    log.errorf(ex, "failed to create record with ip %s: %s", ip, config);
                    failureCount += 1;
                }
            }
        }

        if (failureCount > 0) {
            log.errorf("%s records were not saved", failureCount);
            health.registerFailure();
        }
    }

    private int count(Map<?, ? extends Collection<?>> map) {
        return map.values()
                .stream()
                .mapToInt(Collection::size)
                .sum();
    }

}
