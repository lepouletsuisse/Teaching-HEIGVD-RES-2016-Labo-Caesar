package ch.heigvd.res.caesar;

import java.io.*;
import ch.heigvd.res.caesar.protocol.Protocol;

/**
 * Created by Samuel on 23.03.2016.
 */
public class test {
    public static void main(String args[]){
        Protocol test = new Protocol("MES:Salut comment sa va?");
        System.out.println(test.getParsedMessage());
        test.encode(1);
        System.out.println(test.getParsedMessage());
        test.decode(1);
        System.out.println(test.getParsedMessage());


    }

}
