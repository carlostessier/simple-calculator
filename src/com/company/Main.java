package com.company ;

import java.util.NoSuchElementException;
import java.util.regex.Pattern ;
import java.util.regex.Matcher ;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Iterator ;

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

    public double calculate(String expression) {
        return 0.0 ;
    }


}

class Token {
    enum TokenType {UNARY_OPERATOR, BINARY_OPERATOR, NUMBER, OPEN_PAREN, CLOSE_PAREN}
    final TokenType token_type ;
    final String token ;


    public Token(final TokenType token_type, final String token) {
        this.token_type = token_type ;
        this.token = token ;
    }

    public Token(final TokenType token_type, final char token) {
        this(token_type, new String(new char[] {token})) ;
    }

    public String toString() {
        return new StringBuilder().append(token_type.toString()).append(':').append(token).toString() ;
    }
}

class Tokenizer implements Iterator<Token> {

    private static final String regular_expression = "[()*/+-]|(\\.\\d+)|((\\d*)(\\.\\d+)?)|[\\h\\H]+" ;
    private static final Pattern pattern = Pattern.compile(regular_expression) ;

    private Token nextToken ;
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
        return nextToken != null ;
    }

    @Override
    public Token next() {
        if (hasNext()) {
            final Token tmp_token = nextToken ;
            setNextToken() ;
            return tmp_token ;
        }
        throw new NoSuchElementException() ;
    }

    private void setNextToken() {

//        System.out.println(matcher.toString()) ;
        if (!matcher.find()) {
            if (matcher.start() == in_str_len) {
                nextToken = null ;
                return ;
            }
            throw new RuntimeException("Unrecognized token starting at position: " + matcher.start()) ;
        }


        if (matcher.end() == matcher.start()) {
            // Zero length match
            nextToken = null ;
            return ;
        }

//        System.out.println(matcher.group().charAt(0)) ;
        switch (matcher.group().charAt(0)) {
            case '*':
            case '/':
                nextToken = new Token(Token.TokenType.BINARY_OPERATOR, matcher.group().charAt(0)) ;
                previous_is_number = false ;
                return ;
            case '+':
            case '-':
                // + and - may be unary or binary operators, depending on whether a number precedes
                // the operator.
                nextToken =
                        previous_is_number ?
                                new Token(Token.TokenType.BINARY_OPERATOR, matcher.group().charAt(0)) :
                                new Token(Token.TokenType.UNARY_OPERATOR, matcher.group().charAt(0));
                previous_is_number = false ;
                return ;
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
                nextToken = new Token(Token.TokenType.NUMBER, matcher.group()) ;
                previous_is_number = true ;
                return ;
            case '(':
                nextToken = new Token(Token.TokenType.OPEN_PAREN, matcher.group()) ;
                previous_is_number = false ;
                return ;
            case ')':
                nextToken = new Token(Token.TokenType.CLOSE_PAREN, matcher.group()) ;
                previous_is_number = false ;
                return ;
            case ' ':
            case '\t':
            case '\n':
            case '\f':
            case '\r':
            default:
                // discard this token
        }
    }

    public List<Token> tokenize(final String in_string) {
        List<Token> token_list = new ArrayList<>() ;
        Matcher matcher = pattern.matcher(in_string) ;
        int start = 0 ;
        Token curr_token ;
        boolean previous_is_number = false;
        while (matcher.find(start) && matcher.end() - start != 0) {
            System.out.println(in_string.charAt(start)) ;
            switch (in_string.charAt(start)) {
                case '*':
                case '/':
                    curr_token = new Token(Token.TokenType.BINARY_OPERATOR, in_string.charAt(start)) ;
                    previous_is_number = false ;
                    break ;
                case '+':
                case '-':
                    // + and - may be unary or binary operators, depending on whether a number precedes
                    // the operator.
                    if (previous_is_number) {
                        curr_token = new Token(Token.TokenType.BINARY_OPERATOR, in_string.charAt(start));
                    }
                    else {
                        curr_token = new Token(Token.TokenType.UNARY_OPERATOR, in_string.charAt(start));
                    }
                    previous_is_number = false ;
                    break ;
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
                    curr_token = new Token(Token.TokenType.NUMBER, in_string.substring(start, matcher.end())) ;
                    previous_is_number = true ;
                    break ;
                case '(':
                    curr_token = new Token(Token.TokenType.OPEN_PAREN, in_string.substring(start, matcher.end())) ;
                    previous_is_number = false ;
                    break ;
                case ')':
                    curr_token = new Token(Token.TokenType.CLOSE_PAREN, in_string.substring(start, matcher.end())) ;
                    previous_is_number = false ;
                    break ;
                default:
                    throw new RuntimeException("Unrecognized token starting at" + in_string.substring(start)) ;
            }
            token_list.add(curr_token) ;
            start = matcher.end() ;
        }

        if (start != in_string.length()) {
            throw new RuntimeException("Unconsumed input:" + in_string.substring(start)) ;
        }
        return token_list ;
    }
}

//class ParseTable