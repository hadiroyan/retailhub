package org.hadiroyan.retailhub.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EmailService {

    private static final Logger LOG = Logger.getLogger(EmailService.class);

    @Inject
    Mailer mailer;

    public void sendVerificationEmail(String to, String fullName, String otp) {
        LOG.debugf("action=SEND_VERIFICATION_EMAIL_START to=%s", to);

        try {
            mailer.send(Mail.withText(to,
                    "RetailHub Email Verification",
                    buildVerificationEmailText(fullName, otp)));

            LOG.infof("action=SEND_VERIFICATION_EMAIL_SUCCESS to=%s", to);
        } catch (Exception e) {
            LOG.errorf("action=SEND_VERIFICATION_EMAIL_FAILED to=%s error=%s", to, e.getMessage());
            // do not rethrow, the registration is still successful even if the email fails
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private String buildVerificationEmailText(String fullName, String otp) {
        return """
                Hello %s,

                Thank you for signing up for RetailHub!
                Your email verification code:

                %s

                This code is valid for 10 minutes.
                Please do not share this code with anyone.

                Best regards,
                RetailHub Team
                """.formatted(fullName, otp);
    }
}