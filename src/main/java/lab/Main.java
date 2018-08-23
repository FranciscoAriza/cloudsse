package lab;

import lab.TestDynRH;
import lab.TestRR2Lev;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.InputMismatchException;

public class Main
{
    private static BufferedReader reader;

    public static void main(String[] args) {

        reader = new BufferedReader(new InputStreamReader(System.in));

        int option = -1;

        while (option != 0) {
            try {
                System.out.println("---------------------- >> SSE Lab << ----------------------");
                System.out.println("Choose one of the following options: ");
                System.out.println("1: Test the 2Lev implementation (Static scheme)");
                System.out.println("2: Test the DynRH implementation (Dynamic scheme)");
                System.out.println("0: Exit");
                System.out.println("-----------------------------------------------------------");

                option = Integer.parseInt(reader.readLine());

                switch(option) {
                    case 1: TestRR2Lev.menu(); break;
                    case 2: TestDynRH.menu(); break;
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
}
