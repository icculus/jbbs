/**
 *  Interface for encryption of data.
 *
 *   Copyright (c) Lighting and Sound Technologies, 1997.
 *    Written by Ryan C. Gordon.
 */

public interface Encryptable
{
    public void setEncryptKey(byte[] bytes);
    public String encrypt(String encStr);
    public byte[] encrypt(byte[] encBytes);
    public String decrypt(String decStr);
    public byte[] decrypt(byte[] decBytes);
} // Encryptable

// end of Encryptable.java ...

