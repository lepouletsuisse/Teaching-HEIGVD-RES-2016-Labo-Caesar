package ch.heigvd.res.caesar.server;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.heigvd.res.caesar.client.*;
import ch.heigvd.res.caesar.protocol.Protocol;

import java.util.logging.Logger;

/**
 * This class implements a multi-threaded TCP server. It is able to interact
 * with several clients at the time, as well as to continue listening for
 * connection requests.
 *
 * @author Olivier Liechti
 */
public class CaesarServer {

    public static void main(String args[]){
        new CaesarServer(Protocol.port);
    }

    final static Logger LOG = Logger.getLogger(CaesarServer.class.getName());

    int port;

    /**
     * Constructor
     *
     * @param port the port to listen on
     */
    public CaesarServer(int port) {
        this.port = port;
        serveClients();
    }

    /**
     * This method initiates the process. The server creates a socket and binds it
     * to the previously specified port. It then waits for clients in a infinite
     * loop. When a client arrives, the server will read its input line by line
     * and send back the data converted to uppercase. This will continue until the
     * client sends the "BYE" command.
     */
    public void serveClients() {
        LOG.info("Starting the Receptionist Worker on a new thread...");
        new Thread(new ReceptionistWorker()).start();
    }

    /**
     * This inner class implements the behavior of the "receptionist", whose
     * responsibility is to listen for incoming connection requests. As soon as a
     * new client has arrived, the receptionist delegates the processing to a
     * "servant" who will execute on its own thread.
     */
    private class ReceptionistWorker implements Runnable {

        @Override
        public void run() {
            ServerSocket serverSocket;

            System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tH:%1$tM:%1$tS::%1$tL] Server > %5$s%n");
            LOG.info("Caesar server starting...");

            try {
                serverSocket = new ServerSocket(port);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
                return;
            }

            while (true) {
                LOG.log(Level.INFO, "Waiting (blocking) for a new client on port {0}", port);
                try {
                    Socket clientSocket = serverSocket.accept();
                    LOG.info("A new client has arrived. Starting a new thread and delegating work to a new servant...");
                    new Thread(new ServantWorker(clientSocket)).start();
                } catch (IOException ex) {
                    Logger.getLogger(CaesarServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        /**
         * This inner class implements the behavior of the "servants", whose
         * responsibility is to take care of clients once they have connected. This
         * is where we implement the application protocol logic, i.e. where we read
         * data sent by the client and where we generate the responses.
         */
        private class ServantWorker implements Runnable {

            Socket clientSocket;
            BufferedReader in = null;
            PrintWriter out = null;

            private int key;

            public ServantWorker(Socket clientSocket) {
                try {
                    this.clientSocket = clientSocket;
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new PrintWriter(clientSocket.getOutputStream());
                    Random r = new Random();
                    int max = 26;
                    int min = 1;
                    key = r.nextInt(max + 1 - min) + min;
                    Protocol protocolIni = new Protocol("INI:"+key);
                    out.println(protocolIni.getParsedMessage());
                    out.flush();

                } catch (IOException ex) {
                    Logger.getLogger(CaesarServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void run() {
                String line;
                boolean shouldRun = true;
                try {
                    LOG.info("Reading until client sends BYE Header or closes the connection...");
                    while ((shouldRun) && (line = in.readLine()) != null) {
                        Protocol protocolIn = new Protocol(line);
                        if (protocolIn.getHeader() == Protocol.entete.BYE) {
                            shouldRun = false;
                            continue;
                        }
                        protocolIn.decode(key);
                        System.out.println("> " + protocolIn.getMessage());
                        protocolIn.encode(key);
                        out.println(protocolIn.getParsedMessage());
                        out.flush();
                    }

                    LOG.info("Cleaning up resources...");
                    clientSocket.close();
                    in.close();
                    out.close();

                } catch (IOException ex) {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException ex1) {
                            LOG.log(Level.SEVERE, ex1.getMessage(), ex1);
                        }
                    }
                    if (out != null) {
                        out.close();
                    }
                    if (clientSocket != null) {
                        try {
                            clientSocket.close();
                        } catch (IOException ex1) {
                            LOG.log(Level.SEVERE, ex1.getMessage(), ex1);
                        }
                    }
                    LOG.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }
    }
}
