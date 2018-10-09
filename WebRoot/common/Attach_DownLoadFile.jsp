<%@ page language="java" contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<%@ page import="java.sql.*" %>
<%@ page  import ="java.io.*, java.util.*, java.text.*"  %>
<%
	com.huic.core.util.Request requ = new com.huic.core.util.Request(request,"GBK");
	String fileName = "";
	String sn = "";
	String ErrMsg="<HTML><HEAD><META http-equiv=\"Content-Type\" content=\"text/html; charset=GBK\"></HEAD><BODY><SCRIPT LANGUAGE='JavaScript'>alert(\"您要下载的资料不存在或已被管理员删除了！\");window.close();</script></BODY></HTML>";	
	
	//---- chenjie: 07-01-14 begin
	Connection conn = null;
	Statement stmt = null;
	ResultSet resu = null;
	try{
	String userName = ""; 
	sn = requ.getParameter("sn");
	//userName = (String)session.getAttribute("user");
	userName = requ.getParameter("username");
	System.out.println("attach_download username: "+userName);  //chenjie:070114 test ok		
	String sql = "update tblattach t "
   				+ " set t.readusers = t.readusers||',"+userName+"'"
				+ " where t.sn ='"+sn+"'";
	System.out.println("Attach_download sql: "+sql);
	System.out.println("Attach_down1");
	conn = com.huic.core.db.DBUtils.getConnection();
	System.out.println("Attach_down2");
	stmt = conn.createStatement();
	System.out.println("Attach_down3");
	resu = stmt.executeQuery(sql);
	System.out.println("Attach_down4");
	}catch(Exception n){
		n.printStackTrace();
	}finally{
		conn.close();
		stmt.close();
		resu.close();
	}
	//---- chenjie: 07-01-14 end
	
	try{
		fileName = requ.getParameter("fileName");
		sn = requ.getParameter("sn");
		if(sn!=null || sn.trim().length()>0){
							boolean b =com.huic.gai.upload.UploadFile.FileExport(fileName,sn,request);
					if (b)
					{
						String aFilePath = application.getRealPath("/") +"upload/"+ fileName;    //要下载的文件路径	
						boolean  isInline  =   false ; 
					    response.reset();	
					    java.io.File f  =   new  java.io.File(aFilePath);
				        if  (f.exists()  &&  f.canRead()) {
				            String mimetype  =   null ;
				            mimetype  =  application.getMimeType( aFilePath );
				             if ( mimetype  ==   null  )
				            {
				                mimetype  ="application/octet-stream;charset=ISO8859-1";
				            }

				            response.setContentType( mimetype );

				            String ua  =  request.getHeader("User-Agent"); //  获取终端类型 
				             if (ua  ==   null ) ua  ="User-Agent: Mozilla/4.0 (compatible; MSIE 6.0;)";
				             boolean  isIE  =  ua.toLowerCase().indexOf("msie")  !=   - 1 ; //  是否为 IE 

				             if (isIE  &&   ! isInline) {
				                mimetype  ="application/x-msdownload";
				            }
				            String downFileName  =   new  String(f.getName().getBytes(),"ISO8859-1");

				            String inlineType  =  isInline  ?"inline" :"attachment"; //  是否内联附件

				             //  or using this, but this header might not supported by FireFox
				             // response.setContentType("application/x-download"); 
				            response.setHeader ("Content-Disposition", inlineType  +";filename=\""
				             +  downFileName  +"\""); 

				            response.setContentLength(( int ) f.length()); //  设置下载内容大小 

				             byte [] buffer  =   new   byte [ 4096 ]; //  缓冲区 
				            BufferedOutputStream output  =   null ;
				            BufferedInputStream input  =   null ;

				             //
				             try  {
				                output  =   new  BufferedOutputStream(response.getOutputStream());
				                input  =   new  BufferedInputStream( new  FileInputStream(f));

				                 int  n  =  ( - 1 );
				                 while  ((n  =  input.read(buffer,  0 ,  4096 ))  >   - 1 ) {
				                    output.write(buffer,  0 , n);
				                }
				                response.flushBuffer();
				            }
				             catch  (Exception e) {
				            }  //  用户可能取消了下载 
				             finally  {
				                 if  (input  !=   null ) input.close();
				                 if  (output  !=   null ) output.close();
				            }

				        }	
				        f.delete();
				        f=null;					
					}
			//com.huic.gai.upload.UploadFile.DownloadFile(new String(fileName.getBytes("GBK"), "iso-8859-1"),sn, pageContext.getServletConfig(),request,response);
		}else{
			com.huic.gai.upload.UploadFile.DownloadFile(new String(fileName.getBytes("GBK"), "iso-8859-1"),pageContext);
		}	
	}catch(Exception n){
		n.printStackTrace();
		ErrMsg="<HTML><HEAD><META http-equiv=\"Content-Type\" content=\"text/html; charset=GBK\"></HEAD><BODY><SCRIPT LANGUAGE='JavaScript'>alert(\"抱歉！您要下载资料出现异常！\");window.close();</script></BODY></HTML>";	
			%><%@page contentType="text/html; charset=GBK" language="java" %>
			<%
			out.print(ErrMsg);		
	try{out.print(n);}catch(Exception b){}
	}	
%>
