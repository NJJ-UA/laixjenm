<HTML>
<HEAD>

<TITLE>update</TITLE>


</HEAD>

<%@ page import="java.sql.*" %>
<%@ page import="java.lang.*" %>

<% 
if( session.getAttribute("isLogin")!=null && (Boolean)session.getAttribute("isLogin"))
{

	String userName=session.getAttribute("USERNAME").toString();
 	String group_id = request.getParameter("Mygroup");
	out.println("<h3>Group ID: "+group_id+"</h3>");
	out.println("<br>");
	
  	Connection conn = null;
  	Statement stmt = null;
	ResultSet rset;	

  	String driverName = "oracle.jdbc.driver.OracleDriver";
  	String dbstring = "jdbc:oracle:thin:@gwynne.cs.ualberta.ca:1521:CRS";

	try{
	    //load and register the driver
	    Class drvClass = Class.forName(driverName); 
	    DriverManager.registerDriver((Driver) drvClass.newInstance());
	 }
	catch(Exception ex){
	   out.println("<hr>" + ex.getMessage() + "<hr>");
	   return;
	}
	 
	try{
	    //establish the connection 
	    conn = DriverManager.getConnection(dbstring,"jni1","c3912016");
	    
	    stmt = conn.createStatement();
	}
	catch(Exception ex){
	    
	    out.println("<hr>" + ex.getMessage() + "<hr>");
	    return;
	}

	


    	String sql1 = "SELECT friend_id FROM group_lists where group_id = '"+group_id+"'";

	try{
	    stmt = conn.createStatement();
	    rset = stmt.executeQuery(sql1);
	    %>	

<body>
<td>users:</td>
	<%
	while(rset.next()){
	%>
	<form method="post" action="delete.jsp?gid=<%=group_id%>&id=<%=rset.getString(1)%>" >
	<li><%=rset.getString(1)%></li>
	<input type = "submit" value = "delete"  name = "submit" >
	</form>
	<%}%>
<form method="post" action="add.jsp?gid=<%=group_id%>" name="AddUserForm">
	add user name:<br>
	<input type = "text" name = "user" id = "user">

<input type = "submit" value = "add"  name = "submit" >
</form>

	<%
	conn.close();
	}
 	catch(Exception ex){
	    out.println("<hr>" + ex.getMessage() + "<hr>");
	    return;
    }
	
}
else{
	 out.println("<a href=\"login.html\">You need login first!</a>");
}
%>

<br>
<a target="_blank" href="userDocumentation.html">Help</a>

</body>
</html>
