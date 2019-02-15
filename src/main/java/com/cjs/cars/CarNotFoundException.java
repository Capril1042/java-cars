package com.cjs.cars;

public class CarNotFoundException extends RuntimeException
{
    public CarNotFoundException(Long id)
    {
        super("Could Not Fins Car");
    }
}
