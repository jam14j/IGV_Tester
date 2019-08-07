//THIS CLASS IS THE ENTRY POINT

package igv_tester;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
//import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
//import javax.swing.JOptionPane;


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
    public static String vendor, IGV_serial;
    public static CountDownLatch dialogLatch;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //System.out.println(getCurrentDir());

        mainloop:
        while(true) {
            IGV_serial = "empty";
            color = "empty";
            rightColor = "empty2";
            gui = new IGV_UI(); //make an instance of the main user interface
            gui.rundisplay(gui);
            gui.startTorque();
            //gui.setOutputDC("Connecting to the DC tool\n");
            
            //TESTING
            //This latch is necessary to wait for the dialog return value
//            dialogLatch = new CountDownLatch(1);
//            gui.infoBox("TEST BODY",
//                    "TEST TITLE", new Object[]{"ayy lmao", "lmao ayy"});
//            try {dialogLatch.await();}
//            catch(Exception e) {System.out.println("dialogLatch exception: "+e.getMessage());}
//            System.out.println("RETURN: "+gui.getDialogReturn());
            
//            myLatch = new CountDownLatch(1);
//                picture_UI pic_ui = new picture_UI(myLatch);
//                try {myLatch.await();}
//                catch(Exception e) {System.out.println("Latch exception: "+e.getMessage());}
//                switch(pic_ui.selection) {
//                    case 'A':
//                        System.out.println("APPROVED!"); break;
//                    case 'R':
//                        System.out.println("REJECTED!"); break;
//                    default:
//                        System.out.println("DEFAULT!");
//                }
            //END TESTING
            
            try {
                scanner = new Scanner(new FileInputStream(new File("CONFIG.txt")));
            }
            catch (FileNotFoundException e) {
                //This latch is necessary to wait for the dialog return value
                dialogLatch = new CountDownLatch(1);
                gui.infoBox("CONFIG.txt was not found. Please, place it in the dist folder",
                    "CONFIG file not found", new Object[]{"Try again", "Exit"});
                try {dialogLatch.await();}
                catch(Exception e_latch) {System.out.println("dialogLatch exception: "+e_latch.getMessage());}
                if(gui.getDialogReturn()==0)    //if the user clicks try again
                    continue;
                else    //if the user clicks the second button exit the program
                    System.exit(0);
            }
            
            lines = new ArrayList<>();
            colors = new ArrayList<>();
            numbers = new ArrayList<>();
            while(scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
            scanner.close();
            for(String line : lines) {
                String[] temp = line.split("=");
                //if it is the first line then its the vendor
                if(temp[0].equals("VENDOR"))
                    vendor = temp[1];
                //any other line is a color
                else {
                    colors.add(temp[0]);
                    numbers.add(temp[1]);
                }
            }
            
            //////////////////////////
            //CONNECTING TO THE TOOL//
            //////////////////////////
            boolean flag = true;
            while(flag) {
                try{
                    myHandler = new dcToolHandler();
                    flag = false;
                }
                catch(Exception e) {
                    System.out.println("Problem connecting to the Ingersol Rand tool: "+e.getMessage());
                    dialogLatch = new CountDownLatch(1);
                    gui.infoBox("Error getting connecting to the tool: "+e.getMessage(),
                            "Exception in dcToolHandler()", new Object[]{"Try again", "Exit"});
                    try {dialogLatch.await();}
                    catch(Exception e_latch) {System.out.println("dialogLatch exception: "+e_latch.getMessage());}
                    if(gui.getDialogReturn()==0) {    //if the user clicks try again
                        gui.dispose();
                        continue mainloop;
                    }
                    else {    //if the user clicks the second button exit
                        gui.dispose();
                        System.exit(0);
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
                    serial = myHandler.getData(gui);
                    //serial = "200144";
                    System.out.println("\nSERIAL: "+serial);
                    flag = false;
                }
                catch(Exception e) {
                    System.out.println("Problem connecting to the Ingersol Rand tool: "+e.getMessage());
                    dialogLatch = new CountDownLatch(1);
                    gui.infoBox("Error getting data from the tool: "+e.getMessage(),
                            "Exception in dcToolHandler()", new Object[]{"Try again", "Exit"});
                    try {dialogLatch.await();}
                    catch(Exception e_latch) {System.out.println("dialogLatch exception: "+e_latch.getMessage());}
                    if(gui.getDialogReturn()==0) {    //if the user clicks try again
                        gui.dispose();
                        continue mainloop;
                    }
                    else {    //if the user clicks the second button exit
                        gui.dispose();
                        System.exit(0);
                    }
                }
            }
            //serial = "200144";
            myHandler.classDestructor();
            myHandler = null;
            
            ////////////////////////////////////////
            //READING AND MATCHING POSSIBLE COLORS//
            ////////////////////////////////////////
//            lines = new ArrayList<>();
//            colors = new ArrayList<>();
//            numbers = new ArrayList<>();
//            while(scanner.hasNextLine()) {
//                lines.add(scanner.nextLine());
//            }
//            scanner.close();
//            for(String line : lines) {
//                String[] temp = line.split("=");
//                //if it is the first line then its the vendor
//                if(temp[0].equals("VENDOR"))
//                    vendor = temp[1];
//                //any other line is a color
//                else {
//                    colors.add(temp[0]);
//                    numbers.add(temp[1]);
//                }
//            }
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
                dialogLatch = new CountDownLatch(1);
                gui.infoBox(serial+" is not in CONFIG.txt. Please add it and start again",
                    "Serial number not found", new Object[]{"Start again", "Exit"});
                try {dialogLatch.await();}
                catch(Exception e_latch) {System.out.println("dialogLatch exception: "+e_latch.getMessage());}
                if(gui.getDialogReturn()==0)    //if the user clicks start again
                    continue;
                else    //if the user clicks the second button exit the program
                    System.exit(0);   
            }
            
            /////////////////
            //INSTALL MOTOR//
            /////////////////
            gui.motorReminder();
            
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
            //IGV_Motor.close();
            while(!IGV_Motor.close()) {
                dialogLatch = new CountDownLatch(1);
                gui.infoBox("The closing test failed!",
                    "Closing Test Fail", new Object[]{"Try again", "Start Over"});
                try {dialogLatch.await();}
                catch(Exception e_latch) {System.out.println("dialogLatch exception: "+e_latch.getMessage());}
                if(gui.getDialogReturn()==0)    //if the user clicks start again
                    continue;
                else {    //if the user clicks the second button exit the program
                    gui.dispose();
                    continue mainloop;
                }
            }
            gui.setOutputFunctional("DONE!\n");
            
            gui.setOutputFunctional("Opening a little bit...\n");
            IGV_Motor.openABit();
            
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
                        dialogLatch = new CountDownLatch(1);
                        gui.infoBox("There was a problem accessing the camera.",
                            "Camera Error", new Object[]{"Try again", "Start Over"});
                        try {dialogLatch.await();}
                        catch(Exception e_latch) {System.out.println("dialogLatch exception: "+e_latch.getMessage());}
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
                        dialogLatch = new CountDownLatch(1);
                        gui.infoBox("The color is supposed to be "+rightColor+", but the camera detected "+color,
                            "Wrong Color", new Object[]{"Take Picture Again", "Start Over"});
                        try {dialogLatch.await();}
                        catch(Exception e_latch) {System.out.println("dialogLatch exception: "+e_latch.getMessage());}
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
                        //if the operator approves the picture,
                        //save the generated serial so that we can print it.
//                        IGV_serial = pic_ui.IGV_serial;
                        break pictureloop;
                    case 'R':
                        gui.setOutputFunctional("Picture NOT approved by the operator. Refactor IGV!\n");
                        dialogLatch = new CountDownLatch(1);
                        gui.infoBox("You have rejected the picture, what do you want to do?",
                            "Picture rejected", new Object[]{"Take Picture Again", "Start Over"});
                        try {dialogLatch.await();}
                        catch(Exception e_latch) {System.out.println("dialogLatch exception: "+e_latch.getMessage());}
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
            //flag = false;
            gui.setOutputFunctional("Opening test: ");
            //IGV_Motor.open();
            if(IGV_Motor.open())
                gui.setOutputFunctional("SUCCESS!\n");
            else {
                gui.setOutputFunctional("FAIL!\n");
                flag = true;
                dialogLatch = new CountDownLatch(1);
                gui.infoBox("The opening test failed",
                        "Opening Test Error", new Object[]{"Try again", "Start Over"});
                try {dialogLatch.await();}
                catch(Exception e_latch) {System.out.println("dialogLatch exception: "+e_latch.getMessage());}
                if(gui.getDialogReturn()==0)    //start open/close loop
                    flag = true;
                else {    //if the user clicks the second button start from the beginning
                    gui.dispose();
                    continue mainloop;
                }
            }
            gui.setOutputFunctional("DONE!\n");
            
            ////////////////////////////
            //OPTIONAL OPEN/CLOSE LOOP//
            ////////////////////////////
            while(flag) {
                gui.setOutputFunctional("Performing close/open test again\n");
                boolean combinedFlag = true;
                combinedFlag = combinedFlag & IGV_Motor.close();
                combinedFlag = combinedFlag & IGV_Motor.open();
                //if(!IGV_Motor.close() && !IGV_Motor.open()) 
                if(combinedFlag)
                    gui.setOutputFunctional("SUCCESS!\n");
                else {
                    dialogLatch = new CountDownLatch(1);
                    gui.infoBox("Open/Close test failed again",
                            "Open/Close Test Error", new Object[]{"Try again", "Start Over"});
                    try {dialogLatch.await();}
                    catch(Exception e_latch) {System.out.println("dialogLatch exception: "+e_latch.getMessage());}
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
            printLabel(serial, badge, color, IGV_serial);
            
            ///////
            //END//
            ///////
            dialogLatch = new CountDownLatch(1);
            gui.infoBox("Job finished!",
                            "SUCCESS", new Object[]{"Start Over", "Exit"});
            try {dialogLatch.await();}
            catch(Exception e_latch) {System.out.println("dialogLatch exception: "+e_latch.getMessage());}
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
            boolean breakFlag = false;
            for(int i=path.length()-1; i>0; i--)
            {
                if(path.charAt(i) == '\\')
                {
                    //This gave me some trouble.
                    //The way its setup right now, with a single break and no
                    //if statement, the code wont run from Netbeans(IDE)
                    //However it will run if you execute the .jar file
                    //This is because the executable has to be in the same 
                    //folder as the CONFIG.txt, the NewJob directory, etc.
                    //In order to make it run it NetBeans(IDE), comment out the
                    //break, and uncomment the if statement.
                    path = path.substring(0, i+1);
                    path.trim();
                    //if(breakFlag) {break;}
                    //breakFlag = true;
                    break;
                }
            }
            System.out.println("Path after: " + path);
            return path;
        }
        catch(Exception e) {System.out.println("Exception Caught in getCurrentDir: "+e.getMessage()); return "ERROR";}
        
    }
    
    private static void printLabel(String serial, String operator, String color, String IGV_serial)
    {
        try {
            //print label data to text file
            PrintWriter writer = new PrintWriter(getCurrentDir()+"\\dataToPrint.txt");
            writer.println(serial+","+operator+","+color+","+IGV_serial);
            writer.close();
            
            //wake up bartender by saving file in location
            PrintWriter writer2 = new PrintWriter(getCurrentDir()+"NewJob\\newjob.txt");
            writer2.close();
        }
        catch (Exception e) {System.out.println("Exception caught in printLabel: "+e.getMessage());}
            
        try{Thread.sleep(5000);} catch(Exception f){}
    }
    
}