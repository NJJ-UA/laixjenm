<HTML>


<BODY>

<%@ page import="java.sql.*" %>
<% 
if(request.getParameter("LogOut") != null)
{
  session.setAttribute("isLogin",false);
  session.invalidate();
  String redirectURL = "login.html";
  response.sendRedirect(redirectURL);
  return;
}      
%>




</BODY>
</HTML>
