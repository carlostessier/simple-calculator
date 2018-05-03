package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) throws IOException {
        final BufferedReader in_reader = new BufferedReader(new InputStreamReader(System.in)) ;
        final String str = in_reader.readLine() ;
        new MathEvaluator().calculate("1+1") ;
        System.out.println(MathEvaluator.calculate(str));
    }
}
