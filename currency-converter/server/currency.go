package main

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