/**
 *  Dummy class to implement Encryptable...does no encryption whatsoever.
 *
 *    Copyright (c) Lighting and Sound Technologies, 1997.
 *     Written by Ryan C. Gordon.
 */

public class NoEncryption implements Encryptable
{
    public void setEncryptKey(byte[] bytes) {}  // do nothing.

    public String encrypt(String encStr)
    {
        return(encStr);
    } // encrypt (takes String)

    public byte[] encrypt(byte[] encBytes)
    {
        return(encBytes);
    } // encrypt (takes byte[])

    public String decrypt(String decStr)
    {
        return(decStr);
    } // decrypt (takes String)

    public byte[] decrypt(byte[] decBytes)
    {
        return(decBytes);
    } // decrypt (takes byte[])

} // NoEncryption

// end of NoEncryption.java ...

