package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import static com.parkit.parkingsystem.constants.ParkingType.CAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

public static final String UPDATE_IN_TIME_FOR_TEST = "UPDATE `ticket` SET  IN_TIME=? WHERE ID=?";
private static final DataBaseTestConfig DATA_BASE_TEST_CONFIG = new DataBaseTestConfig();
private static ParkingSpotDAO parkingSpotDAO;
private static TicketDAO ticketDAO;
private static DataBasePrepareService dataBasePrepareService;
@Mock
private static InputReaderUtil inputReaderUtil;


@BeforeAll
static void setUp() {
	parkingSpotDAO = new ParkingSpotDAO();
	parkingSpotDAO.dataBaseConfig = DATA_BASE_TEST_CONFIG;
	ticketDAO = new TicketDAO();
	ticketDAO.dataBaseConfig = DATA_BASE_TEST_CONFIG;
	dataBasePrepareService = new DataBasePrepareService();
	
}

@BeforeEach
void setUpPerTest() {
	when(inputReaderUtil.readSelection()).thenReturn(1);
	when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
}

@AfterEach
void afterEachTestTearDown() {
	dataBasePrepareService.clearDataBaseEntries();
}

private void ticketInTimeModificationForProcessTest(int ticketId) throws SQLException, ClassNotFoundException {
	
	Date inTime = new Date(System.currentTimeMillis() - 60 * 60 * 1000);
	
	Connection con;
	con = DATA_BASE_TEST_CONFIG.getConnection();
	PreparedStatement ps = con.prepareStatement(UPDATE_IN_TIME_FOR_TEST);
	ps.setTimestamp(1, new Timestamp(inTime.getTime()));
	ps.setInt(2, ticketId);
	ps.execute();
}

@Test
void parkingACarTest() {
	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	
	parkingService.processIncomingVehicle();
	
	assertEquals("ABCDEF", ticketDAO.getTicket("ABCDEF").getVehicleRegNumber());
	assertEquals(2, parkingSpotDAO.getNextAvailableSlot(CAR));
}

@Test
void parkingLotExitTest() throws SQLException, ClassNotFoundException {
	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	
	parkingService.processIncomingVehicle();
	
	ticketInTimeModificationForProcessTest(1);
	System.out.println("ATTENTION: inTime modified by Instant.now -1hour, to test with a calcul fare > 0€.");
	System.out.println("The current inTime is: " + ticketDAO.getTicket("ABCDEF").getInTime());
	
	parkingService.processExitingVehicle();
	
	assertNotNull(ticketDAO.getTicket("ABCDEF").getOutTime());
	assertEquals(1.50, ticketDAO.getTicket("ABCDEF").getPrice());
}

@Test
void parkingLotExitRecurringUserTest() throws SQLException, ClassNotFoundException {
	
	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	
	// First come
	parkingService.processIncomingVehicle();
	parkingService.processExitingVehicle();
	
	// Second come
	parkingService.processIncomingVehicle();
	ticketInTimeModificationForProcessTest(2);
	System.out.println("ATTENTION: inTime modified by Instant.now -1hour, to test with a calcul fare > 0€.");
	System.out.println("The current inTime is: " + ticketDAO.getTicket("ABCDEF").getInTime());
	
	parkingService.processExitingVehicle();
	
	assertEquals(1.42, ticketDAO.getTicket("ABCDEF").getPrice());
	
}
}
