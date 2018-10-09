package com.huic.core.db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceHkic;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.support.nativejdbc.CommonsDbcpNativeJdbcExtractor;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;

import com.huic.core.exception.DatabaseException;


/**
 *
 * <p>Title: </p>
 * <p>Description: A  database utility class to handle all the database stuffs
 * </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: MyVietnam.net</p>
 * @author: Minh Nguyen  minhnn@MyVietnam.net
 * @author: Mai  Nguyen  mai.nh@MyVietnam.net
 */
public final class DBUtils  {
	//日志
    private static Log log = LogFactory.getLog(DBUtils.class);
    private static DataSource dataSource = null;
    private static NativeJdbcExtractor nativeJdbcExtractor=new CommonsDbcpNativeJdbcExtractor();
    private static String  DriverClassName;
    private static String  Url;
    private static String  Username;
    private static String  Password;
    private  static Properties properties;
    /**
     * 配置文件名
     */
      //private static String propsName = "Config.properties";
      private static String propsName = "database.properties";
    /**
     * 配置属性表
     */
    private  static HashMap propsMap;    
    public void setDriverClassName(String DriverClassName){
    	DBUtils.DriverClassName = DriverClassName;
    }
    public void setUrl(String Url){
    	DBUtils.Url = Url;
    }
    public void setUsername(String Username){
    	DBUtils.Username = Username;
    }
    public void setPassword(String Password){
    	DBUtils.Password = Password;
    }
    private static String  getDriverClassName(){
    	return DBUtils.DriverClassName;
    }
    private static String  getUrl(){
    	return DBUtils.Url;
    }
    private static String  getUsername(){
    	return DBUtils.Username;
    }
    private static String  getPassword(){
    	return DBUtils.Password;
    }
    
  
    static {  
        try{
            System.setProperty("line.separator","\r\n");
            }catch(Exception ex){}  
       loadProps(propsName);    	
       BasicDataSource bds=new BasicDataSource();
       bds.setDriverClassName(getProp("db.driverName"));
    	
    	bds.setUrl(getProp("db.url"));
        ;
    	bds.setUsername(getProp("db.username"));
    	bds.setPassword(getProp("db.password"));
    	
    	
    	bds.setInitialSize(1);
    	bds.setMaxActive(20);
    	bds.setMaxWait(60000);
    	bds.setMaxIdle(15);
    	bds.setMinIdle(5);
    	bds.setValidationQuery("select 1 from dual");
    	bds.setPoolPreparedStatements(true);
    	bds.setMaxOpenPreparedStatements(10);
    	//bds.setAccessToUnderlyingConnectionAllowed(true);
    	//bds.setTestOnReturn(true);
    	bds.setRemoveAbandoned(true);
    	bds.setRemoveAbandonedTimeout(30);
    	bds.setLogAbandoned(true);
    	
    	dataSource=(DataSource)bds;
    	
    	log.debug("dataSource"+dataSource);
    }

    private DBUtils() {// so cannot new an instance  *
   	
    	log.debug("getDriverClassName:"+getDriverClassName());
    }


    /**
     * 从连接池中获取一个连接.
     * Get a connection from the connection pool. The returned connection
     * must be closed by calling DBUtils.closeConnection()
     * @return : a new connection from the pool if succeed
     * @throws SQLException : if cannot get a connection from the pool
     */
    public static Connection getConnection() throws SQLException {
        log.info("DBUtils.getConnection() is called.");
        log.info("Active Connection Count before getConncetion :"+((BasicDataSource)dataSource).getNumActive());

        ConnectionCaller.setStrID();
        ConnectionCaller.getCaller(Thread.currentThread().getName());//要找到调用这个方法的类的信息
        return dataSource.getConnection();
    }

    /**
     * 关闭当前连接池中的所有连接.
     * 此方法可能适用于刷新连接池中的所有连接.
     * Close all the connections that currently in the pool
     * This method could be used to refresh the database connection
     * @return true if the pool is empty and balance
     *         false if the pool has returned some connection to outside
     */
    public static boolean closeAllConnections() {

        return false;
    }

    /**
     * 将一个连接返回到连接池中
     * 如果这个连接不是从连接池中所获取的(即不是通过DBUtils.getConnnection()获取),不要通过此方法来关闭连接.
     * Use this method to return the connection to the connection pool
     * Do not use this method to close connection that is not from
     * the connection pool
     * @param connection : the connection that needs to be returned to the pool
     */
    public static void closeConnection(Connection connection) {
    	
		log.info("DBUtils.closeConnection() is called.");
        if (connection == null) return;

        try {
        	ConnectionCaller.getCaller(Thread.currentThread().getName());//要找到调用这个方法的类的信息
            connection.close();
        } catch (SQLException e) {
            log.error("DBUtils: Cannot close connection.", e);
        }
        log.info("Active Connection Count After closeConncetion :"+((BasicDataSource)dataSource).getNumActive());
    }

    /**
     * 将一个Statement对象的最大行数和取数据大小设为默认值.
     * Use this method to reset the MaxRows and FetchSize of the Statement
     * to the default values
     * @param statement : the statement that needs to be reseted
     */
    public static void resetStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.setMaxRows(0); //reset to the default value
            } catch (SQLException e) {
                log.error("DBUtils: Cannot reset statement MaxRows.", e);
            }

            try {
                statement.setFetchSize(0); //reset to the default value
            } catch (SQLException sqle) {
                //do nothing, postgreSQL doesnt support this method
            }
        }
    }

    /**
     * 关闭一个Statement
     * Use this method to close the Statement
     * @param statement : the statement that needs to be closed
     */
    public static void closeStatement(Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            log.error("DBUtils: Cannot close statement.", e);
        }
    }

    /**
     * 关闭一个ResultSet
     * Use this method to close the ResultSet
     * @param rs : the resultset that needs to be closed
     */
    public static void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            log.error("DBUtils: Cannot close resultset.", e);
        }
    }
    
    public static void close(Connection connection) {

       log.info("DBUtils.close(Connection) is called.");
        try {
        	if ((connection == null)||connection.isClosed()) {
        		log.info("connection is Closed.Active Connection Count After closeConncetion :"+((BasicDataSource)dataSource).getNumActive());
        		return;
        	}
        	
        	ConnectionCaller.getCaller(Thread.currentThread().getName());//要找到调用这个方法的类的信息             
            connection.close();
        } catch (SQLException e) {
            log.error("DBUtils: Cannot close connection.", e);
        }
        log.info("Active Connection Count After closeConncetion :"+((BasicDataSource)dataSource).getNumActive());
    }
    
    public static void close(ResultSet rs) {
        //log.info("DBUtils: resultset is being closed .");
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            log.error("DBUtils: Cannot close resultset.", e);
        }
    }
    
    public static void close(Statement statement) {
        //log.info("DBUtils: statement is being closed .");
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            log.error("DBUtils: Cannot close statement.", e);
        }
    }
    
    

    public static DataSource getDataSource() {
        return dataSource;
    }
    private static void setDataSource(DataSource dataSource) {
        DBUtils.dataSource = dataSource;
    }
	//访问数据库的例子,请依照此方法处理数据库  
    public static void main(String[] args) throws DatabaseException {
    	
        //DBUtils DBUtils1 = new DBUtils();
        //log.info("i = " + dataSource1);
		 Connection connection = null;
		 PreparedStatement statement = null;
		 ResultSet resultSet = null;
		 java.util.ArrayList retValue = new java.util.ArrayList();
		 StringBuffer sql = new StringBuffer(512);
		 sql.append("SELECT ?");
		 sql.append(" FROM ").append("dual");

		//for testing
		log.debug("Test sql = " + sql.toString());			 

		 try {
			 connection = DBUtils.getConnection();
			 statement = connection.prepareStatement(sql.toString());
			 statement.setInt(1, 10);
			 resultSet = statement.executeQuery();
			 
			 while (resultSet.next()) {
				 Integer perm = new Integer(resultSet.getInt(1));
				 log.debug("The value from dual:"+resultSet.getInt(1));
				 retValue.add(perm);
			 }
			 
			 //for testing
			 if(retValue.size()!=0){
				log.debug("The first value you get is : "+retValue.get(0));
			 }

		 } catch(SQLException sqle) {
			 log.error("Sql Execution Error!", sqle);
			 throw new com.huic.core.exception.DatabaseException("Error executing SQL in DBUtils.main()");
		 } finally {
			 DBUtils.close(resultSet,statement,connection);
		 }
		 
		 
		 int a=0;
    }


    /**
     * @param rs
     * @param pst
     * @param conn
     */
    public static void close(ResultSet rs, Statement stmt, Connection conn) {
    	close(rs);
        close(stmt);
        close(conn);
    }
    
    public static Connection getNativeConnection(Connection con){
    	ConnectionCaller.setStrID();
        ConnectionCaller.getCaller(Thread.currentThread().getName());//要找到调用这个方法的类的信息
    	
        Connection retConn=null;
        try {
            retConn=nativeJdbcExtractor.getNativeConnection(con);
        } catch (SQLException e) {
            log.error("DBUitls.getNativeConnection ERROR!");
            e.printStackTrace();
        }
        return retConn;
    }
 
    public static CallableStatement getNativeConnection(CallableStatement cstmt){
        CallableStatement ret=null;
        try {
            ret=nativeJdbcExtractor.getNativeCallableStatement(cstmt);
        } catch (SQLException e) {
            log.error("DBUitls.getNativeCallableStatement ERROR!");
            e.printStackTrace();
        }
        return ret;
    }
    
    public static Statement getNativeConnection(Statement stmt){
        Statement ret=null;
        try {
            ret=nativeJdbcExtractor.getNativeStatement(stmt);
        } catch (SQLException e) {
            log.error("DBUitls.getNativeStatement ERROR!");
            e.printStackTrace();
        }
        return ret;
    }
    
    public static PreparedStatement getNativePreparedStatement(PreparedStatement pstmt){
        PreparedStatement ret=null;
        try {
            ret=nativeJdbcExtractor.getNativePreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error("DBUitls.getNativePreparedStatement ERROR!");
            e.printStackTrace();
        }
        return ret;
    }
    
    public static ResultSet getNativeResultSet(ResultSet rs){
        ResultSet ret=null;
        try {
            ret=nativeJdbcExtractor.getNativeResultSet(rs);
        } catch (SQLException e) {
            log.error("DBUitls.getNativeResultSet ERROR!");
            e.printStackTrace();
        }
        return ret;
    }

    private  static void loadProps(String resourceURI) {
        properties = new Properties();
        propsMap=new HashMap();
           // in = getClass().getResourceAsStream(resourceURI);
           // properties.load(in);
            properties = new InitPropLoader().getProps(resourceURI);
            if(properties!=null){
                Enumeration em = properties.keys();

                while(em.hasMoreElements()){
                  String k=(String)em.nextElement();
                  String v=properties.getProperty(k);
                  propsMap.put(k,v);

                }           	
            }
    }
    /**
     * 取配置信息
     * @param name 配置项目
     * @return 值
     */
    public  static String getProp(String name) {
        return DBUtils.getProp(name,null);
    }
    //临时增加，为webmail的任务管理所加
    public  static String getProperty(String name) {
        return DBUtils.getProp(name,null);
    }

    /**
     * 取配置信息
     * @param name 配置项目
     * @param defaultValue 缺省值
     * @return 值
     */
    public  static String getProp(String name,String defaultValue) {
	Object oval = propsMap.get(name);
	String sval = (oval instanceof String) ? (String)oval : null;
	return sval == null ? defaultValue : sval;
    }    
	/* （非 Javadoc）
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 
	public void afterPropertiesSet() throws Exception {
        if (dataSource == null) {
            throw new BeanCreationException("Must set dataSource on UserDao");
        }

		DBUtils.dataSource = DBUtils.getDataSource();
	}
	*/
}
class InitPropLoader {

    public Properties getProps(String resourceURI) {
        Properties initProps = new Properties();
        InputStream in = null;
        try {
    		URL url = null;
    		url = getClass().getResource(resourceURI);
    		if(url==null){
    			url = getClass().getClassLoader().getResource(resourceURI);
    		}
    		if (url == null) {
    			throw new FileNotFoundException(
    					" resource [" + resourceURI + "]" + " cannot be resolved to URL because it does not exist");
    		}        	
        	//database.properties位于classes根目录下
    		in = url.openStream();
            //in = getClass().getResourceAsStream("//"+resourceURI);
            initProps.load(in);
        }
        catch (Exception e) {
            System.err.println("load file err:database.properties");
            e.printStackTrace();
        }
        
        finally {
            try {
                if (in != null) { in.close(); }
            } catch (Exception e) {}
        }
        return initProps;
    }
}