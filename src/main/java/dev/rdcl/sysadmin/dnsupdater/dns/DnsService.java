package dev.rdcl.sysadmin.dnsupdater.dns;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.dns.DnsClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class DnsService {

    @Inject Logger log;
    @Inject Vertx vertx;
    @Inject DnsProperties dnsProperties;

    public List<String> resolve(IpVersion ipv, String name) {
        log.debugf("using name server %s:%s", dnsProperties.host(), dnsProperties.port());
        DnsClient client = vertx.createDnsClient(dnsProperties.port(), dnsProperties.host());

        return switch (ipv) {
            case V4 -> await(client.resolveA(name));
            case V6 -> await(client.resolveAAAA(name));
        };
    }

    private <T> T await(Future<T> future) {
        return future.toCompletionStage().toCompletableFuture().join();
    }

}
