package main

import (
	"context"
	pb "currency-converter/proto"
	"flag"
	"fmt"
	"log"
	"net"

	"google.golang.org/grpc"
)

type server struct {
	pb.UnimplementedConverterServiceServer
}

func (s *server) ConvertMoney(ctx context.Context, req *pb.ConvertRequest) (*pb.ConvertResponse, error) {
	log.Printf("Inside convert function.")
	currency := req.Money.Currency
	amount := req.Money.Amount
	targetCurrency := req.TargetCurrency
	sourceCurrency := req.SourceCurrency

	if _, ok := ConversionFactorMap[Currency(currency)]; !ok {
		return &pb.ConvertResponse{}, fmt.Errorf("unsupported currency: %s", currency)
	}

	if _, ok := ConversionFactorMap[Currency(targetCurrency)]; !ok {
		return &pb.ConvertResponse{}, fmt.Errorf("unsupported currency: %s", currency)
	}

	if _, ok := ConversionFactorMap[Currency(sourceCurrency)]; !ok {
		return &pb.ConvertResponse{}, fmt.Errorf("unsupported currency: %s", currency)
	}

	convertedAmount := amount
	serviceCharge := &pb.Money{Currency: "INR", Amount: 0.0}
	if currency != targetCurrency || currency != sourceCurrency {
		convertedAmount = amount / Currency(targetCurrency).GetConversionFactor() * Currency(currency).GetConversionFactor()
		serviceCharge = &pb.Money{Currency: targetCurrency, Amount: 10.0 / Currency(targetCurrency).GetConversionFactor()}
	}

	res := &pb.ConvertResponse{
		Money: &pb.Money{
			Currency: targetCurrency,
			Amount:   convertedAmount,
		},
		ServiceCharge: serviceCharge,
	}
	return res, nil
}

var (
	port = flag.Int("port", 8090, "gRPC server port")
)

func main() {
	fmt.Println("gRPC server running ...")

	lis, err := net.Listen("tcp", fmt.Sprintf(":%d", *port))

	if err != nil {
		log.Fatalf("Failed to listen: %v", err)
	}

	s := grpc.NewServer()
	pb.RegisterConverterServiceServer(s, &server{})

	log.Printf("Server listening at %v", lis.Addr())

	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve : %v", err)
	}
}
