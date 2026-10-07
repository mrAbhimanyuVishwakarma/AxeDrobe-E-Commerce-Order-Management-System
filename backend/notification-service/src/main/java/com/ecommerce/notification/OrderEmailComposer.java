package com.ecommerce.notification;

import com.ecommerce.notification.event.EventItem;
import com.ecommerce.notification.event.OrderCancelledEvent;
import com.ecommerce.notification.event.OrderPlacedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Builds the subject and HTML body for order emails.
 */
@Component
public class OrderEmailComposer {

    private final String brandName;
    private final String storeUrl;

    public OrderEmailComposer(@Value("${app.brand-name:AxeDrobe}") String brandName,
                              @Value("${app.store-url:http://localhost:5173}") String storeUrl) {
        this.brandName = brandName;
        this.storeUrl = storeUrl.replaceAll("/+$", "");
    }

    public Email orderPlaced(OrderPlacedEvent event) {
        String subject = "Your " + brandName + " order " + event.orderNumber() + " is confirmed";
        String body = """
                <p>Hi %s,</p>
                <p>Thanks for shopping with us! Your order <strong>%s</strong> is confirmed and will be packed shortly.</p>
                %s
                <p style="font-size:16px"><strong>Total: %s</strong> (Cash on Delivery)</p>
                <p><a href="%s/orders" style="color:#0070f3">Track your order</a></p>
                """.formatted(escape(firstName(event.customerName())), event.orderNumber(),
                itemsTable(event.items()), rupees(event.total()), storeUrl);
        return new Email(subject, wrap(body));
    }

    public Email orderCancelled(OrderCancelledEvent event) {
        String subject = "Your " + brandName + " order " + event.orderNumber() + " has been cancelled";
        String body = """
                <p>Hi %s,</p>
                <p>Your order <strong>%s</strong> has been cancelled as requested. Nothing has been charged.</p>
                %s
                <p><a href="%s" style="color:#0070f3">Continue shopping</a></p>
                """.formatted(escape(firstName(event.customerName())), event.orderNumber(),
                itemsTable(event.items()), storeUrl);
        return new Email(subject, wrap(body));
    }

    private String wrap(String content) {
        return """
                <div style="font-family:Arial,Helvetica,sans-serif;max-width:560px;margin:0 auto;padding:24px;color:#171717">
                  <h2 style="margin:0 0 20px;letter-spacing:2px">%s</h2>
                  %s
                  <p style="margin-top:24px;color:#888;font-size:12px">This is an automated message about your order.</p>
                </div>
                """.formatted(escape(brandName.toUpperCase(Locale.ROOT)), content);
    }

    private static String itemsTable(List<EventItem> items) {
        String rows = items.stream().map(item -> """
                <tr>
                  <td style="padding:8px 0;border-bottom:1px solid #ebebeb">%s%s</td>
                  <td style="padding:8px 0;border-bottom:1px solid #ebebeb;text-align:center">x%d</td>
                  <td style="padding:8px 0;border-bottom:1px solid #ebebeb;text-align:right">%s</td>
                </tr>
                """.formatted(escape(item.name()),
                item.size() != null ? " <span style=\"color:#888\">(" + escape(item.size()) + ")</span>" : "",
                item.quantity(),
                rupees(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))))
                .collect(Collectors.joining());
        return "<table style=\"width:100%;border-collapse:collapse;margin:16px 0\">" + rows + "</table>";
    }

    /** Indian digit grouping, e.g. 124999 -> ₹1,24,999 (the JDK's en-IN format groups in thousands). */
    static String rupees(BigDecimal amount) {
        long value = amount.setScale(0, RoundingMode.HALF_UP).longValue();
        String digits = Long.toString(Math.abs(value));
        if (digits.length() > 3) {
            String head = digits.substring(0, digits.length() - 3).replaceAll("\\B(?=(\\d{2})+$)", ",");
            digits = head + "," + digits.substring(digits.length() - 3);
        }
        return (value < 0 ? "-" : "") + "₹" + digits;
    }

    private static String firstName(String name) {
        if (name == null || name.isBlank()) {
            return "there";
        }
        return name.trim().split("\\s+")[0];
    }

    private static String escape(String value) {
        return value == null ? "" : HtmlUtils.htmlEscape(value);
    }

    public record Email(String subject, String html) {
    }
}
