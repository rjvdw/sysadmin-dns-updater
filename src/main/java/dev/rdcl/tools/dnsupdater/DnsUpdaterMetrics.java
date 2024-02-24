package dev.rdcl.tools.dnsupdater;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.BaseUnits;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DnsUpdaterMetrics {

    private final Counter failureCounter;
    private final Counter recordsUpdatedCounter;
    private final Counter recordsCreatedCounter;

    public DnsUpdaterMetrics(MeterRegistry registry) {
        this.failureCounter = Counter.builder("tools.sysadmin.update-dns.fails")
                .baseUnit(BaseUnits.EVENTS)
                .description("Incremented whenever the DNS updater fails to run correctly")
                .register(registry);

        this.recordsUpdatedCounter = Counter.builder("tools.sysadmin.update-dns.records-updated")
                .baseUnit(BaseUnits.EVENTS)
                .description("Incremented whenever a DNS record is updated")
                .register(registry);

        this.recordsCreatedCounter = Counter.builder("tools.sysadmin.update-dns.records-created")
                .baseUnit(BaseUnits.EVENTS)
                .description("Incremented whenever a DNS record is created")
                .register(registry);
    }

    public Counter failureCounter() {
        return failureCounter;
    }

    public Counter recordsUpdatedCounter() {
        return recordsUpdatedCounter;
    }

    public Counter recordsCreatedCounter() {
        return recordsCreatedCounter;
    }
}
