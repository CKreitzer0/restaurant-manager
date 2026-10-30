package Restaurant_Manager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Restaurant Manager UI (pure Swing, no extra libraries).
 *
 * Only calls public methods on Restaurant / Floor / Table / Order / OrderItem /
 * Ticket / Kitchen / MenuItems. No data structure code lives here.
 *
 * LAYOUT:   header (stats)  |  floor  |  menu + order  |  kitchen  |  message bar
 *
 * TO RESTYLE: change the THEME section right below. Every color and font in the
 * app comes from those constants.
 */
public class RestaurantUI extends JFrame {

    // =====================================================================
    // THEME: edit these to restyle the whole app
    // =====================================================================
    private static final String FONT_FAMILY = "SansSerif";   // try "Segoe UI", "Helvetica Neue", "Verdana"

    private static final Color BG        = new Color(0xF4F1EC);  // window background
    private static final Color CARD      = Color.WHITE;          // panel background
    private static final Color INK       = new Color(0x2B2B2B);  // main text
    private static final Color MUTED     = new Color(0x8A8680);  // secondary text
    private static final Color LINE      = new Color(0xE6E1D9);  // borders and dividers
    private static final Color HEADER_BG = new Color(0x22262E);  // top bar
    private static final Color ACCENT    = new Color(0x4C5FD5);  // primary buttons, selection
    private static final Color RUSH      = new Color(0xF2994A);  // rush button
    private static final Color DANGER    = new Color(0xD64545);  // error text

    private static final Color EMPTY_COLOR = new Color(0x3FA773);  // table status colors
    private static final Color BUSY_COLOR  = new Color(0xE2594D);
    private static final Color DIRTY_COLOR = new Color(0xE7A721);

    private static Font font(int style, int size) {
        return new Font(FONT_FAMILY, style, size);
    }

    // =====================================================================
    // Fields
    // =====================================================================
    private final Restaurant restaurant = new Restaurant();
    private int selectedTable = -1;       // -1 = nothing selected
    private int partySize = 2;

    private TableCard[] cards;
    private JLabel partyLabel;

    private JLabel earningsValue, emptyChip, busyChip, dirtyChip, kitchenChip;

    private JLabel orderTitle, orderSub, totalValue;
    private final DefaultListModel<OrderItem> orderModel = new DefaultListModel<>();
    private final JList<OrderItem> orderList = new JList<>(orderModel);
    private RoundedButton[] menuButtons;
    private RoundedButton minusBtn, plusBtn, removeBtn, sendBtn, rushBtn,
            checkoutBtn, leaveBtn, cleanBtn;

    private JLabel kitchenCount;
    private final DefaultListModel<Ticket> kitchenModel = new DefaultListModel<>();
    private final JList<Ticket> kitchenList = new JList<>(kitchenModel);
    private RoundedButton cookBtn, undoBtn;

    private JLabel toast;

    // =====================================================================
    // Construction
    // =====================================================================
    public RestaurantUI() {
        super("Restaurant Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 16, 8, 16));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.insets = new Insets(0, 0, 0, 14);
        c.gridx = 0; c.weightx = 0.28; body.add(buildFloorPanel(), c);
        c.gridx = 1; c.weightx = 0.44; body.add(buildOrderPanel(), c);
        c.gridx = 2; c.weightx = 0.28; c.insets = new Insets(0, 0, 0, 0);
        body.add(buildKitchenPanel(), c);
        add(body, BorderLayout.CENTER);

        add(buildToast(), BorderLayout.SOUTH);

        refresh();
        setMinimumSize(new Dimension(1150, 760));
        setSize(1280, 860);
        setLocationRelativeTo(null);
    }

    // ---------- header ----------
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(HEADER_BG);
        h.setBorder(new EmptyBorder(14, 22, 14, 22));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Restaurant Manager");
        title.setFont(font(Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Floor, orders and kitchen in one place");
        sub.setFont(font(Font.PLAIN, 12));
        sub.setForeground(new Color(0xA8ADB8));
        left.add(title);
        left.add(sub);
        h.add(left, BorderLayout.WEST);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 8));
        chips.setOpaque(false);
        emptyChip = chip();
        busyChip = chip();
        dirtyChip = chip();
        kitchenChip = chip();
        chips.add(emptyChip);
        chips.add(busyChip);
        chips.add(dirtyChip);
        chips.add(kitchenChip);
        h.add(chips, BorderLayout.CENTER);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        JLabel eLabel = new JLabel("TODAY'S EARNINGS");
        eLabel.setFont(font(Font.BOLD, 10));
        eLabel.setForeground(new Color(0xA8ADB8));
        eLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        earningsValue = new JLabel("$0.00");
        earningsValue.setFont(font(Font.BOLD, 24));
        earningsValue.setForeground(Color.WHITE);
        earningsValue.setAlignmentX(Component.RIGHT_ALIGNMENT);
        right.add(eLabel);
        right.add(earningsValue);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    private JLabel chip() {
        JLabel l = new JLabel();
        l.setFont(font(Font.PLAIN, 13));
        l.setForeground(Color.WHITE);
        return l;
    }

    // ---------- floor ----------
    private JPanel buildFloorPanel() {
        CardPanel p = new CardPanel(new BorderLayout(0, 12));
        p.add(sectionTitle("Floor", "Click a table to select it"), BorderLayout.NORTH);

        int n = restaurant.getFloor().size();
        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
        grid.setOpaque(false);
        cards = new TableCard[n];
        for (int i = 0; i < n; i++) {
            cards[i] = new TableCard(i + 1);
            grid.add(cards[i]);
        }
        // wrapper keeps cards from stretching absurdly tall when there are few tables
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(grid, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(wrap);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        p.add(scroll, BorderLayout.CENTER);

        // seat group row
        JPanel seat = new JPanel(new GridBagLayout());
        seat.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 4, 0, 4);
        c.gridy = 0;

        JLabel lbl = new JLabel("Party of");
        lbl.setFont(font(Font.PLAIN, 14));
        lbl.setForeground(MUTED);
        c.gridx = 0; seat.add(lbl, c);

        RoundedButton minus = new RoundedButton("\u2212", RoundedButton.Style.SOFT);
        minus.addActionListener(e -> { if (partySize > 1) partySize--; partyLabel.setText("" + partySize); });
        c.gridx = 1; seat.add(minus, c);

        partyLabel = new JLabel("" + partySize, SwingConstants.CENTER);
        partyLabel.setFont(font(Font.BOLD, 20));
        partyLabel.setPreferredSize(new Dimension(36, 30));
        c.gridx = 2; seat.add(partyLabel, c);

        RoundedButton plus = new RoundedButton("+", RoundedButton.Style.SOFT);
        plus.addActionListener(e -> { if (partySize < 20) partySize++; partyLabel.setText("" + partySize); });
        c.gridx = 3; seat.add(plus, c);

        RoundedButton seatBtn = new RoundedButton("Seat group", RoundedButton.Style.PRIMARY);
        seatBtn.addActionListener(e -> seatGroup());
        c.gridx = 4; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 10, 0, 0);
        seat.add(seatBtn, c);
        p.add(seat, BorderLayout.SOUTH);
        return p;
    }

    // ---------- order ----------
    private JPanel buildOrderPanel() {
        CardPanel p = new CardPanel(new BorderLayout(0, 12));

        // top: title + menu
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setAlignmentX(Component.LEFT_ALIGNMENT);
        orderTitle = new JLabel("No table selected");
        orderTitle.setFont(font(Font.BOLD, 20));
        orderTitle.setForeground(INK);
        orderSub = new JLabel("Select a table on the floor to take an order");
        orderSub.setFont(font(Font.PLAIN, 13));
        orderSub.setForeground(MUTED);
        titles.add(orderTitle);
        titles.add(Box.createVerticalStrut(2));
        titles.add(orderSub);
        top.add(titles);
        top.add(Box.createVerticalStrut(14));

        JLabel menuLabel = smallCaps("ADD TO ORDER");
        menuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(menuLabel);
        top.add(Box.createVerticalStrut(6));

        MenuItems[] menu = restaurant.getMenu();
        JPanel menuGrid = new JPanel(new GridLayout(0, 2, 8, 8));
        menuGrid.setOpaque(false);
        menuGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuButtons = new RoundedButton[menu.length];
        for (int i = 0; i < menu.length; i++) {
            final int menuNum = i + 1;
            RoundedButton b = new RoundedButton(
                    menu[i].getName() + "   " + MenuItems.money(menu[i].getPrice()),
                    RoundedButton.Style.SOFT);
            b.addActionListener(e -> addItem(menuNum));
            menuButtons[i] = b;
            menuGrid.add(b);
        }
        top.add(menuGrid);
        top.add(Box.createVerticalStrut(14));
        JLabel orderLabel = smallCaps("CURRENT ORDER");
        orderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(orderLabel);
        p.add(top, BorderLayout.NORTH);

        // center: order lines
        orderList.setCellRenderer(new OrderLineRenderer());
        orderList.setFixedCellHeight(46);
        orderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = new JScrollPane(orderList);
        sp.setBorder(new LineBorder(LINE, 1, true));
        sp.getViewport().setBackground(CARD);
        p.add(sp, BorderLayout.CENTER);

        // bottom: controls
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        JPanel qtyRow = new JPanel(new GridLayout(1, 3, 8, 0));
        qtyRow.setOpaque(false);
        minusBtn  = new RoundedButton("\u2212 One", RoundedButton.Style.SOFT);
        plusBtn   = new RoundedButton("+ One", RoundedButton.Style.SOFT);
        removeBtn = new RoundedButton("Remove line", RoundedButton.Style.GHOST);
        minusBtn.addActionListener(e -> adjustSelected(-1));
        plusBtn.addActionListener(e -> adjustSelected(+1));
        removeBtn.addActionListener(e -> removeSelected());
        qtyRow.add(minusBtn);
        qtyRow.add(plusBtn);
        qtyRow.add(removeBtn);
        bottom.add(qtyRow);
        bottom.add(Box.createVerticalStrut(10));

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        JLabel tl = new JLabel("Total");
        tl.setFont(font(Font.PLAIN, 15));
        tl.setForeground(MUTED);
        totalValue = new JLabel("$0.00");
        totalValue.setFont(font(Font.BOLD, 24));
        totalValue.setForeground(INK);
        totalRow.add(tl, BorderLayout.WEST);
        totalRow.add(totalValue, BorderLayout.EAST);
        bottom.add(totalRow);
        bottom.add(Box.createVerticalStrut(10));

        JPanel sendRow = new JPanel(new GridLayout(1, 2, 8, 0));
        sendRow.setOpaque(false);
        sendBtn = new RoundedButton("Send to kitchen", RoundedButton.Style.PRIMARY);
        rushBtn = new RoundedButton("Rush to front", RoundedButton.Style.RUSH);
        sendBtn.addActionListener(e -> sendOrder(false));
        rushBtn.addActionListener(e -> sendOrder(true));
        sendRow.add(sendBtn);
        sendRow.add(rushBtn);
        bottom.add(sendRow);
        bottom.add(Box.createVerticalStrut(8));

        checkoutBtn = new RoundedButton("Check out", RoundedButton.Style.DARK);
        checkoutBtn.addActionListener(e -> checkout());
        checkoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel coRow = new JPanel(new GridLayout(1, 1));
        coRow.setOpaque(false);
        coRow.add(checkoutBtn);
        bottom.add(coRow);
        bottom.add(Box.createVerticalStrut(8));

        JPanel lastRow = new JPanel(new GridLayout(1, 2, 8, 0));
        lastRow.setOpaque(false);
        leaveBtn = new RoundedButton("Leave without ordering", RoundedButton.Style.GHOST);
        cleanBtn = new RoundedButton("Mark table clean", RoundedButton.Style.GHOST);
        leaveBtn.addActionListener(e -> leaveTable());
        cleanBtn.addActionListener(e -> cleanTable());
        lastRow.add(leaveBtn);
        lastRow.add(cleanBtn);
        bottom.add(lastRow);

        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    // ---------- kitchen ----------
    private JPanel buildKitchenPanel() {
        CardPanel p = new CardPanel(new BorderLayout(0, 12));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(sectionTitle("Kitchen line", "Front of the line is cooked first"), BorderLayout.CENTER);
        kitchenCount = new JLabel("0");
        kitchenCount.setFont(font(Font.BOLD, 26));
        kitchenCount.setForeground(ACCENT);
        head.add(kitchenCount, BorderLayout.EAST);
        p.add(head, BorderLayout.NORTH);

        kitchenList.setCellRenderer(new TicketRenderer());
        kitchenList.setFocusable(false);
        // tickets are display-only, so block selection
        kitchenList.setSelectionModel(new DefaultListSelectionModel() {
            @Override public void setSelectionInterval(int a, int b) { }
        });
        JScrollPane sp = new JScrollPane(kitchenList);
        sp.setBorder(new LineBorder(LINE, 1, true));
        sp.getViewport().setBackground(CARD);
        p.add(sp, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        buttons.setOpaque(false);
        cookBtn = new RoundedButton("Cook next ticket", RoundedButton.Style.PRIMARY);
        undoBtn = new RoundedButton("Undo newest", RoundedButton.Style.GHOST);
        cookBtn.addActionListener(e -> cookNext());
        undoBtn.addActionListener(e -> undoNewest());
        buttons.add(cookBtn);
        buttons.add(undoBtn);
        p.add(buttons, BorderLayout.SOUTH);
        return p;
    }

    // ---------- message bar ----------
    private JPanel buildToast() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 16, 14, 16));
        CardPanel inner = new CardPanel(new BorderLayout());
        inner.setBorder(new EmptyBorder(10, 16, 10, 16));
        toast = new JLabel();
        toast.setFont(font(Font.PLAIN, 14));
        inner.add(toast, BorderLayout.CENTER);
        bar.add(inner, BorderLayout.CENTER);
        say("Welcome! Pick a party size and seat a group to get started.");
        return bar;
    }

    // ---------- small helpers ----------
    private JPanel sectionTitle(String title, String subtitle) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(font(Font.BOLD, 20));
        t.setForeground(INK);
        JLabel s = new JLabel(subtitle);
        s.setFont(font(Font.PLAIN, 13));
        s.setForeground(MUTED);
        p.add(t);
        p.add(Box.createVerticalStrut(2));
        p.add(s);
        return p;
    }

    private JLabel smallCaps(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(Font.BOLD, 11));
        l.setForeground(MUTED);
        return l;
    }

    private static Color blend(Color a, Color b, float amountOfB) {
        float x = amountOfB;
        return new Color(
                Math.round(a.getRed()   * (1 - x) + b.getRed()   * x),
                Math.round(a.getGreen() * (1 - x) + b.getGreen() * x),
                Math.round(a.getBlue()  * (1 - x) + b.getBlue()  * x));
    }

    private static Color statusColor(String status) {
        if (status.equals(Status.EMPTY)) return EMPTY_COLOR;
        if (status.equals(Status.BUSY))  return BUSY_COLOR;
        return DIRTY_COLOR;
    }

    private static String hex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    private static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // =====================================================================
    // Custom components
    // =====================================================================

    /** White rounded panel with a soft border. */
    private static class CardPanel extends JPanel {
        CardPanel(LayoutManager lm) {
            super(lm);
            setOpaque(false);
            setBorder(new EmptyBorder(18, 18, 18, 18));
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.setColor(CARD);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
            g.setColor(LINE);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
            g.dispose();
            super.paintComponent(g0);
        }
    }

    /** Rounded button with hover and pressed states. */
    private static class RoundedButton extends JButton {
        enum Style { PRIMARY, DARK, RUSH, SOFT, GHOST }

        private final Style style;
        private boolean hover;

        RoundedButton(String text, Style style) {
            super(text);
            this.style = style;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(font(Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 14, 10, 14));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            boolean on = isEnabled();

            Color bg, fg;
            switch (style) {
                case PRIMARY: bg = ACCENT;                fg = Color.WHITE; break;
                case DARK:    bg = HEADER_BG;             fg = Color.WHITE; break;
                case RUSH:    bg = RUSH;                  fg = Color.WHITE; break;
                case SOFT:    bg = new Color(0xEEEAE3);   fg = INK;         break;
                default:      bg = CARD;                  fg = INK;         break;   // GHOST
            }
            if (!on) {
                bg = (style == Style.GHOST) ? CARD : new Color(0xEFECE7);
                fg = new Color(0xB5B0A8);
            } else if (getModel().isPressed()) {
                bg = blend(bg, Color.BLACK, 0.15f);
            } else if (hover) {
                bg = (style == Style.SOFT || style == Style.GHOST)
                        ? blend(bg, Color.BLACK, 0.06f) : blend(bg, Color.WHITE, 0.12f);
            }

            int w = getWidth(), h = getHeight();
            g.setColor(bg);
            g.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
            if (style == Style.GHOST) {
                g.setColor(on ? new Color(0xD9D3C9) : LINE);
                g.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
            }

            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            String text = getText();
            int tx = (w - fm.stringWidth(text)) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            g.setColor(fg);
            g.drawString(text, tx, ty);
            g.dispose();
        }
    }

    /** One table on the floor plan. */
    private class TableCard extends JComponent {
        private final int num;
        private boolean hover;

        TableCard(int num) {
            this.num = num;
            setPreferredSize(new Dimension(150, 118));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { selectedTable = TableCard.this.num; refresh(); }
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            Table t = restaurant.getFloor().getTable(num);
            Color sc = statusColor(t.getStatus());
            boolean selected = (num == selectedTable);
            int w = getWidth(), h = getHeight();

            g.setColor(blend(sc, Color.WHITE, hover ? 0.80f : 0.87f));
            g.fillRoundRect(2, 2, w - 5, h - 5, 20, 20);
            g.setStroke(new BasicStroke(selected ? 3.5f : 1.5f));
            g.setColor(selected ? ACCENT : blend(sc, Color.WHITE, 0.5f));
            g.drawRoundRect(2, 2, w - 5, h - 5, 20, 20);

            // "TABLE" + big number
            g.setColor(MUTED);
            g.setFont(font(Font.BOLD, 10));
            g.drawString("TABLE", 16, 26);
            g.setColor(INK);
            g.setFont(font(Font.BOLD, 34));
            g.drawString("" + t.getNum(), 15, 62);

            // seats, top right
            g.setFont(font(Font.PLAIN, 12));
            g.setColor(MUTED);
            String seats = "Seats " + t.getCapacity();
            g.drawString(seats, w - 16 - g.getFontMetrics().stringWidth(seats), 26);

            // status pill, bottom left
            String label = t.getStatus();
            g.setFont(font(Font.BOLD, 11));
            FontMetrics fm = g.getFontMetrics();
            int pw = fm.stringWidth(label) + 20, ph = 22, py = h - 38;
            g.setColor(sc);
            g.fillRoundRect(16, py, pw, ph, ph, ph);
            g.setColor(Color.WHITE);
            g.drawString(label, 16 + 10, py + (ph - fm.getHeight()) / 2 + fm.getAscent());

            // running total, bottom right (only when there is an order)
            Order o = t.getOrder();
            if (o != null && !o.isEmpty()) {
                String total = MenuItems.money(o.calculateTotal());
                g.setFont(font(Font.BOLD, 14));
                g.setColor(INK);
                g.drawString(total, w - 16 - g.getFontMetrics().stringWidth(total), py + 16);
            }
            g.dispose();
        }
    }

    /** One line of the current order: qty, name, price. */
    private static class OrderLineRenderer extends JPanel implements ListCellRenderer<OrderItem> {
        private final JLabel qty = new JLabel();
        private final JLabel name = new JLabel();
        private final JLabel price = new JLabel();

        OrderLineRenderer() {
            super(new BorderLayout(12, 0));
            setBorder(new EmptyBorder(8, 14, 8, 14));
            qty.setFont(font(Font.BOLD, 15));
            qty.setForeground(ACCENT);
            qty.setPreferredSize(new Dimension(44, 20));
            name.setFont(font(Font.PLAIN, 15));
            name.setForeground(INK);
            price.setFont(font(Font.BOLD, 15));
            price.setForeground(INK);
            add(qty, BorderLayout.WEST);
            add(name, BorderLayout.CENTER);
            add(price, BorderLayout.EAST);
        }

        @Override public Component getListCellRendererComponent(JList<? extends OrderItem> list,
                                                                OrderItem v, int index, boolean selected, boolean focus) {
            qty.setText(v.getQuantity() + "\u00D7");
            name.setText(v.getItem().getName());
            price.setText(MenuItems.money(v.getSubtotal()));
            setOpaque(true);
            setBackground(selected ? blend(ACCENT, Color.WHITE, 0.88f) : CARD);
            return this;
        }
    }

    /** One kitchen ticket card. */
    private static class TicketRenderer extends JPanel implements ListCellRenderer<Ticket> {
        private final JLabel head = new JLabel();
        private final JLabel wait = new JLabel();
        private final JLabel body = new JLabel();

        TicketRenderer() {
            super(new BorderLayout(0, 4));
            setBorder(new EmptyBorder(12, 14, 12, 14));
            head.setFont(font(Font.BOLD, 15));
            head.setForeground(INK);
            wait.setFont(font(Font.PLAIN, 12));
            wait.setForeground(MUTED);
            body.setFont(font(Font.PLAIN, 13));
            body.setForeground(INK);
            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            top.add(head, BorderLayout.CENTER);
            top.add(wait, BorderLayout.EAST);
            add(top, BorderLayout.NORTH);
            add(body, BorderLayout.CENTER);
        }

        @Override public Component getListCellRendererComponent(JList<? extends Ticket> list,
                                                                Ticket t, int index, boolean selected, boolean focus) {
            String next = (index == 0)
                    ? "  <font color='" + hex(ACCENT) + "' size='2'>NEXT UP</font>" : "";
            head.setText("<html>#" + t.getTicketNumber() + " \u00B7 Table " + t.getTableNumber() + next + "</html>");
            wait.setText("~" + t.getEstimatedWaitMinutes() + " min");
            StringBuilder sb = new StringBuilder("<html>");
            for (int i = 0; i < t.getItemCount(); i++) {
                sb.append(t.getItemQuantity(i)).append("\u00D7 ").append(t.getItemName(i)).append("<br>");
            }
            body.setText(sb.append("</html>").toString());
            setOpaque(true);
            setBackground(index == 0 ? blend(ACCENT, Color.WHITE, 0.92f) : CARD);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, LINE), new EmptyBorder(12, 14, 12, 14)));
            return this;
        }
    }

    // =====================================================================
    // Messages
    // =====================================================================
    private void say(String text) {
        toast.setText("<html><font color='" + hex(ACCENT) + "'>\u25CF</font>&nbsp;&nbsp;" + text + "</html>");
    }

    private void err(String text) {
        toast.setText("<html><font color='" + hex(DANGER) + "'>\u25CF</font>&nbsp;&nbsp;" + text + "</html>");
    }

    // =====================================================================
    // Actions: call Restaurant, show a message, refresh
    // =====================================================================
    private Table selectedTableObject() {
        return (selectedTable == -1) ? null : restaurant.getFloor().getTable(selectedTable);
    }

    private Order currentOrder() {
        Table t = selectedTableObject();
        return (t == null) ? null : t.getOrder();
    }

    private int quantityOf(Order order, String name) {
        for (int i = 0; i < order.size(); i++) {
            if (order.getOrderItem(i).getItem().getName().equals(name)) {
                return order.getOrderItem(i).getQuantity();
            }
        }
        return 0;
    }

    private void seatGroup() {
        Table t = restaurant.seatGroup(partySize);
        if (t == null) {
            err("No empty table fits a party of " + partySize + ". Try again when one is cleaned.");
        } else {
            selectedTable = t.getNum();
            say("Seated a party of " + partySize + " at table " + t.getNum() + ".");
        }
        refresh();
    }

    private void addItem(int menuNum) {
        Order order = currentOrder();
        if (order == null) { err("Select a seated table first."); return; }
        MenuItems item = restaurant.getMenuItems(menuNum);
        if (quantityOf(order, item.getName()) >= 99) {
            err("That's the maximum of 99 for one item.");
            return;
        }
        if (restaurant.addItem(selectedTable, menuNum, 1)) {
            say("Added " + item.getName() + ".");
        } else {
            err("Couldn't add " + item.getName() + ". The item may be unavailable or the order locked.");
        }
        refresh();
    }

    private void adjustSelected(int delta) {
        Order order = currentOrder();
        int idx = orderList.getSelectedIndex();
        if (order == null || idx == -1) { err("Click a line in the order first."); return; }
        OrderItem line = order.getOrderItem(idx);
        String name = line.getItem().getName();
        int newQty = line.getQuantity() + delta;
        boolean ok;
        if (newQty < 1) {
            ok = restaurant.removeItem(selectedTable, name);
        } else if (newQty > 99) {
            err("That's the maximum of 99 for one item.");
            return;
        } else {
            ok = restaurant.changeQuantity(selectedTable, name, newQty);
        }
        if (ok) say(name + (newQty < 1 ? " removed." : " is now " + newQty + "."));
        else    err("Couldn't change " + name + ". The order may be locked.");
        refresh();
    }

    private void removeSelected() {
        Order order = currentOrder();
        int idx = orderList.getSelectedIndex();
        if (order == null || idx == -1) { err("Click a line in the order first."); return; }
        String name = order.getOrderItem(idx).getItem().getName();
        if (restaurant.removeItem(selectedTable, name)) say("Removed " + name + ".");
        else err("Couldn't remove " + name + ". The order may be locked.");
        refresh();
    }

    private void sendOrder(boolean rush) {
        if (selectedTable == -1) { err("Select a table first."); return; }
        Ticket t = rush ? restaurant.rushOrder(selectedTable) : restaurant.sendOrder(selectedTable);
        if (t == null) {
            err("Couldn't send. The order may be empty or already sent.");
        } else {
            say((rush ? "Rush ticket #" : "Ticket #") + t.getTicketNumber() + " sent. Estimated wait "
                    + t.getEstimatedWaitMinutes() + " min.");
        }
        refresh();
    }

    private void checkout() {
        if (selectedTable == -1) { err("Select a table first."); return; }
        int tableNum = selectedTable;
        Order done = restaurant.checkout(tableNum);
        if (done == null) {
            err("Can't check out yet. The order may be empty, not sent, or still in the kitchen line.");
            return;
        }
        showReceipt(tableNum, done);      // 'done' still holds the items even though the table forgot them
        say("Table " + tableNum + " checked out and now needs cleaning.");
        refresh();
    }

    private void leaveTable() {
        if (selectedTable == -1) { err("Select a table first."); return; }
        if (restaurant.freeTable(selectedTable)) say("Table " + selectedTable + " is now dirty.");
        else err("Can't free that table. It may not be occupied, or a ticket is still in the kitchen.");
        refresh();
    }

    private void cleanTable() {
        if (selectedTable == -1) { err("Select a table first."); return; }
        if (restaurant.cleanTable(selectedTable)) say("Table " + selectedTable + " is clean and ready.");
        else err("That table isn't dirty.");
        refresh();
    }

    private void cookNext() {
        Ticket t = restaurant.getNextTicket();
        if (t == null) err("No pending tickets.");
        else say("Cooking ticket #" + t.getTicketNumber() + " for table " + t.getTableNumber() + ".");
        refresh();
    }

    private void undoNewest() {
        Ticket t = restaurant.undoLastTicket();
        if (t == null) err("No tickets to undo.");
        else say("Removed the newest ticket (#" + t.getTicketNumber() + ").");
        refresh();
    }

    // =====================================================================
    // Receipt dialog
    // =====================================================================
    private void showReceipt(int tableNum, Order order) {
        JDialog d = new JDialog(this, "Receipt", true);
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(24, 30, 20, 30));

        JLabel title = new JLabel("Receipt \u00B7 Table " + tableNum);
        title.setFont(font(Font.BOLD, 20));
        title.setForeground(INK);
        p.add(title, BorderLayout.NORTH);

        JPanel lines = new JPanel();
        lines.setOpaque(false);
        lines.setLayout(new BoxLayout(lines, BoxLayout.Y_AXIS));
        for (int i = 0; i < order.size(); i++) {
            OrderItem oi = order.getOrderItem(i);
            JPanel row = new JPanel(new BorderLayout(14, 0));
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(5, 0, 5, 0));
            JLabel l = new JLabel(oi.getQuantity() + "\u00D7  " + oi.getItem().getName());
            l.setFont(font(Font.PLAIN, 15));
            JLabel r = new JLabel(MenuItems.money(oi.getSubtotal()));
            r.setFont(font(Font.PLAIN, 15));
            row.add(l, BorderLayout.CENTER);
            row.add(r, BorderLayout.EAST);
            lines.add(row);
        }
        JSeparator sep = new JSeparator();
        sep.setForeground(LINE);
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.setBorder(new EmptyBorder(10, 0, 0, 0));
        JLabel tl = new JLabel("TOTAL");
        tl.setFont(font(Font.BOLD, 14));
        tl.setForeground(MUTED);
        JLabel tv = new JLabel(MenuItems.money(order.calculateTotal()));
        tv.setFont(font(Font.BOLD, 24));
        totalRow.add(tl, BorderLayout.WEST);
        totalRow.add(tv, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(lines, BorderLayout.NORTH);
        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        foot.add(sep, BorderLayout.NORTH);
        foot.add(totalRow, BorderLayout.CENTER);
        center.add(foot, BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);

        RoundedButton close = new RoundedButton("Done", RoundedButton.Style.PRIMARY);
        close.addActionListener(e -> d.dispose());
        p.add(close, BorderLayout.SOUTH);

        d.setContentPane(p);
        d.pack();
        d.setSize(Math.max(d.getWidth(), 380), d.getHeight());
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    // =====================================================================
    // Refresh: reread everything from Restaurant and repaint
    // =====================================================================
    private void refresh() {
        Floor floor = restaurant.getFloor();
        earningsValue.setText(MenuItems.money(restaurant.getTotalEarnings()));
        emptyChip.setText(chipText(EMPTY_COLOR, floor.countStatus(Status.EMPTY) + " empty"));
        busyChip.setText(chipText(BUSY_COLOR, floor.countStatus(Status.BUSY) + " busy"));
        dirtyChip.setText(chipText(DIRTY_COLOR, floor.countStatus(Status.DIRTY) + " dirty"));
        int pending = restaurant.getKitchen().size();
        kitchenChip.setText(chipText(ACCENT.brighter(), pending + (pending == 1 ? " ticket" : " tickets") + " waiting"));

        for (TableCard c : cards) c.repaint();
        refreshOrder();
        refreshKitchen();
    }

    private String chipText(Color dot, String text) {
        return "<html><font color='" + hex(dot) + "'>\u25CF</font>&nbsp;&nbsp;" + text + "</html>";
    }

    private void refreshOrder() {
        int keep = orderList.getSelectedIndex();
        orderModel.clear();

        Table t = selectedTableObject();
        Order order = (t == null) ? null : t.getOrder();
        boolean busy  = (t != null && t.getStatus().equals(Status.BUSY));
        boolean dirty = (t != null && t.getStatus().equals(Status.DIRTY));

        if (t == null) {
            orderTitle.setText("No table selected");
            orderSub.setText("Select a table on the floor to take an order");
            totalValue.setText("$0.00");
        } else {
            orderTitle.setText("Table " + t.getNum());
            String state = busy ? "Seated \u00B7 up to " + t.getCapacity() + " guests"
                    : dirty ? "Needs cleaning"
                    : "Empty and ready for a party";
            orderSub.setText(state);
            if (order != null) {
                for (int i = 0; i < order.size(); i++) orderModel.addElement(order.getOrderItem(i));
                totalValue.setText(MenuItems.money(order.calculateTotal()));
            } else {
                totalValue.setText("$0.00");
            }
        }
        if (keep >= 0 && orderModel.size() > 0) {
            orderList.setSelectedIndex(Math.min(keep, orderModel.size() - 1));
        }

        for (RoundedButton b : menuButtons) b.setEnabled(busy);
        minusBtn.setEnabled(busy);
        plusBtn.setEnabled(busy);
        removeBtn.setEnabled(busy);
        sendBtn.setEnabled(busy);
        rushBtn.setEnabled(busy);
        checkoutBtn.setEnabled(busy);
        leaveBtn.setEnabled(busy);
        cleanBtn.setEnabled(dirty);
    }

    private void refreshKitchen() {
        kitchenModel.clear();
        Kitchen k = restaurant.getKitchen();
        for (int i = 0; i < k.size(); i++) kitchenModel.addElement(k.getTicket(i));
        kitchenCount.setText("" + k.size());
        cookBtn.setEnabled(k.size() > 0);
        undoBtn.setEnabled(k.size() > 0);
    }

    // =====================================================================
    // Launcher
    // =====================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Metal look and feel so custom colors behave the same on every OS
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) { }
            new RestaurantUI().setVisible(true);
        });
    }
}