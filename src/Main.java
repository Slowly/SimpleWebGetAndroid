
/*
 * John Bonifas
 * ACO 330 Fall 2015
 * Programming Assignment
 * 10/19/2015
 * 
 * Portions of this project came from:
 * 
 * package edu.uoregon.tau.common;
 * http://www.cs.uoregon.edu/research/tau/tau_releases/tau-2.20.1/tools/src/common/src/
 */

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.InetAddress;
import java.text.DecimalFormat;

public class Main
{
  // CONSTRUCTOR
  
  public static void wget(String URL, String file, boolean status) throws IOException
  {
    
    wget(URL, file, status, new Progress() 
    {
      int size;
      
      // ===============  THIS IS WHERE THE PROGRESS BAR HAS TO GO, AND THE FINISHED REPORT LATER
      
      public void reportProgress(int bytes, double elapsedTime) 
      {
        System.out.print("\r" + bytes / 1000 + "k bytes... of " + size / 1000 + "k bytes (" + 
                          (bytes / elapsedTime) / 1000 + " k per second)    ");
      }
      public void reportSize(int bytes) 
      {
        size = bytes;
      }
      // =========================================================================================
    }
    );
  }
  
  // ENGINE
  
  public static void wget(String strURL, String strFile, boolean status, Progress progress) throws IOException 
  {
    InetAddress ipInet;
    int ipPort = 80;
    URLConnection connURL;
    DataInputStream dsIn;
    OutputStream dsOut;
    String strDomain;
    double timerStart, timerElapsed = 0;
    
    // just make it a default filename if none is provided
    if(strFile.length() == 0 ) 
    {
      strFile = "index.html";
    }

    // announce what were gonna do
    System.out.println(strURL + "\n=> '" + strFile + "'");
    
    // resolve URL
    try
    {
      int urlFirstIndex = strURL.indexOf("http://") + 7;
      int urlLastIndex = strURL.indexOf("/", strURL.indexOf("http://") + 7);
      if(urlLastIndex < 8) urlLastIndex = strURL.length();
      strDomain =  strURL.substring(urlFirstIndex,urlLastIndex); 
      System.out.print("\nResolving " + strDomain + " ... ");
      ipInet = InetAddress.getByName(strDomain);
      System.out.println(ipInet.getHostAddress());
    } 
    catch (Exception e) 
    {
      System.out.println("failed: Host not found or bad URL.");
      return;
    }
    
    // connect to URL and initialize file 
    System.out.print("\nConnecting to " + strDomain + "[" + ipInet.getHostAddress() + "]:" + ipPort + "...");
    try 
    {
      
      connURL = new URL(strURL).openConnection(); 
      dsIn = new DataInputStream(connURL.getInputStream());
      System.out.println(" connected.");
      dsOut = new FileOutputStream(strFile);
      System.out.println("\nHTTP request sent, awaiting response... " + 
                         connURL.getHeaderField(0).substring(connURL.getHeaderField(0).indexOf(" ") + 1) + "\n");     
    }
    catch (Exception e) 
    {
      System.out.print(" 404 Not Found");
      System.out.println("\nERROR 404: Not Found.");
      return;
    }
    
    // download progress
    progress.reportSize(connURL.getContentLength());
    int i = 0;
      
    try 
    {
      timerStart = System.currentTimeMillis();
      while (true) 
      {
        dsOut.write(dsIn.readUnsignedByte());
        i++;
        if (status && i % 100000 == 0)
        {
          timerElapsed = ((double)System.currentTimeMillis() - timerStart) / 1000;
          progress.reportProgress(i,timerElapsed);
        }
      }
    } 
    catch (EOFException e) 
    {
      dsOut.close();
      if (status)
      {
        System.out.println("\r" + i / 1000 + "k bytes... ");
        System.out.println("Done. Download time: " + timerElapsed + " seconds");
      }
      return;
    }
    
  }
  
  // PROGRAM ENTRY POINT
  
  public static void main(String[] args) 
  {
    switch (args.length) 
    {
      case 1:  
        try 
        {
          Main.wget(args[0], args[0].substring(args[0].lastIndexOf('/')+1), true);
        } 
        catch (IOException e) 
        {
          System.out.println("Failed getting " + args[0]);
        }
        break;
      
      case 2:  
        try 
        {
          Main.wget(args[0], args[1], true);
        } 
        catch (IOException e) 
        {
          System.out.println("Failed getting " + args[0]);
        }
        break;
        
      case 3:  
        try 
        {
          Main.wget(args[0], args[1], args[2].equalsIgnoreCase("true"));
        } 
        catch (IOException e) 
        {
          System.out.println("Failed getting " + args[0]);
        }
        break;
        
      default: 
        System.out.println("\nSimpleWebGet 2.1\nJohn Bonifas\nACO 330 Fall 2015\nprogramming assignment\nUsage: Wget <url> [local filename]");
        break;
    }
  }
  
  public static interface Progress
  {
    public void reportProgress(int bytes, double elapsedTime);
    public void reportSize(int bytes);
  }
  
}
