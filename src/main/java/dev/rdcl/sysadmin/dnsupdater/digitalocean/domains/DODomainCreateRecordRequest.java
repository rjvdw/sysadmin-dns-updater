package dev.rdcl.sysadmin.dnsupdater.digitalocean.domains;

public record DODomainCreateRecordRequest(String type, String name, String data) {
}
