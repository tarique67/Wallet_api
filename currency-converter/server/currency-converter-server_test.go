package main

import (
	"context"
	"testing"
	pb "currency-converter/proto"
)

func TestConvertCurrencyToINR(t *testing.T) {
    srv := &server{}
    req := &pb.ConvertRequest{
        Money : &pb.Money{
			Currency: "USD",
			Amount: 100,
		},
		SourceCurrency: "USD",
		TargetCurrency: "INR",
    }
    ctx := context.Background()

    res, err := srv.ConvertMoney(ctx, req)

    if err != nil {
        t.Errorf("Error converting currency: %v", err)
    }

    if res == nil {
        t.Error("Conver response is nil")
    }
	expected := &pb.ConvertResponse{
		Money: &pb.Money{
			Currency: "INR",
			Amount: 8310,
		},
		ServiceCharge: &pb.Money{
			Currency: "INR",
			Amount: 10,
		},
	}

	
	if(res.Money.Amount != expected.Money.Amount || res.Money.Currency != expected.Money.Currency){
		t.Errorf("Unexpected result.")
	}

	if(res.ServiceCharge.Amount != expected.ServiceCharge.Amount || res.ServiceCharge.Currency != expected.ServiceCharge.Currency) {
		t.Errorf("Unexpected result.")
	}
}


func TestConvertCurrencyToUSD(t *testing.T) {
    srv := &server{}
    req := &pb.ConvertRequest{
        Money : &pb.Money{
			Currency: "INR",
			Amount: 831,
		},
		SourceCurrency: "INR",
		TargetCurrency: "USD",
    }
    ctx := context.Background()

    res, err := srv.ConvertMoney(ctx, req)

    if err != nil {
        t.Errorf("Error converting currency: %v", err)
    }

    if res == nil {
        t.Error("Conver response is nil")
    }
	expected := &pb.ConvertResponse{
		Money: &pb.Money{
			Currency: "USD",
			Amount: 10,
		},
		ServiceCharge: &pb.Money{
			Currency: "INR",
			Amount: 10,
		},
	}

	
	if(res.Money.Amount != expected.Money.Amount || res.Money.Currency != expected.Money.Currency){
		t.Errorf("Unexpected result.")
	}

	if(res.ServiceCharge.Amount != expected.ServiceCharge.Amount || res.ServiceCharge.Currency != expected.ServiceCharge.Currency) {
		t.Errorf("Unexpected result.")
	}
}