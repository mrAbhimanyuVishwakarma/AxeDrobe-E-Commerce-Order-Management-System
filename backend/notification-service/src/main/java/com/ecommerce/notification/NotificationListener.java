package com.ecommerce.notification;

import com.ecommerce.notification.OrderEmailComposer.Email;
import com.ecommerce.notification.event.OrderCancelledEvent;
import com.ecommerce.notification.event.OrderPlacedEvent;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Emails customers when their order is placed or cancelled, through the Brevo API (BREVO_API_KEY) or SMTP.
 * Without either it only logs, which keeps local development simple.
 */
@Slf4j
@Component
public class NotificationListener {

    private final ObjectProvider<JavaMailSender> mailSender;
    private final BrevoClient brevo;
    private final OrderEmailComposer composer;
    private final String host;
    private final String from;
    private final String brandName;

    public NotificationListener(ObjectProvider<JavaMailSender> mailSender,
                                BrevoClient brevo,
                                OrderEmailComposer composer,
                                @Value("${spring.mail.host:}") String host,
                                @Value("${app.mail.from:}") String from,
                                @Value("${app.brand-name:AxeDrobe}") String brandName) {
        this.mailSender = mailSender;
        this.brevo = brevo;
        this.composer = composer;
        this.host = host;
        this.from = from;
        this.brandName = brandName;
    }

    @KafkaListener(topics = "${app.kafka.topics.order-placed}")
    public void onOrderPlaced(OrderPlacedEvent event) {
        deliver(event.orderNumber(), event.customerEmail(), composer.orderPlaced(event));
    }

    @KafkaListener(topics = "${app.kafka.topics.order-cancelled}")
    public void onOrderCancelled(OrderCancelledEvent event) {
        deliver(event.orderNumber(), event.customerEmail(), composer.orderCancelled(event));
    }

    private void deliver(String orderNumber, String to, Email email) {
        if (!StringUtils.hasText(to)) {
            log.info("Order {}: customer has no email address, skipping \"{}\"", orderNumber, email.subject());
            return;
        }
        JavaMailSender sender = mailSender.getIfAvailable();
        boolean smtp = StringUtils.hasText(host) && sender != null;
        if (!StringUtils.hasText(from) || (!brevo.isConfigured() && !smtp)) {
            log.info("Order {}: email not configured, would send \"{}\"", orderNumber, email.subject());
            return;
        }
        try {
            if (brevo.isConfigured()) {
                brevo.send(from, brandName, to, email.subject(), email.html(), null);
                log.info("Order {}: sent \"{}\"", orderNumber, email.subject());
                return;
            }
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from, brandName);
            helper.setTo(to);
            helper.setSubject(email.subject());
            helper.setText(email.html(), true);
            sender.send(message);
            log.info("Order {}: sent \"{}\"", orderNumber, email.subject());
        } catch (Exception e) {
            // A failed email should not block the queue; the order itself is already saved
            log.error("Order {}: could not send email: {}", orderNumber, e.getMessage());
        }
    }
}
