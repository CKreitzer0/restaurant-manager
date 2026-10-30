package Restaurant_Manager;

import javax.swing.*;
import java.awt.*;

/**
 * Simple version of the Restaurant Manager UI.
 * Standard Swing components only. Every button does the same three things:
 *   1. call a Restaurant method
 *   2. show a message
 *   3. call refresh() to redraw
 */
public class RestaurantUISimple extends JFrame {

    // ----- Colors and fonts (change these to restyle) -----
    private static final Color EMPTY_COLOR = new Color(0x8FD19E);   // green
    private static final Color BUSY_COLOR  = new Color(0xF28B82);   // red
    private static final Color DIRTY_COLOR = new Color(0xFDD663);   // yellow
    private static final Color SELECTED    = new Color(0x3F51B5);   // blue outline
    private static final Font  NORMAL = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font  BOLD   = new Font("SansSerif", Font.BOLD, 14);
    private static final Font  TITLE  = new Font("SansSerif", Font.BOLD, 20);

    private final Restaurant restaurant = new Restaurant();
    private int selectedTable = -1;          // -1 means nothing selected
    private int partySize = 2;

    // things we need to update later
    private JButton[] tableButtons;
    private JLabel earningsLabel, partyLabel, orderTitle, totalLabel, message;
    private DefaultListModel<String> orderModel = new DefaultListModel<>();
    private JList<String> orderList = new JList<>(orderModel);
    private DefaultListModel<String> kitchenModel = new DefaultListModel<>();
    private JList<String> kitchenList = new JList<>(kitchenModel);

    public RestaurantUISimple() {
        super("Restaurant Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(makeTopBar(), BorderLayout.NORTH);
        add(makeFloorPanel(), BorderLayout.WEST);
        add(makeOrderPanel(), BorderLayout.CENTER);
        add(makeKitchenPanel(), BorderLayout.EAST);

        message = new JLabel("Pick a party size and click Seat group to begin.");
        message.setFont(NORMAL);
        add(message, BorderLayout.SOUTH);

        refresh();
        setSize(1100, 700);
        setLocationRelativeTo(null);
    }

    // =====================================================================
    // Building the screen
    // =====================================================================
    private JPanel makeTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Restaurant Manager");
        title.setFont(TITLE);
        earningsLabel = new JLabel();
        earningsLabel.setFont(TITLE);
        bar.add(title, BorderLayout.WEST);
        bar.add(earningsLabel, BorderLayout.EAST);
        return bar;
    }

    private JPanel makeFloorPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Floor"));

        int n = restaurant.getFloor().size();
        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 8));
        tableButtons = new JButton[n];
        for (int i = 0; i < n; i++) {
            final int tableNum = i + 1;
            JButton b = new JButton();
            b.setFont(NORMAL);
            b.setOpaque(true);
            b.setFocusPainted(false);
            b.setPreferredSize(new Dimension(110, 75));
            b.addActionListener(e -> { selectedTable = tableNum; refresh(); });
            tableButtons[i] = b;
            grid.add(b);
        }
        panel.add(grid, BorderLayout.CENTER);

        // party size row
        JPanel seat = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        JButton minus = new JButton("-");
        JButton plus = new JButton("+");
        partyLabel = new JLabel("Party of " + partySize);
        partyLabel.setFont(BOLD);
        JButton seatButton = new JButton("Seat group");
        minus.addActionListener(e -> { if (partySize > 1) partySize--; partyLabel.setText("Party of " + partySize); });
        plus.addActionListener(e -> { if (partySize < 20) partySize++; partyLabel.setText("Party of " + partySize); });
        seatButton.addActionListener(e -> seatGroup());
        seat.add(minus);
        seat.add(partyLabel);
        seat.add(plus);
        seat.add(seatButton);
        panel.add(seat, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel makeOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Order"));

        orderTitle = new JLabel("No table selected");
        orderTitle.setFont(BOLD);

        // menu buttons across the top
        MenuItems[] menu = restaurant.getMenu();
        JPanel menuPanel = new JPanel(new GridLayout(0, 2, 6, 6));
        for (int i = 0; i < menu.length; i++) {
            final int menuNum = i + 1;
            JButton b = new JButton(menu[i].getName() + " " + MenuItems.money(menu[i].getPrice()));
            b.addActionListener(e -> addItem(menuNum));
            menuPanel.add(b);
        }
        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.add(orderTitle, BorderLayout.NORTH);
        top.add(menuPanel, BorderLayout.CENTER);
        panel.add(top, BorderLayout.NORTH);

        // the order list
        orderList.setFont(NORMAL);
        panel.add(new JScrollPane(orderList), BorderLayout.CENTER);

        // total + buttons
        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(TITLE);

        JPanel buttons = new JPanel(new GridLayout(0, 3, 6, 6));
        addButton(buttons, "+1", e -> adjustSelected(1));
        addButton(buttons, "-1", e -> adjustSelected(-1));
        addButton(buttons, "Remove", e -> adjustSelected(-999));
        addButton(buttons, "Send", e -> sendOrder(false));
        addButton(buttons, "Rush", e -> sendOrder(true));
        addButton(buttons, "Check out", e -> checkout());
        addButton(buttons, "Leave", e -> leaveTable());
        addButton(buttons, "Mark clean", e -> cleanTable());

        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.add(totalLabel, BorderLayout.NORTH);
        bottom.add(buttons, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel makeKitchenPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Kitchen (top = next)"));
        panel.setPreferredSize(new Dimension(280, 0));

        kitchenList.setFont(NORMAL);
        panel.add(new JScrollPane(kitchenList), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 6, 0));
        addButton(buttons, "Cook next", e -> cookNext());
        addButton(buttons, "Undo", e -> undoNewest());
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void addButton(JPanel panel, String text, java.awt.event.ActionListener action) {
        JButton b = new JButton(text);
        b.addActionListener(action);
        panel.add(b);
    }

    // =====================================================================
    // Actions
    // =====================================================================
    private Order currentOrder() {
        if (selectedTable == -1) return null;
        return restaurant.getFloor().getTable(selectedTable).getOrder();
    }

    private void seatGroup() {
        Table t = restaurant.seatGroup(partySize);
        if (t == null) {
            message.setText("No empty table fits a party of " + partySize + ".");
        } else {
            selectedTable = t.getNum();
            message.setText("Seated a party of " + partySize + " at table " + t.getNum() + ".");
        }
        refresh();
    }

    private void addItem(int menuNum) {
        if (currentOrder() == null) { message.setText("Select a seated table first."); return; }
        if (restaurant.addItem(selectedTable, menuNum, 1)) {
            message.setText("Added " + restaurant.getMenuItems(menuNum).getName() + ".");
        } else {
            message.setText("Couldn't add that item.");
        }
        refresh();
    }

    // change = +1 or -1; a very negative number removes the whole line
    private void adjustSelected(int change) {
        Order order = currentOrder();
        int index = orderList.getSelectedIndex();
        if (order == null || index == -1) { message.setText("Click a line in the order first."); return; }

        OrderItem line = order.getOrderItem(index);
        String name = line.getItem().getName();
        int newQty = line.getQuantity() + change;

        if (newQty < 1) {
            restaurant.removeItem(selectedTable, name);
        } else if (newQty <= 99) {
            restaurant.changeQuantity(selectedTable, name, newQty);
        } else {
            message.setText("Maximum is 99 per item.");
            return;
        }
        refresh();
    }

    private void sendOrder(boolean rush) {
        if (selectedTable == -1) { message.setText("Select a table first."); return; }
        Ticket t = rush ? restaurant.rushOrder(selectedTable) : restaurant.sendOrder(selectedTable);
        if (t == null) message.setText("Couldn't send. The order may be empty or already sent.");
        else message.setText("Ticket #" + t.getTicketNumber() + " sent (about " + t.getEstimatedWaitMinutes() + " min).");
        refresh();
    }

    private void checkout() {
        if (selectedTable == -1) { message.setText("Select a table first."); return; }
        int tableNum = selectedTable;
        Order done = restaurant.checkout(tableNum);
        if (done == null) {
            message.setText("Can't check out yet. The order may be empty or not sent.");
            return;
        }
        // build the receipt text from the finished order
        String receipt = "Table " + tableNum + "\n\n";
        for (int i = 0; i < done.size(); i++) {
            OrderItem oi = done.getOrderItem(i);
            receipt += oi.getQuantity() + " x " + oi.getItem().getName()
                    + "   " + MenuItems.money(oi.getSubtotal()) + "\n";
        }
        receipt += "\nTOTAL: " + MenuItems.money(done.calculateTotal());
        JOptionPane.showMessageDialog(this, receipt, "Receipt", JOptionPane.PLAIN_MESSAGE);
        message.setText("Table " + tableNum + " checked out. It needs cleaning.");
        refresh();
    }

    private void leaveTable() {
        if (selectedTable == -1) { message.setText("Select a table first."); return; }
        if (restaurant.freeTable(selectedTable)) message.setText("Table " + selectedTable + " is now dirty.");
        else message.setText("Can't free that table right now.");
        refresh();
    }

    private void cleanTable() {
        if (selectedTable == -1) { message.setText("Select a table first."); return; }
        if (restaurant.cleanTable(selectedTable)) message.setText("Table " + selectedTable + " is clean.");
        else message.setText("That table isn't dirty.");
        refresh();
    }

    private void cookNext() {
        Ticket t = restaurant.getNextTicket();
        if (t == null) message.setText("No pending tickets.");
        else message.setText("Cooking ticket #" + t.getTicketNumber() + " for table " + t.getTableNumber() + ".");
        refresh();
    }

    private void undoNewest() {
        Ticket t = restaurant.undoLastTicket();
        if (t == null) message.setText("No tickets to undo.");
        else message.setText("Removed ticket #" + t.getTicketNumber() + ".");
        refresh();
    }

    // =====================================================================
    // Redraw everything from the current state
    // =====================================================================
    private void refresh() {
        earningsLabel.setText("Earnings: " + MenuItems.money(restaurant.getTotalEarnings()));

        // floor buttons
        Table[] tables = restaurant.getFloor().getTables();
        for (int i = 0; i < tables.length; i++) {
            Table t = tables[i];
            JButton b = tableButtons[i];
            b.setText("<html><center><b>Table " + t.getNum() + "</b><br>Seats " + t.getCapacity()
                    + "<br>" + t.getStatus() + "</center></html>");
            if (t.getStatus().equals(Status.EMPTY)) b.setBackground(EMPTY_COLOR);
            else if (t.getStatus().equals(Status.BUSY)) b.setBackground(BUSY_COLOR);
            else b.setBackground(DIRTY_COLOR);
            b.setBorder(BorderFactory.createLineBorder(
                    t.getNum() == selectedTable ? SELECTED : Color.GRAY,
                    t.getNum() == selectedTable ? 4 : 1));
        }

        // order list
        int keep = orderList.getSelectedIndex();
        orderModel.clear();
        Order order = currentOrder();
        if (selectedTable == -1) {
            orderTitle.setText("No table selected");
        } else {
            orderTitle.setText("Table " + selectedTable + " (" + restaurant.getFloor().getTable(selectedTable).getStatus() + ")");
        }
        if (order != null) {
            for (int i = 0; i < order.size(); i++) {
                OrderItem oi = order.getOrderItem(i);
                orderModel.addElement(oi.getQuantity() + " x " + oi.getItem().getName()
                        + "   " + MenuItems.money(oi.getSubtotal()));
            }
            totalLabel.setText("Total: " + MenuItems.money(order.calculateTotal()));
        } else {
            totalLabel.setText("Total: $0.00");
        }
        if (keep >= 0 && orderModel.size() > 0) {
            orderList.setSelectedIndex(Math.min(keep, orderModel.size() - 1));
        }

        // kitchen list
        kitchenModel.clear();
        Kitchen k = restaurant.getKitchen();
        for (int i = 0; i < k.size(); i++) {
            Ticket t = k.getTicket(i);
            // HTML lets one list entry span several lines
            String entry = "<html><b>#" + t.getTicketNumber() + "  Table " + t.getTableNumber() + "</b>";
            for (int j = 0; j < t.getItemCount(); j++) {
                entry += "<br>" + t.getItemQuantity(j) + "x " + t.getItemName(j);
            }
            kitchenModel.addElement(entry + "<br>&nbsp;</html>");
        }
    }

    // =====================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Metal look and feel so button colors show on every OS
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) { }
            new RestaurantUISimple().setVisible(true);
        });
    }
}