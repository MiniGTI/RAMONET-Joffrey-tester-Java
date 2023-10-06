package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static com.parkit.parkingsystem.constants.ParkingType.BIKE;
import static com.parkit.parkingsystem.constants.ParkingType.CAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

private static ParkingService parkingService;

@Mock
private static InputReaderUtil inputReaderUtil;
@Mock
private static ParkingSpotDAO parkingSpotDAO;
@Mock
private static TicketDAO ticketDAO;
private final Ticket ticket = new Ticket();

@BeforeEach
void setUpPerTest() {
	parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
}

@Test
void processIncomingCarTest() {
	when(inputReaderUtil.readSelection()).thenReturn(1); //Switch case ParkingService.getVehicleType
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("GHIJKL");
	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1); // ID parkingSpot attribution
	ParkingSpot parkingSpot = new ParkingSpot(1, CAR, false);
	
	Date inTime = new Date();
	
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("GHIJKL");
	ticket.setPrice(0);
	ticket.setInTime(inTime);
	ticket.setOutTime(null);
	when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
	
	parkingService.processIncomingVehicle(); // CUT
	verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
}

@Test
void processIncomingBikeTest() {
	when(inputReaderUtil.readSelection()).thenReturn(2); //Switch case ParkingService.getVehicleType
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("GHIJKL");
	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1); // ID parkingSpot attribution
	ParkingSpot parkingSpot = new ParkingSpot(1, BIKE, false);
	
	Date inTime = new Date();
	
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("GHIJKL");
	ticket.setPrice(0);
	ticket.setInTime(inTime);
	ticket.setOutTime(null);
	when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
	
	parkingService.processIncomingVehicle(); // CUT
	verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
}

@Test
void processExitingVehicleTest() {
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("GHIJKL");
	
	ParkingSpot parkingSpot = new ParkingSpot(1, CAR, false);
	
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("GHIJKL");
	ticket.setPrice(0);
	ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
	ticket.setOutTime(new Date());
	when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
	
	parkingService.processExitingVehicle();
	
	verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
	verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
}

@Test
void processExitingVehicleTestUnableUpdate() {
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
	ParkingSpot parkingSpot = new ParkingSpot(1, CAR, false);
	
	ticket.setParkingSpot(parkingSpot);
	ticket.setVehicleRegNumber("GHIJKL");
	ticket.setInTime(new Date());
	
	when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
	when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
	
	parkingService.processExitingVehicle();
	verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
}

@Test
void getNextParkingNumberIfAvailableTest() {
	when(inputReaderUtil.readSelection()).thenReturn(1);
	
	ParkingSpot parkingSpot = new ParkingSpot(1, CAR, true);
	
	when(parkingSpotDAO.getNextAvailableSlot(CAR)).thenReturn(1);
	
	parkingService.getNextParkingNumberIfAvailable();
	verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(CAR);
	assertEquals(1, parkingSpot.getId());
}

@Test
void getNextParkingNumberIfAvailableParkingNumberNotFound() {
	when(inputReaderUtil.readSelection()).thenReturn(1);
	
	when(parkingSpotDAO.getNextAvailableSlot(CAR)).thenReturn(0);
	
	parkingService.getNextParkingNumberIfAvailable();
	verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(CAR);
}

@Test
void getNextParkingNumberIfAvailableParkingNumberWrongArgument() {
	when(inputReaderUtil.readSelection()).thenReturn(3);
	
	parkingService.getNextParkingNumberIfAvailable();
	
	verify(inputReaderUtil, Mockito.times(1)).readSelection();
}


}


