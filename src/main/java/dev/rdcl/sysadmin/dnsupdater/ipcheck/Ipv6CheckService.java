package dev.rdcl.sysadmin.dnsupdater.ipcheck;

import jakarta.ws.rs.GET;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "ip-check-ipv6")
public interface Ipv6CheckService {

    @GET
    String get();

}
