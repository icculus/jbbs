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


        /**
         *  Constructor; Builds this session, spins threads, and gets
         *   it all going...
         *
         *     @param socket Socket connected to another user.
         *     @throw LostCarrierException if connection to user was lost.
         */
    public OnlineSession(Socket socket) throws LostCarrierException
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


        /**
         * Get the SocketStream associated with this OnlineSession.
         *  Use this if the built-in i/o methods (getYN(), etc) don't
         *  suffice for your needs.
         *
         *  @return The SocketStream associated with this OnlineSession.
         */
    public SocketStream getSocketStream()
    {
        return(io);
    } // getSocketStream


        /**
         * Asks the user a Yes, No question.
         *
         *   @param prompt Question to ask user
         *  @return <em>true</em> if user answered yes, <em>false</em>
         *          if the user answered no.
         *   @throw LostCarrierException if connection to user was lost.
         */
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


        /**
         * Dump a file to this session's SocketStream.
         *
         *   @param fileName file to send. This is just blindly sent,
         *          so you'll probably not want to use this for fancy
         *          file transfer.
         *  @return <em>true</em> if entire file was sucessfully sent,
         *          <em>false</em> if there were any problems.
         *   @throw LostCarrierException if connection to user was lost.
         */
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


        /**
         * Get a user name and password from remote client. Check it
         *  for validity.
         *
         *   @return <em>true</em> if valid login, <em>false</em> otherwise.
         *   @throw LostCarrierException if connection to user was lost.
         */
    protected boolean login() throws LostCarrierException
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


        /**
         *  This method tries to find the most functional emulation
         *   available to the remote client, and creates a SocketStream
         *   deriviative based on it.
         *
         *   @param socket Socket for communication with client.
         *  @return SocketStream for all further communication with the client.
         *   @throw LostCarrierException if connection to user was lost.
         */
    protected SocketStream checkEmulation(Socket socket)
                                   throws LostCarrierException
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


        /**
         *  Kick off user, and close socket.
         *
         *    params : void.
         *   returns : void.
         */
    protected void closeConnection()
    {
        if (io != null)
        {
            io.close();
            io = null;
        } // if
    } // closeConnection


        /**
         * Shutdown this session. Kicks off user, kills threads, etc...
         */
    public void shutdown()
    {
        if (sessionThread != null)
        {
            sessionThread.stop();
            sessionThread = null;
        } // if

        closeConnection();
    } // finalize


        /**
         * Pause until user hits a key. This is good for
         *  scrolling multiple pages of text.
         *
         *   @throw LostCarrierException if connection to user was lost.
         */
    protected void pause() throws LostCarrierException
    {
        int i;
        int curLine = 0;
        String pauseMsg = "Hit a key";

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
        else        // try anyhow...
        {
            for (i = 0; i < pauseMsg.length(); i++)
                io.send(ASCII_BACKSPACE);

            for (i = 0; i < pauseMsg.length(); i++)
                io.send(' ');

            for (i = 0; i < pauseMsg.length(); i++)
                io.send(ASCII_BACKSPACE);
        } // else
    } // pause


        /**
         *  This gets called after the user has successfully logged on.
         *
         *   @throw LostCarrierException if connection to user was lost.
         */
    protected void sessionIntro() throws LostCarrierException
    {
        if (JBBSConfig.useAutoPosts)
            AutoPosts.doAutoPosts(this);

        // do the rest...        

    } // sessionIntro


        /**
         * Code drops here after a successful connection has been
         *  made, and a terminal type has been configured.
         *
         *   @throw LostCarrierException if connection to user was lost.
         */
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

        /**
         *  This is just here so the user's JBBS session runs in a separate
         *   thread.
         */
    public void run()
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

