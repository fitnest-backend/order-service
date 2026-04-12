package az.fitnest.order.grpc;

import az.fitnest.notifications.grpc.NotificationsServiceGrpc;
import az.fitnest.notifications.grpc.SendPushNotificationRequest;
import az.fitnest.notifications.grpc.SendSimpleEmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationGrpcClient {

    @GrpcClient("notifications-backend")
    private NotificationsServiceGrpc.NotificationsServiceBlockingStub notificationsStub;

    public void sendPushNotification(Long userId, String title, String body) {
        try {
            SendPushNotificationRequest request = SendPushNotificationRequest.newBuilder()
                    .setUserId(userId)
                    .setTitle(title)
                    .setBody(body)
                    .build();
            notificationsStub.sendPushNotification(request);
            log.info("Push notification sent to user {}", userId);
        } catch (Exception e) {
            log.error("Failed to send push notification to user {}: {}", userId, e.getMessage());
        }
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            SendSimpleEmailRequest request = SendSimpleEmailRequest.newBuilder()
                    .setTo(to)
                    .setSubject(subject)
                    .setBody(body)
                    .build();
            notificationsStub.sendSimpleEmail(request);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
