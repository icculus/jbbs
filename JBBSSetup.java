/**
 * Application for creating/updating JBBS user database.
 *
 *   Copyright (c) Lighting and Sound Technologies, 1997.
 *    Written by Ryan C. Gordon.
 */

import java.io.*;

public class JBBSSetup extends JBBSUser
{
    protected static final int VERSETUP_MAJOR = 0;
    protected static final int VERSETUP_MINOR = 10;

    protected static boolean createDB() throws IOException
    {
        JBBSUser rootUser = new JBBSUser();

        rootUser.handle   = "root";
        rootUser.realName = "John Sysop";
        rootUser.number = 0;
        rootUser.password = "";
        update(rootUser);

        rndDB.seek(0);                     // update version tags...
        rndDB.writeByte(VERSETUP_MAJOR);
        rndDB.writeByte(VERSETUP_MINOR);

        return(true);
    } // createDB


    protected static boolean updateUserDB() throws IOException
    {
        System.out.println("Your user database isn't from this version.");
        System.out.print  ("   ...Attempting to update...");

        if (dbMajorVer == 0)
        {
            if (dbMinorVer == 0)     // version 0.0; new file.
            {
                System.out.println("Actually, it's new.");
                System.out.print  ("   ...Building default...");
                return(createDB());
            } // if
        } // if

        return(false);     // if code falls here, version was unrecognized.
    } // updateUserDB


    protected static boolean initialize(String[] args)
    {
        String cfgFileName;

// !!!    if (args.length > 0)
//            cfgFileName = args[0];
//        else
            cfgFileName = JBBSConfig.DEFAULT_CFGFILENAME;

        System.out.println("Using config file '" + cfgFileName + "'.");

        try
        {
            if (JBBSConfig.in(cfgFileName) == false)
                System.out.println("Couldn't read config file.");
        } // try
        catch (EOFException e)
        {
            System.out.println("Config file is from older version." +
                               "   ...will be updated.");
        } // catch

        if (openUserDB() == false)
        {
            System.out.println("Cannot access user database.");
            System.out.println("  ...make sure JBBS isn't running.");
            return(false);
        } // if

        if ((JBBS.VERSION_MAJOR != VERSETUP_MAJOR) ||
            (JBBS.VERSION_MINOR != VERSETUP_MINOR))
        {
            System.out.println("You are running the wrong version of" +
                               " JBBSSetup.class.");
            return(false);
        } // if

        checkVersion(VERSETUP_MAJOR, VERSETUP_MINOR);
        return(true);
    } // initialize


    public static void configSystem() throws IOException,
                                             NumberFormatException
    {
        String pathSep = System.getProperty("file.separator");

        System.out.println("Configuring System...");
        System.out.println();

        JBBSConfig.maxConnections = JBBS.getIntDefault(
                                              "Maximum connections?",
                                              JBBSConfig.maxConnections);

        JBBSConfig.portNum = JBBS.getIntDefault(
                          "TCP/IP port to listen on for connections?",
                          JBBSConfig.portNum);

        JBBSConfig.idleTimeout = JBBS.getIntDefault(
                                     "Idle user timeout in minutes?",
                                     JBBSConfig.idleTimeout);

        JBBSConfig.loginTries = JBBS.getIntDefault(
                                   "Login attempts before disconnect?",
                                   JBBSConfig.loginTries);

        JBBSConfig.BBSName = JBBS.getStrDefault("BBS Name?", 75,
                                                JBBSConfig.BBSName);

        JBBSConfig.dataDir = JBBS.getStrDefault("Data directory?", 255,
                                                JBBSConfig.dataDir);
        if (!JBBSConfig.dataDir.endsWith(pathSep))
            JBBSConfig.dataDir += pathSep;

        JBBSConfig.useAutoPosts = JBBS.getYN("Use autoposts?",
                                             JBBSConfig.useAutoPosts);
        JBBSConfig.logIOExceptions = JBBS.getYN("Log IOExceptions?",
                                             JBBSConfig.logIOExceptions);
        JBBSConfig.logLostCarrierExceptions = JBBS.getYN("Log disconnects?",
                                        JBBSConfig.logLostCarrierExceptions);

        if (JBBS.getYN("Accept new configuration?", false))
        {
            if (JBBSConfig.out(JBBSConfig.DEFAULT_CFGFILENAME) == false)
                System.out.println("Error writing config file.");
            else
                System.out.println("Done.");
	} // if
    } // configSystem


    public static void main(String[] args) throws IOException,
                                                  NumberFormatException
    {
        if (!initialize(args))
            return;

        if ((dbMajorVer != VERSETUP_MAJOR) ||
            (dbMinorVer != VERSETUP_MINOR))
        {
            if (!updateUserDB())
            {
                System.out.println("Couldn't update user database.");
                return;
            } // if
            else
                System.out.println("OK.");
        } // if

        configSystem();
    } // main
} // JBBSSetup

// end of JBBSSetup.java ...


