package calculator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.List;

public class CalculatorUI extends JFrame {
    private JTextField display;
    private CalculatorLogic logic = new CalculatorLogic();
    private boolean darkMode = false;
    private boolean isDegree = true;  // Biến lưu trạng thái đơn vị góc

    public CalculatorUI() {
        super("Advanced Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initUI();
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setupKeyBindings();
    }

    private void initUI() {
        display = new JTextField();
        display.setEditable(false);
        display.setFont(new Font("Consolas", Font.PLAIN, 28));
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setPreferredSize(new Dimension(500, 50));  // Điều chỉnh kích thước của display

        JPanel funcPanel = new JPanel(new GridLayout(1, 8, 5, 5)); // Điều chỉnh GridLayout cho bàn phím
        String[] funcLabels = {"MC", "C", "CE", "←", "%", "Copy", "Paste", "→"};  // Sửa các chức năng
        addButtons(funcPanel, funcLabels);

        JPanel advPanel = new JPanel(new GridLayout(1, 8, 5, 5)); // Điều chỉnh GridLayout cho bàn phím
        String[] advLabels = {"√", "^", "log", "ln", "sin", "cos", "tan", "="};
        addButtons(advPanel, advLabels);

        JPanel gridPanel = new JPanel(new GridLayout(5, 4, 5, 5)); // Điều chỉnh số dòng và cột trong gridPanel
        String[] gridLabels = {
            "7", "8", "9", "÷",
            "4", "5", "6", "×",
            "1", "2", "3", "−",
            "0", ".", "±", "+"};
        addButtons(gridPanel, gridLabels);

        // Bảng điều khiển
        JPanel controlPanel = new JPanel(new GridLayout(1, 4, 10, 5)); // Điều chỉnh số cột của bảng điều khiển
        JButton histBtn = new JButton("History");
        JButton clearHistBtn = new JButton("Clear History");
        JButton themeBtn = new JButton("D/L MODE");
        JButton unitBtn = new JButton("Toggle Unit (Deg/Rad)");
        styleButton(histBtn);
        styleButton(clearHistBtn);
        styleButton(themeBtn);
        styleButton(unitBtn);
        histBtn.addActionListener(e -> showHistory());
        clearHistBtn.addActionListener(e -> clearHistory());
        themeBtn.addActionListener(e -> switchTheme());
        unitBtn.addActionListener(e -> toggleUnit());
        controlPanel.add(histBtn);
        controlPanel.add(clearHistBtn);
        controlPanel.add(themeBtn);
        controlPanel.add(unitBtn);

        JPanel content = new JPanel();
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(display);
        content.add(Box.createRigidArea(new Dimension(0, 10)));
        content.add(funcPanel);
        content.add(Box.createRigidArea(new Dimension(0, 10)));
        content.add(advPanel);
        content.add(Box.createRigidArea(new Dimension(0, 10)));
        content.add(gridPanel);
        content.add(Box.createRigidArea(new Dimension(0, 10)));
        content.add(controlPanel);

        setContentPane(content);
        setupShortcuts();
    }

    private void addButtons(JPanel panel, String[] labels) {
        for (String txt : labels) {
            JButton btn = new JButton(txt);
            styleButton(btn);
            btn.addActionListener(this::handle);
            panel.add(btn);
        }
    }

    private void styleButton(JButton b) {
        b.setFont(new Font("Arial", Font.PLAIN, 20));
        b.setPreferredSize(new Dimension(85, 50)); // Điều chỉnh kích thước nút để chúng đều nhau
    }

    private void handle(ActionEvent evt) {
        String cmd = ((JButton)evt.getSource()).getText();
        try {
            switch (cmd) {
                case "C": display.setText(""); break;
                case "CE": backspace(); break;
                case "←": backspace(); break;
                case "%": handlePercentage(); break;
                case "MC": logic.memoryClear(); break;
                case "Copy": copyResultToClipboard(); break;
                case "Paste": pasteResultFromClipboard(); break;
                case "→": forward(); break;
                case "√": append("sqrt("); break;
                case "^": append("^"); break;
                case "log": append("log10("); break;
                case "ln": append("ln("); break;
                case "sin": appendSinCosTan("sin"); break;
                case "cos": appendSinCosTan("cos"); break;
                case "tan": appendSinCosTan("tan"); break;
                case "=": calculate(); break;
                case "÷": append("/"); break;
                case "×": append("*"); break;
                case "−": append("-"); break;
                case "+": append("+"); break;
                case "±": negate(); break;
                default:
                    if (cmd.matches("\\d|\\.")) append(cmd);
            }
        } catch (Exception ex) {
            display.setText("Error");
        }
    }

    private void append(String s) {
        display.setText(display.getText() + s);
    }

    private void backspace() {
        String t = display.getText();
        if (!t.isEmpty()) display.setText(t.substring(0, t.length()-1));
    }

    private double parseDisplay() {
        return display.getText().isEmpty() ? 0 : Double.parseDouble(display.getText());
    }

    private void negate() {
        double v = parseDisplay();
        display.setText(Double.toString(-v));
    }

    private void calculate() {
        try {
            String expr = display.getText().replace("×", "*").replace("÷", "/");
            expr = autoFixParentheses(expr);
            double res = logic.evaluateExpression(expr);
            String resultStr = (res == (long) res) ? String.format("%d", (long) res) : Double.toString(res);
            display.setText(resultStr);
            saveHistoryToFile(expr + " = " + resultStr);
        } catch (Exception e) {
            display.setText("Error");
        }
    }

    private String autoFixParentheses(String expr) {
        int open = 0, close = 0;
        for (char c : expr.toCharArray()) {
            if (c == '(') open++;
            else if (c == ')') close++;
        }
        int missing = open - close;
        return expr + ")".repeat(Math.max(0, missing));
    }

    private void showHistory() {
        List<String> h = logic.getHistory();
        JOptionPane.showMessageDialog(this,
            h.isEmpty() ? "No history." : String.join("\n", h),
            "Calculation History", JOptionPane.PLAIN_MESSAGE);
    }

    private void saveHistoryToFile(String entry) {
        try (FileWriter writer = new FileWriter("history.txt", true)) {
            writer.write(entry + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearHistory() {
        try {
            new FileWriter("history.txt").close();
            logic.clearHistory();
            JOptionPane.showMessageDialog(this, "History cleared.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchTheme() {
        darkMode = !darkMode;
        Color bg = darkMode ? new Color(45, 45, 45) : Color.WHITE;
        Color fg = darkMode ? Color.WHITE : Color.BLACK;
        getContentPane().setBackground(bg);
        display.setBackground(bg);
        display.setForeground(fg);
    }

    private void setupShortcuts() {
        display.getInputMap().put(KeyStroke.getKeyStroke("control C"), "copy");
        display.getActionMap().put("copy", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { display.copy(); }
        });
    }

    private void setupKeyBindings() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .addKeyEventDispatcher(e -> {
                if (e.getID() == KeyEvent.KEY_TYPED && Character.isDefined(e.getKeyChar())) {
                    char c = e.getKeyChar();
                    if (Character.isDigit(c) || "+-*/.^()%".indexOf(c) != -1) {
                        append(String.valueOf(c));
                    } else if (c == '\n') {
                        calculate();
                    }
                }
                return false;
            });
    }

    private void handlePercentage() {
        String currentText = display.getText();
        if (!currentText.isEmpty()) {
            try {
                double value = Double.parseDouble(currentText);
                double result = value / 100;
                display.setText(Double.toString(result));
            } catch (NumberFormatException e) {
                display.setText("Error");
            }
        }
    }

    private void toggleUnit() {
        isDegree = !isDegree;
        String unit = isDegree ? "Deg" : "Rad";
        JOptionPane.showMessageDialog(this, "Unit switched to " + unit);
    }

    private void appendSinCosTan(String function) {
        String angle = display.getText();
        try {
            double angleValue = Double.parseDouble(angle);
            if (isDegree) {
                angleValue = Math.toRadians(angleValue);  // Chuyển từ độ sang radian
            }
            append(function + "(" + angleValue + ")");
        } catch (NumberFormatException e) {
            display.setText("Error");
        }
    }

    private void copyResultToClipboard() {
        String result = display.getText();
        StringSelection selection = new StringSelection(result);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    private void pasteResultFromClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            String clipboardText = (String) clipboard.getData(DataFlavor.stringFlavor);
            display.setText(clipboardText);
        } catch (UnsupportedFlavorException | IOException e) {
            display.setText("Error");
        }
    }

    private void forward() {
        String currentText = display.getText();
        if (currentText.length() > 0) {
            String nextChar = currentText.substring(1);  // Di chuyển ký tự đầu tiên
            display.setText(nextChar);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CalculatorUI::new);
    }
}
