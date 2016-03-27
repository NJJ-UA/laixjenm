import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import oracle.jdbc.driver.*;
import java.text.*;
import java.net.*;

/**
 *  A simple example to demonstrate how to use servlet to 
 *  query and display a list of pictures
 *
 *  @author  Li-Yan Yuan
 *
 */
public class PictureBrowse extends HttpServlet implements SingleThreadModel {
    
    /**
     *  Generate and then send an HTML file that displays all the thermonail
     *  images of the photos.
     *
     *  Both the thermonail and images will be generated using another 
     *  servlet, called GetOnePic, with the photo_id as its query string
     *
     */
    public void doGet(HttpServletRequest request,
		      HttpServletResponse res)
	throws ServletException, IOException {

	//  send out the HTML file
	res.setContentType("text/html");
	PrintWriter out = res.getWriter ();




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
        String que = request.getParameter("query");
        
        String query;
        String select="SELECT photo_id FROM IMAGES WHERE ('admin'='"+userName+"' or owner_name='"+userName+"' OR permitted = 1 OR '"+userName+"' in(select friend_id from group_lists where permitted=group_id ) )";
        String order="";

        if (request.getParameter("search") != null)
        {
    
    
          if((request.getParameter("query").equals(""))&&(request.getParameter("FROM").equals(""))&&(request.getParameter("TO").equals("")))
          {




          }else if ((request.getParameter("query").equals(""))&&(!(request.getParameter("FROM").equals("")))&&(!(request.getParameter("TO").equals("")))){


            select=select+" AND (contains(SUBJECT, '"+que+"', 1) > 0 OR contains(PLACE, '"+que+"', 2) > 0  OR contains(description, '"+que+"', 3) > 0) ";


          }else if ((!(request.getParameter("query").equals("")))&&(request.getParameter("FROM").equals(""))&&(request.getParameter("TO").equals(""))){




          }
        }
        String query = select + order;
        
          
            PreparedStatement doSearch = m_con.prepareStatement("SELECT score(1), itemName, description FROM item WHERE contains(description, ?, 1) > 0 order by score(1) desc");
            doSearch.setString(1, request.getParameter("query"));
            ResultSet rset2 = doSearch.executeQuery();
            out.println("<table border=1>");
            out.println("<tr>");
            out.println("<th>Item Name</th>");
            out.println("<th>Item Description</th>");
            out.println("<th>Score</th>");
            out.println("</tr>");
            while(rset2.next())
            {
              out.println("<tr>");
              out.println("<td>"); 
              out.println(rset2.getString(2));
              out.println("</td>");
              out.println("<td>"); 
              out.println(rset2.getString(3)); 
              out.println("</td>");
              out.println("<td>");
              out.println(rset2.getObject(1));
              out.println("</td>");
              out.println("</tr>");
            } 
            out.println("</table>");
          }
          else
          {
            out.println("<br><b>Please enter text for quering</b>");
          }            
        }
        m_con.close();
   
        
	out.println("<html>");
	out.println("<head>");
	out.println("<title> Photo List </title>");
	out.println("</head>");
	out.println("<body bgcolor=\"#000000\" text=\"#cccccc\" >");
	out.println("<center>");
	out.println("<h3>The List of Images </h3>");

	/*
	 *   to execute the given query
	 */
	try {
	    String query = "select photo_id from images";

	    Connection conn = getConnected();
	    Statement stmt = conn.createStatement();
	    ResultSet rset = stmt.executeQuery(query);
	    String p_id = "";

	    while (rset.next() ) {
		p_id = (rset.getObject(1)).toString();

	       // specify the servlet for the image
               out.println("<a href=\"GetOnePic?big"+p_id+"\">");
	       // specify the servlet for the themernail
	       out.println("<img src=\"GetOnePic?"+p_id +
	                   "\"></a>");
	    }
	    stmt.close();
	    conn.close();
	} catch ( Exception ex ){ out.println( ex.toString() );}
    
	out.println("<P><a href=\"main.jsp\"> Return </a>");
	out.println("</body>");
	out.println("</html>");
    }
    
    /*
     *   Connect to the specified database
     */
    private Connection getConnected() throws Exception {

	String username = "jni1";
	String password = "c3912016";
        /* one may replace the following for the specified database */
	String dbstring = "jdbc:oracle:thin:@gwynne.cs.ualberta.ca:1521:CRS";
	String driverName = "oracle.jdbc.driver.OracleDriver";

	/*
	 *  to connect to the database
	 */
	Class drvClass = Class.forName(driverName); 
	DriverManager.registerDriver((Driver) drvClass.newInstance());
	return( DriverManager.getConnection(dbstring,username,password) );
    }
}




