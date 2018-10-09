package com.huic.core.db;

import java.io.Serializable;
import java.sql.*;

import org.apache.commons.logging.*;
import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;

/**
 * @author jason
 *
 */
public class OracleSysGUID implements IdentifierGenerator {
    
    private Log log=LogFactory.getLog(OracleSysGUID.class);

    /**
     * 
     */
    public OracleSysGUID() {
        super();
        // TODO
    }

    /* 
     * @see org.hibernate.id.IdentifierGenerator#generate(org.hibernate.engine.SessionImplementor, java.lang.Object)
     */
    public Serializable generate(SessionImplementor session, Object arg1) throws HibernateException {
		PreparedStatement st = null;
		Serializable result=new String("");
		try {
		    st=session.getBatcher().prepareStatement("select sys_guid() from dual");
			ResultSet rs = st.executeQuery();
			
			try {
				rs.next();
				result = new String(rs.getString(1));
			}
			finally {
				rs.close();
			}
			if (log.isDebugEnabled())
				log.debug("Sequence identifier generated: " + result);
			return result;
		}
		catch (SQLException sqle) {
			log.error(sqle);
			sqle.printStackTrace();
		}
		finally {
			try {
                session.getBatcher().closeStatement(st);
            } catch (SQLException e) {
                e.printStackTrace();
            }
		}
		
		return result;

    }

    
    public static void main(String[] args) {
    }
}
