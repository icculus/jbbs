/**
 *   Class that handles incoming connections. This class will create
 *    sockets to deal with them, and spin threads for them to run in.
 *
 *      Copyright (c) Lighting and Sound Technologies, 1997.
 *       Written by Ryan C. Gordon.
 */

import java.io.IOException;
import java.net.*;

public class ConnectionManager implements Runnable
{
    protected static ServerSocket incoming = null;
    protected static Thread cmThread;
    protected static OnlineSession[] sessionList;
    protected static long totalConnected = 0; // total connections.
    protected static int currentConnected = 0; // total current connections.


    public static long getTotalConnectionCount()
    {
        return(totalConnected);
    } // getTotalConnectionCount


    public static int getCurrentConnectionCount()
    {
        return(currentConnected);
    } // getCurrentConnectionCount


    public static boolean saturated()
    {
        return((currentConnected >= JBBSConfig.maxConnections) ? true : false);
    } // saturated


    public static synchronized boolean addSession(OnlineSession addMe)
    {
        int i;

        if (saturated())
            return(false);

        totalConnected++;
        currentConnected++;

        for (i = 0; i < JBBSConfig.maxConnections; i++)
        {
            if (sessionList[i] == null)
            {
                sessionList[i] = addMe;
                return(true);
            } // if
        } // for

	return(false);   // keeps compiler happy; shouldn't ever hit this.
    } // addSession


    public static synchronized void removeSession(OnlineSession removeMe)
    {
        int i;

        for (i = 0; i < JBBSConfig.maxConnections; i++)
        {
            if (sessionList[i] == removeMe)
            {
                sessionList[i] = null;
                currentConnected--;
                return; 
           } // if
        } // for
    } // removeSession


    public ConnectionManager() throws IOException
    {
        int i;

        if (incoming != null)
            throw(new IOException("Connection Manager already running."));

        sessionList = new OnlineSession[JBBSConfig.maxConnections];

            // Clear our new sessions listing...
        for (i = 0; i < JBBSConfig.maxConnections; i++)
            sessionList[i] = null;

        incoming = new ServerSocket(JBBSConfig.portNum,
                                    JBBSConfig.maxConnections);
        cmThread = new Thread(this);
        cmThread.start();
    } // Constructor


    public static synchronized void shutdown()
    {
        int i;

        cmThread.stop();
        cmThread = null;
        try
        {
            incoming.close();
        } // try
        catch (IOException e)
        {
            // don't care...it's going in the trash anyway...
        } // catch
        incoming = null;

        for (i = 0; i < sessionList.length; i++)
        {
            if (sessionList[i] != null)
            {
                sessionList[i].shutdown();
                sessionList[i] = null;
                currentConnected--;
            } // if
        } // for

        sessionList = null;
    } // finalize


        // Runnable implementation...

    public void run()
    {
        int i;

        while (true)    // loop until killed.
        {
            try
            {
                Thread.yield();
                new OnlineSession(incoming.accept());
            } // try

            catch (LostCarrierException e)     // !!! why?
            {
                System.out.println("LostCarrierException on ServerSocket! [" +
                                   e.getMessage() + "]");
            } // catch (LostCarrierException)

            catch (IOException e)
            {
                System.out.println("IOException on ServerSocket! [" +
                                    e.getMessage() + "]");
            } // catch (IOException)
        } // while
    } // run

} // ConnectionManager


// end of ConnectionManager.java ...

