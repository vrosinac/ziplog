/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ziplog;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.FileWriter;

/**
 *
 * @author vrosinac
 */
public class Ziplog {
    public static void main(String[] args) throws IOException {
        String jdbc ="elloe" ;
        String zoneProfile = "hello" ;
        String globalProfile = "hello" ;
        String globalPassword ="elloe" ;
        String zonePassword ="elloe" ;
        String jobId = "hello" ;
        String dbType ="DB2";
        Connection globalConnection;
        Connection zoneConnection;
        boolean creatingJDBCObjectZone= false;
        boolean creatingJDBCObjectGlobal= false;
        Statement stmt, globalStmt;
        ResultSet rs1;
        String sqlQuery="";
        
        if (args.length == 4) {
            // System.out.println("we have 3 arguments");
            if (!args[0].isEmpty()) {
                     jdbc = args[0];
            }
            if (!args[1].isEmpty()) {
                     globalProfile = args[1];
            }
            if (!args[2].isEmpty()) {
                     zoneProfile = args[2];
            }
            if (!args[3].isEmpty()) {
                     jobId = args[3];
            }
            
            Console console = System.console();
            char[] password1 = console.readPassword("Global password: ");   
            globalPassword = new String(password1);
            //System.out.println(globalPassword);

            char[] password2 = console.readPassword("Zone password: ");   
            zonePassword = new String(password2);
           // System.out.println(zonePassword);



        }
        else
        {
            System.out.println("usage: java - jar Ziplog.jar [jdbc url] [Global profile]  [Zone profile] [job id]");
            System.exit(0);    
            /* TEST DATA 
            jdbc="jdbc:db2://mancswgtb0022:50000/FBTI";
            globalProfile = "TIGLOBAL";
            zoneProfile ="TIZONE";
            globalProfile ="TIGLOBAL";
            jobId="9512";
            zonePassword = "T!Z0N3123";
            globalPassword="G10b@!123";
            */
        }    
          
        //need to decide dbtype from connection string
        
        if (jdbc.contains("db2"))
        {
            dbType="DB2";
        }
        
        if (jdbc.contains("oracle"))
        {
            dbType="Oracle";
        }
         
        if (jdbc.contains("sqlserver"))
        {
            dbType="sqlserver";
        }
          
        if (dbType.equals("DB2") ) 
        {
            try 
            {

                ClassLoader cl = ClassLoader.getSystemClassLoader();;
                Class.forName("com.ibm.db2.jcc.DB2Driver", false,cl);
                

            } catch (ClassNotFoundException e) {
       
                    e.toString();
            }
        }
        if (dbType.equals("Oracle") ) 
        {

            try {

                    ClassLoader cl = ClassLoader.getSystemClassLoader();;
                    Class.forName("oracle.jdbc.driver.OracleDriver",false,cl);
                    
            } catch (ClassNotFoundException e) {
                   
                    e.toString();
            }

        }
        
         if (dbType.equals("MsSQLServer")) 
         {
             //not sure what to do there
                   
         }

        
              // Create the connection using the IBM Data Server Driver for JDBC
        if (!zoneProfile.isEmpty() && !zonePassword.isEmpty()
                              && !jdbc.isEmpty()) 
        {
            try 
            {

                        zoneConnection = DriverManager.getConnection(jdbc, zoneProfile,
                                        zonePassword);
                        System.out.println( " **** Got connection with zone");
                        creatingJDBCObjectZone= true;
                        
                if (globalProfile.isEmpty() || !globalPassword.isEmpty()
                              && jdbc.isEmpty()) 
                {
                     System.out.println("usage: java - jar Ziplog.jar [jdbc url] [Global profile] [Zone profile] [job id]");
                     System.exit(0);    
                }
                
                globalConnection = DriverManager.getConnection(jdbc, globalProfile,globalPassword);
                 System.out.println( " **** Got connection with global");
                 creatingJDBCObjectGlobal= true;


                /////////////////**//////////
                // Create the Statement

                if (creatingJDBCObjectZone && creatingJDBCObjectGlobal) 
                {
                    try {
                        
                        
                          /// JobControl ----------------------------------
                        System.out.println( " **** Gatherinng JOBCONTROL data");
                            stmt = zoneConnection.createStatement();
                            
                            sqlQuery = "SELECT * from " + zoneProfile + ".JobControl where Job_Id = " + jobId;
                            rs1 = stmt.executeQuery(sqlQuery);
                            
                            String result = "";
                            ResultSetMetaData rsmd=rs1.getMetaData();
                            int ncols = rsmd.getColumnCount();   
                             
                            for (int i=0; i<ncols ; i++)
                            {
                                result = result + "\"" + rsmd.getColumnName(i+1) + "\"" +";";
                            }
                            result = result + "\n";


                            while (rs1.next()) {
                                for (int i=0; i<ncols ; i++)
                                {
                                    result = result + "\"" + rs1.getString(i+1)+ "\"" +";";
                                }
                                result = result + "\n";
                            }
                             
                            FileWriter fileWriter = new FileWriter("JobControl.csv");
                            fileWriter.write(result);
                            fileWriter.close();                             
                             
                            
                            // StepControl ------------------------------------------------------------------------
                            System.out.println( " **** Gatherinng STEPCONTROL data");
                            sqlQuery = "SELECT * from " + zoneProfile + ".StepControl where Job_Id = " + jobId;
                            rs1 = stmt.executeQuery(sqlQuery);
                            
                            result = "";
                            rsmd=rs1.getMetaData();
                            ncols = rsmd.getColumnCount();   
                             
                            for (int i=0; i<ncols ; i++)
                            {
                                result = result + "\"" + rsmd.getColumnName(i+1) + "\"" +";";
                            }
                            result = result + "\n";


                            while (rs1.next()) {
                                for (int i=0; i<ncols ; i++)
                                {
                                    result = result + "\"" + rs1.getString(i+1)+ "\"" +";";
                                }
                                result = result + "\n";
                            }
                             
                            fileWriter = new FileWriter("StepControl.csv");
                            fileWriter.write(result);
                            fileWriter.close();                             
                             
                            
                            
                            
                            //BatchStep --------------------------------------------------
                             System.out.println( " **** Gatherinng BATCHSTEP data");
                            sqlQuery = "SELECT * from " + zoneProfile + ".batchstep where Job_Id = " + jobId;
                            rs1 = stmt.executeQuery(sqlQuery);
                            
                            result = "";
                            rsmd=rs1.getMetaData();
                            ncols = rsmd.getColumnCount();   
                             
                            for (int i=0; i<ncols ; i++)
                            {
                                result = result + "\"" + rsmd.getColumnName(i+1) + "\"" +";";
                            }
                            result = result + "\n";


                            while (rs1.next()) {
                                for (int i=0; i<ncols ; i++)
                                {
                                    result = result + "\"" + rs1.getString(i+1)+ "\"" +";";
                                }
                                result = result + "\n";
                            }
                             
                            fileWriter = new FileWriter("batchstep.csv");
                            fileWriter.write(result);
                            fileWriter.close();                             
                            
                            
                            //FROM BAtchMSG ----------------------------------------------------
                             System.out.println( " **** Gatherinng BATCHMSG data");
                            sqlQuery = "SELECT * from " + zoneProfile + ".batchmsg";
                            rs1 = stmt.executeQuery(sqlQuery);
                            
                            result = "";
                            rsmd=rs1.getMetaData();
                            ncols = rsmd.getColumnCount();   
                             
                            for (int i=0; i<ncols ; i++)
                            {
                                result = result + "\"" + rsmd.getColumnName(i+1) + "\"" +";";
                            }
                            result = result + "\n";


                            while (rs1.next()) {
                                for (int i=0; i<ncols ; i++)
                                {
                                    result = result + "\"" + rs1.getString(i+1)+ "\"" +";";
                                }
                                result = result + "\n";
                            }
                             
                            fileWriter = new FileWriter("batchmsg.csv");
                            fileWriter.write(result);
                            fileWriter.close();                             
                            
                            
                            
                            
                            
                            
                            ///FROM SS_TUNING_PARAM_DEF ----------------------------------------------------
                            System.out.println( " **** Gatherinng SS_TUNING_PARAM_DEF data");
        
                            globalStmt = globalConnection.createStatement();
                            
                            sqlQuery = "SELECT * from " + globalProfile + ".SS_TUNING_PARAM_DEF";
                            rs1 = globalStmt.executeQuery(sqlQuery);
                            
                            result = "";
                            rsmd=rs1.getMetaData();
                            ncols = rsmd.getColumnCount();   
                             
                            for (int i=0; i<ncols ; i++)
                            {
                                result = result + "\"" + rsmd.getColumnName(i+1) + "\"" +";";
                            }
                            result = result + "\n";


                            while (rs1.next()) {
                                for (int i=0; i<ncols ; i++)
                                {
                                    result = result + "\"" + rs1.getString(i+1)+ "\"" +";";
                                }
                                result = result + "\n";
                            }
                             
                            fileWriter = new FileWriter("sstuningparamdef.csv");
                            fileWriter.write(result);
                            fileWriter.close();                             
                             
                            
                            //ASYNC_ALLOCATION-----------------------------
                            System.out.println( " **** Gatherinng ASYNC_ALLOCATION data");
        
                            sqlQuery = "SELECT * from " + globalProfile + ".ASYNC_ALLOCATION";
                            rs1 = globalStmt.executeQuery(sqlQuery);
                            
                            result = "";
                            rsmd=rs1.getMetaData();
                            ncols = rsmd.getColumnCount();   
                             
                            for (int i=0; i<ncols ; i++)
                            {
                                result = result + "\"" + rsmd.getColumnName(i+1) + "\"" +";";
                            }
                            result = result + "\n";


                            while (rs1.next()) {
                                for (int i=0; i<ncols ; i++)
                                {
                                    result = result + "\"" + rs1.getString(i+1)+ "\"" +";";
                                }
                                result = result + "\n";
                            }
                             
                            fileWriter = new FileWriter("asyncallocation.csv");
                            fileWriter.write(result);
                            fileWriter.close();                             
                            
                            
                            

                    } catch (Exception e) {
                            System.out.println(" **** EXCEPTION in Created 1st JDBC Statement object");
                            e.printStackTrace();
                    }

                }

            }
            catch (Exception e) {
                 e.toString();
            }
        }    

        
        // ZIP IT ALL UP
        System.out.println( " **** Creating the zip archive");
    
        
        List<String> srcFiles = Arrays.asList("JobControl.csv", "StepControl.csv","batchstep.csv","batchmsg.csv","sstuningparamdef.csv","asyncallocation.csv");
        FileOutputStream fos = new FileOutputStream("multiCompressed.zip");
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        for (String srcFile : srcFiles) {
            File fileToZip = new File(srcFile);
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        zipOut.close();
        fos.close();
        
   
          // Delete the files now.
        for (String srcFile : srcFiles) {
            File fileToZip = new File(srcFile);
            fileToZip.delete();
        }
         System.out.println(" **** Success");
     
    }
  
   
}
///    jdbc connection string
////   zone profile
////   password
/////  job id
