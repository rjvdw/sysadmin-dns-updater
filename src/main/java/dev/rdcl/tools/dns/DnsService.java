package dev.rdcl.tools.dns;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.dns.DnsClient;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class DnsService {

    private final Vertx vertx;
    private final DnsProperties dnsProperties;

    public List<String> resolve(IpVersion ipv, String name) {
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
