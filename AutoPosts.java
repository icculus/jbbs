/**
 * Okay, it's a rip-off of VBBS, and it probably won't be the last.
 *
 *   AutoPosts: for leaving brief messages for others to see when they log
 *    on...
 *
 *    Copyright (c) Lighting and Sound Technologies, 1997.
 *     Written by Ryan C. Gordon.
 */

import java.io.*;

public class AutoPosts
{
    protected static final String POST_FILENAME = "autoposts";

    protected static void addAutoPost(OnlineSession session)
                                throws LostCarrierException
    {
        int i;
        String[] postLines = new String[4];

        postLines[0] = "From: " + session.user.getHandle() + "\r\n";

        if (session.getYN("Write an autopost? : "))
        {
            for (i = 0; i < postLines.length; i++)
                postLines[i] = session.getSocketStream().recvln(78);

            writeAutoPostMutex(postLines);
        } // if

    } // addAutoPost


    protected static synchronized byte[] readAutoPostsMutex()
    /**
     *  This function protects access to the autoposts file, which is
     *   constantly being reread and rewritten.
     *
     *    params : void.
     *   returns : whole autopost file mapped to a byte array.
     */
    {
        byte[] retVal;
        FileInputStream fin;
        BufferedInputStream buffer;
        int bytesToRead;

        try
        {
            fin = new FileInputStream(JBBSConfig.dataDir + POST_FILENAME);
            buffer = new BufferedInputStream(fin);
        } // try

        catch (IOException e)
        {
            return(null);
        } // catch

        try
        {
            bytesToRead = buffer.available();
            retVal = new byte[bytesToRead];
            buffer.read(retVal);
            buffer.close();
        } // try

        catch (IOException e)
        {
            retVal = null;
        } // catch

        return(retVal);
    } // readAutoPostsMutex


    protected static synchronized void writeAutoPostMutex(String[] lines)
    /**
     *  Prevents multiple threads from opening, let alone writing to,
     *   the autoposts file.
     *
     *     params : lines == array of lines to write to file.
     *    returns : void.
     */
    {
        FileOutputStream fout;
        int i;

        try
        {
            fout = new FileOutputStream(JBBSConfig.dataDir + POST_FILENAME);
        } // try

        catch (IOException e)
        {
            return;
        } // catch

        try
        {
            for (i = 0; i < lines.length; i++)
            {
                fout.write(lines[i].getBytes());
                fout.write('\r');
                fout.write('\n');
            } // for
        } // try

        catch (IOException e)
        {
            // don't care.
        } // catch

        try
        {
            fout.close();
        } // try
        catch (IOException e)
        {
            // don't care.
        } // catch

    } // writeAutoPostMutex


    public static void doAutoPosts(OnlineSession session)
                                    throws LostCarrierException
    /**
     *  Call this from whereever, and this class will deal with the
     *   rest of the details.
     *
     *     params : session == Session needing some autopost lovin'...
     *    returns : void.
     */
    {
        SocketStream io = session.getSocketStream();
        byte[] autoPostBytes;

        autoPostBytes = readAutoPostsMutex();
        if (autoPostBytes == null)
            io.sendln("Sorry, autoposts not available.");
        else
        {
            io.sendln();
            io.send(autoPostBytes);
            io.sendln();
            addAutoPost(session);
        } // else
    } // doAutoPosts

} // AutoPosts


// end of AutoPosts.java ...


