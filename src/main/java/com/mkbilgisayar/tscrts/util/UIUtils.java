package com.mkbilgisayar.tscrts.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class UIUtils {

    // --- Colors ---
    public static final Color ACCENT_COLOR = new Color(41, 128, 185); // Primary Action Blue
    public static final Color LOGO_ORANGE = new Color(255, 102, 0);   // Brand Orange
    public static final Color BUTTON_BG_DEFAULT = new Color(240, 240, 240);
    public static final Color BUTTON_FG_DEFAULT = new Color(50, 50, 50);

    // Status Colors
    public static final Color STATUS_PENDING = new Color(243, 156, 18);     // Amber
    public static final Color STATUS_IN_PROGRESS = new Color(52, 152, 219); // Blue
    public static final Color STATUS_COMPLETED = new Color(39, 174, 96);    // Green
    public static final Color STATUS_ARCHIVED = new Color(149, 165, 166);   // Gray

    // Category Colors (Muted)
    public static final Color CAT_HARDWARE = new Color(155, 89, 182);       // Muted Purple
    public static final Color CAT_NETWORK = new Color(52, 73, 94);          // Dark Slate
    public static final Color CAT_SQL = new Color(211, 84, 0);              // Muted Orange-Red
    public static final Color CAT_ERP = new Color(22, 160, 133);            // Muted Teal
    public static final Color CAT_ETRANS = new Color(241, 196, 15);         // Muted Yellow

    public static Color getStatusColor(String status) {
        if (status == null) return Color.GRAY;
        switch (status.toLowerCase()) {
            case "pending": return STATUS_PENDING;
            case "in_progress": return STATUS_IN_PROGRESS;
            case "completed": return STATUS_COMPLETED;
            case "archived": return STATUS_ARCHIVED;
            default: return Color.GRAY;
        }
    }

    public static Color getCategoryColor(String cat) {
        if (cat == null) return Color.GRAY;
        switch (cat.toLowerCase()) {
            case "hardware": return CAT_HARDWARE;
            case "network": return CAT_NETWORK;
            case "sql_database": return CAT_SQL;
            case "erp_installation": return CAT_ERP;
            case "e_transformation": return CAT_ETRANS;
            default: return Color.GRAY;
        }
    }

    // --- Formatters ---
    public static String formatCategory(String cat) {
        if (cat == null) return "Unknown";
        switch (cat.toLowerCase()) {
            case "hardware": return "🖥️ Hardware";
            case "network": return "🌐 Network";
            case "sql_database": return "🗄️ SQL Database";
            case "erp_installation": return "⚙️ ERP Installation";
            case "e_transformation": return "📄 E-Transformation";
            default: return cat;
        }
    }

    public static void styleComboBoxForEmojis(JComboBox<?> comboBox) {
        Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 13);
        comboBox.setFont(emojiFont);
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setFont(emojiFont);
                return c;
            }
        });
    }

    // --- Button Styling ---
    public static void styleButton(JButton btn) {
        applyBaseButtonStyle(btn);
        btn.setBackground(BUTTON_BG_DEFAULT);
        btn.setForeground(BUTTON_FG_DEFAULT);
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(6, 16, 6, 16)
        ));
    }

    public static void stylePrimaryButton(JButton btn) {
        applyBaseButtonStyle(btn);
        btn.setBackground(ACCENT_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setBorder(new CompoundBorder(
                new LineBorder(ACCENT_COLOR.darker(), 1, true),
                new EmptyBorder(6, 16, 6, 16)
        ));
        // FlatLaf specific property to prevent default background painting overriding ours easily
        btn.putClientProperty("JButton.buttonType", "roundRect");
    }

    public static void enableUndoRedo(JTextComponent... textComponents) {
        for (JTextComponent tc : textComponents) {
            UndoManager undoManager = new UndoManager();
            tc.getDocument().addUndoableEditListener(undoManager);

            InputMap inputMap = tc.getInputMap(JComponent.WHEN_FOCUSED);
            ActionMap actionMap = tc.getActionMap();

            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "Undo");
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "Redo");

            actionMap.put("Undo", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (undoManager.canUndo()) undoManager.undo();
                }
            });
            actionMap.put("Redo", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (undoManager.canRedo()) undoManager.redo();
                }
            });
        }
    }

    public static void styleSuccessButton(JButton btn) {
        applyBaseButtonStyle(btn);
        Color successColor = new Color(39, 174, 96);
        btn.setBackground(successColor);
        btn.setForeground(Color.WHITE);
        btn.setBorder(new CompoundBorder(
                new LineBorder(successColor.darker(), 1, true),
                new EmptyBorder(6, 16, 6, 16)
        ));
        btn.putClientProperty("JButton.buttonType", "roundRect");
    }

    public static void styleDangerButton(JButton btn) {
        applyBaseButtonStyle(btn);
        Color dangerColor = new Color(231, 76, 60);
        btn.setBackground(dangerColor);
        btn.setForeground(Color.WHITE);
        btn.setBorder(new CompoundBorder(
                new LineBorder(dangerColor.darker(), 1, true),
                new EmptyBorder(6, 16, 6, 16)
        ));
        btn.putClientProperty("JButton.buttonType", "roundRect");
    }

    private static void applyBaseButtonStyle(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Ensure minimum sizing for consistency
        Dimension size = btn.getPreferredSize();
        size.height = Math.max(size.height, 36);
        btn.setMinimumSize(new Dimension(100, 36));
        // Removed setPreferredSize to prevent text truncation
    }

    // --- Badge Creation ---
    public static JLabel createBadge(String text, Color bgColor, Color fgColor) {
        JLabel badge = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false); // Let our custom paint handle the background
        badge.setBackground(bgColor);
        badge.setForeground(fgColor);
        badge.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return badge;
    }
}
