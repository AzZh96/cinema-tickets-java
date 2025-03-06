package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    // Constructor
    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validateRequest(accountId, ticketTypeRequests);
        
        int totalAmount = calculateTotalAmount(ticketTypeRequests);
        int totalSeats = calculateTotalSeats(ticketTypeRequests);

        makePayment(accountId, totalAmount);
        reserveSeats(accountId, totalSeats);
    }

    // Helper Method: Validate ticket requests and account
    private void validateRequest(Long accountId, TicketTypeRequest... ticketTypeRequests) {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException();
        }
    
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException();
        }
    
        int totalTickets = 0;
        boolean hasAdultTicket = false;
    
        for (TicketTypeRequest request : ticketTypeRequests) {
            totalTickets += request.getNoOfTickets();
    
            if (request.getTicketType() == TicketTypeRequest.Type.ADULT) {
                hasAdultTicket = true;
            }
        }
    
        // Check totalTickets is within range
        if (totalTickets > 25) {
            throw new InvalidPurchaseException();
        }
    
        // Child and Infant tickets cannot be purchased without at least one Adult ticket
        for (TicketTypeRequest request : ticketTypeRequests) {
            if ((request.getTicketType() == TicketTypeRequest.Type.CHILD ||
                 request.getTicketType() == TicketTypeRequest.Type.INFANT) && !hasAdultTicket) {
                throw new InvalidPurchaseException();
            }
        }
    }
    

    // Helper Method: Calculate total payment amount
    private int calculateTotalAmount(TicketTypeRequest... ticketTypeRequests) {
        int totalAmount = 0;

        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            switch (ticketTypeRequest.getTicketType()) {
                case ADULT:
                    totalAmount += 25;
                    break;
                case CHILD:
                    totalAmount += 15;
                    break;
                case INFANT:
                    // Infants don't cost anything
                    break;
            }
        }

        return totalAmount;
    }

    // Helper Method: Calculate the total number of seats to reserve
    private int calculateTotalSeats(TicketTypeRequest... ticketTypeRequests) {
        int totalSeats = 0;

        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            if (ticketTypeRequest.getTicketType() != TicketTypeRequest.Type.INFANT) {
                totalSeats += ticketTypeRequest.getNoOfTickets();
            }
        }

        return totalSeats;
    }

    // Helper Method: Make the payment
    private void makePayment(Long accountId, int totalAmount) {
        ticketPaymentService.makePayment(accountId, totalAmount);
    }

    // Helper Method: Reserve the seats
    private void reserveSeats(Long accountId, int totalSeats) {
        seatReservationService.reserveSeat(accountId, totalSeats);
    }
}
