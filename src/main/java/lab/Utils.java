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

    public static final String ALGORITHM = "AES";

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
                throw new Exception("The name of the file --> " + name + " <-- has whitespaces. Please rename the file by removing them and try again. \n");
            }
        }
    }

    // ***********************************************************************************************//

    ///////////////////// Save Serializable Objects /////////////////////////////

    // ***********************************************************************************************//
    public static void saveObject(String filePathString, Serializable object) throws Exception
    {
        FileOutputStream f = new FileOutputStream(new File(filePathString));
        ObjectOutputStream o = new ObjectOutputStream(f);
        o.writeObject(object);
        o.close();
        f.close();
    }

    public static Serializable readObject(String filePathString) throws FileNotFoundException, IOException, ClassNotFoundException {
        Serializable ans = null;
        FileInputStream fi = new FileInputStream(new File(filePathString));
        ObjectInputStream oi = new ObjectInputStream(fi);

        // Read objects
        ans = (Serializable) oi.readObject();

        oi.close();
        fi.close();

        return ans;

    }

    // ***********************************************************************************************//

    ///////////////////// Encrypt/decrypt files /////////////////////////////

    // ***********************************************************************************************//

    public static void encryptFiles(File[] filelist, byte[] keyBytes)
    {
        System.out.println("\n \n Beginning of files encryption \n");

        try
        {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            Arrays.asList(filelist).forEach(FILE -> {
                try
                {
                    encryptFile(FILE, cipher, keyBytes, ALGORITHM);
                }
                catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
                        | IOException e) {
                    System.err.println("Couldn't encrypt " + FILE.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            });
            System.out.println("\n Files encrypted successfully!");
            System.out.println("You can find this encrypted versions in the same folder instead of the original files.");

        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e)
        {
            System.err.println(e.getMessage());
        }
    }

    public static void decryptFiles(File[] filelist, byte[] keyBytes, String pathFolderDestiny)
    {
        int files = filelist.length;
        File[] destinyFiles = new File[files];

        try
        {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            for(int i = 0; i < files; i++)
            {
                destinyFiles[i] = new File(pathFolderDestiny + File.separator + "decrypted_" + filelist[i].getName());
                try {
                    decryptFile(filelist[i], cipher, keyBytes, ALGORITHM, destinyFiles[i]);
                } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
                        | IOException e) {
                    System.err.println("Couldn't decrypt " + filelist[i].getName() + ": " + e.getMessage());
                }
            }
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e)
        {
            System.err.println(e.getMessage());
        }
    }


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
