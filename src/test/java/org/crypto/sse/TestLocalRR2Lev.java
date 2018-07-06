/** * Copyright (C) 2018 Juan D. Vega. Adapted from 2016 Tarik Moataz (Clusion-Library)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

//***********************************************************************************************//
// This file is to test the 2Lev construction by Cash et al. NDSS'14. 
//**********************************************************************************************

package org.crypto.sse;

import org.apache.commons.collections.bag.SynchronizedSortedBag;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class TestLocalRR2Lev {

	private static BufferedReader reader;

	public static final String ALGORITHM = "AES";

	public static void main(String[] args) {

	    reader = new BufferedReader(new InputStreamReader(System.in));

		int option = -1;

		while (option != 0) {
			try {
				System.out.println("---------------SSE Lab - 2Lev Implementation---------------");
				System.out.println("Choose one of the following options: ");
				System.out.println("1: Test indexing and query");
				System.out.println("2: Test files encryption and query over those files");
				System.out.println("0: Exit");
				System.out.println("-----------------------------------------------------------");

				option = Integer.parseInt(reader.readLine());

				switch(option) {
					case 1: test1(); break;
					case 2: test2(); break;
				}
			}			
			catch (InputMismatchException | NumberFormatException ime ) {
			    try
                {
                    System.out.println("You did not select a valid number");
                    System.out.println("Put any letter and then press enter to continue");
                    reader.readLine();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

			}
            catch (IOException e) {
                e.printStackTrace();
            }
		}
		
        try
        {
           reader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

	}

	public static void test1() throws InputMismatchException, IOException, NumberFormatException
    {

		System.out.println("If you want to create a new index over your files in order to perform SSE over them, select 1.");
		System.out.println("Otherwise, if you want to make queries with an existing index, select 2.");

		int option = Integer.parseInt(reader.readLine());

        byte[] key = null;
        RR2Lev twolev = null;
		if(option == 1)
		{
		    key = generateKey();

            System.out.println("Enter the relative path name of the folder that contains the files to make searchable");
            String pathName = reader.readLine();
            twolev = buildIndex(key, pathName);
        }
        else if (option == 2)
        {
            System.out.println("Enter the relative path name of the file where you previously saved your index.");
            String indexPath = reader.readLine();
            twolev = (RR2Lev) readObject(indexPath);

            System.out.println("Enter the relative path name of the file that contains the secret key needed to perform your queries.");
            String keyPath = reader.readLine();
            key = (byte[]) readObject(keyPath);
        }
        else
        {
            throw new InputMismatchException();
        }

        int exit = -1;

        System.out.println();
        System.out.println("---------------SSE Lab - Query Tool ---------------\n");

        while (exit != 0)
        {
            System.out.println(":::::::::: SSE Lab - New Query ::::::::::::\n");

            List<String> ans = query(key, twolev);
            System.out.println("Final Result: " + ans);

            System.out.println(":::::::::::::::::::::::::::::::::::::::::::\n");


            System.out.println("If you want to stop querying, select 0. Otherwise, select any number.");
            exit = Integer.parseInt(reader.readLine());
        }
	}

	public static void test2( ) throws InputMismatchException, IOException, NumberFormatException
    {
        System.out.println("If you want to encrypt and index a new set of files, select 1.");
        System.out.println("Otherwise, if you want to make queries over a previously encrypted data, select 2.");

        int option = Integer.parseInt(reader.readLine());

        byte[] key = null;
        RR2Lev twolev = null;
        String pathName = "";

        if(option == 1)
        {
            key = generateKey();

            System.out.println("Enter the relative path name of the folder that contains the files that you want to encrypt and make searchable.");
            pathName = reader.readLine();
            twolev = buildIndex(key, pathName);

            File dir = new File(pathName);
            File[] filelist = dir.listFiles();
            encryptFiles(filelist, key);
        }
        else if (option == 2)
        {
            System.out.println("Enter the relative path name of the file where you previously saved your index.");
            String indexPath = reader.readLine();
            twolev = (RR2Lev) readObject(indexPath);

            System.out.println("Enter the relative path name of the file that contains the secret key needed to perform your queries.");
            String keyPath = reader.readLine();
            key = (byte[]) readObject(keyPath);

            System.out.println("Enter the relative path name of the folder with the previously encrypted files.");
            pathName = reader.readLine();
        }
        else
        {
            throw new InputMismatchException();
        }

        int exit = -1;

        System.out.println();
        System.out.println("---------------SSE Lab - Query Tool ---------------\n");

        while (exit != 0)
        {
            System.out.println(":::::::::: SSE Lab - New Query ::::::::::::\n");

            List<String> ans = query(key, twolev);
            System.out.println("Final Result: " + ans);

            int size = ans.size();
            if(size > 0)
            {
                File[] filelistAns = new File[ans.size()];

                int i = 0;

                for(String s: ans)
                {
                    filelistAns[i] = new File(pathName+"\\"+s);
                    i++;
                }

                decryptFiles(filelistAns, key);

                System.out.println("The files returned by the query have been successfully decrypted.");

                System.out.println("You can find them in the same origin folder that you previously entered.");
            }
            else
            {
                System.out.println("The entered keyword was not found in any file.");
            }


            System.out.println(":::::::::::::::::::::::::::::::::::::::::::\n");


            System.out.println("If you want to stop querying, select 0. Otherwise, select any number.");
            exit = Integer.parseInt(reader.readLine());
        }

    }

	public static byte[] generateKey( )
    {
        byte[] key = null;
        try
        {
            System.out.println("Enter a password as a seed for the key generation:");

            String pass = reader.readLine();           

            key = RR2Lev.keyGen(128, pass, "salt/salt", 100000);

            System.out.println("Enter the relative path name of the folder where you want to save the secret key");

            String pathName2 = reader.readLine();
            saveObject(pathName2+"\\key", key);

        }
        catch(Exception exp)
        {
            System.out.println("An error occurred while generating the key \n");
            System.out.println(exp.getMessage());
        }
        return key;

    }

	public static RR2Lev buildIndex(byte[] key, String pathName)
    {
        RR2Lev twolev = null;
        try
        {

            ArrayList<File> listOfFile = new ArrayList<File>();
            TextProc.listf(pathName, listOfFile);

            TextProc.TextProc(false, pathName);

            // The two parameters depend on the size of the dataset. Change
            // accordingly to have better search performance
            int bigBlock = 1000;
            int smallBlock = 100;

            int dataSize = 10000;

            // Construction of the global multi-map
            System.out.println("\n Beginning of Encrypted Multi-map creation \n");
            System.out.println("Number of keywords "+TextExtractPar.lp1.keySet().size());
            System.out.println("Number of pairs "+	TextExtractPar.lp1.keys().size());
            //start
            long startTime = System.nanoTime();
            twolev = RR2Lev.constructEMMParGMM(key, TextExtractPar.lp1, bigBlock, smallBlock, dataSize);
            //end
            long endTime = System.nanoTime();

            //time elapsed
            long output = endTime - startTime;
            System.out.println("Elapsed time in seconds: " + output / 1000000000);

            System.out.println("Enter the relative path name of the folder where you want to save the Index");

            String pathName2 = reader.readLine();
            saveObject(pathName2+"\\index", twolev);

        }
        catch(Exception exp)
        {
            System.out.println("An error occurred while generating the index \n");
            System.out.println(exp.getMessage());
        }
        return twolev;
    }

	public static List<String> query(byte[] key, RR2Lev twolev)
    {
        List<String> ans = null;
        try
        {
            System.out.println("Enter the keyword to search for:");
            String keyword = reader.readLine();
            byte[][] token = RR2Lev.token(key, keyword);

            ans = twolev.query(token, twolev.getDictionary(), twolev.getArray());

        }
        catch(Exception exp)
        {
            System.out.println("An error occurred while performing your query \n");
            System.out.println(exp.getMessage());
        }
        return ans;

    }

    public static void encryptFiles(File[] filelist, byte[] keyBytes)
    {
        System.out.println();
        System.out.println();
        System.out.println("Beginning of files encryption \n");

        try
        {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            Arrays.asList(filelist).forEach(file -> {
                try
                {
                    encryptFile(file, cipher, keyBytes, ALGORITHM);
                }
                catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
                        | IOException e) {
                    System.err.println("Couldn't encrypt " + file.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            });
            System.out.println();
            System.out.println("Files encrypted successfully!");
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e)
        {
            System.err.println(e.getMessage());
        }
    }

    public static void decryptFiles(File[] filelist, byte[] keyBytes)
    {
        try
        {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            Arrays.asList(filelist).forEach(file -> {
                try {
                    decryptFile(file, cipher, keyBytes, ALGORITHM);
                } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
                        | IOException e) {
                    System.err.println("Couldn't decrypt " + file.getName() + ": " + e.getMessage());
                }
            });
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e)
        {
            System.err.println(e.getMessage());
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
        System.out.println("Encrypting file: " + f.getName());
        SecretKeySpec key = new SecretKeySpec(keyBytes, 0, keyBytes.length, algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        writeToFile(f, cipher);
    }

    public static void decryptFile(File f, Cipher cipher, byte[] keyBytes, String algorithm)
            throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException
    {
        System.out.println("Decrypting file: " + f.getName());
        SecretKeySpec key = new SecretKeySpec(keyBytes, 0, keyBytes.length, algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        writeToFile(f, cipher);
    }

    public static void writeToFile(File f, Cipher cipher) throws IOException, IllegalBlockSizeException, BadPaddingException {
        FileInputStream in = new FileInputStream(f);
        byte[] input = new byte[(int) f.length()];
        in.read(input);

        FileOutputStream out = new FileOutputStream(f);
        byte[] output = cipher.doFinal(input);
        out.write(output);

        out.flush();
        out.close();
        in.close();
    }

}
