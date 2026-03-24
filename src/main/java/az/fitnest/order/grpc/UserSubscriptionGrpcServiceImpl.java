package az.fitnest.order.grpc;

import az.fitnest.order.dto.ActiveSubscriptionResponse;
import az.fitnest.order.service.impl.UserSubscriptionService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.format.DateTimeFormatter;
import java.util.List;

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

            if (dto.status() != null) {
                grpcResponse.setSubscriptionStatus(dto.status());
            }
            if (dto.subscription() != null) {
                var sub = dto.subscription();
                if (sub.packageName() != null) grpcResponse.setPackageName(sub.packageName());
                if (sub.packageId() != null) grpcResponse.setPackageId(Long.parseLong(sub.packageId()));
                if (sub.totalLimit() != null) grpcResponse.setTotalLimit(sub.totalLimit());
                if (sub.remainingLimit() != null) grpcResponse.setRemainingLimit(sub.remainingLimit());
                if (sub.endAt() != null) grpcResponse.setExpiresAt(sub.endAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
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

    @Override
    public void getUserIdsByPackageId(az.fitnest.order.grpc.GetUserIdsByPackageIdRequest request, StreamObserver<az.fitnest.order.grpc.GetUserIdsByPackageIdResponse> responseObserver) {
        try {
            Long packageId = request.getPackageId();
            List<Long> userIds = subscriptionService.getUserIdsByPackageId(packageId);
            az.fitnest.order.grpc.GetUserIdsByPackageIdResponse response = az.fitnest.order.grpc.GetUserIdsByPackageIdResponse.newBuilder()
                .addAllUserIds(userIds)
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Failed to get user IDs: " + e.getMessage())
                .withCause(e)
                .asRuntimeException());
        }
    }

    @Override
    public void getUserIdsByDurationMonths(az.fitnest.order.grpc.GetUserIdsByDurationMonthsRequest request, StreamObserver<az.fitnest.order.grpc.GetUserIdsByPackageIdResponse> responseObserver) {
        try {
            int durationMonths = request.getDurationMonths();
            List<Long> userIds = subscriptionService.getUserIdsByDurationMonths(durationMonths);
            az.fitnest.order.grpc.GetUserIdsByPackageIdResponse response = az.fitnest.order.grpc.GetUserIdsByPackageIdResponse.newBuilder()
                .addAllUserIds(userIds)
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Failed to get user IDs by duration: " + e.getMessage())
                .withCause(e)
                .asRuntimeException());
        }
    }

    @Override
    public void getUserIdsByType(az.fitnest.order.grpc.GetUserIdsByTypeRequest request, StreamObserver<az.fitnest.order.grpc.GetUserIdsByPackageIdResponse> responseObserver) {
        try {
            String type = request.getType();
            List<Long> userIds = subscriptionService.getUserIdsByType(type);
            az.fitnest.order.grpc.GetUserIdsByPackageIdResponse response = az.fitnest.order.grpc.GetUserIdsByPackageIdResponse.newBuilder()
                .addAllUserIds(userIds)
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Failed to get user IDs by type: " + e.getMessage())
                .withCause(e)
                .asRuntimeException());
        }
    }
}
