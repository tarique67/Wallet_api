package currency_converter

import (
	context "context"
	"log"
)

type Currency string

const (
	INR Currency = "INR"
	USD Currency = "USD"
	EUR Currency = "EUR"
)

var ConversionFactorMap = map[Currency]float32{
	INR: 1.0,
	USD: 83.10,
	EUR: 89.04,
}

func (c Currency) GetConversionFactor() float32 {
	return ConversionFactorMap[c]
}

type server struct {
	UnimplementedConverterServiceServer
}


func (s *server) ConvertMoney(ctx context.Context, req *ConvertRequest) (*ConvertResponse, error) {
	log.Printf("Inside convert function.")
	
	currency := req.Money.Currency
	amount := req.Money.Amount
	targetCurrency := req.TargetCurrency
	sourceCurrency := req.SourceCurrency

	convertedAmount := amount
	serviceCharge := &Money{Currency: "INR", Amount: 0.0}
	if currency != targetCurrency || currency != sourceCurrency {
		convertedAmount = amount / Currency(targetCurrency).GetConversionFactor() * Currency(currency).GetConversionFactor()
		serviceCharge = &Money{Currency: "INR", Amount: 10.0}
	}

	res := &ConvertResponse{
		Money: &Money{
			Currency: targetCurrency,
			Amount:   convertedAmount,
		},
		ServiceCharge: serviceCharge,
	}
	return res, nil
}