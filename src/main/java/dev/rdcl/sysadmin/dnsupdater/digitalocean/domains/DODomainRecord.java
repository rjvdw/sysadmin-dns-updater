package dev.rdcl.sysadmin.dnsupdater.digitalocean.domains;

public record DODomainRecord(
        long id,
        String type,
        String name,
        String data
) {
}
