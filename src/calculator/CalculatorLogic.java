package calculator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CalculatorLogic {
    private double memory = 0;
    private List<String> history = new ArrayList<>();
    private int historyIndex = -1;
    private final String historyFile = "history.txt";

    public CalculatorLogic() {
        loadHistoryFromFile(); // Tự động nạp lịch sử khi khởi tạo
    }

    // Basic operations
    public double add(double a, double b) { return a + b; }
    public double subtract(double a, double b) { return a - b; }
    public double multiply(double a, double b) { return a * b; }
    public double divide(double a, double b) { return a / b; }
    public double percent(double a) { return a / 100; }
    public double sqrt(double a) { return Math.sqrt(a); }
    public double square(double a) { return a * a; }
    public double reciprocal(double a) { return 1 / a; }
    public double changeSign(double a) { return -a; }
    public double exp(double base, double exponent) { return Math.pow(base, exponent); }
    public double log10(double a) { return Math.log10(a); }
    public double ln(double a) { return Math.log(a); }
    public double sin(double a) { return Math.sin(a); }
    public double cos(double a) { return Math.cos(a); }
    public double tan(double a) { return Math.tan(a); }
    public double toRadians(double deg) { return Math.toRadians(deg); }
    public double toDegrees(double rad) { return Math.toDegrees(rad); }

    // Memory functions
    public void memoryAdd(double a) { memory += a; }
    public void memorySubtract(double a) { memory -= a; }
    public double memoryRecall() { return memory; }
    public void memoryClear() { memory = 0; }

    // History management
    public void addHistory(String expr, double result) {
        String entry = expr + " = " + formatResult(result);
        history.add(entry);
        saveHistoryToFile(entry);
        historyIndex = history.size();
    }

    public List<String> getHistory() { return history; }

    public void deleteHistoryEntry(int index) {
        if (index >= 0 && index < history.size()) {
            history.remove(index);
            historyIndex = history.size();
            overwriteFullHistoryToFile();
        }
    }

    public List<String> searchHistory(String query) {
        List<String> res = new ArrayList<>();
        for (String e : history)
            if (e.contains(query)) res.add(e);
        return res;
    }

    // Expression evaluation
    public double evaluateExpression(String expr) throws Exception {
        double result = ExpressionParser.evaluate(expr);
        addHistory(expr, result);
        return result;
    }

    // Format output to remove .0 if unnecessary
    private String formatResult(double res) {
        return (res == (long) res) ? String.format("%d", (long) res) : Double.toString(res);
    }

    // File saving
    private void saveHistoryToFile(String entry) {
        try (FileWriter writer = new FileWriter(historyFile, true)) {
            writer.write(entry + "\n");
        } catch (IOException e) {
            System.err.println("Error saving history: " + e.getMessage());
        }
    }

    private void overwriteFullHistoryToFile() {
        try (FileWriter writer = new FileWriter(historyFile)) {
            for (String line : history) writer.write(line + "\n");
        } catch (IOException e) {
            System.err.println("Error overwriting history: " + e.getMessage());
        }
    }

    private void loadHistoryFromFile() {
        File file = new File(historyFile);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                history.add(line);
            }
            historyIndex = history.size();
        } catch (IOException e) {
            System.err.println("Error loading history: " + e.getMessage());
        }
    }
    public void clearHistory() {
    history.clear();
    }
}
