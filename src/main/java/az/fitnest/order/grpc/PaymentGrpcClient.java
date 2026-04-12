package az.fitnest.order.grpc;

import az.fitnest.payment.grpc.CreatePaymentRequest;
import az.fitnest.payment.grpc.CreatePaymentResponse;
import az.fitnest.payment.grpc.PaymentServiceGrpc;
import az.fitnest.order.dto.epoint.EpointPaymentRequest;
import az.fitnest.order.dto.epoint.EpointResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentGrpcClient {

    @GrpcClient("payment-backend")
    private PaymentServiceGrpc.PaymentServiceBlockingStub paymentServiceStub;

    public EpointResponse initiatePayment(EpointPaymentRequest request) {
        CreatePaymentRequest.Builder grpcRequestBuilder = CreatePaymentRequest.newBuilder()
                .setOrderId(request.order_id())
                .setAmount(request.amount())
                .setCurrency(request.currency())
                .setDescription(request.description() != null ? request.description() : "")
                .setLanguage(request.language() != null ? request.language() : "az")
                .setIsInstallment(request.is_installment() != null ? request.is_installment() : 0)
                .setRefund(request.refund() != null ? request.refund() : 0);

        if (request.other_attr() != null) {
            List<String> otherAttrs = request.other_attr().stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
            grpcRequestBuilder.addAllOtherAttr(otherAttrs);
        }

        CreatePaymentRequest grpcRequest = grpcRequestBuilder.build();

        CreatePaymentResponse grpcResponse = paymentServiceStub.createPayment(grpcRequest);

        return EpointResponse.builder()
                .status(grpcResponse.getStatus())
                .redirect_url(grpcResponse.getRedirectUrl())
                .transaction(grpcResponse.getTransactionId())
                .message(grpcResponse.getMessage())
                .build();
    }

    public az.fitnest.payment.grpc.PayWithCardResponse payWithCard(Long userId, String cardId, Long packageId, Long optionId) {
        az.fitnest.payment.grpc.PayWithCardRequest request = az.fitnest.payment.grpc.PayWithCardRequest.newBuilder()
                .setUserId(userId)
                .setCardId(cardId)
                .setPackageId(packageId)
                .setOptionId(optionId)
                .build();
        return paymentServiceStub.payWithCard(request);
    }

    public List<az.fitnest.payment.grpc.UserCardDto> getUserCards(Long userId) {
        az.fitnest.payment.grpc.GetUserCardsRequest request = az.fitnest.payment.grpc.GetUserCardsRequest.newBuilder()
                .setUserId(userId)
                .build();
        az.fitnest.payment.grpc.GetUserCardsResponse response = paymentServiceStub.getUserCards(request);
        return response.getCardsList();
    }
}
