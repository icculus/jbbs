/**
 *  ANSI terminal emulation for JBBS. This class derives SocketStream.
 *
 *    Copyright (c) Lighting and Sound Technologies, 1997.
 *     Written by Ryan C. Gordon.
 */

import java.io.*;
import java.net.Socket;
import SocketStream;

public class ANSITerminal extends SocketStream
{
        // constants...
    public static final byte ASCII_ESCAPE = 27;
    protected static final String TERMEMUL_NAME = "ANSI";

        // Instance variables...
    protected int currentBackColor;

    public static boolean detectTerminal(InputStream _in, OutputStream _out)
                            throws LostCarrierException
    /**
     *   Check to see if ANSI is supported by remote client...
     *
     *     params : in, out == streams (sockets, etc...) for communicating
     *                         with remote client.
     *    returns : boolean true is ANSI supported, boolean false otherwise.
     */
    {
        int i;
        boolean getOut = false;
        byte[] detectSequence = new byte[3];
        byte sentBack = 0;

        detectSequence[0] = ASCII_ESCAPE;
        detectSequence[1] = (byte) '[';
        detectSequence[2] = (byte) 'R';

        try
        {
            _in.skip(_in.available());       // clear inputstream...
            _out.write(detectSequence);      // send detection sequence...

                // block 3 seconds, or until character returned...
            for (i = 0; (i < 3) && (!getOut); i++)
            {
                JBBS.nap(1000);     // wait a second...
                if (_in.available() > 0)
                {
                    sentBack = (byte) _in.read();
                    getOut = true;
                } // if
            } // for

        } // try

        catch (IOException e)
        {
            handleIOException(e, "ANSITerminal.detectTerminal();");
        } // catch

        return((sentBack == ASCII_ESCAPE) ? true : false);
    } // detectTerminal


    public ANSITerminal(Socket s) throws LostCarrierException
    /**
     *  Don't actually create an instance of ANSITerminal unless the
     *   static method ANSITerminal.detectTerminal() returns true.
     *
     *      params : s == socket that connects to client with ANSI support.
     *                    This gets passed to SocketStream's constructor.
     *     returns : Constructor; void.
     *      throws : LostCarrierException == on network errors.
     */
    {
        super(s);
        setForeColor(TERMCOLOR_WHITE);
        setBackColor(TERMCOLOR_BLACK);
        clearTerminal();
    } // Constructor


        /**
         * Overrides for abstract SocketStream methods...
         */

    public String getTermEmulName()
    {
        return(TERMEMUL_NAME);
    } // getTermEmulName

    public boolean doesForeColor()
    {
        return(true);
    } // doesForeColor

    public boolean doesBackColor()
    {
        return(true);
    } // doesBackColor

    public boolean doesPositioning()
    {
        return(true);
    } // doesPositioning

    public void setBackColor(int newColor) throws LostCarrierException
    {
        byte[] colorSequence = new byte[5];  // ANSI sequence to be sent.

        colorSequence[0] = ASCII_ESCAPE;     // ANSI escape sequence...
        colorSequence[1] = (byte) '[';
        colorSequence[2] = (byte) '4';       // signify backcolor change...

        switch (newColor)
        {
            case TERMCOLOR_BLACK:
            case TERMCOLOR_HIBLACK:
                colorSequence[3] = (byte) '0';
                break;

            case TERMCOLOR_RED:
            case TERMCOLOR_HIRED:
                colorSequence[3] = (byte) '1';
                break;

            case TERMCOLOR_GREEN:
            case TERMCOLOR_HIGREEN:
                colorSequence[3] = (byte) '2';
                break;

            case TERMCOLOR_YELLOW:
            case TERMCOLOR_HIYELLOW:
                colorSequence[3] = (byte) '3';
                break;

            case TERMCOLOR_BLUE:
            case TERMCOLOR_HIBLUE:
                colorSequence[3] = (byte) '4';
                break;

            case TERMCOLOR_MAGENTA:
            case TERMCOLOR_HIMAGENTA:
                colorSequence[3] = (byte) '5';
                break;

            case TERMCOLOR_CYAN:
            case TERMCOLOR_HICYAN:
                colorSequence[3] = (byte) '6';
                break;

            case TERMCOLOR_WHITE:
            case TERMCOLOR_HIWHITE:
                colorSequence[3] = (byte) '7';
                break;

            default:        // bogus color.
                return;
        } // switch

        colorSequence[4] = (byte) 'm';     // signifies color change request.

        send(colorSequence);
        currentBackColor = newColor;
    } // setBackColor

    
    public void setForeColor(int newColor) throws LostCarrierException
    {
        byte[] colorSequence = new byte[7];  // ANSI sequence to be sent.

        colorSequence[0] = ASCII_ESCAPE;
        colorSequence[1] = (byte) '[';

        if (newColor >= INTENSITY_COLORS)
            colorSequence[2] = (byte) '1';   // signify intensity attribute.
        else
            colorSequence[2] = (byte) '0';   // signify attribute reset.
            
        colorSequence[3] = (byte) ';';
        colorSequence[4] = (byte) '3';       // '3' == signifies fore color.

        switch (newColor)
        {
            case TERMCOLOR_BLACK:
            case TERMCOLOR_HIBLACK:
                colorSequence[5] = (byte) '0';
                break;

            case TERMCOLOR_RED:
            case TERMCOLOR_HIRED:
                colorSequence[5] = (byte) '1';
                break;

            case TERMCOLOR_GREEN:
            case TERMCOLOR_HIGREEN:
                colorSequence[5] = (byte) '2';
                break;

            case TERMCOLOR_YELLOW:
            case TERMCOLOR_HIYELLOW:
                colorSequence[5] = (byte) '3';
                break;

            case TERMCOLOR_BLUE:
            case TERMCOLOR_HIBLUE:
                colorSequence[5] = (byte) '4';
                break;

            case TERMCOLOR_MAGENTA:
            case TERMCOLOR_HIMAGENTA:
                colorSequence[5] = (byte) '5';
                break;

            case TERMCOLOR_CYAN:
            case TERMCOLOR_HICYAN:
                colorSequence[5] = (byte) '6';
                break;

            case TERMCOLOR_WHITE:
            case TERMCOLOR_HIWHITE:
                colorSequence[5] = (byte) '7';
                break;

            default:        // bogus color.
                return;
        } // switch

        colorSequence[6] = (byte) 'm';     // signifies color change.

        send(colorSequence);

            // Sending a "non intensity" command is the same as
            //  sending a "reset" command, so we lose the background
            //  color. If the new forecolor is non-intensity, just resend
            //  the current backcolor...
        if (newColor < INTENSITY_COLORS)
            setBackColor(currentBackColor);
    } // setForeColor


    protected int intFromByteArray(byte[] conv, int start, int len)
    /**
     *  !!!
     */
    {
        int base;
        int intFromASCII;
        int retVal = 0;

        len--;

        for (base = 1; len > -1; len--)
        {
            intFromASCII = conv[start + len] - ((byte) '0');
            retVal += (intFromASCII * base);
            base *= 10;
        } // for

        return(retVal);
    } // intFromByteArray


    protected int[] getPosXY() throws LostCarrierException
    {
        int[] retVal = new int[2];
        byte[] getPosSequence = new byte[3];    // request position sequence.
        byte[] reply = new byte[10];            // Client's returned answer.
        int rIndex = 0;                         // Reply array's index.

        getPosSequence[0] = ASCII_ESCAPE;       // set up for request.
        getPosSequence[1] = (byte) '[';
        getPosSequence[2] = (byte) 'R';

        send(getPosSequence);      // send request position sequence.
 
        do      // get client reply.
        {
            reply[rIndex] = recv();
            rIndex++;
        } while ((reply[rIndex - 1] != (byte) 'R') && (rIndex < reply.length));

        retVal[0] = intFromByteArray(reply, 2, 2);  // X position
        retVal[1] = intFromByteArray(reply, 5, 2);  // Y position

        return(retVal);
    } // getPosXY


    public int getPosX() throws LostCarrierException
    {
        int[] posXY;

        posXY = getPosXY();
        return(posXY[0]);        
    } // getPosX


    public int getPosY() throws LostCarrierException
    {
        int[] posXY;

        posXY = getPosXY();
        return(posXY[1]);
    } // getPosY


    protected int intToByteArray(int iConv, byte[] bArray, int start)
    /**
     *   !!!
     */
    {
        int i;
        int retVal = 1;

        if (iConv == 0)    
            bArray[start] = (byte) '0';
        else
        {
            for (i = 1; i < iConv; i *= 10);   // find decimal places.

            i /= 10;   // bump it back one decimal place...

            for (retVal = 0; i >= 1; i /= 10)
            {
                bArray[start + retVal] = (byte) ((iConv / i) + (byte) '0');
                retVal++;
                iConv %= 10;
            } // for
        } // else

        return(retVal);
    } // intToByteArray


    public void setPosXY(int X, int Y) throws LostCarrierException
    {
// ```!!! REWRITE !!!
        byte[] newPosSequence = new byte[8];
        int seqIndex;

        newPosSequence[0] = ASCII_ESCAPE;
        newPosSequence[1] = (byte) '[';

        seqIndex = intToByteArray(X, newPosSequence, 2);

        newPosSequence[3 + seqIndex] = (byte) ';';

        seqIndex = intToByteArray(Y, newPosSequence, 4 + seqIndex);

        newPosSequence[3] = (byte) 'H';
        send(newPosSequence);
    } // setPosXY


    public void clearTerminal() throws LostCarrierException
    {
        byte clearSequence[] = new byte[3];

        clearSequence[0] = ASCII_ESCAPE;
        clearSequence[1] = (byte) '2';
        clearSequence[2] = (byte) 'J';

        send(clearSequence);
    } // clearTerminal

} // ANSITerminal

// end of ANSITerminal.java ...

