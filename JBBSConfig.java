/**
 *  Configuration for JBBS at runtime.
 *
 *    Copyright (c) Lighting and Sound Technologies, 1997.
 *     Written by Ryan C. Gordon.
 */

//import java.awt.*;
//import java.awt.event.*;

import java.io.*;

public class JBBSConfig // extends Frame implements WindowListener
{
        // Static Constants...
    public static final int DEFAULT_MAXCONNECTIONS = 4;
    public static final int DEFAULT_PORTNUM = 72; // !!! 23;
    public static final int DEFAULT_IDLETIMEOUT = 5;
    public static final int DEFAULT_LOGINTRIES = 3;
    public static final String DEFAULT_CFGFILENAME = "jbbs.cfg";
    public static final String BACKUP_CFGFILENAME  = "jbbscfg.bak";

        // Global config settings...
    public static int maxConnections = DEFAULT_MAXCONNECTIONS;
    public static int portNum = DEFAULT_PORTNUM;
    public static int idleTimeout = DEFAULT_IDLETIMEOUT;
    public static int loginTries = DEFAULT_LOGINTRIES;
    public static String BBSName = "My BBS";
    public static String dataDir = ".";
    public static boolean useAutoPosts = true;
    public static boolean logIOExceptions = true;
    public static boolean logLostCarrierExceptions = true;
    public static boolean debugging = true;

        // Static config file accessing methods...
    public static synchronized boolean in(String cfgFileName)
                                    throws EOFException
    {
        RandomAccessFile cfgFile = null;
        boolean retVal = true;

        try
        {
            cfgFile = new RandomAccessFile(cfgFileName, "r");
        } // try
        catch (IOException e)
        {
            retVal = false;
        } // catch

        if (retVal == true)
        {
            try
            {
                cfgFile.seek(0);
                maxConnections = cfgFile.readInt();
                portNum = cfgFile.readInt();
                idleTimeout = cfgFile.readInt();
                loginTries = cfgFile.readInt();
                BBSName = cfgFile.readUTF();
                dataDir = cfgFile.readUTF();
                useAutoPosts = cfgFile.readBoolean();
                logIOExceptions = cfgFile.readBoolean();
                logLostCarrierExceptions = cfgFile.readBoolean();
                cfgFile.close();
            } // try

            catch (EOFException e)
            {
                try
                {
                    cfgFile.close();
                } // try
                catch (IOException ioe)
                {
                    // don't care. We've done all we can at this point...
                } // catch
                throw (e);
            } // catch (EOFException)

            catch (IOException e)
            {
                retVal = false;
            } // catch (IOException)
        } // if

        return(retVal);
    } // in


    public static synchronized boolean out(String cfgFileName)
    {
        RandomAccessFile cfgFile = null;
        boolean retVal = true;

        try
        {
            cfgFile = new RandomAccessFile(cfgFileName, "rw");
        } // try
        catch (IOException e)
        {
            retVal = false;
        } // catch

        if (retVal == true)
        {
            try
            {
                cfgFile.seek(0);
                cfgFile.writeInt(maxConnections);
                cfgFile.writeInt(portNum);
                cfgFile.writeInt(idleTimeout);
                cfgFile.writeInt(loginTries);
                cfgFile.writeUTF(BBSName);
                cfgFile.writeUTF(dataDir);
                cfgFile.writeBoolean(useAutoPosts);
                cfgFile.writeBoolean(logIOExceptions);
                cfgFile.writeBoolean(logLostCarrierExceptions);
                cfgFile.close();
            } // try

            catch (IOException e)
            {
                retVal = false;
            } // catch (IOException)
        } // if

        return(retVal);
    } // out


        // Graphical krapola...

/*    public JBBSConfig()

     *  All the non-graphical details of JBBSConfig are static. You only
     *   need call (new JBBSConfig();) when the configuration window should
     *   be displayed.
     
    {
        super(JBBS.TITLE + " " + JBBS.VERSION);

        out(BACKUP_CFGFILENAME);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        Font font = new Font("Serif", Font.BOLD | Font.ITALIC, 36);

        //setIconImage(image);  // !!!

        setFont(font);
        setBackground(Color.blue);
        addWindowListener(this);

        setSize(d.width / 2, d.height / 2);
        setLocation(new Point(d.width / 4, d.height / 4));
        setResizable(false);

        setVisible(true);
    } // Constructor


    public void paint(Graphics g)
    {
        Dimension d = getSize();
        Insets insets = getInsets();
        FontMetrics fm = g.getFontMetrics();
        String drawTitle = getTitle();

        d.width  -= (insets.left + insets.right + fm.stringWidth(drawTitle));
        
        g.setColor(Color.black);
        g.drawString(drawTitle, d.width / 2, fm.getHeight() + insets.top + 1);

        g.setColor(Color.white);
        g.drawString(drawTitle, (d.width / 2),
                     fm.getHeight() + insets.top);
    } // paint


        // WindowListener implementation...

    public void windowClosing(WindowEvent e)
    {
        out(DEFAULT_CFGFILENAME);
        e.getWindow().dispose();
    } // windowClosing

    public void windowClosed(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    */

} // JBBSConfig

// end of JBBSConfiguration.java ...

