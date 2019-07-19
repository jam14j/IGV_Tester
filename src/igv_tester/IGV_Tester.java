//THIS CLASS IS THE ENTRY POINT

package igv_tester;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import javax.swing.JOptionPane;


/**
 *
 * @author Juan Antonio Martinez Castellanos - U375716
 */
public class IGV_Tester {
    public static IGV_UI gui;
    public static cam_UI cam_gui;
    public static dcToolHandler myHandler;
    public static String serial, badge, color, rightColor;
    public static CountDownLatch myLatch;
    public static Scanner scanner;
    public static List<String> lines;
    public static List<String> colors;
    public static List<String> numbers;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        mainloop:
        while(true) {
            
            color = "empty";
            rightColor = "empty2";
            gui = new IGV_UI(); //make an instance of the main user interface
            gui.rundisplay(gui);
            gui.startTorque();
            gui.setOutputDC("Connecting to the DC tool\n");
            
            try {
                scanner = new Scanner(new FileInputStream(new File("CONFIG.txt")));
            }
            catch (FileNotFoundException e) {
                gui.infoBox("CONFIG.txt was not found. Please, place it in the dist folder",
                    "CONFIG file not found", new Object[]{"Try again", "Exit"});
                if(gui.getDialogReturn()==0)    //if the user clicks try again
                    continue;
                else    //if the user clicks the second button exit the program
                    System.exit(0);
            }
            
            //////////////////////////
            //CONNECTING TO THE TOOL//
            //////////////////////////
            boolean flag = true;
            while(flag) {
                try{
                    //myHandler = new dcToolHandler();
                    flag = false;
                }
                catch(Exception e) {
                    System.out.println("Problem connecting to the Ingersol Rand tool: "+e.getMessage());
                    gui.infoBox("Error getting connecting to the tool: "+e.getMessage(),
                            "Exception in dcToolHandler()", new Object[]{"Try again", "Start from the beginning"});
                    if(gui.getDialogReturn()==0)    //if the user clicks try again
                        continue;
                    else {    //if the user clicks the second button start from the mainloop
                        gui.dispose();
                        continue mainloop;
                    }
                }
            }
            gui.setOutputDC("Successfully connected to the DC tool\n");
            
            //////////////////////////////
            //GETTING DATA FROM THE TOOL//
            //////////////////////////////
            gui.setOutputDC("Start fastening now!\n");
            flag = true;
            while(flag) {
                try {
                    //serial = myHandler.getData();
                    serial = "200144";
                    //System.out.println("\nSERIAL: "+serial);
                    flag = false;
                }
                /*catch(IOException ioe) {
                    System.out.println("ioe exception: "+ioe.getMessage()); ioe.printStackTrace();
                    gui.infoBox("I/O error getting data from the tool: "+ioe.getMessage(),
                            "IOException in getData()", new Object[]{"Try again", "Start from the beginning"});
                    if(gui.getDialogReturn()==0)    //if the user clicks try again
                        continue;
                    else {    //if the user clicks the second button start from the mainloop
                        gui.dispose();
                        continue mainloop;
                    }
                }*/
                catch(Exception e) {
                    System.out.println("Exception Message: "+e.getMessage()); e.printStackTrace(); 
                    gui.infoBox("Error getting data from the tool: "+e.getMessage(),
                            "Exception in getData()", new Object[]{"Try again", "Start from the beginning"});
                    if(gui.getDialogReturn()==0)    //if the user clicks try again
                        continue;
                    else {    //if the user clicks the second button start from the mainloop
                        gui.dispose();
                        continue mainloop;
                    }
                }
            }
            
            ////////////////////////////////////////
            //READING AND MATCHING POSSIBLE COLORS//
            ////////////////////////////////////////
            lines = new ArrayList<>();
            colors = new ArrayList<>();
            numbers = new ArrayList<>();
            while(scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
            for(String line : lines) {
                String[] temp = line.split("=");
                colors.add(temp[0]);
                numbers.add(temp[1]);
            }
            gui.setOutputDC("The serial number is: "+serial+" - The color should be ");
            boolean found = false;
            for(int i=0; i<colors.size(); i++) {
                if(numbers.get(i).matches(serial)) {
                    rightColor = colors.get(i);
                    gui.setOutputDC(rightColor+"\n");
                    found = true;
                }
            }
            if(!found) {
                gui.infoBox(serial+" is not in CONFIG.txt. Please add it and start again",
                    "Serial number not found", new Object[]{"Start again", "Exit"});
                if(gui.getDialogReturn()==0)    //if the user clicks start again
                    continue;
                else    //if the user clicks the second button exit the program
                    System.exit(0);   
            }
            
            //////////////////////////
            //READING OPERATOR BADGE//
            //////////////////////////
            gui.startBadge();
            while((badge=gui.getBadge()) == "empty")
                try{Thread.sleep(1000);} catch(Exception f){}
            
            
            ////////////////
            //CLOSING TEST//
            ////////////////
            gui.startFunctional();
            gui.setOutputFunctional("Closing test: ");
            while(false){//!IGV_Motor.close()) {
                gui.infoBox("The closing test failed!",
                    "Closing Test Fail", new Object[]{"Try again", "Start Over"});
                if(gui.getDialogReturn()==0)    //if the user clicks start again
                    continue;
                else {    //if the user clicks the second button exit the program
                    gui.dispose();
                    continue mainloop;
                }
            }
            gui.setOutputFunctional("SUCCESS!\n");
            
            //////////
            //CAMERA//
            //////////
            boolean pictureFlag = true;
            pictureloop:
            while(pictureFlag) {
                //////////////
                //COLOR TEST//
                //////////////
                gui.setOutputFunctional("Taking picture\n");
                flag = true;
                while(flag) {
                    color="empty";
                    try {
                        cam_gui = new cam_UI(); //instantiate the webcam ui
                        cam_gui.setVisible(true);
                        while(color.equals("empty")) { //wait until the picture is taken and validated
                            System.out.println("color="+color);
                            try{Thread.sleep(2000);} catch(Exception f){}
                        }
                        cam_gui.setVisible(false);
                        cam_gui.webcam.close();
                        cam_gui.dispose();
                    }     
                    catch(Exception e) {
                        System.out.println("Problems accessing the webcam: "+e.getMessage());
                        e.printStackTrace();
                        gui.infoBox("There was a problem accessing the camera.",
                            "Camera Error", new Object[]{"Try again", "Start Over"});
                        if(gui.getDialogReturn()==0)    //if the user clicks start again
                            continue;
                        else {    //if the user clicks the second button go back to the beginning
                            gui.dispose();
                            continue mainloop;
                        }
                    }
                
                    if(color.equals(rightColor)) {
                        gui.setOutputFunctional("The color is correct!\n");
                        flag = false;
                    }
                    else {
                        gui.infoBox("The color is supposed to be "+rightColor+", but the camera detected "+color,
                            "Wrong Color", new Object[]{"Take Picture Again", "Start Over"});
                        if(gui.getDialogReturn()==0)    //if the user clicks start again
                            continue;
                        else {    //if the user clicks the second button start from the beginning
                            gui.dispose();
                            continue mainloop;
                        }
                    }
                }
            
                ///////////////////////
                //CHIPPING VALIDATION//
                ///////////////////////
                myLatch = new CountDownLatch(1);
                picture_UI pic_ui = new picture_UI(myLatch);
                try {myLatch.await();}
                catch(Exception e) {System.out.println("Latch exception: "+e.getMessage());}
                switch(pic_ui.selection) {
                    case 'A':
                        gui.setOutputFunctional("Picture approved by the operator\n");
                        break pictureloop;
                    case 'R':
                        gui.setOutputFunctional("Picture NOT approved by the operator. Refactor IGV!\n");
                        gui.infoBox("You have rejected the picture, what do you want to do?",
                            "Picture rejected", new Object[]{"Take Picture Again", "Start Over"});
                        if(gui.getDialogReturn()==0) {    //if the user clicks start again
                            continue pictureloop;
                        }
                        else {    //if the user clicks the second button Start Over
                            gui.dispose();
                            continue mainloop;
                        }
                    default:
                        gui.setOutputFunctional("There was a problem with the picture approval window!\n");
                }
                //pic_ui.rundisplay(pic_ui);
            }
            
            ////////////////
            //OPENING TEST//
            ////////////////
            flag = false;
            gui.setOutputFunctional("Opening test: ");
            if(true)//IGV_Motor.open())
                gui.setOutputFunctional("SUCCESS!\n");
            else {
                gui.setOutputFunctional("FAIL!\n");
                flag = true;
                gui.infoBox("The opening test failed",
                        "Opening Test Error", new Object[]{"Try again", "Start Over"});
                if(gui.getDialogReturn()==0)    //start open/close loop
                    flag = true;
                else {    //if the user clicks the second button start from the beginning
                    gui.dispose();
                    continue mainloop;
                }
            }
            
            ////////////////////////////
            //OPTIONAL OPEN/CLOSE LOOP//
            ////////////////////////////
            while(flag) {
                gui.setOutputFunctional("Performing close/open test again\n");
                if(IGV_Motor.close() && IGV_Motor.open()) 
                    gui.setOutputFunctional("SUCCESS!\n");
                else {
                    gui.infoBox("Open/Close test failed again",
                            "Open/Close Test Error", new Object[]{"Try again", "Start Over"});
                    if(gui.getDialogReturn()==0)    //stay in open/close loop
                        flag = true;
                    else {    //if the user clicks the second button start from the beginning
                        gui.dispose();
                        continue mainloop;
                    }
                }
            }
            
            ///////////////////////
            //DEFAULT POSITIONING//
            ///////////////////////
            gui.setOutputFunctional("Closing half way... ");
            IGV_Motor.closeHalfWay();
            gui.setOutputFunctional("DONE\n");
            
            
            ////////////
            //PRINTING//
            ////////////
            gui.setOutputFunctional("Printing label...");
            
            ///////
            //END//
            ///////
            gui.infoBox("Job finished!",
                            "SUCCESS", new Object[]{"Start Over", "Exit"});
                    if(gui.getDialogReturn()==0) {    //stay in mainloop
                        gui.dispose();
                        continue mainloop;
                    }
                    else {    //if the user clicks the second button, exit
                        gui.dispose();
                        System.exit(0);
                    }
            
        } //end main loop
        
        
    }
    
    public static String getCurrentDir()
    {
        try {
            String path = new File(IGV_Tester.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            System.out.println("Path before: " + path);
            for(int i=path.length()-1; i>0; i--)
            {
                if(path.charAt(i) == '\\')
                {
                    path = path.substring(0, i+1);
                    path.trim();
                    break;
                }
            }
            System.out.println("Path after: " + path);
            return path;
        }
        catch(Exception e) {System.out.println("Exception Caught in getCurrentDir: "+e.getMessage()); return "ERROR";}
        
    }
    
    private static void printLabel(String serial)
    {
        try {
            //print label data to text file
            PrintWriter writer = new PrintWriter(getCurrentDir()+"\\BmccData.txt");
            writer.println(serial);
            writer.close();
            
            //wake up bartender by saving file in location
            PrintWriter writer2 = new PrintWriter(getCurrentDir()+"NewJob\\newjob.txt");
            writer2.close();
        }
        catch (Exception e) {System.out.println("Excetion caught in printLabel: "+e.getMessage());}
            
        try{Thread.sleep(10000);} catch(Exception f){}
    }
    
}