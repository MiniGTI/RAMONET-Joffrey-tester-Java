package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.text.DecimalFormat;

public class FareCalculatorService {

    // private static final int MILLISECONDE_TO_HOUR = 2.77778E-7;
    private static final int MILLISECONDE_TO_HOUR = 1000 * 60 * 60;

    public double discountReduction(boolean discount) {
        if (discount==true) {
            return 0.95;
        } else {
            return 1;
        }
    }

    public double duration(Ticket ticket) {

        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        double inHour = ticket.getInTime().getTime();
        double outHour = ticket.getOutTime().getTime();

        double duration = (outHour - inHour) / MILLISECONDE_TO_HOUR;

        if (duration <= 0.5) {
            return 0.000;
        } else {
            return duration;
        }
    }

    public void calculateFare(Ticket ticket, boolean discount) {

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(Math.round(((duration(ticket) * Fare.CAR_RATE_PER_HOUR) * discountReduction(discount)) * 1000.0) / 1000.0);
                break;
            }
            case BIKE: {
                ticket.setPrice(Math.round(((duration(ticket) * Fare.BIKE_RATE_PER_HOUR) * discountReduction(discount)) * 1000.0) / 1000.0);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }
    }

}