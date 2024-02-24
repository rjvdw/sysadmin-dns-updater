package dev.rdcl.tools.digitalocean.domains;

public record DODomainRecord(
        long id,
        String type,
        String name,
        String data
) {
}
