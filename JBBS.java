/**
 *  Initialization code for JBBS. Your Java VM should run this class.
 *
 *    Copyright (c) Lighting and Sound Technologies, 1997.
 *     Written by Ryan C. Gordon.
 */

import java.io.*;

public final class JBBS
{
        // Static Constants...
    public static final String TITLE      = "JBBS";
    public static final int VERSION_MAJOR = 0;
    public static final int VERSION_MINOR = 10;

        // Static variables...
    private static long startTime;


    public static byte asciiByteToLower(byte conv)
    /**
     *  Convert an ASCII-encoded byte from capital to lowercase lettering.
     *
     *     params : conv == byte to convert.
     *    returns : lowercase version if needed, conv if no conversion.
     */
    {
        if ((conv >= 65) && (conv <= 90))  // 'A' to 'Z'?
            conv += 32;                    //  ...then make it 'a' to 'z'.

        return(conv);
    } // asciiByteToLower


    public static String versionString()
    /**
     *  Simply converts VERSION_MAJOR and VERSION_MINOR into a
     *   string.
     *
     *       params : void.
     *      returns : if VERSION ints above where 0 and 10, respectively,
     *                 then return value would be "0.10" ...
     */
    {
        StringBuffer verBuf = new StringBuffer();

        verBuf.append(VERSION_MAJOR);
        verBuf.append('.');
        verBuf.append(VERSION_MINOR);

        return(verBuf.toString());
    } // versionString


    public static boolean getYN(String prompt, boolean def)
                                               throws IOException
    {
        String inLine;
        char ch;

        do
        {
            System.out.print(prompt + " [");
            System.out.print((def) ? "Y/n" : "y/N");
            System.out.print("] : ");
            inLine = readLine(1).trim();
            if (inLine.length() == 0)
                ch = ((def) ? 'y' : 'n');
            else
                ch = inLine.toLowerCase().charAt(0);
        } while ((ch != 'y') && (ch != 'n'));

        return((ch == 'y') ? true : false);
    } // getYN


    public static int getIntDefault(String prompt, int def) throws IOException
    {
        String inLine;

        inLine = getStrDefault(prompt, 8, Integer.toString(def));
        return(Integer.parseInt(inLine));        
    } // getIntDefault


    public static String getStrDefault(String prompt, int max, String def)
                                       throws IOException
    {
        String retVal;

        System.out.print(prompt + " [" + def + "] : ");
        retVal = readLine(max).trim();
        if (retVal.length() == 0)
            retVal = def;

        return(retVal);        
    } // getStrDefault


    public static String readLine(int lineMax) throws IOException
    /**
     *   !!! comment !!!
     */
    {
        byte readByte;
        byte[] inLine = new byte[lineMax];
        int bytesRead = 0;
        boolean getOut = false;
        
        while (!getOut)
        {
            while (System.in.available() == 0)
                Thread.yield();

            readByte = (byte) System.in.read();   // chars? !!!
            if (readByte == '\b')            // backspace?
            {
                if (bytesRead > 0)
                {
                    inLine[bytesRead] = ' ';
                    bytesRead--;
                } // if
            } // if

            else if ((readByte == '\r') || (readByte == '\n'))
            {
                getOut = true;
                System.in.skip(System.in.available());   // clear stream...
            } // if

            else
            {
                if (bytesRead < lineMax)
                {
                    inLine[bytesRead] = readByte;
                    bytesRead++;
                } // if
            } // else
        } // while

        return(new String(inLine));
    } // readLine


    private static boolean parseConsoleCommand(String command)
    /**
     *  !!! comment !!!
     */
    {
        boolean retVal = false;
        long freeMem;
        int splitIndex;
        Runtime rt;
        String params;

        if (command.length() == 0)
            return(false);

        splitIndex = command.indexOf(' ');
        if (splitIndex != -1)
        {
            params = command.substring(splitIndex + 1).trim();
            command = command.substring(0, splitIndex - 1).trim();
        } // if
        else
            params = null;

        if (command.equalsIgnoreCase("shutdown"))
        {
            try
            {
                if (getYN("Are you sure?", false))
                    retVal = true;
            } // try
            catch (IOException e)
            {
                // don't care.
            } // catch
        } // if

        else if (command.equalsIgnoreCase("stats"))
        {
            rt = Runtime.getRuntime();
            freeMem = rt.freeMemory();

            System.out.println("  " + TITLE + " " + versionString());
            System.out.println("  Users currently connected : " +
                               ConnectionManager.getCurrentConnectionCount());
            System.out.println("  Total connection since boot : " +
                               ConnectionManager.getTotalConnectionCount());
            System.out.println("  Memory usage : (" +
                                (rt.totalMemory() - freeMem) + " of "
                                + rt.totalMemory() + " bytes)");
            System.out.println("  Uptime : " + getUptime());
        } // else if

        else if (command.equalsIgnoreCase("gc"))
        {
            rt = Runtime.getRuntime();
            freeMem = rt.freeMemory();

            System.out.println("Doing finalization and garbage collection...");
            rt.runFinalization();
            rt.gc();
            System.out.println("Regained " + 
                                (rt.freeMemory() - freeMem) + " bytes.");
        } // else if

        else if (command.equalsIgnoreCase("help"))
        {
            System.out.println("Available commands...");
            System.out.println("  [help]     -- this information.");
            System.out.println("  [shutdown] -- shutdown BBS server.");
            System.out.println("  [stats]    -- give current stats.");
            System.out.println("  [gc]       -- run garbage collection.");
        } // else if

        else
            System.out.println("Unknown command.");

        return(retVal);
    } // parseConsoleCommand


    private static void processConsole()
    /**
     *   !!! comment !!!
     */
    {
        String command;
        boolean getOut = false;

        System.out.println("Type HELP for list of commands.");
        System.out.println();

        while (!getOut)
        {
            try
            {
                command = readLine(30).trim();
                getOut = parseConsoleCommand(command);
            } // try
            catch (IOException e)
            {
                System.out.println();
                System.out.println("IOException reading console!");
                System.out.println();
                getOut = true;
            } // catch
        } // while
    } // processConsoleCommands


    public static String getUptime()
    /**
     *  Returns a string depicting the time this BBS has been up.
     *
     *     params : void.
     *    returns : see above.
     */
    {
        String retVal;
        long uptimeSecs;
        long uptimeDays;
        long uptimeHours;
        long uptimeMins;

        uptimeSecs = (((System.currentTimeMillis()) - startTime) / 1000);
        uptimeDays = (uptimeSecs / 86400);   // 86400 seconds in a day.

        uptimeSecs %= 86400;
        uptimeHours = (uptimeSecs / 3600);   // 3600 seconds in a hour.

        uptimeSecs %= 3600;
        uptimeMins = (uptimeSecs / 60);      // 60 seconds in a minute.

        uptimeSecs %= 60;

        retVal = uptimeDays + " days, " + uptimeHours + " hours, " +
                 uptimeMins + " minutes, " + uptimeSecs + " seconds.";
        
        return(retVal);
    } // getUptime


    public static void nap(int napTime)
    /**
     *  Make current thread sleep, unconditionally, for napTime milliseconds.
     *  Exceptions are ignored, and, as it goes for any multitasking
     *   environment, timing is not guaranteed to be anywhere near exact.
     *
     *     params : napTime == Time to sleep in milliseconds.
     *    returns : void.
     */
    {
        try
        {
            Thread.sleep(napTime);
        } // try
        catch (Exception e)
        {
            // don't care.
        } // catch
    } // nap (takes int for millisecond delay...)


    protected static boolean initialize(String[] args)
    /**
     *  All sorts of miscellaneous startup crap.
     *
     *       params : args == command line arguments, from JBBS.main();
     *      returns : boolean TRUE on successful init, FALSE otherwise.
     */
    {
        String errMsg;

        System.out.println();
        System.out.println(TITLE + " " + versionString() + " starting up...");
        System.out.println();

        System.runFinalizersOnExit(true);

        try
        {
            System.out.print("Reading configuration file...");
            if (JBBSConfig.in(JBBSConfig.DEFAULT_CFGFILENAME) == false)
            {
                System.out.println("Couldn't access config.");
                System.out.println("   ...Will try defaults.");
            } // if
            else
                System.out.println("good.");
        } // try
        catch (EOFException e)
        {
            System.out.println("Your config file is NOT up to date.");
            System.out.println("   ...run JBBSSetup.class to update it.");
        } // catch

        System.out.print("Opening user database...");
        if (JBBSUser.openUserDB() == false)
        {
            System.out.println("Error.");
            System.out.println("   ...running JBBSSetup.class may help.");
            return(false);
        }// if
        System.out.println("done.");

        System.out.print("Checking database version...");
        if (JBBSUser.checkVersion(VERSION_MAJOR, VERSION_MINOR) == false)
        {
            System.out.println("User database's version doesn't match" +
                               " current JBBS version.");
            System.out.println("   ...run JBBSSetup.class to update it.");
            return(false);
        } // if
        System.out.println("It's cool.");

        System.out.print("Revving up the Connection Manager...");
        try
        {
            new ConnectionManager();
        } // try
        catch (IOException e)
        {
            System.out.println("Error starting ConnectionManager!");
            System.out.println("   ..." + e.getMessage() + ".");
            return(false);
        } // catch

        System.out.println("done.");

        System.out.print  ("Garbage collecting initialization objects...");
        System.runFinalization(); // Clean up initial crap before we begin.
        System.gc();
        System.out.println("done.");

        startTime = System.currentTimeMillis();
        System.out.println("Initialization is complete; System's up!");
        return(true);
    } // initialize


    public static void cleanup()
    /**
     *  Various random cleanups and shit.
     *
     *      params : void.
     *     returns : void.
     */
    {
        System.out.println();
        System.out.println("   ...Shutting down...");
        ConnectionManager.shutdown();
        System.out.println();
    } // cleanup


    public static void main(String[] args)
    /**
     *  The Mainline of JBBS. Doesn't do much. There's no command line
     *   arguments right now.
     *
     *    params : args[] == command line arguments, if any.
     *   returns : void.
     */
    {
        if (!initialize(args))
            System.out.println("   ...aborting...");
        else
        {
            processConsole();
            cleanup();
        } // else
    } // main

} // JBBS

// end of JBBS.java ...

