/**
 *  A class to encapulize the BBS user. Static methods are also
 *   provided for access to the JBBS user database here.
 *
 *    Copyright (c) Lighting and Sound Technologies, 1997.
 *     Written by Ryan C. Gordon.
 */

import java.io.*;

public class JBBSUser
{
        // Constants...
    public final static String FILENAME_USERDB = "users";
    public final static int MAX_USERNAME = 20;
    public final static int MAX_REALNAME = 40;
    public final static int MAX_PASSWORD = 15;
    public final static int LEN_RECORD   = 75;

        // Static class variables...
    protected static RandomAccessFile rndDB = null;
    protected static int dbMajorVer = 0;
    protected static int dbMinorVer = 0;

        // Instance variables...
    protected int    number;    //                  not stored in file.
    protected String handle;    //                  max 20 bytes.
    protected String realName;  //                  max 40 bytes.
    protected String password;  //                  max 15 bytes.
                                //                  -------------
                                // LEN_RECORD should be 75.   

    public static boolean checkVersion(int majorVer, int minorVer)
    {
        boolean retVal = false;

        try    // Make sure this version of the database matches JBBS...
        {
            rndDB.seek(0);
            dbMajorVer = (int) rndDB.readByte();
            dbMinorVer = (int) rndDB.readByte();

            if ((dbMajorVer == majorVer) &&
                (dbMinorVer == minorVer))
            {
                retVal = true;
            } // if
        } // try

        catch (Exception e)
        {
            // don't care; retVal is already false.
        } // catch (IOException)

        return(retVal);
    } // checkVersion


    protected static boolean openUserDB()
    /**
     *  Attempts to open the User database.
     *
     *    params : void.
     *   returns : RandomAccessFile, ready for accessing on success,
     *             null on failure.
     */
    {
        boolean retVal = true;

        if (rndDB != null)         // file already open?
            return(false);         //   ...if so, just fail.

        try
        {
            rndDB = new RandomAccessFile(JBBSConfig.dataDir + FILENAME_USERDB,
                                         "rw");
        } // try
        catch (IOException e)
        {
            //if (JBBSConfig.logUnexpectedErrors)
            //    JBBSLog.add(!!! "openUserDB()");

            retVal = false;
        } // catch

        return(retVal);
    } // openUserDB


//  !!! doesn't compile. Need to handle IOException... !!!
//    public static synchronized long getTotalUsers()
//    /**
//     *  Calculate total number of users registered with the system.
//     *
//     *     params : void.
//     *    returns : count of users in system, -1 on error.
//     */
//    {
//        return((rndDB.length() - 2) / LEN_RECORD);
//    } // getTotalUsers


    protected static synchronized boolean dumpToDB(byte[] dump, int recNum)
    /**
     *  This dumps the actual bytes of a user record to the user
     *   database, so we spend as little time in a synchronized
     *   method as possible. All setup for this function may be
     *   done asynchronous to other threads, thus making a more
     *   efficient system.
     *
     *     params : dump   == array of LEN_RECORD bytes, to be written
     *                        AS IS to user database.
     *              recNum == position in database to write to. 
     *                        ((recNum * LEN_RECORD) + 2) positions us
     *                        on this particular record's start position.
     *    returns : boolean TRUE on successful write. boolean FALSE
     *              otherwise.
     */
    {
        boolean retVal;

        if (dump.length != LEN_RECORD)     // array correct record size?
            return(false);                 //  ...if not, bail.

        try
        {
            rndDB.seek((recNum * LEN_RECORD) + 2);   // seek to record #...
            rndDB.write(dump);                       //  ...and write it.
            retVal = true;                           // it's all good.
        } // try
        catch (IOException e)
        {
            retVal = false;            // write error...set bad retVal.
        } // catch

        return(retVal);
    } // dumpToDB


    public static boolean update(JBBSUser user)
    /**
     *  This method writes a user's record into the database.
     *   Not only does this update existing records, but can be  
     *   used to add, copy, and otherwise modify the database,
     *   since the record written to is base on the user.number
     *   field.
     *
     *       params : user == Record to write to database.
     *      returns : boolean TRUE if record written, false otherwise.
     */
   {
       int i;
       byte[] outputter = new byte[LEN_RECORD];
       byte[] rc;

           // outputter will be a memory image of what the user database
           //  record will look like on disk.

       for (i = 0; i < LEN_RECORD; i++)     // initialize array.
           outputter[i] = ' ';

       rc = user.handle.getBytes();
       System.arraycopy(rc, 0, outputter, 0, rc.length);
       rc = user.realName.getBytes();
       System.arraycopy(rc, 0, outputter, MAX_USERNAME + 1, rc.length);
       rc = user.password.getBytes();
       System.arraycopy(rc, 0, outputter,
                        MAX_USERNAME + MAX_REALNAME + 1,
                        rc.length);

       return(dumpToDB(outputter, user.number));  // write and return.
    } // update


    public static boolean compareArrays(byte[] b1, byte[] b2)
    /**
     *  Compare two arrays, and determine if their contents match.
     *   (this only compares first b2.length bytes, and fails immediately
     *   if b2.length > b1.length...hey, it suits -MY- purposes. :)  )
     *
     *     params : b1, b2 == arrays to compare.
     *    returns : boolean TRUE on match, boolean false on nonmatch.
     */
    {
        int i;

        if (b1.length < b2.length)     // b2 larger?
            return(false);             //  ...then fail compare immediately.

        for (i = 0; i < b2.length; i++)
        {
            if (b1[i] != b2[i])        // fail when two bytes don't match.
                return(false);
        } // for

        return(true);          // if the code makes it here, arrays match.
    } // compareArrays


    public static synchronized JBBSUser retrieve(byte[] userName,
                                                 String pWord)
    /**
     *  Create a JBBSUser instance from a record stored in the database.
     *
     *     params : userName == name to find in database.
     *              pWord    == Password for account userName.
     *    returns : new JBBSUser instance on success, null on failure.
     */
    {
        long fileLen;
        byte[] readIn = new byte[LEN_RECORD];
        JBBSUser retVal = null;
        int i;

        try
        {
            fileLen = rndDB.length();
            for (i = 2; i < fileLen; i += LEN_RECORD)
            {
                rndDB.seek(i);
                rndDB.read(readIn, 0, MAX_USERNAME);

                if (compareArrays(readIn, userName))
                {
                    retVal = new JBBSUser();    // yeah? read it in.

                    retVal.number = ((i - 2) / LEN_RECORD);

                    retVal.handle = new String(readIn, 0, MAX_USERNAME);
                    retVal.handle = retVal.handle.trim();

                    rndDB.read(readIn, 0, MAX_REALNAME);
                    retVal.realName = new String(readIn, 0, MAX_REALNAME);
                    retVal.realName = retVal.realName.trim();

                    rndDB.read(readIn, 0, MAX_PASSWORD);
                    retVal.password = new String(readIn, 0, MAX_PASSWORD);
                    retVal.password = retVal.password.trim();

                    if (retVal.password.equalsIgnoreCase(pWord) == false)
                       retVal = null;

                    return(retVal);   // and send it down the river...
                } // if
            } // for
        } // try
        catch (IOException e)
        {
            return(null);      // on error, just return a failure.
        } // catch

        return(null);  // username not found if code drops to here...
    } // retrieve


        // Instance variable gateways...

    public String getHandle()
    {
        return(handle);
    } // getHandle


    public void setHandle(String newStr)
    {
        if (newStr.length() > MAX_USERNAME)   // need to truncate string?
            handle = newStr.substring(0, MAX_USERNAME - 1);
        else
            handle = newStr;
    } // setHandle


    public String getRealName()
    {
        return(realName);
    } // getRealName


    public void setRealName(String newStr)
    {
        if (newStr.length() > MAX_REALNAME)   // need to truncate string?
            realName = newStr.substring(0, MAX_REALNAME - 1);
        else
            realName = newStr;
    } // setRealName


    public String getPassword()
    {
        return(password);
    } // getPassword


    public void setPassword(String newStr)
    {
        if (newStr.length() > MAX_PASSWORD)    // need to truncate string?
            password = newStr.substring(0, MAX_PASSWORD - 1);
        else
            password = newStr;
    } // setPassword


    public int getNumber()
    {
        return(number);
    } // getNumber


    public void setNumber(int newNum)
    /**
     *  For security reasons, this method does nothing if newNum <= 0.
     *   this prevents confusion from negative values, and prevents
     *   direct meddling with the JBBS SysOp (superuser, root, et al.)
     *   account, which is user #0.
     */
    {
        if (newNum > 0)       // Headache prevention.
            number = newNum;
    } // setNumber

} // JBBSUser

// end of JBBSUser.java ...


