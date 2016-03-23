package ch.heigvd.res.caesar.protocol;

import java.util.IllegalFormatException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;


/**
 * @author Olivier Liechti
 */
public class Protocol {

    final static Logger LOG = Logger.getLogger(Protocol.class.getName());
    final public static int port = 1001;
    final public static String addr = "127.0.0.1";

    public enum entete {MES, INI, BYE}

    private entete header;
    private String message;

    /**
     * Syntaxe:
     * HEADER: Payload
     * Exemple:
     * MES:Salut
     * BYE:...
     * INI:5
     */
    //Enum en-tete + class + crypto
    public Protocol(String str) throws IllegalArgumentException {
        String parts[] = str.split(":");
        if(parts.length != 2){
            throw new IllegalArgumentException("MalformedHeaderException");
        }
        boolean exist = false;
        for (entete e : entete.values()) {
            if (e.name().equals(parts[0])) {
                exist = true;
            }
        }
        if (!exist) {
            throw new IllegalArgumentException("MalformedHeaderException");
        }
        header = entete.valueOf(parts[0]);
        message = parts[1];
    }

    public entete getHeader() {
        return header;
    }

    public String getMessage() {
        return message;
    }

    public String getParsedMessage() {
        return header.name() + ":" + message;
    }

    public void encode(int key) {
        String newString = "";
        int diff = 0;
        for (char c : message.toCharArray()) {
            if (Character.isUpperCase(c) && Character.isAlphabetic(c)) {
                diff = 65;
            } else if (Character.isLowerCase(c) && Character.isAlphabetic(c)) {
                diff = 97;
            }
            if (Character.isAlphabetic(c)) {
                int numC = (int) c;
                numC -= diff;
                newString += (char) (((numC + key) % 26) + diff);
            } else {
                newString += c;
            }
        }
        message = newString;
    }

    public void decode(int key) {
        String newString = "";
        int diff = 0;
        for (char c : message.toCharArray()) {
            if (Character.isUpperCase(c) && Character.isAlphabetic(c)) {
                diff = 65;
            } else if (Character.isLowerCase(c) && Character.isAlphabetic(c)) {
                diff = 97;
            }
            if (Character.isAlphabetic(c)) {
                int numC = (int) c;
                numC -= diff;
                newString += (char) (((numC - key) % 26) + diff);
            } else {
                newString += c;
            }
        }
        message = newString;
    }

}
