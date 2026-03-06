package az.fitnest.order.grpc;

import az.fitnest.order.dto.ActiveSubscriptionResponse;
import az.fitnest.order.service.impl.UserSubscriptionService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.format.DateTimeFormatter;

@GrpcService
@RequiredArgsConstructor
public class UserSubscriptionGrpcServiceImpl extends az.fitnest.order.grpc.UserSubscriptionServiceGrpc.UserSubscriptionServiceImplBase {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final UserSubscriptionService subscriptionService;

    @Override
    public void getActiveSubscription(GetActiveSubscriptionRequest request, StreamObserver<az.fitnest.order.grpc.ActiveSubscriptionResponse> responseObserver) {
        try {
            Long userId = request.getUserId();
            ActiveSubscriptionResponse dto = subscriptionService.getActiveSubscription(userId);

            az.fitnest.order.grpc.ActiveSubscriptionResponse.Builder grpcResponse = az.fitnest.order.grpc.ActiveSubscriptionResponse.newBuilder();

            // Set subscription status (active, frozen, or none)
            if (dto.status() != null) {
                grpcResponse.setSubscriptionStatus(dto.status());
            }

            // Always set package name (including "No Plan" for users without subscription)
            if (dto.subscription() != null && dto.subscription().packageName() != null) {
                grpcResponse.setPackageName(dto.subscription().packageName());
            }

            responseObserver.onNext(grpcResponse.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to get active subscription: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}
