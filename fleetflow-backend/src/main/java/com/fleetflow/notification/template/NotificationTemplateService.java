
package com.fleetflow.notification.template;

import com.fleetflow.notification.entity.NotificationTemplate;
import com.fleetflow.notification.enums.NotificationChannel;
import com.fleetflow.notification.enums.NotificationType;
import com.fleetflow.notification.repository.NotificationTemplateRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationTemplateService {

    private static final Pattern VARIABLE =
            Pattern.compile("\\{\\{([a-zA-Z][a-zA-Z0-9_]*)}}");

    private final NotificationTemplateRepository repository;

    public RenderedTemplate render(
            NotificationType type,
            NotificationChannel channel,
            String locale,
            Map<String, String> variables
    ) {
        NotificationTemplate template = repository
                .findFirstByNotificationTypeAndChannelAndLocaleAndEnabledTrue(
                        type, channel, locale
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Notification template not configured"
                        ));

        return new RenderedTemplate(
                renderText(template.getTitleTemplate(), variables),
                renderText(template.getMessageTemplate(), variables)
        );
    }

    private String renderText(
            String template,
            Map<String, String> variables
    ) {
        Matcher matcher = VARIABLE.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = variables.get(key);

            if (value == null) {
                throw new IllegalArgumentException(
                        "Missing template variable: " + key
                );
            }

            matcher.appendReplacement(
                    result,
                    Matcher.quoteReplacement(value)
            );
        }

        matcher.appendTail(result);
        return result.toString();
    }

    public record RenderedTemplate(String title, String message) {}
}
