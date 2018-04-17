package com.company ;

import java.util.*;
import java.util.regex.Pattern ;
import java.util.regex.Matcher ;

public class Main {

    public static void main(String[] args) {
	// write your code here
        MathEvaluator me = new MathEvaluator() ;
        final String str = "1.7*-.2-(-3)" ;
//        final String str = "(-3 )" ;
        System.out.println(str) ;
        final Tokenizer tokenizer = new Tokenizer(str) ;
        while (tokenizer.hasNext()) {
            System.out.println(tokenizer.next()) ;
        }
        //me.tokenize("1.7*-.2-(-3)") ;
    }
}

class MathEvaluator {




    public MathEvaluator() {

    }

    public double calculate(String expression) {
        return 0.0 ;
    }


}

enum SymbolType {PLUS, MINUS, TIMES, DIV, OPEN_PAREN, CLOSE_PAREN, NUMBER, NULL,
    EXPRESSION, EXPRESSIONP, TERM, TERMP, FACTOR, END_OF_INPUT}

class Symbol {
    final SymbolType symbol_type;


    public Symbol(final SymbolType symbol_type) {
        this.symbol_type = symbol_type;
    }

    public String toString() {
        return symbol_type.toString() ;
    }

    public boolean isTerminal() {
        return false;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Symbol
                && ((Symbol)obj).symbol_type == symbol_type ;
    }

    public static final Symbol expression = new Symbol(SymbolType.EXPRESSION) ;
    public static final Symbol expressionp = new Symbol(SymbolType.EXPRESSIONP) ;
    public static final Symbol term = new Symbol(SymbolType.TERM) ;
    public static final Symbol termp = new Symbol(SymbolType.TERMP) ;
    public static final Symbol factor = new Symbol(SymbolType.FACTOR) ;
}

class Terminal extends Symbol {
    final String token ;

    public Terminal(final SymbolType symbol_type, final String token) {
        super(symbol_type) ;
        this.token = token ;
    }

    public Terminal(final SymbolType symbol_type, final char token) {
        this(symbol_type, new String(new char[] {token})) ;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Terminal
                && ((Terminal)obj).symbol_type == symbol_type
                && ((Terminal)obj).token.equals(token) ;
    }

    public boolean isTerminal() {
        return true ;
    }

    public String toString() {

        return new StringBuilder().append(symbol_type.toString()).append(':').append(token).toString() ;
    }

    static final Terminal times = new Terminal(SymbolType.TIMES, '*') ;
    static final Terminal div = new Terminal(SymbolType.DIV, '/') ;
    static final Terminal plus = new Terminal(SymbolType.PLUS, '+') ;
    static final Terminal minus = new Terminal(SymbolType.MINUS, '-') ;
    static final Terminal openParen = new Terminal(SymbolType.OPEN_PAREN, '(') ;
    static final Terminal closeParen = new Terminal(SymbolType.CLOSE_PAREN, ')') ;
    static final Terminal dummyNumber = new Terminal(SymbolType.NUMBER, "") ;
    static final Terminal endOfInput = new Terminal(SymbolType.END_OF_INPUT, "") ;
}

class Tokenizer implements Iterator<Symbol> {

    private static final String regular_expression = "[()*/+-]|(\\.\\d+)|((\\d*)(\\.\\d+)?)|(\\s+)" ;
    private static final Pattern pattern = Pattern.compile(regular_expression) ;

    private Symbol nextSymbol;
//    private final String in_string ;
    private final Matcher matcher ;
    private final int in_str_len ;
    private boolean previous_is_number ;


    public Tokenizer(final String in_string) {
        in_str_len = in_string.length() ;
        matcher = pattern.matcher(in_string) ;
        setNextToken() ;
    }

    @Override
    public boolean hasNext() {
        return nextSymbol != null && nextSymbol.symbol_type != SymbolType.END_OF_INPUT;
    }

    @Override
    public Symbol next() {
        if (hasNext()) {
            final Symbol tmp_symbol = nextSymbol;
            setNextToken() ;
            return tmp_symbol;
        }
        throw new NoSuchElementException() ;
    }

    private void setNextToken() {

//        System.out.println(matcher.toString()) ;
        if (!matcher.find()) {
            if (matcher.start() == in_str_len) {
                nextSymbol = null ;
                return ;
            }
            throw new RuntimeException("Unrecognized token starting at position: " + matcher.start()) ;
        }

        if (matcher.end() == matcher.start()) {
            // Zero length match
            nextSymbol = null ;
            return ;
        }

//        System.out.println(matcher.group().charAt(0)) ;
        boolean is_white_space = false ;
        while (!is_white_space) {
            switch (matcher.group().charAt(0)) {
                case '*':
                    nextSymbol = Terminal.times ;
                    return;
                case '/':
                    nextSymbol = Terminal.div ;
                    return;
                case '+':
                    nextSymbol = Terminal.plus ;
                    return;
                case '-':
                    // + and - may be unary or binary operators, depending on whether a number precedes
                    // the operator.
                    nextSymbol = Terminal.minus ;
                    return;
                case '.':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    nextSymbol = new Terminal(SymbolType.NUMBER, matcher.group());
                    previous_is_number = true;
                    return;
                case '(':
                    nextSymbol = Terminal.openParen ;
                    return;
                case ')':
                    nextSymbol = Terminal.closeParen ;
                    return;
                case ' ':
                case '\t':
                case '\n':
                case '\f':
                case '\r':
                    is_white_space = true ;
                    continue;
                default:
                    // This should not happen
                    assert false ;
            }
        }
    }
}

class ParseTree {

    public static Object parse(final String input, final ParseTable parseTable) {

        final Deque<SymbolType> parseStack  = new ArrayDeque<>() ;
        parseStack.push(Terminal.endOfInput.symbol_type) ;

        final Tokenizer tokens = new Tokenizer(input) ;
        Symbol currentToken = tokens.next() ;
        List<SymbolType> reverseRuleRHS ;

        boolean isError = false ;
        while (!isError && parseStack.size() != 0) {
            if (parseStack.peek() instanceof Terminal) {
                isError = !parseStack.peek().equals(currentToken) ;
                if (!isError) {
                    popAction((Terminal)parseStack.pop());
                }
            }
            else {
                reverseRuleRHS = parseTable.entry(parseStack.peek().symbol_type, currentToken.symbol_type) ;
                isError = reverseRuleRHS == null ;
                if (!isError) {
                    popAction(parseStack.pop()) ;
                    pushAction(reverseRuleRHS) ;
                    for (int i = 0 ; i !=  reverseRuleRHS.size() ; ++i) {
                        parseStack.push(reverseRuleRHS.get(i)) ;
                    }

                }

            }
        }
        return null ;
    }

    public static void popAction(final Symbol stackSymbol) {
        System.out.println("popped: " + stackSymbol.toString());
    }

    public static void pushAction(final List<SymbolType> reverseRuleRHS) {
        for (int i = 0 ; i !=  reverseRuleRHS.size() ; ++i) {
            System.out.println("push: " +  reverseRuleRHS.get(i).toString());
        }
    }
}

class ParseTable {

    private static final Map<SymbolType, Map<SymbolType, List<SymbolType>>> parseTable ;
    static {
        parseTable = new EnumMap<>(SymbolType.class) ;
        final List<SymbolType> emptyRowEntry = new ArrayList<>(1) ;
        emptyRowEntry.add(SymbolType.NULL) ;

        final List<SymbolType> parensExpressionRowEntry = new ArrayList<>(3) ;
        parensExpressionRowEntry.add(SymbolType.CLOSE_PAREN) ;
        parensExpressionRowEntry.add(SymbolType.EXPRESSION) ;
        parensExpressionRowEntry.add(SymbolType.OPEN_PAREN) ;

        final List<SymbolType> factorTermp = new ArrayList<>(2) ;
        factorTermp.add(SymbolType.TERMP) ;
        factorTermp.add(SymbolType.FACTOR) ;

        // E
        Map row = new EnumMap<SymbolType, ArrayList<SymbolType>>(SymbolType.class) ;
        // E -> - E
        List<SymbolType> rowEntry = new ArrayList<>(2) ;
        rowEntry.add(SymbolType.EXPRESSION) ;
        rowEntry.add(SymbolType.MINUS) ;
        row.put(SymbolType.MINUS, row) ;

        // E -> + E
        rowEntry = new ArrayList<>(2) ;
        rowEntry.add(SymbolType.EXPRESSION) ;
        rowEntry.add(SymbolType.PLUS) ;
        row.put(SymbolType.PLUS, row) ;

        // E -> ( E )
        row.put(SymbolType.OPEN_PAREN, parensExpressionRowEntry) ;

        // E -> T E'
        rowEntry = new ArrayList<>(2) ;
        rowEntry.add(SymbolType.EXPRESSIONP) ;
        rowEntry.add(SymbolType.TERM) ;

        parseTable.put(SymbolType.EXPRESSION, row) ;

        // E'
        row = new EnumMap<SymbolType, ArrayList<SymbolType>>(SymbolType.class) ;

        // E' -> eps
        row.put(SymbolType.END_OF_INPUT, emptyRowEntry) ;

        // E' -> -TE'
        rowEntry = new ArrayList<>(3) ;
        rowEntry.add(SymbolType.EXPRESSIONP) ;
        rowEntry.add(SymbolType.TERM) ;
        rowEntry.add(SymbolType.MINUS) ;
        row.put(SymbolType.MINUS, rowEntry) ;

        // E' -> +TE'
        rowEntry = new ArrayList<>(3) ;
        rowEntry.add(SymbolType.EXPRESSIONP) ;
        rowEntry.add(SymbolType.TERM) ;
        rowEntry.add(SymbolType.PLUS) ;
        row.put(SymbolType.PLUS, rowEntry) ;

        parseTable.put(SymbolType.EXPRESSIONP, row) ;

        // T
        row = new EnumMap<SymbolType, ArrayList<SymbolType>>(SymbolType.class) ;
        // T -> FT'
        row.put(SymbolType.OPEN_PAREN, factorTermp) ;
        row.put(SymbolType.NUMBER, factorTermp) ;

        parseTable.put(SymbolType.TERM, row) ;

        row = new EnumMap<SymbolType, ArrayList<SymbolType>>(SymbolType.class) ;
        // T' -> eps
        row.put(SymbolType.END_OF_INPUT, emptyRowEntry) ;
        row.put(SymbolType.CLOSE_PAREN, emptyRowEntry) ;

        // T' -> *FT'
        rowEntry = new ArrayList<>(3) ;
        rowEntry.add(SymbolType.TERMP) ;
        rowEntry.add(SymbolType.FACTOR) ;
        rowEntry.add(SymbolType.TIMES) ;
        row.put(SymbolType.TIMES, rowEntry) ;

        // T' -> /FT'
        rowEntry = new ArrayList<>(3) ;
        rowEntry.add(SymbolType.TERMP) ;
        rowEntry.add(SymbolType.FACTOR) ;
        rowEntry.add(SymbolType.DIV) ;
        row.put(SymbolType.DIV, rowEntry) ;

        parseTable.put(SymbolType.TERMP, row) ;

        // F
        row = new EnumMap<>(SymbolType.class) ;

        // F -> (E)
        row.put(SymbolType.OPEN_PAREN, parensExpressionRowEntry) ;

        // F -> NUMBER
        rowEntry = new ArrayList<>(1) ;
        rowEntry.add(SymbolType.NUMBER) ;

        row.put(SymbolType.NUMBER, rowEntry) ;

        parseTable.put(SymbolType.FACTOR, row) ;
    }

    List<SymbolType> entry(final SymbolType nonTerminal, final SymbolType terminal) {

        final Map<SymbolType, List<SymbolType>> rowMap = parseTable.get(nonTerminal) ;
        if (rowMap == null) {
            return null ;
        }
        return rowMap.get(terminal) ;
    }
}