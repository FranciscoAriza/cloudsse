package lab;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class Utils
{

    // ***********************************************************************************************//

    ///////////////////// Verify file names in order to detect the presence of whitespaces /////////////

    // ***********************************************************************************************//

    public static void renameFiles(ArrayList<File> filelist) throws Exception
    {
        String name = "";
        for(File f: filelist)
        {
            name = f.getName();
            if(name.contains(" "))
            {
                throw new Exception("The name of the FILE --> " + name + " <-- has whitespaces. Please rename the FILE by removing them and try again.");
            }
        }
    }

    // ***********************************************************************************************//

    ///////////////////// Save Serializable Objects /////////////////////////////

    // ***********************************************************************************************//
    public static void saveObject(String filePathString, Serializable object)
    {
        try
        {
            FileOutputStream f = new FileOutputStream(new File(filePathString));
            ObjectOutputStream o = new ObjectOutputStream(f);
            o.writeObject(object);
            o.close();
            f.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public static Serializable readObject(String filePathString)
    {
        Serializable ans = null;
        try
        {
            FileInputStream fi = new FileInputStream(new File(filePathString));
            ObjectInputStream oi = new ObjectInputStream(fi);

            // Read objects
            ans = (Serializable) oi.readObject();

            oi.close();
            fi.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        return ans;

    }

    // ***********************************************************************************************//

    ///////////////////// Encrypt/decrypt files /////////////////////////////

    // ***********************************************************************************************//

    public static void encryptFile(File f, Cipher cipher, byte[] keyBytes, String algorithm)
            throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException
    {
        System.out.println("Encrypting FILE: " + f.getName());
        SecretKeySpec key = new SecretKeySpec(keyBytes, 0, keyBytes.length, algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        writeToFile(f, cipher, f);
    }

    public static void decryptFile(File origin, Cipher cipher, byte[] keyBytes, String algorithm, File destiny)
            throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException
    {
        System.out.println("Decrypting FILE: " + origin.getName());
        SecretKeySpec key = new SecretKeySpec(keyBytes, 0, keyBytes.length, algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        writeToFile(origin, cipher, destiny);
    }

    public static void writeToFile(File origin, Cipher cipher, File destiny) throws IOException, IllegalBlockSizeException, BadPaddingException {
        FileInputStream in = new FileInputStream(origin);
        byte[] input = new byte[(int) origin.length()];
        in.read(input);

        FileOutputStream out = new FileOutputStream(destiny);
        byte[] output = cipher.doFinal(input);
        out.write(output);

        out.flush();
        out.close();
        in.close();
    }

}
