package calculator;

import java.util.*;

public class ExpressionParser {
    private static final Set<String> OPERATORS = Set.of("+", "-", "*", "/", "%", "^");
    private static final Set<String> FUNCTIONS = Set.of("sin", "cos", "tan", "sqrt", "log", "ln", "abs", "exp", "rad", "deg");

    public static double evaluate(String expr) throws Exception {
        List<String> tokens = tokenize(expr);
        List<String> rpn = toRPN(tokens);
        return evalRPN(rpn);
    }

    // Tokenizer có xử lý số âm và hàm toán học
    private static List<String> tokenize(String expr) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        char[] chars = expr.replaceAll("\\s+", "").toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (Character.isDigit(c) || c == '.') {
                current.append(c);
            } else {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }

                if (Character.isLetter(c)) {
                    StringBuilder func = new StringBuilder();
                    while (i < chars.length && Character.isLetter(chars[i])) {
                        func.append(chars[i++]);
                    }
                    i--; // quay lại 1 ký tự
                    tokens.add(func.toString());
                } else if (c == '-' && (i == 0 || "()+-*/%^,".contains(String.valueOf(chars[i - 1])))) {
                    current.append(c); // là số âm
                } else {
                    tokens.add(String.valueOf(c));
                }
            }
        }

        if (current.length() > 0) tokens.add(current.toString());
        return tokens;
    }

    // Chuyển đổi sang hậu tố (RPN)
    private static List<String> toRPN(List<String> tokens) throws Exception {
        List<String> output = new ArrayList<>();
        Stack<String> ops = new Stack<>();

        for (String token : tokens) {
            if (isNumber(token)) {
                output.add(token);
            } else if (FUNCTIONS.contains(token)) {
                ops.push(token);
            } else if (token.equals(",")) {
                while (!ops.isEmpty() && !ops.peek().equals("(")) {
                    output.add(ops.pop());
                }
            } else if (OPERATORS.contains(token)) {
                while (!ops.isEmpty() && precedence(ops.peek()) >= precedence(token)) {
                    output.add(ops.pop());
                }
                ops.push(token);
            } else if (token.equals("(")) {
                ops.push(token);
            } else if (token.equals(")")) {
                while (!ops.isEmpty() && !ops.peek().equals("(")) {
                    output.add(ops.pop());
                }
                if (ops.isEmpty()) throw new Exception("Thiếu dấu mở ngoặc '('");
                ops.pop(); // Bỏ "("
                if (!ops.isEmpty() && FUNCTIONS.contains(ops.peek())) {
                    output.add(ops.pop());
                }
            }
        }

        while (!ops.isEmpty()) {
            if (ops.peek().equals("(") || ops.peek().equals(")"))
                throw new Exception("Lỗi ngoặc đơn");
            output.add(ops.pop());
        }

        return output;
    }

    private static int precedence(String op) {
        switch (op) {
            case "+": case "-": return 1;
            case "*": case "/": case "%": return 2;
            case "^": return 3;
            default: return 4; // Hàm toán học ưu tiên cao nhất
        }
    }

    private static double evalRPN(List<String> rpn) throws Exception {
        Stack<Double> st = new Stack<>();
        for (String token : rpn) {
            if (isNumber(token)) {
                st.push(Double.valueOf(token));
            } else if (OPERATORS.contains(token)) {
                double b = st.pop();
                double a = st.pop();
                switch (token) {
                    case "+": st.push(a + b); break;
                    case "-": st.push(a - b); break;
                    case "*": st.push(a * b); break;
                    case "/": st.push(a / b); break;
                    case "%": st.push(a % b); break;
                    case "^": st.push(Math.pow(a, b)); break;
                }
            } else if (FUNCTIONS.contains(token)) {
                double a = st.pop();
                switch (token) {
                    case "sin": st.push(Math.sin(Math.toRadians(a))); break;
                    case "cos": st.push(Math.cos(Math.toRadians(a))); break;
                    case "tan": st.push(Math.tan(Math.toRadians(a))); break;
                    case "sqrt": st.push(Math.sqrt(a)); break;
                    case "log": st.push(Math.log10(a)); break;
                    case "ln": st.push(Math.log(a)); break;
                    case "abs": st.push(Math.abs(a)); break;
                    case "exp": st.push(Math.exp(a)); break;
                    case "rad": st.push(Math.toRadians(a)); break;
                    case "deg": st.push(Math.toDegrees(a)); break;
                }
            } else {
                throw new Exception("Token không hợp lệ: " + token);
            }
        }
        return st.pop();
    }

    private static boolean isNumber(String s) {
        return s.matches("-?\\d+(\\.\\d+)?");
    }
}