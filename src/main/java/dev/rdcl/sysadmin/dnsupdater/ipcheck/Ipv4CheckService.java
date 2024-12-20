package dev.rdcl.sysadmin.dnsupdater.ipcheck;

import jakarta.ws.rs.GET;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "ip-check-ipv4")
public interface Ipv4CheckService {

    @GET
    String get();

}
