package dev.rdcl.tools.dnsupdater;

import dev.rdcl.tools.dnsupdater.config.DomainConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class DnsUpdaterMetricsService {

    private static final String NAME_INVALID_CONFIG = "tools.sysadmin.update-dns.invalid-config";
    private static final String NAME_RECORDS_UPDATED = "tools.sysadmin.update-dns.records-updated";
    private static final String NAME_RECORDS_CREATED = "tools.sysadmin.update-dns.records-created";

    private static final String RESULT_SUCCESS = "success";
    private static final String RESULT_FAILURE = "failure";

    private static final String EXCEPTION_NONE = "none";

    private final MeterRegistry registry;

    public Counter invalidConfigCounter(Exception ex) {
        return registry.counter(NAME_INVALID_CONFIG,
                "exception", ex.getLocalizedMessage()
        );
    }

    public Counter updatedCounter(DomainConfig config, String ip) {
        return updatedCounter(config, ip, RESULT_SUCCESS, EXCEPTION_NONE);
    }

    public Counter updatedCounter(DomainConfig config, String ip, Exception ex) {
        return updatedCounter(config, ip, RESULT_FAILURE, ex.getLocalizedMessage());
    }

    private Counter updatedCounter(DomainConfig config, String ip, String result, String exception) {
        return counter(NAME_RECORDS_UPDATED, config, ip, result, exception);
    }

    public Counter createdCounter(DomainConfig config, String ip) {
        return createdCounter(config, ip, RESULT_SUCCESS, EXCEPTION_NONE);
    }

    public Counter createdCounter(DomainConfig config, String ip, Exception ex) {
        return createdCounter(config, ip, RESULT_FAILURE, ex.getLocalizedMessage());
    }

    private Counter createdCounter(DomainConfig config, String ip, String result, String exception) {
        return counter(NAME_RECORDS_CREATED, config, ip, result, exception);
    }

    private Counter counter(String name, DomainConfig config, String ip, String result, String exception) {
        return registry.counter(name,
                "domain", config.topDomain(),
                "name", config.name(),
                "ip", ip,
                "result", result,
                "exception", exception
        );
    }
}
