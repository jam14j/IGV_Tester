/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package igv_tester;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Juan Antonio Martinez Castellanos - U375716
 */
public class IGV_Motor {
    
    
    public static boolean open()
    {
        Runtime rt = Runtime.getRuntime();
        try {
            //System.out.println("Moving");
            //rt.exec("cmd //c ticcmd --exit-safe-start --position 10000");
            //Thread.sleep(5000)
            //rt.exec("ticcmd --halt-and-hold");
            //System.out.println("Stopped");
            
            String[] cmd = {"cmd.exe","/c","ticcmd --exit-safe-start --position-relative 13000"};
            Process openIGV = Runtime.getRuntime().exec(cmd); //Opening
            Thread.sleep(41000); //Sleep for 41 seconds
            int TXflag = getFlag("TX"); //ON when OPENED
            int RXflag = getFlag("RX"); //ON when CLOSED
            System.out.printf("\n\nOPEN:\n\tTX = %d\nRX = %d\n", TXflag, RXflag);
            if(TXflag == 0)
                return false;
            else
                return true;
        }
        catch (IOException e) {System.out.println(e.getMessage()); return false;}
        catch (Exception e) {System.out.println(e.getMessage()); return false;}
    }
    
    public static boolean close()
    {
        Runtime rt = Runtime.getRuntime();
        try {
            String[] cmd = {"cmd.exe","/c","ticcmd --exit-safe-start --position-relative -13000"};
            Process openIGV = Runtime.getRuntime().exec(cmd); //Closing
            Thread.sleep(41000); //Sleep for 41 seconds
            int TXflag = getFlag("TX"); //ON when OPENED
            int RXflag = getFlag("RX"); //ON when CLOSED
            System.out.printf("\n\nCLOSE:\n\tTX = %d\nRX = %d\n", TXflag, RXflag);
            if(RXflag == 0)
                return false;
            else
                return true;
        }
        catch (IOException e) {System.out.println(e.getMessage()); return false;}
        catch (Exception e) {System.out.println(e.getMessage()); return false;}
    }
    
    public static void closeHalfWay()
    {
        Runtime rt = Runtime.getRuntime();
        try {
            String[] cmd = {"cmd.exe","/c","ticcmd --exit-safe-start --position-relative -7000"};
            Process openIGV = Runtime.getRuntime().exec(cmd); //Closing
            Thread.sleep(25000); //Sleep for 25 seconds
            int TXflag = getFlag("TX"); //ON when OPENED
            int RXflag = getFlag("RX"); //ON when CLOSED
            System.out.printf("\n\nCLOSE:\n\tTX = %d\nRX = %d\n", TXflag, RXflag);
        }
        catch (IOException e) {System.out.println(e.getMessage());}
        catch (Exception e) {System.out.println(e.getMessage());}
    }
    
    //This function parses the status command to get the TX or RX flag
    public static int getFlag(String s) //s has to be either "TX" or "RX"
    {
        try {
            String[] cmd = {"cmd.exe","/c","ticcmd --status --full"};
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String temp = "";
            String[] tokens = new String[2];
            while (temp != null) {
                System.out.println(temp);
                if(temp.contains(s + " pin:")) {
                    System.out.println(input.readLine());
                    System.out.println(input.readLine());
                    String dataLine = input.readLine();
                    System.out.println(dataLine);
                    tokens = dataLine.split("\\s+");
                }
                temp = input.readLine();
            }
            input.close();
            for(int i=0; i<tokens.length; i++)
                System.out.printf("\ntoken[%d] = %s", i, tokens[i]);
            System.out.printf("\nReturn value = %d", Character.getNumericValue(tokens[3].charAt(0)));
            return Character.getNumericValue(tokens[3].charAt(0));
        }
        catch (IOException e) {System.out.println(e.getMessage()); return -1;}
        catch (Exception e) {System.out.println(e.getMessage()); return -1;}
    }
    
    
}
