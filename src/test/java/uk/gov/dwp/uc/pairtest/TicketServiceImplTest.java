package uk.gov.dwp.uc.pairtest;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImplTest {

    private TicketPaymentService ticketPaymentService;
    private SeatReservationService seatReservationService;
    private TicketServiceImpl ticketService;

    @Before
    public void setUp() {
        ticketPaymentService = mock(TicketPaymentService.class);
        seatReservationService = mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
    }

    @Test
    public void testPurchaseTickets_withValidAdultTicket() {
        TicketTypeRequest adultTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);

        ticketService.purchaseTickets(1L, adultTicketRequest);

        verify(ticketPaymentService).makePayment(1L, 25);
        verify(seatReservationService).reserveSeat(1L, 1);
    }

    @Test
    public void testPurchaseTickets_withValidChildAndAdultTickets() {
        TicketTypeRequest adultTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest childTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);

        ticketService.purchaseTickets(1L, adultTicketRequest, childTicketRequest);

        verify(ticketPaymentService).makePayment(1L, 40);  // 25 + 15
        verify(seatReservationService).reserveSeat(1L, 2);  // 2 seats
    }

    @Test
    public void testPurchaseTickets_withAdultAndInfant() {
        TicketTypeRequest adultTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest infantTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        ticketService.purchaseTickets(1L, adultTicketRequest, infantTicketRequest);

        verify(ticketPaymentService).makePayment(1L, 25);  // Only Adult is charged
        verify(seatReservationService).reserveSeat(1L, 1); // Only Adult gets a seat
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTickets_withMoreThan25Tickets() {
        TicketTypeRequest adultTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26);
        ticketService.purchaseTickets(1L, adultTicketRequest); // Over ticket allowance
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTickets_withoutAdultTicket_whenChildOrInfantIsRequested() {
        TicketTypeRequest childTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);

        ticketService.purchaseTickets(1L, childTicketRequest);  // No Adult ticket
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTickets_withInvalidAccountId() {
        TicketTypeRequest adultTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);

        ticketService.purchaseTickets(0L, adultTicketRequest);  // Invalid accountId
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTickets_withNoTickets() {
        ticketService.purchaseTickets(1L);  // No tickets provided
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTickets_withNullAccountId() {
        TicketTypeRequest adultTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        ticketService.purchaseTickets(null, adultTicketRequest); // Null accountId
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTickets_withNullTicketRequests() {
        ticketService.purchaseTickets(1L, (TicketTypeRequest[]) null); // Null TicketTypeRequest 
    }

}
