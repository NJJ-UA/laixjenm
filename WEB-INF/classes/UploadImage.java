/***
 *  A sample program to demonstrate how to use servlet to 
 *  load an image file from the client disk via a web browser
 *  and insert the image into a table in Oracle DB.
 *  
 *  Copyright 2007 COMPUT 391 Team, CS, UofA                             
 *  Author:  Fan Deng
 *                                                                  
 *  Licensed under the Apache License, Version 2.0 (the "License");         
 *  you may not use this file except in compliance with the License.        
 *  You may obtain a copy of the License at                                 
 *      http://www.apache.org/licenses/LICENSE-2.0                          
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  Shrink function from
 *  http://www.java-tips.org/java-se-tips/java.awt.image/shrinking-an-image-by-skipping-pixels.html
 *
 *
 *  the table shall be created using the following
      CREATE TABLE pictures (
            pic_id int,
	        pic_desc  varchar(100),
		    pic  BLOB,
		        primary key(pic_id)
      );
      *
      *  One may also need to create a sequence using the following 
      *  SQL statement to automatically generate a unique pic_id:
      *
      *   CREATE SEQUENCE pic_id_sequence;
      *
      ***/

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;
import oracle.sql.*;
import oracle.jdbc.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/**
 *  The package commons-fileupload-1.0.jar is downloaded from 
 *         http://jakarta.apache.org/commons/fileupload/ 
 *  and it has to be put under WEB-INF/lib/ directory in your servlet context.
 *  One shall also modify the CLASSPATH to include this jar file.
 */
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;

public class UploadImage extends HttpServlet {
    public String response_message;
    public void doPost(HttpServletRequest request,HttpServletResponse response)
	throws ServletException, IOException {
	//  change the following parameters to connect to the oracle database
	String username = "jni1";
	String password = "c3912016";
	String drivername = "oracle.jdbc.driver.OracleDriver";
	String dbstring ="jdbc:oracle:thin:@gwynne.cs.ualberta.ca:1521:CRS";
	int pic_id=0;

        Connection  conn;

        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String  response_message;
   
	// Get the session 
	HttpSession session = request.getSession( false );
        if (session == null || !((Boolean)session.getAttribute("isLogin"))){
          
          out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
                      "Transitional//EN\">\n" +
                      "<HTML>\n" +
                      "<HEAD><TITLE>Upload Message</TITLE></HEAD>\n" +
                      "<BODY>\n" +
                      "<a href=\"login.html\">You need login first!</a>" +
                      "</BODY></HTML>");
          return;
        }
       
        String userName = session.getAttribute("USERNAME").toString();



        
          
        try{
          // Connect to the database and create a statement
          conn = getConnected(drivername,dbstring, username,password);
          conn.setAutoCommit(false);
          
        }
        catch( Exception ex ) {
          //System.out.println( ex.getMessage());
          response_message = ex.getMessage();
           
          out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
                      "Transitional//EN\">\n" +
                      "<HTML>\n" +
                      "<HEAD><TITLE>Upload Message</TITLE></HEAD>\n" +
                      "<BODY>\n" +
                      "<H1>" +
                      response_message +
                      "</H1>\n" +
                      "</BODY></HTML>");
          return;
            
	}
        
	try {
          //Parse the HTTP request to get the image stream
          DiskFileUpload fu = new DiskFileUpload();
          List FileItems = fu.parseRequest(request);
	        
          // Process the uploaded items, assuming only 1 image file uploaded
          Iterator i = FileItems.iterator();
          FileItem item;//= (FileItem) i.next();
          int begin_id=-1;
          while (i.hasNext()) {
            item = (FileItem) i.next();
	    if(item.isFormField()){
              break;
            }

	    //Get the image stream
	    InputStream instream = item.getInputStream();
            InputStream instreamThu = item.getInputStream();
	    BufferedImage img = ImageIO.read(instreamThu);
	    BufferedImage thumbNail = shrink(img, 10);

            Statement stmt = conn.createStatement();
            
	    /*
	     *  First, to generate a unique pic_id using an SQL sequence
	     */
	    ResultSet rset1 = stmt.executeQuery("SELECT photo_id_seq.nextval from dual");
	    rset1.next();
	    pic_id = rset1.getInt(1);
            if (begin_id==-1){
              begin_id=pic_id;
            }
	    //Insert an empty blob into the table first. Note that you have to 
	    //use the Oracle specific function empty_blob() to create an empty blob
	    stmt.execute("INSERT INTO IMAGES VALUES("+pic_id+",'"+userName+"',2,null,null,null,null,empty_blob(),empty_blob())");
 
	    // to retrieve the lob_locator 
	    // Note that you must use "FOR UPDATE" in the select statement
	    String cmd = "SELECT * FROM IMAGES WHERE photo_id = "+pic_id+" FOR UPDATE";
	    ResultSet rset = stmt.executeQuery(cmd);
	    rset.next();
	    BLOB myblobThu = ((OracleResultSet)rset).getBLOB(8);
            BLOB myblob = ((OracleResultSet)rset).getBLOB(9);


	    //Write the image to the blob object
	    OutputStream outstreamThu = myblobThu.getBinaryOutputStream();
            OutputStream outstream = myblob.getBinaryOutputStream();
	    ImageIO.write(thumbNail, "jpg", outstreamThu);
	    
	    
	    int size = myblob.getBufferSize();
	    byte[] buffer = new byte[size];
	    int length = -1;
	    while ((length = instream.read(buffer)) != -1)
              outstream.write(buffer, 0, length);
	    
	    instream.close();
            instreamThu.close();
	    outstream.close();
            outstreamThu.close();

            stmt.executeUpdate("commit");
          }
            
          response_message = " Upload OK!  ";
            
          conn.commit();
          conn.close();

          String redirectURL;
          if(begin_id == pic_id){            
            redirectURL = "updateimage.jsp?p_id="+pic_id;
              
          }else{
            redirectURL = "updateimage.jsp?p_id="+begin_id+"+"+pic_id;
          }
          response.sendRedirect(redirectURL);
	} catch( Exception ex ) {
	    //System.out.println( ex.getMessage());
	    response_message = ex.getMessage();
            try{
              conn.rollback();
            }catch(Exception ex1){
              response_message += ex1.getMessage();
            }
            errorPage(out,response_message);
	}

	//Output response to the client
	
	

        
    }

    /*
      /*   To connect to the specified database
    */
    private static Connection getConnected( String drivername,
					    String dbstring,
					    String username, 
					    String password  ) 
	throws Exception {
	Class drvClass = Class.forName(drivername); 
	DriverManager.registerDriver((Driver) drvClass.newInstance());
	return( DriverManager.getConnection(dbstring,username,password));
    } 

    //shrink image by a factor of n, and return the shrinked image
    public static BufferedImage shrink(BufferedImage image, int n) {

        int w = image.getWidth() / n;
        int h = image.getHeight() / n;

        BufferedImage shrunkImage =
            new BufferedImage(w, h, image.getType());

        for (int y=0; y < h; ++y)
            for (int x=0; x < w; ++x)
                shrunkImage.setRGB(x, y, image.getRGB(x*n, y*n));

        return shrunkImage;
    }

     public static void errorPage(PrintWriter outs,String errormess) {
       outs.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
                   "Transitional//EN\">\n" +
                   "<HTML>\n" +
                   "<HEAD><TITLE>Upload Message</TITLE></HEAD>\n" +
                   "<BODY>\n" +
                   "<H1>" +
                   errormess +
                   "</H1>\n" +
                   "<a href=\"uploadpage.jsp\">back to upload</a>"+
                   "</BODY></HTML>");
        
     }
     public static String addQM(String s){
       if(s==""){
         return "null";
       }
       return "'"+s+"'";
     }

    
}
