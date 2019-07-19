/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package igv_tester;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectInputStream;

/**
 *
 * @author Juan Antonio MartiU375716
 */
class dcToolHandler {
    
    //The dcTool opens a ServerSocket,
    //so this program is supposed to open a client socket instead. 
    //private static ServerSocket mySocket;
    private static Socket clientSocket;
    private static InputStream is;
    
    //When the dcToolHandler is instantiated the socket is opened
    //The input stream from the socket is also fetched
    public dcToolHandler() throws Exception {
        System.out.println("1 about to connect");
        clientSocket = new Socket("192.168.4.4", 1069);
        System.out.println("2 Connected! to: " + clientSocket.getInetAddress().getHostName());
        //this input stream reads bytes
        is = clientSocket.getInputStream();
        System.out.println("3 Obtained InputStream");
    }
    
    //This method checks that there have been 4 correct fastenings
    //and returns the serial number, so that it can be matched with a color in main
    public static String getData() throws Exception, IOException {
        int data = 0;   //null character
        int counter = 0;
        String barcode = "";
        System.out.println("Waiting for IR tool");
        while(counter < 4) {        //count 4 successful fastenings 
            data=is.read();
            //System.out.println("counter<4 loop");
            while((data=is.read())!=44) { //skip the peak torque value, find the comma
                //System.out.println("data="+((char)data));
                continue;
            }
            data = is.read();
            //System.out.println("data OUT OF LOOP="+((char)data));
            if(data == 80)     //if the second value is P, count++
                counter++;
            data=is.read(); //get the comma
            while((data=is.read())!=44) { //skip the torque units
                //System.out.println("data="+((char)data));
                continue;
            }
            String temp = "";
            //System.out.println("BEFORE WHILE LOOP");
            while(!temp.equals("290005-1") && !temp.equals("255006") && !temp.equals("200144") && !temp.equals("200232-1")) {
                //System.out.println("IN WHILE LOOP");
                temp += (char) is.read();
                temp = temp.trim();
                //System.out.println("temp in loop="+temp);
            }
            //System.out.println("AFTER WHILE LOOP");
            //System.out.println("temp = "+temp);
            if(counter == 1)
                barcode += temp;
            else if (!barcode.matches(temp))
                throw new Exception("Barcodes don't match between runs. "+barcode+"!="+temp);
            //System.out.println("Barcode"+counter+" = "+barcode);
        }
        return barcode;
    }
}
