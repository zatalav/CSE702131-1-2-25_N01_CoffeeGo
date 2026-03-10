package He_thong_quan_ly.demo.auth.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AuthSecurityService {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(AuthSecurityService.class.getName());

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final Environment environment;

    public AuthSecurityService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            Environment environment) {
        this.mailSenderProvider = mailSenderProvider;
        this.environment = environment;
    }

    public String safe(String value) {
        return value == null ? "" : value.trim();
    }

    public boolean isOtpExpired(Instant expiresAt) {
        return expiresAt == null || expiresAt.isBefore(Instant.now());
    }

    public String generateNumericOtp(int length) {
        int digits = Math.max(1, length);
        int bound = (int) Math.pow(10, digits);
        SecureRandom random = new SecureRandom();
        int number = random.nextInt(bound);
        return String.format("%0" + digits + "d", number);
    }

    public Optional<String> validatePassword(
            String password,
            String confirmPassword,
            int minLength,
            String emptyMessage,
            String minLengthMessage,
            String mismatchMessage) {
        if (password == null || password.isBlank()) {
            return Optional.of(emptyMessage);
        }
        if (password.length() < minLength) {
            return Optional.of(minLengthMessage);
        }
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            return Optional.of(mismatchMessage);
        }
        return Optional.empty();
    }

    public boolean isMailConfigured() {
        String host = environment.getProperty("spring.mail.host");
        String username = environment.getProperty("spring.mail.username");
        String password = environment.getProperty("spring.mail.password");
        return host != null && !host.isBlank()
                && username != null && !username.isBlank()
                && password != null && !password.isBlank();
    }

    public Optional<String> sendOtpEmail(
            String to,
            String subject,
            String body,
            String unavailableMessage,
            String failureMessage) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            return Optional.of(unavailableMessage);
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(environment.getProperty("spring.mail.username"));
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            sender.send(message);
            return Optional.empty();
        } catch (MailException | IllegalArgumentException ex) {
            LOGGER.log(java.util.logging.Level.WARNING, "Send OTP failed", ex);
            return Optional.of(failureMessage);
        }
    }

    public String safe(Map<String, String> payload, String key) {
        return safe(payload == null ? null : payload.get(key));
    }
}
