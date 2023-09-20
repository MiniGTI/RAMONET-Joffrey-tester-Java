package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.text.DecimalFormat;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct

        double duration = (outHour - inHour) * 2.77778E-7;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(Math.round((duration * Fare.CAR_RATE_PER_HOUR)*1000.0)/1000.0);
                break;
            }
            case BIKE: {
                ticket.setPrice(Math.round((duration * Fare.BIKE_RATE_PER_HOUR)*1000.0)/1000.0);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}