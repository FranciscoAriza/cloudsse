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
// This FILE is to test the 2Lev construction by Cash et al. NDSS'14. 
//**********************************************************************************************

package lab;

import com.google.common.collect.ArrayListMultimap;
import org.crypto.sse.RR2Lev;
import org.crypto.sse.TextExtractPar;
import org.crypto.sse.TextProc;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class TestRR2Lev {

	private static BufferedReader reader;

	public static void menu( ) {

	    reader = new BufferedReader(new InputStreamReader(System.in));

		int option = -1;

		while (option != 0) {
			try {
				System.out.println("--------------- >> SSE Lab - 2Lev (Static Implementation) << ---------------");
				System.out.println("Choose one of the following options: ");
				System.out.println("1: Test indexing and query");
				System.out.println("2: Test files encryption and query over those files");
				System.out.println("0: Return");
				System.out.println("----------------------------------------------------------------------------");

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
            catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
		}
	}

	public static void test1() throws InputMismatchException, IOException, NumberFormatException, ClassNotFoundException
    {

        System.out.println("------------------------------------------------------------------------");
        System.out.println("--------------> Choose one of the following options: <------------------");
		System.out.println("1. Create a new index over your files in order to perform SSE over them.");
		System.out.println("2. Make queries with an existing index.");
        System.out.println("------------------------------------------------------------------------");

		int option = Integer.parseInt(reader.readLine());

        byte[] key = null;
        RR2Lev twolev = null;
		if(option == 1)
		{
		    key = generateKey("keyRR2Lev_v1");

		    if(key == null) return;


            System.out.println("Enter the absolute path name of the FOLDER that contains the files to make searchable");
            String pathName = reader.readLine();
            twolev = buildIndex(key, pathName, "indexRR2Lev_v1");

            if(twolev == null) return;
        }
        else if (option == 2)
        {
            System.out.println("Enter the absolute path name of the FILE where you previously saved your index.");
            String indexPath = reader.readLine();
            twolev = (RR2Lev) Utils.readObject(indexPath);

            System.out.println("Enter the absolute path name of the FILE that contains the secret key needed to perform your queries.");
            String keyPath = reader.readLine();
            key = (byte[]) Utils.readObject(keyPath);
        }
        else
        {
            throw new InputMismatchException();
        }

        System.out.println();
        System.out.println("--------------- >> SSE Lab - Query Tool << ---------------\n");

        while (true)
        {
            System.out.println(":::::::::: SSE Lab - New Query ::::::::::::\n");

            System.out.println("Enter the keyword to search for (only use lowercase letters):");
            String keyword = reader.readLine();

            if(keyword.equals(".")) break;

            List<String> ans = query(key, twolev, keyword);
            System.out.println("Final Result: " + ans);

            System.out.println(":::::::::::::::::::::::::::::::::::::::::::\n");


            System.out.println(" ----> If you want to STOP querying, enter . (one dot) instead of your keyword \n");

        }
	}

	public static void test2( ) throws InputMismatchException, IOException, NumberFormatException, ClassNotFoundException
    {
        System.out.println("------------------------------------------------------------------------");
        System.out.println("--------------> Choose one of the following options: <------------------");
        System.out.println("1. Encrypt and index a new set of files.");
        System.out.println("2. Make queries over a previously encrypted data.");
        System.out.println("------------------------------------------------------------------------");


        int option = Integer.parseInt(reader.readLine());

        byte[] key = null;
        RR2Lev twolev = null;
        String pathName = "";

        if(option == 1)
        {
            key = generateKey("keyRR2Lev_v2");

            if(key == null) return;

            System.out.println("Enter the absolute path name of the FOLDER that contains the files that you want to encrypt and make searchable.");
            pathName = reader.readLine();
            twolev = buildIndex(key, pathName, "indexRR2Lev_v2");

            if(twolev == null) return;

            File dir = new File(pathName);
            File[] filelist = dir.listFiles();
            Utils.encryptFiles(filelist, key);
        }
        else if (option == 2)
        {
            System.out.println("Enter the absolute path name of the FILE where you previously saved your index.");
            String indexPath = reader.readLine();
            twolev = (RR2Lev) Utils.readObject(indexPath);

            System.out.println("Enter the absolute path name of the FILE that contains the secret key needed to perform your queries.");
            String keyPath = reader.readLine();
            key = (byte[]) Utils.readObject(keyPath);

            System.out.println("Enter the absolute path name of the FOLDER with the previously encrypted files.");
            pathName = reader.readLine();
        }
        else
        {
            throw new InputMismatchException();
        }

        System.out.println("\n --------------- >> SSE Lab - Query Tool << ---------------\n");

        while (true)
        {
            System.out.println(":::::::::: SSE Lab - New Query ::::::::::::\n");

            System.out.println("Enter the keyword to search for (only use lowercase letters):");
            String keyword = reader.readLine();

            if(keyword.equals(".")) break;

            List<String> ans = query(key, twolev, keyword);
            System.out.println("Final Result: " + ans);

            int size = ans.size();
            if(size > 0)
            {
                System.out.println("------------------------------------------------------------------------");
                System.out.println("-----> Do you want to decrypt the files returned by your query? <-------");
                System.out.println("1. Yes");
                System.out.println("2. No");
                System.out.println("------------------------------------------------------------------------");


                try
                {
                    int decrypt = Integer.parseInt(reader.readLine());

                    if(decrypt == 1)
                    {
                        System.out.println("Enter the absolute path name of the FOLDER where you want to save the decrypted files.");
                        String pathNameDestiny = reader.readLine();

                        File[] filelistAns = new File[ans.size()];
                        int i = 0;

                        for(String s: ans)
                        {
                            filelistAns[i] = new File(pathName + File.separator + s);
                            i++;
                        }
                        Utils.decryptFiles(filelistAns, key, pathNameDestiny);

                        System.out.println("The files returned by the query have been successfully decrypted.");
                    }
                }
                catch(InputMismatchException | NumberFormatException e)
                {
                    System.out.println("\n You did not select a valid number");
                    System.out.println("Try again.\n");
                }

            }
            else
            {
                System.out.println("The entered keyword was not found in any file.");
            }


            System.out.println(":::::::::::::::::::::::::::::::::::::::::::\n");


            System.out.println(" ----> If you want to STOP querying, enter . (one dot) instead of your keyword \n");

        }

    }

	public static byte[] generateKey(String name)
    {
        byte[] key = null;
        try
        {
            System.out.println("Enter a password as a seed for the key generation:");

            String pass = reader.readLine();           

            key = RR2Lev.keyGen(128, pass, "salt/salt", 100000);

            System.out.println("Enter the absolute path name of the FOLDER where you want to save the secret key");

            String pathName2 = reader.readLine();
            Utils.saveObject(pathName2+ File.separator + name, key);

        }
        catch(Exception exp)
        {
            key = null;
            System.out.println("An error occurred while generating the key \n");
            System.out.println(exp.getMessage());
            exp.printStackTrace();
            System.out.println();
        }
        return key;

    }

	public static RR2Lev buildIndex(byte[] key, String pathName, String name)
    {
        RR2Lev twolev = null;
        try
        {
            ArrayList<File> listOfFile = new ArrayList<File>();
            TextProc.listf(pathName, listOfFile);

            //Verifies the presence of whithespaces in the filenames in order to avoid future format problems.
            Utils.renameFiles(listOfFile);

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

            System.out.println("Enter the absolute path name of the FOLDER where you want to save the Index");

            String pathName2 = reader.readLine();
            Utils.saveObject(pathName2+ File.separator + name, twolev);

            // Empty the previous multimap
            // to avoid adding the same set of documents for every execution
            TextExtractPar.lp1 = ArrayListMultimap.create();
        }
        catch(Exception exp)
        {
            twolev = null;
            System.out.println( );
            System.out.println("An error occurred while generating the index \n");
            System.out.println(exp.getMessage());
            exp.printStackTrace();
            System.out.println( );
        }
        return twolev;
    }

	public static List<String> query(byte[] key, RR2Lev twolev, String keyword)
    {
        List<String> ans = null;
        try
        {

            byte[][] token = RR2Lev.token(key, keyword);

            ans = twolev.query(token, twolev.getDictionary(), twolev.getArray());

        }
        catch(Exception exp)
        {
            System.out.println("An error occurred while performing your query \n");
            System.out.println(exp.getMessage());
            exp.printStackTrace();
            System.out.println( );
        }
        return ans;

    }


}
