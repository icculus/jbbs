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
    public static final byte ASCII_ESCAPE    = 27;
    public static final byte ASCII_CR        = 13;
    public static final byte ASCII_LF        = 10;

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


        /**
         * This methods tries to log any I/O problems, then throws the
         *  exception, so other routines may deal with the exception as
         *  necessary.
         *
         *   @param e An IOException generated through network woes.
         *   @param details Text of error details: method exception called in.
         *   @throw LostCarrierException if connection to user was lost.
         */
    public static void handleIOException(IOException e, String details)
                                        throws LostCarrierException
    {
        String errStr = e.getMessage();

        //if (JBBSConfig.logIOExeceptions)                  !!!
        //    JBBSLog.add("IOException", errStr);

        throw(new LostCarrierException(errStr, details));
    } // handleIOException


        /**
         *  Sends a carriage return/line feed (a "newline").
         *
         *   @throw LostCarrierException if connection to user was lost.
         */
    public void sendln() throws LostCarrierException
    {
        send("\r\n");
    } // sendln (void)


        /**
         *  Send a string of text to the remote user.
         *
         *   @param str String to transmit to remote client.
         *   @throw LostCarrierException if connection to user was lost.
         */
    public void send(String str) throws LostCarrierException
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


        /**
         *  Send a string of text to the remote user, with newline appended.
         *
         *   @param str String to transmit to remote client.
         *   @throw LostCarrierException if connection to user was lost.
         */
    public void sendln(String str) throws LostCarrierException
    {
        send(str + "\r\n");
    } // sendln (takes String)


        /**
         *  Send one byte to the remote user.
         *
         *   @param b Byte to transmit.
         *   @throw LostCarrierException if connection to user was lost.
         */
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


        /**
         *  Send one byte to the remote user, followed by a newline.
         *
         *   @param b Byte to transmit.
         *   @throw LostCarrierException if connection to user was lost.
         */
    public void sendln(byte b) throws LostCarrierException
    {
        send(b);
        sendln();
    } // sendln (one byte)


        /**
         *  Send one char to the remote user. It is cast to a byte before
         *   sending, so you still only get one byte.
         *
         *   @param ch Char (byte) to transmit.
         *   @throw LostCarrierException if connection to user was lost.
         */
    public void send(char ch) throws LostCarrierException
    {
        send((byte) ch);
    } // send (one char, reduced to a byte)


        /**
         *  Send one char to the remote user. It is cast to a byte before
         *   sending, so you still only get one byte.
         *
         *  A newline is also sent.
         *
         *   @param ch Char (byte) to transmit.
         *   @throw LostCarrierException if connection to user was lost.
         */
    public void sendln(char ch) throws LostCarrierException
    {
        sendln((byte) ch);
    } // sendln (one char, reduced to a byte)


        /**
         *  Send the first len bytes in an array to the remote user.
         *
         *   @param bytes Bytes to transmit to remote client.
         *   @param len Number of bytes to send.
         *   @throw LostCarrierException if connection to user was lost.
         */
    public void send(byte[] bytes, int len) throws LostCarrierException
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


        /**
         *  Send an array of bytes to the remote user.
         *
         *   @param bytes Bytes to transmit to remote client.
         *                <em>bytes.length</em> bytes will be sent.
         *   @throw LostCarrierException if connection to user was lost.
         */
    public void send(byte[] bytes) throws LostCarrierException
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


        /**
         *  Send an array of bytes to the remote user.
         *
         *  A newline is also sent.
         *
         *   @param bytes Bytes to transmit to remote client.
         *                <em>bytes.length</em> bytes will be sent.
         *   @throw LostCarrierException if connection to user was lost.
         */
    public void sendln(byte[] bytes) throws LostCarrierException
    {
        send(bytes);
        sendln();
    } // sendln (takes byte[])


        /**
         * Retrieve one byte from the socket. Blocks until it retrieves it.
         *
         *   @returns Byte from socket.
         *   @throw LostCarrierException if connection to user was lost.
         */
    public byte recv() throws LostCarrierException
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


        /**
         * Retrieve a line of text from socket. The line is delimited by
         *  a carriage return (ASCII 13) byte sent through the socket.
         *
         * A maximum size of input is specified.
         *
         *   @param max Maximum characters to accept in String.
         *  @return String of read bytes.
         *   @throw LostCarrierException if connection to user was lost.
         */
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

                case ASCII_CR:
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


        /**
         * Retrieve a line of text from socket. The line is delimited by
         *  a carriage return (ASCII 13) byte sent through the socket.
         *
         *  @return String of read bytes.
         *   @throw LostCarrierException if connection to user was lost.
         */
    public String recvln() throws LostCarrierException
    {
        return(recvln(Integer.MAX_VALUE));
    } // recvln (default maximums)


        /**
         *  Find out if there's any user input waiting to be processed.
         *
         *   @return Number of bytes waiting.
         *    @throw LostCarrierException if connection to user was lost.
         */
    public int dataWaiting() throws LostCarrierException
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


        /**
         *  Throw out any characters waiting from the inputstream.
         *
         *    @throw LostCarrierException if connection to user was lost.
         */
    public void clearBuffer() throws LostCarrierException
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


        /**
         *  Close a socket. Disconnects user, and clears up resources.
         */
    public void close()
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

