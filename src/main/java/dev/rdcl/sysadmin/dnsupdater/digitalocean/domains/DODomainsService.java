package dev.rdcl.sysadmin.dnsupdater.digitalocean.domains;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/domains")
@RegisterRestClient(configKey = "digital-ocean")
@ClientHeaderParam(name = "Authorization", value = "Bearer ${digital-ocean.token}")
public interface DODomainsService {

    @Path("/{domain}/records")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    DODomainGetRecordsResponse getDnsRecords(
            @PathParam("domain") String domain,
            @QueryParam("per_page") int perPage
    );

    @Path("/{domain}/records")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    void createDnsRecord(
            @PathParam("domain") String domain,
            DODomainCreateRecordRequest body
    );

    @Path("/{domain}/records/{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void updateDnsRecord(
            @PathParam("domain") String domain,
            @PathParam("id") long id,
            DODomainUpdateRecordRequest body
    );

}
