/**
 *  When all other terminal emulations fail, there's always...
 *
 *      DUMBTERMINAL!
 *
 *  No colors, no positions, no nifties at all. Just a stream o'
 *   bytes.
 *
 *
 *    Copyright (c) Lighting and Sound Technologies, 1997.
 *     Written by Ryan C. Gordon.
 */

import java.net.*;

public class DumbTerminal extends SocketStream
{
    protected static final String TERMEMUL_NAME = "Dumb";

    public DumbTerminal(Socket s) throws LostCarrierException
    {
        super(s);
        send("\r\n\r\n");
    } // DumbTerminal


        // SocketStream's abstract method overrides...

    public String getTermEmulName()
    {
        return(TERMEMUL_NAME);
    } // getTermEmulName

    public boolean doesForeColor()
    {
        return(false);
    } // doesForeColor

    public boolean doesBackColor()
    {
        return(false);
    } // doesBackColor

    public boolean doesPositioning()
    {
        return(false);
    } // doesPositioning

    public void setForeColor(int newColor) throws LostCarrierException
    {
    } // setForeColor

    public void setBackColor(int newColor) throws LostCarrierException
    {
    } // setBackColor

    public int getPosX() throws LostCarrierException
    {
        return(-1);
    } // getPosX

    public int getPosY() throws LostCarrierException
    {
        return(-1);
    } // getPosY

    public void setPosXY(int X, int Y) throws LostCarrierException
    {
    } // setPosXY

    public void clearTerminal() throws LostCarrierException
    /**
     * Okay, it's retarded, but it works. Send 50 crlfs, and it's just
     *  about the same as a clear screen...
     */
    {
        int i;

        for (i = 1; i < 50; i++)
            send("\r\n");
    } // clearTerminal

} // DumbTerminal


