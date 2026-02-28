package az.fitnest.order.grpc;

import az.fitnest.order.dto.ActiveSubscriptionResponse;
import az.fitnest.order.service.impl.UserSubscriptionService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserSubscriptionGrpcServiceImpl extends az.fitnest.order.grpc.UserSubscriptionServiceGrpc.UserSubscriptionServiceImplBase {

    private final UserSubscriptionService subscriptionService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public void getActiveSubscription(GetActiveSubscriptionRequest request, StreamObserver<az.fitnest.order.grpc.ActiveSubscriptionResponse> responseObserver) {
        try {
            Long userId = request.getUserId();
            ActiveSubscriptionResponse dto = subscriptionService.getActiveSubscription(userId);

            az.fitnest.order.grpc.ActiveSubscriptionResponse.Builder grpcResponse = az.fitnest.order.grpc.ActiveSubscriptionResponse.newBuilder()
                    .setStatus(dto.getStatus() != null ? dto.getStatus() : "");

            if (dto.getSubscription() != null) {
                az.fitnest.order.dto.SubscriptionDetailsDto info = dto.getSubscription();
                if (info.getSubscriptionId() != null) grpcResponse.setSubscriptionId(info.getSubscriptionId());
                if (info.getPackageName() != null) grpcResponse.setPackageName(info.getPackageName());
                if (info.getStartAt() != null) grpcResponse.setStartAt(info.getStartAt().format(DATE_FORMATTER));
                if (info.getEndAt() != null) grpcResponse.setEndAt(info.getEndAt().format(DATE_FORMATTER));
                if (info.getTotalLimit() != null) grpcResponse.setTotalLimit(info.getTotalLimit());
                if (info.getRemainingLimit() != null) grpcResponse.setRemainingLimit(info.getRemainingLimit());
            }

            responseObserver.onNext(grpcResponse.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Failed to get active subscription for user {}: {}", request.getUserId(), e.getMessage());
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to get active subscription: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}
