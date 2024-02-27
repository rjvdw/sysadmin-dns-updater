package dev.rdcl.tools.reporter;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;

@ApplicationScoped
@RequiredArgsConstructor
public class ReporterService {

    private final Logger log;
    private final Mailer mailer;
    private final ReporterProperties properties;

    public void reportHealth(Class<?> clz, boolean healthy) {
        String healthyText = healthy ? "healthy" : "unhealthy";
        String message = "Health status of '%s' has changed. It is now %s.".formatted(clz, healthyText);

        log.info(message);

        Mail mail = new Mail();
        mail.setFrom(properties.sender());
        mail.setTo(properties.recipients());
        mail.setSubject("[%s] Health status changed to: %s".formatted(clz, healthyText));
        mail.setText(message);
        mail.setHtml("<p>%s</p>".formatted(message));

        mailer.send(mail);
    }
}
