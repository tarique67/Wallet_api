package com.swiggy.wallet.currencyConverterGrpcClient;

import com.swiggy.wallet.entities.Money;
import com.swiggy.wallet.enums.Currency;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import proto.ConvertRequest;
import proto.ConvertResponse;
import proto.ConverterServiceGrpc;

public class CurrencyConverter {

//    @Value("${converter.grpc.service.host}")
    private final String converterHost = "localhost";

//    @Value("${converter.grpc.service.port}")
    private final int port = 8090;



    public ConvertResponse convertMoney(Money money, Currency sourceCurrency, Currency targetCurrency){
        ManagedChannel channel = ManagedChannelBuilder.forAddress(converterHost, port)
                .usePlaintext().build();

        ConverterServiceGrpc.ConverterServiceBlockingStub stub = ConverterServiceGrpc.newBlockingStub(channel);
        ConvertRequest request = ConvertRequest.newBuilder().setMoney(proto.Money.newBuilder().setAmount((float) money.getAmount()).setCurrency(money.getCurrency().toString()).build())
                .setSourceCurrency(sourceCurrency.toString()).setTargetCurrency(targetCurrency.toString()).build();
        var response = stub.convertMoney(request);
        channel.shutdown();
        return response;
    }
}
