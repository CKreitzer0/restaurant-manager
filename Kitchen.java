package Restaurant;

public class Kitchen {

    private LinkedDeque<Ticket> pendingTickets;

    public Kitchen() {
        pendingTickets = new LinkedDeque<>();
    }

    public void sendTicketToBack(Ticket ticket) {
        if (ticket == null) {
            System.out.println("Cannot add a null ticket.");
            return;
        }

        pendingTickets.addRear(ticket);
    }

    public void sendTicketToFront(Ticket ticket) {
        if (ticket == null) {
            System.out.println("Cannot add a null ticket.");
            return;
        }

        pendingTickets.addFront(ticket);
    }

    public Ticket undoNewestTicket() {
        Ticket removed = pendingTickets.removeNewest();

        if (removed == null) {
            System.out.println("No tickets to undo.");
        }

        return removed;
    }

    public Ticket getNextTicket() {
        Ticket next = pendingTickets.removeFront();

        if (next == null) {
            System.out.println("No pending tickets.");
        }

        return next;
    }

    public void printPendingTickets() {
        System.out.println("===== PENDING KITCHEN TICKETS =====");

        if (pendingTickets.isEmpty()) {
            System.out.println("No pending tickets.");
            return;
        }

        for (int i = 0; i < pendingTickets.size(); i++) {
            Ticket ticket = pendingTickets.get(i);
            ticket.printTicket();
        }
    }

    public Ticket getTicket(int index) {
        return pendingTickets.get(index);
    }

    public int size() {
        return pendingTickets.size();
    }

    public boolean isEmpty() {
        return pendingTickets.isEmpty();
    }
}
