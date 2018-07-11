package lab;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.TreeMultimap;
import org.crypto.sse.DynRH;
import org.crypto.sse.TextExtractPar;
import org.crypto.sse.TextProc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;

public class TestDynRH {
    private static BufferedReader reader;

    public static void menu( ) {

        reader = new BufferedReader(new InputStreamReader(System.in));

        int option = -1;

        while (option != 0) {
            try {
                System.out.println("---------------SSE Lab - DynnRH Implementation---------------");
                System.out.println("Choose one of the following options: ");
                System.out.println("1: Test indexing and query");
                System.out.println("2: Test files encryption and query over those files");
                System.out.println("0: Exit");
                System.out.println("-----------------------------------------------------------");

                option = Integer.parseInt(reader.readLine());

                switch(option) {
                    case 1: test1(); break;
                   // case 2: test2(); break;
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
    }

    public static void test1( ) throws InputMismatchException, IOException, NumberFormatException
    {
        System.out.println("If you want to create a new index over your files in order to perform SSE over them, select 1.");
        System.out.println("Otherwise, if you want to work with an existing index, select 2.");

        int option = Integer.parseInt(reader.readLine());

        byte[] key = null;
        HashMap<String, byte[]> index = null;
        if(option == 1)
        {
            key = generateKey();

            System.out.println("Enter the relative path name of the FOLDER that contains the files to make searchable");
            String pathName = reader.readLine();
            index = buildIndex(key, pathName);

            if(key == null || index == null) return;
        }
        else if (option == 2)
        {
            System.out.println("Enter the relative path name of the FILE where you previously saved your index.");
            String indexPath = reader.readLine();
            index = (HashMap<String, byte[]>) Utils.readObject(indexPath);

            System.out.println("Enter the relative path name of the FILE that contains the secret key needed to perform your queries.");
            String keyPath = reader.readLine();
            key = (byte[]) Utils.readObject(keyPath);
        }
        else
        {
            throw new InputMismatchException();
        }

        int test1Option = -1;

        while (test1Option != 0)
        {
            System.out.println("-----------------------------------------------------------");
            System.out.println("Choose one of the following options: ");
            System.out.println("1: Add new files to your index");
            System.out.println("2: Delete files from your index");
            System.out.println("3: Perform a query over your files using SSE");
            System.out.println("0: Exit");
            System.out.println("-----------------------------------------------------------");

            test1Option = Integer.parseInt(reader.readLine());

            switch(test1Option) {
                case 1: updateIndex(key, index); break;
                case 2:
                    System.out.println("Enter a keyword that appears in the file that you want to delete:");
                    String keywordDelete = reader.readLine();
                    List<String> ans = query(key,index,keywordDelete);
                    if(ans.size() > 0) deleteElement(keywordDelete, index, key);
                    else System.out.println("The entered keyword was not found in any file.");

                    break;
                case 3:
                    System.out.println("Enter the keyword to search for:");
                    String keyword = reader.readLine();
                    query(key, index, keyword);
                    break;
            }
        }

    }


    public static byte[] generateKey() {
        byte[] key = null;
        try {
            System.out.println("Enter a password as a seed for the key generation:");

            String pass = reader.readLine();

            key = DynRH.keyGen(128, pass, "salt/salt", 100000);

            System.out.println("Enter the relative path name of the FOLDER where you want to save the secret key");

            String pathName2 = reader.readLine();
            Utils.saveObject(pathName2 + "\\key", key);

        } catch (Exception exp) {
            System.out.println();
            System.out.println("An error occurred while generating the key \n");
            System.out.println(exp.getMessage());
        }
        return key;
    }

    public static HashMap<String, byte[]> buildIndex(byte[] key, String pathName)
    {
        HashMap<String, byte[]> emm = null;
        try
        {
            ArrayList<File> listOfFile = new ArrayList<File>();
            TextProc.listf(pathName, listOfFile);

            TextProc.TextProc(false, pathName);

            // Construction of the encrypted multi-map
            // The setup will be performed as multiple update operations
            System.out.println("\n Beginning of Encrypted Multi-map creation \n");
            emm = DynRH.setup();

            // start
            long startTime = System.nanoTime();

            // Generate the updates
            // This operation will generate update tokens for the entire data set
            TreeMultimap<String, byte[]> tokenUp = DynRH.tokenUpdate(key, TextExtractPar.lp1);

            // Update the encrypted Multi-map
            DynRH.update(emm, tokenUp);

            // end
            long endTime = System.nanoTime();

            // time elapsed
            long output = endTime - startTime;

            System.out.println("\nElapsed time in seconds: " + output / 1000000000);
            System.out.println("Number of keywords " + TextExtractPar.lp1.keySet().size());
            System.out.println("Number of pairs " + TextExtractPar.lp1.keys().size());

            // Empty the previous multimap
            // to avoid adding the same set of documents for every update

            TextExtractPar.lp1 = ArrayListMultimap.create();

        }
        catch (Exception e)
        {
            System.out.println( );
            System.out.println("An error occurred while generating the index \n");
            System.out.println(e.getMessage());
        }

        return emm;
    }

    public static void updateIndex(byte[] key, HashMap<String, byte[]> emm)
    {
        try
        {
            System.out.println("Enter the relative path name of the folder that contains the files to add:");
            String pathName = reader.readLine();

            ArrayList<File> listOfFile = new ArrayList<File>();
            TextProc.listf(pathName, listOfFile);
            TextProc.TextProc(false, pathName);

            System.out.println("\n Beginning of Encrypted Multi-map update \n");

            long startTime = System.nanoTime();
            // This operation is similar to the one performed in the generateIndex() method
            TreeMultimap<String, byte[]> tokenUp = DynRH.tokenUpdate(key, TextExtractPar.lp1);
            DynRH.update(emm, tokenUp);

            long endTime = System.nanoTime();

            long output = endTime - startTime;

            System.out.println("\nElapsed time in seconds: " + output / 1000000000);
            System.out.println("Number of keywords " + TextExtractPar.lp1.keySet().size());
            System.out.println("Number of pairs " + TextExtractPar.lp1.keys().size());
        }
        catch (Exception e)
        {
            System.out.println( );
            System.out.println("An error occurred while updating the index \n");
            System.out.println(e.getMessage());
        }

    }

    public static List<String> query(byte[] key, HashMap<String, byte[]> emm, String keyword)
    {
        List<String> ans = new ArrayList<>( );
        try
        {
            byte[][] token = DynRH.genTokenFS(key, keyword);
            // start
            long startTime = System.nanoTime();
            ans = DynRH.resolve(key, DynRH.queryFS(token, emm));
            System.out.println(ans);
            // end
            long endTime = System.nanoTime();

            // time elapsed
            long output = endTime - startTime;

            System.out.println("\nElapsed time in microseconds: " + output / 1000);

        }
        catch(Exception exp)
        {
            System.out.println("An error occurred while performing your query \n");
            System.out.println(exp.getMessage());
        }
        return ans;
    }

    public static void deleteElement(String keyword, HashMap<String, byte[]> emm, byte[] key)
    {
        try
        {
            System.out.println("\n Enter index of the element that you want to delete");
            System.out.println("-This index is the position of the file that you want to delete within the array that you just saw (starting from 0)-");
            String index = reader.readLine();
            List<Integer> deletions = new ArrayList<Integer>();
            deletions.add(Integer.parseInt(index));
            byte[][] delToken = DynRH.delTokenFS(key, keyword, deletions);
            DynRH.deleteFS(delToken, emm);

            System.out.println("\n The selected file has been succesfully deleted from your index!");
        }
        catch(Exception exp)
        {
            System.out.println("An error occurred while deleting the selected element.\n");
            System.out.println(exp.getMessage());
        }
    }
}
