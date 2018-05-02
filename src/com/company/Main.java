package com.company ;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern ;
import java.util.regex.Matcher ;

public class Main {

    public static void main(String[] args) throws IOException {
	// write your code here
        MathEvaluator me = new MathEvaluator() ;
//        final String str = "1" ;
//        final String str = "1.7*-.2" ;
//        final String str = "1.7*4" ;
//        final String str = "3*(4+2)" ;
        final BufferedReader in_reader = new BufferedReader(new InputStreamReader(System.in)) ;
        final String str = in_reader.readLine() ;
        System.err.println(str) ;
        final Tokenizer tokenizer = new Tokenizer(str) ;
        while (tokenizer.hasNext()) {
            System.err.println(tokenizer.next()) ;
        }

        ParseTree.compute(str) ;
    }
}

class MathEvaluator {

    public MathEvaluator() {

    }

    public double calculate(String expression) {
        return 0.0 ;
    }


}

enum SymbolType {
    UNARY_PLUS, UNARY_MINUS, BINARY_PLUS, BINARY_MINUS, BINARY_TIMES, BINARY_DIV,
    OPEN_PAREN, CLOSE_PAREN, NUMBER, NULL, END_OF_INPUT,
    EXPRESSION, TERM, MORE_TERMS, FACTOR, MORE_FACTORS,
    UNARY_PLUS_PF, UNARY_MINUS_PF, BINARY_PLUS_PF, BINARY_MINUS_PF, BINARY_TIMES_PF, BINARY_DIV_PF ;


    static boolean isTerminal(final SymbolType symbolType) {

        switch (symbolType) {
            case UNARY_PLUS:
            case BINARY_PLUS:
            case UNARY_MINUS:
            case BINARY_MINUS:
            case BINARY_TIMES:
            case BINARY_DIV:
            case OPEN_PAREN:
            case CLOSE_PAREN:
            case NUMBER:
            case NULL:
            case END_OF_INPUT:
                return true ;
            default:
                return false ;
        }
    }

    static boolean isNonTerminal(final SymbolType symbolType) {
        switch(symbolType) {
            case EXPRESSION:
            case TERM:
            case MORE_TERMS:
            case FACTOR:
            case MORE_FACTORS:
                return true ;
            default:
                return false ;
        }
    }

    static boolean isPostFixOperator(final SymbolType symbolType) {
        switch (symbolType) {
            case UNARY_PLUS_PF:
            case UNARY_MINUS_PF:
            case BINARY_PLUS_PF:
            case BINARY_MINUS_PF:
            case BINARY_TIMES_PF:
            case BINARY_DIV_PF:
                return true ;
            default:
                return false ;
        }
    }
}

class Symbol {

    public final SymbolType symbol_type ;

    protected Symbol(final SymbolType symbol_type) {
        this.symbol_type = symbol_type ;
    }
}

class NonTerminal extends Symbol {

    public final List<Symbol> children ;

    protected NonTerminal(final SymbolType symbol_type, final List<Symbol> children) {
        super(symbol_type) ;
        this.children = children ;
    }

    public NonTerminal(final SymbolType symbol_type) {
        this(symbol_type, new ArrayList<>()) ;
    }
}

class Terminal extends Symbol {

    protected Terminal(final SymbolType symbol_type) {
        super(symbol_type) ;
    }

    public static final Terminal binary_plus = new Terminal(SymbolType.BINARY_PLUS) ;
    public static final Terminal unary_plus = new Terminal(SymbolType.UNARY_PLUS) ;
    public static final Terminal binary_minus = new Terminal(SymbolType.BINARY_MINUS) ;
    public static final Terminal unary_minus = new Terminal(SymbolType.UNARY_MINUS) ;
    public static final Terminal binary_times = new Terminal(SymbolType.BINARY_TIMES) ;
    public static final Terminal binary_div = new Terminal(SymbolType.BINARY_DIV) ;
    public static final Terminal open_paren = new Terminal(SymbolType.OPEN_PAREN) ;
    public static final Terminal close_paren = new Terminal(SymbolType.CLOSE_PAREN) ;
    public static final Terminal none = new Terminal(SymbolType.NULL) ;
    public static final Terminal end_of_input = new Terminal(SymbolType.END_OF_INPUT) ;

}

class Number extends Terminal {
    public double number ;

    public Number(final String number) {
        super(SymbolType.NUMBER) ;
        this.number = Double.parseDouble(number) ;
    }

    public Number(final double number) {
        super(SymbolType.NUMBER) ;
        this.number = number ;
    }
}

//class PostFixOperator extends Symbol {
//    protected PostFixOperator(final SymbolType symbol_type) {
//        super(symbol_type) ;
//    }
//
//    public static final PostFixOperator binary_plus = new PostFixOperator(SymbolType.BINARY_PLUS_PF) ;
//    public static final PostFixOperator unary_plus = new PostFixOperator(SymbolType.UNARY_PLUS_PF) ;
//    public static final PostFixOperator binary_minus = new PostFixOperator(SymbolType.BINARY_MINUS_PF) ;
//    public static final PostFixOperator unary_minus = new PostFixOperator(SymbolType.UNARY_MINUS_PF) ;
//    public static final PostFixOperator binary_times = new PostFixOperator(SymbolType.BINARY_TIMES_PF) ;
//    public static final PostFixOperator binary_div = new PostFixOperator(SymbolType.BINARY_DIV_PF) ;
//
//}


class Tokenizer implements Iterator<Symbol> {

    private static final char times = '*' ;
    private static final char div = '/' ;
    private static final char plus = '+' ;
    private static final char minus = '-' ;

    private static final String regular_expression = (new StringBuilder()).append("[()").append(times).append(div).append(plus).append(minus).append(']').append("|(\\.\\d+)|((\\d+)(\\.?\\d*)?)|(\\s+)").toString() ;
    private static final Pattern pattern = Pattern.compile(regular_expression) ;

    private final List<String> token_list ;
    private final Iterator<String> token_iterator ;
    private int token_i ;

    private static List<String> tokenize(final String in_string) {

        if (in_string.length() == 0) {
            throw new RuntimeException("Cannot parse zero length input.") ;
        }

        List<String> token_list = new ArrayList<>() ;
        final Matcher matcher = pattern.matcher(in_string) ;
        boolean has_next = matcher.find() ;
        String token_string = "" ;
        int end = 0 ;
        while (has_next) {

            token_string = matcher.group() ;
            end = matcher.end() ;
            // By design, zero length match is impossible.
            assert token_string.length() != 0 ;

            if (token_string.charAt(0) == ' '
                    || token_string.charAt(0) == '\t'
                    || token_string.charAt(0) == '\n'
                    || token_string.charAt(0) == '\f'
                    || token_string.charAt(0) == '\r'
                    || token_string.charAt(0) == '\u000B') {
                // \s	A whitespace character: [ \t\n\x0B\f\r]
                continue ;
            }

            token_list.add(token_string) ;

            has_next = end != in_string.length() && matcher.find() ;
        }

        if (end != in_string.length()) {
            final String err_message = new StringBuilder().append("Unrecognized character in input:").append(in_string.charAt(end)).toString() ;
            throw new java.lang.RuntimeException(err_message) ;
        }
        return token_list ;
    }

    public Tokenizer(final String in_string) {

        token_list = tokenize(in_string) ;
        token_iterator = token_list.iterator() ;
        token_i = 0 ;
    }

    @Override
    public boolean hasNext() {
        return token_iterator.hasNext() ;
    }

    @Override
    public Symbol next() {

        if (!token_iterator.hasNext()) {
            return Terminal.end_of_input ;
        }
        final String token = token_iterator.next() ;

        Symbol nextSymbol = null ;
        switch (token.charAt(0)) {
            case '*':
                nextSymbol = Terminal.binary_times ;
                break ;
            case '/':
                nextSymbol = Terminal.binary_div ;
                break ;
            case '+':

                if (token_i != 0) {
                    switch (token_list.get(token_i-1).charAt(0)) {
                        case times:
                        case div:
                        case plus:
                        case minus:
                            nextSymbol = Terminal.unary_plus ;
                            break ;
                        default:
                            nextSymbol = Terminal.binary_plus ;
                    }
                }
                else {
                    nextSymbol = Terminal.unary_plus ;
                }
                break ;
            case '-':
                if (token_i != 0) {
                    switch (token_list.get(token_i-1).charAt(0)) {
                        case times:
                        case div:
                        case plus:
                        case minus:
                            nextSymbol = Terminal.unary_minus ;
                            break ;
                        default:
                            nextSymbol = Terminal.binary_minus ;
                    }
                }
                else {
                    nextSymbol = Terminal.unary_minus ;
                }
                break ;
            case '(':
                nextSymbol = Terminal.open_paren ;
                break;
            case ')':
                nextSymbol = Terminal.close_paren ;
                break;
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
                nextSymbol = new Number(token_list.get(token_i)) ;
                break;
            default:
                // This should not happen
                assert false ;
        }
        ++token_i ;
        return nextSymbol ;
    }
}

class ParseTree {

    public static double compute(final String in_string) {

        final Deque<SymbolType> parseStack = new ArrayDeque<>() ;
        final Deque<Symbol> computeStack = new ArrayDeque<>() ;

        parseStack.push(SymbolType.END_OF_INPUT) ;
        parseStack.push(SymbolType.EXPRESSION) ;
        SymbolType peek_symbol_type ;
        final Tokenizer tokenizer = new Tokenizer(in_string) ;
        boolean is_unfinished_parse = true ;
        Symbol current_symbol = tokenizer.next() ;

        while (is_unfinished_parse) {

            peek_symbol_type = parseStack.peek() ;
            if (peek_symbol_type == SymbolType.END_OF_INPUT && current_symbol.symbol_type == SymbolType.END_OF_INPUT) {
                break ;
            }
            else if (SymbolType.isNonTerminal(peek_symbol_type)) {
                final Map<SymbolType, SymbolType[]> row = ParseTable.parseTable.get(peek_symbol_type) ;
                if (row.containsKey(current_symbol.symbol_type)) {
                    final SymbolType[] terminal_children = row.get(current_symbol.symbol_type) ;
                    for(int i = terminal_children.length - 1 ; i != -1 ; --i) {
                        parseStack.push(terminal_children[i]) ;
                    }
                }
                else {
                    // print expected characters in Exception
                    assert false ;
                }
            }
            else if (SymbolType.isTerminal(peek_symbol_type) && SymbolType.isTerminal(current_symbol.symbol_type)) {
                if (current_symbol.symbol_type == SymbolType.NUMBER) {
                    computeStack.push(current_symbol) ;
                }
                parseStack.pop() ;
                current_symbol = tokenizer.next() ;
            }
            else if (SymbolType.isPostFixOperator(peek_symbol_type)) {

                final Number top_Number = ((Number)computeStack.peek()) ;
                Number first_Number ;
                if (peek_symbol_type == SymbolType.UNARY_MINUS_PF) {
                    top_Number.number = -top_Number.number ;
                }
                else if (peek_symbol_type == SymbolType.UNARY_PLUS_PF) {

                }
                else {

                    computeStack.pop() ;
                    first_Number = (Number)computeStack.pop() ;
                    switch (peek_symbol_type) {
                        case BINARY_PLUS_PF:
                            computeStack.push(new Number(first_Number.number + top_Number.number)) ;
                            break ;
                        case BINARY_MINUS_PF:
                            computeStack.push(new Number(first_Number.number - top_Number.number)) ;
                            break ;
                        case BINARY_TIMES_PF:
                            computeStack.push(new Number(first_Number.number * top_Number.number)) ;
                            break ;
                        case BINARY_DIV_PF:
                            computeStack.push(new Number(first_Number.number / top_Number.number)) ;
                            break ;
                        default:
                            assert false ;
                    }
                }
            }
            else {
                assert false ;
            }
        }
    return ((Number)computeStack.pop()).number ;
    }
}


class ParseTable {

    // parseTable maps a non terminal SymbolType to  M, where M maps a terminal SymbolType to the RHS of a
    // production, assuming such a production exists
    public static final Map<SymbolType, Map<SymbolType, SymbolType[]>> parseTable ;

    static {
        parseTable = new EnumMap<>(SymbolType.class);

        final SymbolType[]
                null_entry = {SymbolType.NULL},
                term_moreterms_entry = {SymbolType.TERM, SymbolType.MORE_TERMS},
                factor_morefactors_entry = {SymbolType.FACTOR, SymbolType.MORE_FACTORS};

        Map row = new EnumMap<SymbolType, SymbolType[]>(SymbolType.class);

        // expr ////////////////////////////////////////////////////////////////////////////
        // expr -> term more_terms
        row.put(SymbolType.UNARY_PLUS, term_moreterms_entry);
        row.put(SymbolType.UNARY_MINUS, term_moreterms_entry);
        row.put(SymbolType.NUMBER, term_moreterms_entry);
        row.put(SymbolType.OPEN_PAREN, term_moreterms_entry);
        parseTable.put(SymbolType.EXPRESSION, row);

        // more_terms //////////////////////////////////////////////////////////////////////
        // more_terms -> eps
        row = new EnumMap<SymbolType, SymbolType[]>(SymbolType.class);
        row.put(SymbolType.END_OF_INPUT, null_entry);
        row.put(SymbolType.CLOSE_PAREN, null_entry);

        // more_terms -> + term more_terms (+ term PF(+) more_terms)
        List<SymbolType> rowEntry = new ArrayList<>(4);
        row.put(SymbolType.BINARY_PLUS, new SymbolType[]{SymbolType.BINARY_PLUS, SymbolType.TERM, SymbolType.BINARY_PLUS_PF, SymbolType.MORE_TERMS});

        // more_terms -> - term more_terms (- term PF(-) more_terms)
        row.put(SymbolType.BINARY_MINUS, new SymbolType[]{SymbolType.BINARY_MINUS, SymbolType.TERM, SymbolType.BINARY_MINUS_PF, SymbolType.MORE_TERMS});
        parseTable.put(SymbolType.MORE_TERMS, row);

        // term ////////////////////////////////////////////////////////////////////////////
        // term -> factor more_factors
        row = new EnumMap<SymbolType, SymbolType[]>(SymbolType.class);
        row.put(SymbolType.UNARY_PLUS, factor_morefactors_entry);
        row.put(SymbolType.UNARY_MINUS, factor_morefactors_entry);
        row.put(SymbolType.NUMBER, factor_morefactors_entry);
        row.put(SymbolType.OPEN_PAREN, factor_morefactors_entry);
        parseTable.put(SymbolType.TERM, row) ;

        // more_factors ////////////////////////////////////////////////////////////////////////////
        row = new EnumMap<SymbolType, SymbolType[]>(SymbolType.class);
        row.put(SymbolType.END_OF_INPUT, null_entry);
        row.put(SymbolType.BINARY_PLUS, null_entry);
        row.put(SymbolType.BINARY_MINUS, null_entry);
        row.put(SymbolType.CLOSE_PAREN, null_entry);

        // more_factors -> * factor more_factors (* factor PF(*) more_factors)
        row.put(SymbolType.BINARY_TIMES, new SymbolType[]{SymbolType.BINARY_TIMES, SymbolType.FACTOR, SymbolType.BINARY_TIMES_PF, SymbolType.MORE_FACTORS});

        // more_factors -> * factor more_factors (/ factor PF(/) more_factors)
        row.put(SymbolType.BINARY_DIV, new SymbolType[]{SymbolType.BINARY_DIV, SymbolType.FACTOR, SymbolType.BINARY_DIV_PF, SymbolType.MORE_FACTORS});
        parseTable.put(SymbolType.MORE_FACTORS, row);

        // factor ////////////////////////////////////////////////////////////////////////////////////
        // factor -> - factor (factor -> - factor PF(-))
        row = new EnumMap<SymbolType, SymbolType[]>(SymbolType.class);
        row.put(SymbolType.UNARY_MINUS, new SymbolType[]{SymbolType.UNARY_MINUS, SymbolType.FACTOR, SymbolType.UNARY_MINUS_PF});

        // factor -> + factor (factor -> + factor PF(+))
        row.put(SymbolType.UNARY_PLUS, new SymbolType[]{SymbolType.UNARY_PLUS, SymbolType.FACTOR, SymbolType.UNARY_PLUS_PF});

        // factor -> number
        row.put(SymbolType.NUMBER, new SymbolType[]{SymbolType.NUMBER});

        // factor -> ( expr )
        row.put(SymbolType.OPEN_PAREN, new SymbolType[]{SymbolType.OPEN_PAREN, SymbolType.EXPRESSION, SymbolType.CLOSE_PAREN});

        parseTable.put(SymbolType.FACTOR, row);
    }
}

//class Symbol {
//
//    public final SymbolType symbol_type;
//    public final List<Symbol> children ;
//
//
//    protected Symbol(final SymbolType symbol_type, final List<Symbol> children) {
//        this.symbol_type = symbol_type;
//        this.children = children ;
//    }
//
//    protected Symbol(final SymbolType symbol_type) {
//        this(symbol_type, new ArrayList<>()) ;
//    }
//
//    public String toString() {
//        return symbol_type.toString() ;
//    }
//
////    public boolean isTerminal() {
////        return false;
////    }
//
//    @Override
//    public boolean equals(final Object obj) {
//        return obj instanceof Symbol
//                && ((Symbol)obj).symbol_type == symbol_type ;
//    }
//
//    @Override
//    public int hashCode() {
//        return symbol_type.hashCode() ;
//    }
//
//    public static Symbol newSymbol(final SymbolType symbolType) {
//        switch (symbolType) {
//            case EXPRESSION:
//            case TERM:
//            case MORE_TERMS:
//            case FACTOR:
//                return new Symbol(symbolType) ;
//            case BINARY_PLUS:
//                return Symbol.binary_plus;
//            case BINARY_MINUS:
//                return Symbol.binary_minus ;
//            case BINARY_TIMES:
//                return Symbol.binary_times ;
//            case BINARY_DIV:
//                return Symbol.binary_div;
//            case OPEN_PAREN:
//                return Symbol.open_paren;
//            case CLOSE_PAREN:
//                return  Symbol.close_paren;
//            case NULL:
//                return Symbol.none ;
//            case NUMBER:
//                return new MutableTokenTerminal(symbolType, null) ;
//            case END_OF_INPUT:
//                return Symbol.end_of_input ;
//        }
//        assert false ;
//        return null ;
//    }
//
//    public static final Symbol binary_plus = new Symbol(SymbolType.BINARY_PLUS) ;
//    public static final Symbol unary_plus = new Symbol(SymbolType.UNARY_PLUS) ;
//    public static final Symbol binary_minus = new Symbol(SymbolType.BINARY_MINUS) ;
//    public static final Symbol unary_minus = new Symbol(SymbolType.UNARY_MINUS) ;
//    public static final Symbol binary_times = new Symbol(SymbolType.BINARY_TIMES) ;
//    public static final Symbol binary_div = new Symbol(SymbolType.BINARY_DIV) ;
//    public static final Symbol open_paren = new Symbol(SymbolType.OPEN_PAREN) ;
//    public static final Symbol close_paren = new Symbol(SymbolType.CLOSE_PAREN) ;
//    public static final Symbol none = new Symbol(SymbolType.NULL) ;
//    public static final Symbol end_of_input = new Symbol(SymbolType.END_OF_INPUT) ;
//}


//class Terminal extends Symbol {
//
//    protected String token ;
//
//    protected Terminal(final SymbolType symbol_type, final String token) {
//        super(symbol_type) ;
//        this.token = token ;
//    }
//
//    protected Terminal(final SymbolType symbol_type, final char token) {
//        this(symbol_type, new String(new char[] {token})) ;
//    }
//
//    public static Terminal newNumber(final String token) {
//        return new Terminal(SymbolType.NUMBER, token) ;
//    }
//
//    @Override
//    public boolean equals(final Object obj) {
//        return obj instanceof Terminal
//                && ((Terminal)obj).symbol_type == symbol_type
//                && ((Terminal)obj).token.equals(token) ;
//    }
//
////    public boolean isTerminal() {
////        return true ;
////    }
//
//    public String getToken() {
//        return token ;
//    }
//
//    public String toString() {
//
//        return new StringBuilder().append(symbol_type.toString()).append(':').append(getToken()).toString() ;
//    }
//
//    public static final Terminal times = new Terminal(SymbolType.BINARY_TIMES, '*') ;
//    public static final Terminal div = new Terminal(SymbolType.BINARY_DIV, '/') ;
//    public static final Terminal plus = new Terminal(SymbolType.BINARY_PLUS, '+') ;
//    public static final Terminal unary_plus = new Terminal(SymbolType.UNARY_PLUS, '+') ;
//    public static final Terminal minus = new Terminal(SymbolType.BINARY_MINUS, '-') ;
//    public static final Terminal unary_minus = new Terminal(SymbolType.UNARY_MINUS, '-') ;
//    public static final Terminal openParen = new Terminal(SymbolType.OPEN_PAREN, '(') ;
//    public static final Terminal closeParen = new Terminal(SymbolType.CLOSE_PAREN, ')') ;
//    public static final Terminal empty = new Terminal(SymbolType.NULL, null) ;
//    public static final Terminal endOfInput = new Terminal(SymbolType.END_OF_INPUT, null) ;
//}
//
//class MutableTokenTerminal extends Terminal {
//    public MutableTokenTerminal(final SymbolType symbol_type, final String token) {
//        super(symbol_type, token) ;
//    }
//
//    public void setToken(final String token) {
//        this.token = token ;
//    }
//}




//    public static Symbol parse(final String input) {
//
//        final Deque<Symbol> parseStack  = new ArrayDeque<>() ;
//        final Symbol root = new Symbol(SymbolType.EXPRESSION) ;
//        Symbol parent = null, child = null ;
//        parseStack.push(Terminal.end_of_input) ;
//        parseStack.push(root) ;
//
//        final Tokenizer tokens = new Tokenizer(input) ;
//        Symbol currentToken = tokens.next() ;
//        List<SymbolType> reverseRuleRHS ;
//
//        boolean isTerminalError = false, isNonTerminalError = false ;
//        while (!isTerminalError && !isNonTerminalError && parseStack.size() != 0) {
//            parent = parseStack.pop() ;
//            popAction(parent.symbol_type) ;
//            if (SymbolType.isTerminal(parent.symbol_type)) {
//                isTerminalError = parent.symbol_type != currentToken.symbol_type ;
//                if (!isTerminalError) {
//                    switch (parent.symbol_type) {
//                        case NUMBER:
//                            ((MutableTokenTerminal)parent).setToken(((Terminal)currentToken).getToken()) ;
//                            currentToken = tokens.next() ;
//                            break ;
//                        case END_OF_INPUT:
//                            break ;
//                        default:
//                            currentToken = tokens.next() ;
//                    }
//                }
//            }
//            else {
//                reverseRuleRHS = ParseTable.entry(parent.symbol_type, currentToken.symbol_type) ;
//                isNonTerminalError = reverseRuleRHS == null ;
//                if (!isNonTerminalError) {
//                    if (reverseRuleRHS.get(0) == SymbolType.NULL) {
//                        parent.children.add(Symbol.newSymbol(SymbolType.NULL)) ;
////                        parseStack.pop() ;
//                        continue ;
//                    }
//                    for (int i = 0 ; i !=  reverseRuleRHS.size() ; ++i) {
//                        parent.children.add(null) ;
//                    }
//                    pushAction(reverseRuleRHS) ;
//                    for (int i = 0 ; i !=  reverseRuleRHS.size() ; ++i) {
//                        child = Symbol.newSymbol(reverseRuleRHS.get(i)) ;
//                        parseStack.push(child) ;
//                        parent.children.set(parent.children.size() - 1 -i, child) ;
//                    }
//                }
//            }
//        }
//
//        if (isTerminalError) {
//            System.err.print("terminal_error parent:" + parent.symbol_type.toString()) ;
//            System.err.print(" terminal_error token:" + currentToken.symbol_type.toString()) ;
//        }
//        else if (isNonTerminalError) {
//            System.err.print("nonterminal_error parent:" + parent.symbol_type.toString()) ;
//            System.err.print(" nonterminal_error token:" + currentToken.symbol_type.toString()) ;
//        }
//        return isTerminalError ? null : root ;
//    }
//
//    public static void popAction(final SymbolType stackSymbol) {
//        System.err.println("popped: " + stackSymbol.toString());
//    }
//
//    public static void pushAction(final List<SymbolType> reverseRuleRHS) {
//        for (int i = 0 ; i !=  reverseRuleRHS.size() ; ++i) {
//            System.err.println("push: " +  reverseRuleRHS.get(i).toString());
//        }
//    }
//
//    public static void dump(final Symbol root) {
//        final String indent = "  " ;
//        final Deque<Symbol> stack = new ArrayDeque<>() ;
//        final Deque<Integer> indentStack = new ArrayDeque<>() ;
//        stack.push(root) ;
//        indentStack.push(0) ;
//        while (stack.size() != 0) {
//            final List<Symbol> children = stack.peek().children ;
//            final Symbol parent = stack.pop() ;
//            final int indentLevel = indentStack.pop() ;
//            for (int i = 0 ; i != indentLevel ; ++i) {
//                System.err.print(indent) ;
//            }
//            System.err.print(parent.symbol_type) ;
//            if (parent.symbol_type == SymbolType.NUMBER) {
//                System.err.print(':'); ;
//                System.err.print(((Terminal)parent).token) ;
//            }
//            System.err.print('\n') ;
//            for(int i = children.size()-1 ; i != -1 ; --i) {
//                stack.push(children.get(i)) ;
//                indentStack.push(indentLevel+1) ;
//            }
//        }
//    }