/**
 *  This class is the entry point for communication between the BBS and
 *   the user on the other end of the 'net. The online session starts here.
 *
 *     Copyright (c) Lighting and Sound Technologies, 1997.
 *      Written by Ryan C. Gordon.
 */

import java.io.*;
import java.net.*;

public class OnlineSession implements Runnable
{
        // Constants...
    public static final byte ASCII_BACKSPACE = 0x08;

        // Instance variables...
    protected SocketStream io;               // socket's output stream.
    protected Thread sessionThread;          // All work is done in this thread.
    protected JBBSUser user = null;

    public SocketStream getSocketStream()
    {
        return(io);
    } // getSocketStream


    public OnlineSession(Socket socket) throws LostCarrierException
    /**
     *  Constructor; Builds this session, spins threads, and gets
     *   it all going...
     *
     *     params : s == socket connected to another user.
     *    returns : Constructor; void.
     */
    {
        //!!!socket.setSoTimeout((JBBSConfig.idleTimeout * 60) * 1000);

        try
        {
            io = checkEmulation(socket);
        } // try
        catch (LostCarrierException e)
        {
            io = null;
            throw(e);    // rethrow exception...
        } // catch

        if (ConnectionManager.addSession(this) == false)
        {
            io.send("Sorry, we can't connect you at this time. " +
                    "Try again later. Thanks!\r\n\r\n");
            closeConnection();
        } // if

        else    // no problems? Spin a thread to start user's session...
        {
            sessionThread = new Thread(this);
            sessionThread.start();
        } // else
    } // Constructor


    public boolean getYN(String prompt) throws LostCarrierException
    {
        byte readByte;
        boolean retVal;

        io.send(prompt);

        do
        {
            readByte = JBBS.asciiByteToLower(io.recv());
        } while ((readByte != 'y') && (readByte != 'n'));

        if (readByte == 'y')
        {
            retVal = true;  
            io.sendln("yes");
        } // if
        else
        {
            retVal = false;
            io.sendln("no");
        } // else

        return(retVal);
    } // getYN


    public boolean putFile(String fileName) throws LostCarrierException
    {
        boolean retVal = true;
        byte[] outBytes = new byte[256];
        int bytesRead;
        BufferedInputStream binp = null;

        try
        {
            binp = new BufferedInputStream(new FileInputStream(fileName));
        } // try
        catch (FileNotFoundException e)
        {
            io.sendln("Couldn't send '" + fileName + "'!");
            return(false);
        } // catch

        try
        {
            while (binp.available() > 0)
            {
                bytesRead = binp.read(outBytes);
                io.send(outBytes, bytesRead);
            } // while
            binp.close();
        } // try
        catch (IOException e)
        {
            //!!!log this?
            io.sendln("Error sending '" + fileName + "'!");
            retVal = false;
        } // catch

        return(retVal);
    } // putFile


    protected boolean login() throws LostCarrierException
    /**
     * Get a user name and password from remote client. Check it
     *  for validity.
     *
     *    params : void.
     *   returns : boolean true if valid login, boolean false otherwise.
     */
    {
        byte[] userName;
        String password;
        int tries;
        boolean retVal = false;

        for (tries = 0; (tries < JBBSConfig.loginTries) && (!retVal); tries++)
        {
            // The (MAX_???????? + 20) is just to further baffle maximums
            //  in the name of security.

            io.send("username : ");
            userName = io.recvln(JBBSUser.MAX_USERNAME + 20).getBytes();
            io.send("password : ");
            password = io.recvln(JBBSUser.MAX_PASSWORD + 20);

       	    user = JBBSUser.retrieve(userName, password);
            if (user == null)
            {
                //if (JBBSConfig.logSecurity)
                //    JBBSLog.add("!!!");
                io.sendln("Login incorrect.");
                io.sendln();
            } // if
            else
                retVal = true;
        } // for

        return(retVal);
    } // login


    protected SocketStream checkEmulation(Socket socket)
                                   throws LostCarrierException
    /**
     *  This method tries to find the most functional emulation
     *   available to the remote client, and creates a SocketStream
     *   deriviative based on it.
     *
     *    params : socket == socket for communication with client.
     *   returns : SocketStream == use this retval for all further
     *                             communication with the client.
     */
    {
        SocketStream retVal;
        InputStream in = null;
        OutputStream out = null;

        try
        {
            in = socket.getInputStream();
            out = socket.getOutputStream();
            out.write("Please wait...checking term emulation...".getBytes());
        } // try
        catch (IOException e)
        {
            SocketStream.handleIOException(e,
                                    "OnlineSession.checkEmulation();");
        } // catch

        if (ANSITerminal.detectTerminal(in, out))
            retVal = new ANSITerminal(socket);
        else    // if all else fails, use Dumb Terminal Emulation...
            retVal = new DumbTerminal(socket);

        in = null;
        out = null;

        return(retVal);
    } // checkEmulation


    protected void closeConnection()
    /**
     *  Kick off user, and close socket.
     *
     *    params : void.
     *   returns : void.
     */
    {
        if (io != null)
        {
            io.close();
            io = null;
        } // if
    } // closeConnection


    public void shutdown()
    {
        if (sessionThread != null)
        {
            sessionThread.stop();
            sessionThread = null;
        } // if

        closeConnection();
    } // finalize


    protected void pause() throws LostCarrierException
    {
        int i;
        int curLine = 0;
        String pauseMsg = "Hit a key, nootch!";

        io.sendln();

        if (io.doesPositioning())
            curLine = io.getPosY();

        io.send(pauseMsg);
        io.recv();
        
        if (io.doesPositioning())
        {
            io.setPosXY(1, curLine);

            for (i = 0; i < pauseMsg.length(); i++)
                io.send(' ');

            io.setPosXY(1, curLine);
        } // if
        else
        {
            for (i = 0; i < pauseMsg.length(); i++)
                io.send(ASCII_BACKSPACE);

            for (i = 0; i < pauseMsg.length(); i++)
                io.send(' ');

            for (i = 0; i < pauseMsg.length(); i++)
                io.send(ASCII_BACKSPACE);
        } // else
    } // pause


    protected void sessionIntro() throws LostCarrierException
    /**
     *  This gets called after the user has successfully logged on.
     *
     *     params : void.
     *    returns : void.
     */
    {
        if (JBBSConfig.useAutoPosts)
            AutoPosts.doAutoPosts(this);

        // do the rest...        

    } // sessionIntro


    protected void beginSession() throws LostCarrierException
    {
        byte readIn;
        
        io.sendln("Connected with " + io.getTermEmulName() + " terminal.");
        io.sendln();
        io.sendln(JBBSConfig.BBSName);
        io.sendln("  Please Login.");

        if (login() == true)
            sessionIntro();
    } // beginSession


        // Runnable implementation...

    public void run()
    /**
     *  This is just here so the user's JBBS session runs in a separate
     *   thread.
     *
     *     params : void.
     *    returns : void, then thread terminates.
     */
    {
        try
        {
            beginSession();
            closeConnection();
            ConnectionManager.removeSession(this);
        } // try

        catch (LostCarrierException e)
        {
            //if (JBBSConfig.logLostCarrierExceptions)   !!!
            //    JBBSLog.add("LostCarrierException", e.getDetails());
        } // catch
    } // run

} // OnlineSession

// end of OnlineSession.java ...

