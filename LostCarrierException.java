/**
 *  Exception to note that user has lost connection to the
 *   BBS server for whatever reason.
 *
 *  !!! need to map out logic for how this exception is used.
 *
 *   Copyright (c) Lighting and Sound Technologies, 1997.
 *    Written by Ryan C. Gordon.
 */

public class LostCarrierException extends Exception
{
    protected String why = null;

    public LostCarrierException(String errMsg, String details)
    {
        super(errMsg);
        why = details;
    } // Constructor

    public String getDetails()
    {
        return(why);
    } // getDetails

} // LostCarrierException

// end of LostCarrierException.java ...

