package ch.heigvd.res.caesar.client;

import java.io.*;
import java.net.*;

import ch.heigvd.res.caesar.protocol.Protocol;
import ch.heigvd.res.caesar.server.CaesarServer;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Olivier Liechti (olivier.liechti@heig-vd.ch)
 */
public class CaesarClient {


    private int key;

    public static void main(String args[]){
        new CaesarClient(Protocol.port, Protocol.addr);
    }

    private static final Logger LOG = Logger.getLogger(CaesarClient.class.getName());

    public CaesarClient(int port, String address) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tH:%1$tM:%1$tS::%1$tL] Client > %5$s%n");
        LOG.info("Caesar client starting...");
        Socket s = new Socket();
        InetAddress addr;
        PrintWriter out;
        try {
            addr = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            LOG.log(Level.SEVERE, "Erreur getByName: \n" + e.getMessage());
            return;
        }
        SocketAddress sockAddr = new InetSocketAddress(addr, port);
        BufferedReader in;
        try {
            s.connect(sockAddr);
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            Protocol protocolIni = new Protocol(in.readLine());
            if(protocolIni.getHeader() != Protocol.entete.INI) throw new IOException("Bad iniialisation header");
            key = Integer.parseInt(protocolIni.getMessage());
            System.out.println("Key: "+key);

        } catch (java.io.IOException e) {
            LOG.log(Level.SEVERE, "Erreur connect: " + e.getMessage());
            return;
        }
        try{
            out = new PrintWriter(s.getOutputStream());
        }catch(java.io.IOException e){
            LOG.log(Level.SEVERE, "Erreur getOutputStream: " + e.getMessage());
            return;
        }

        try{
            while(true){
                System.out.print("Message: ");
                Scanner scan = new Scanner(System.in);
                String input = scan.nextLine();
                Protocol protocolOut;
                try{
                    protocolOut = new Protocol("MES:"+input);
                }catch(IllegalArgumentException e){
                    LOG.log(Level.SEVERE, "Erreur protocole: " + e.getMessage());
                    LOG.log(Level.SEVERE, "Le message va être ignoré!");
                    continue;
                }
                protocolOut.encode(key);
                out.println(protocolOut.getParsedMessage());
                out.flush();
                String line;
                line = in.readLine();
                Protocol protocolIn = new Protocol(line);
                protocolIn.decode(key);
                System.out.println("Server: " + protocolIn.getMessage());
            }
        }catch(java.io.IOException e){
            LOG.log(Level.SEVERE, "Erreur write: " + e.getMessage());
        }
    }
}
