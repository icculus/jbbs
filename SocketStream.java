/**
 *  Class to make JBBS Socket I/O much more friendly.
 *
 *  This class wraps a Socket object, adding friendly I/O
 *   methods, terminal emulation, and more functionality.
 *
 *  Depending on the setting of JBBSConfig.logIOExceptions,
 *   I/O exceptions are recorded in JBBS's logfile.
 *
 *    Copyright (c) Lighting and Sound Technologies, 1997.
 *     Written by Ryan C. Gordon.
 */

import java.io.*;
import java.net.*;

public abstract class SocketStream
{
        // Constants...
    public static final byte ASCII_BACKSPACE = 8;

    public static final int TERMCOLOR_BLACK     = 0;
    public static final int TERMCOLOR_RED       = 1;
    public static final int TERMCOLOR_GREEN     = 2;
    public static final int TERMCOLOR_YELLOW    = 3;
    public static final int TERMCOLOR_BLUE      = 4;
    public static final int TERMCOLOR_MAGENTA   = 5;
    public static final int TERMCOLOR_CYAN      = 6;
    public static final int TERMCOLOR_WHITE     = 7;
    public static final int INTENSITY_COLORS    = 8;
    public static final int TERMCOLOR_HIBLACK   = 8;
    public static final int TERMCOLOR_HIRED     = 9;
    public static final int TERMCOLOR_HIGREEN   = 10;
    public static final int TERMCOLOR_HIYELLOW  = 11;
    public static final int TERMCOLOR_HIBLUE    = 12;
    public static final int TERMCOLOR_HIMAGENTA = 13;
    public static final int TERMCOLOR_HICYAN    = 14;
    public static final int TERMCOLOR_HIWHITE   = 15;

        // Instance variables...
    protected Socket       socket;      // the initial socket.
    protected OutputStream out;         // for writing to socket.
    protected InputStream  in;          // for reading from socket.

    public SocketStream(Socket s) throws LostCarrierException
    {
        socket = s;

        try
        {
            out = socket.getOutputStream();
            in  = socket.getInputStream();
        } // try
        catch (IOException e)
        {
            handleIOException(e, "SocketStream.<init>");
        } // catch
    } // Constructor


    public static void handleIOException(IOException e, String details)
                                        throws LostCarrierException
    /**
     *   This methods tries to log any I/O problems, then throws the
     *    exception, so other routines may deal with the exception as
     *    necessary.
     *
     *       params : e == an IOException generated through network woes.
     *      returns : void, but always...
     *       throws : LostCarrierException
     */
    {
        String errStr = e.getMessage();

        //if (JBBSConfig.logIOExeceptions)                  !!!
        //    JBBSLog.add("IOException", errStr);

        throw(new LostCarrierException(errStr, details));
    } // handleIOException


    public void send(String str) throws LostCarrierException
    /**
     *  Send a string of text to the remote user.
     *
     *     params : str == String to transmit to remote client.
     *    returns : void.
     *     throws : LostCarrierException == on network errors...
     */
    {
        try
        {
            out.write(str.getBytes());
        } // try
        catch (IOException e)
        {
            handleIOException(e, "SocketStream.send(String);");
        } // catch
    } // send (takes String)


    public void sendln(String str) throws LostCarrierException
    {
        send(str + "\r\n");
    } // sendln (takes String)


    public void send(byte b) throws LostCarrierException
    {
        try
        {
            out.write(b);
        } // try
        catch (IOException e)
        {
            handleIOException(e, "SocketStream.send(byte);");
        } // catch
    } // send (one byte)


    public void sendln(byte b) throws LostCarrierException
    {
        send(b);
        sendln();
    } // sendln (one byte)


    public void send(char ch) throws LostCarrierException
    {
        send((byte) ch);
    } // send (one char, reduced to a byte)


    public void sendln(char ch) throws LostCarrierException
    {
        sendln((byte) ch);
    } // sendln (one char, reduced to a byte)


    public void send(byte[] bytes, int len) throws LostCarrierException
    /**
     *  Send an first len bytes in an array to the remote user.
     *
     *     params : bytes == Bytes to transmit to remote client.
     *                       this sends bytes.length bytes.
     *              len   == number of bytes to send.
     *    returns : void.
     *     throws : LostCarrierException == on network errors...
     */
    {
        try
        {
            out.write(bytes, 0, len);
        } // try
        catch (IOException e)
        {
            handleIOException(e, "SocketStream.send(byte[], len);");
        } // catch
    } // send (takes first x bytes in array of bytes)


    public void send(byte[] bytes) throws LostCarrierException
    /**
     *  Send an array of bytes to the remote user.
     *
     *     params : bytes == Bytes to transmit to remote client.
     *                       this sends bytes.length bytes.
     *    returns : void.
     *     throws : LostCarrierException == on network errors...
     */
    {
        try
        {
            out.write(bytes);
        } // try
        catch (IOException e)
        {
            handleIOException(e, "SocketStream.send(byte[]);");
        } // catch
    } // send (takes byte[])


    public void sendln(byte[] bytes) throws LostCarrierException
    {
        send(bytes);
        sendln();
    } // sendln (takes byte[])


    public void sendln() throws LostCarrierException
    /**
     *  Sends a carriage return/line feed.
     *
     *    params : void.
     *   returns : void.
     */
    {
        send("\r\n");
    } // sendln (void)


    public byte recv() throws LostCarrierException
    /**
     *   Return one byte from the socket.
     *
     *     params : void.
     *    returns : see above.
     *     throws : LostCarrierException == on network errors...
     */
    {
        try
        {
            return((byte) in.read());
        } // try
        catch (IOException e)
        {
            handleIOException(e, "SocketStream.recv();");
        } // catch

        return(0);    // keeps compiler happy; never hits this point.
    } // recv


    public String recvln(int max) throws LostCarrierException
    {
        StringBuffer strBuf = new StringBuffer();
        byte inByte;
        boolean getOut = false;

        do
        {
            inByte = recv();

            switch (inByte)
            {
                case ASCII_BACKSPACE:
                    if (strBuf.length() > 0)
                    {
                        // !!! remove last byte from StringBuffer...
                        send(ASCII_BACKSPACE);
                        send(' ');
                        send(ASCII_BACKSPACE);
                    } // if
                    break;

                case '\r':
                    sendln();
                    getOut = true;
                    break;

                default:
                    if (strBuf.length() < max)
                    {
                        send(inByte);
                        strBuf.append((char) inByte);
                    } // if
                   break;
            } // switch
        } while (getOut == false);

        clearBuffer();
        return(strBuf.toString());
    } // recvln (takes maximum entry length)


    public String recvln() throws LostCarrierException
    {
        return(recvln(Integer.MAX_VALUE));
    } // recvln (default maximums)


    public int dataWaiting() throws LostCarrierException
    /**
     *  Find out if there's any user input waiting to be processed.
     *
     *     params : void.
     *    returns : number of bytes waiting.
     *     throws : LostCarrierException == on network errors...
     */
    {
        try
        {
            return(in.available());
        } // try
        catch (IOException e)
        {
            handleIOException(e, "SocketStream.dataWaiting();");
        } // catch

        return(0);    // keeps compiler happy; never hits this point.
    } // dataWaiting


    public void clearBuffer() throws LostCarrierException
    /**
     *  Throw out any characters waiting from the inputstream.
     *
     *     params : void.
     *    returns : void.
     *     throws : LostCarrierException == on network errors...
     */
    {
        int bytesWaiting;
        try
        {
            bytesWaiting = in.available();
            if (bytesWaiting > 0)
                in.skip(bytesWaiting);
        } // try
        catch (IOException e)
        {
            handleIOException(e, "SocketStream.clearBuffer();");
        } // catch
    } // clearBuffer


    public void close()
    /**
     *  Close a socket. Disconnects user, and clears up resources.
     */
    {
        try
        {
            in.close();
            out.close();
            socket.close();
        } // try
        catch (IOException e)
        {
            try
            {
                handleIOException(e, "SocketStream.close();");
            } // try
            catch (LostCarrierException lce)
            {
                // don't care...
            } // catch
        } // catch
    } // close


        /**
         *  These abstract members allow for terminal emulations.
         *
         *    Derive SocketStream to make various term emulations,
         *     such as ANSI, AVATAR, VT100, etc...
         */
    public abstract String getTermEmulName();
    public abstract boolean doesForeColor();
    public abstract boolean doesBackColor();
    public abstract boolean doesPositioning();
    public abstract void setForeColor(int newColor) throws LostCarrierException;
    public abstract void setBackColor(int newColor) throws LostCarrierException;
    public abstract int getPosX() throws LostCarrierException;
    public abstract int getPosY() throws LostCarrierException;
    public abstract void setPosXY(int X, int Y) throws LostCarrierException;
    public abstract void clearTerminal() throws LostCarrierException;

} // SocketStream

// end of SocketStream.java ...

