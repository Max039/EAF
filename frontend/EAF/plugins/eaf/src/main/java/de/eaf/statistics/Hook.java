package de.eaf.statistics;

import de.evoal.languages.model.base.Instance;
import de.evoal.optimisation.api.statistics.IterationResult;
import de.evoal.optimisation.api.statistics.writer.StatisticsWriter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.Dependent;
import javax.inject.Named;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;


/**
 * A class that extends the shell version to execute extra code. In this case its use to process each generation.
 */
@Slf4j
@Dependent
@Named("de.eaf.statistics.hook.eaf-hook")
public class Hook implements StatisticsWriter  {

    public static int eafPort = 11113;
    public static PrintWriter eafInput = null;

    public static Socket eafClientSocket = null;

    public static ServerSocket eafServerSocket = null;
    @Override
    public StatisticsWriter init(final Instance config) {
        try {
            StatisticsWriter.super.init(config);
            System.out.println("Opening port: " + eafPort);
            eafServerSocket = new ServerSocket(eafPort); // Create a server socket

            // Set a timeout of 5 seconds (5000 milliseconds) for the accept method
            eafServerSocket.setSoTimeout(5000);

            System.out.println("Waiting for eaf to connect ...");
            try {
                eafClientSocket = eafServerSocket.accept(); // Wait for client to connect
                eafInput = new PrintWriter(eafClientSocket.getOutputStream(), true);
                System.out.println("Eaf connected!");
            } catch (SocketTimeoutException e) {
                System.out.println("Eaf Connection Timeout: No connection was made within 5 seconds.");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }
    @Override
    public void add(IterationResult iterationResult){
        System.out.println(iterationResult);
    };

    /**
     * Writes the output
     */
    @Override
    public void write() {

    };


}
