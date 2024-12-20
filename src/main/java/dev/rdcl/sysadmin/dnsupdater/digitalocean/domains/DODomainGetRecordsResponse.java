package dev.rdcl.sysadmin.dnsupdater.digitalocean.domains;

import java.util.List;

public record DODomainGetRecordsResponse(
        List<DODomainRecord> domain_records
) {
}
