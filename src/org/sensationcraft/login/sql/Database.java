package org.sensationcraft.login.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Database
{

	protected Connection con;

	private final Logger log;

	protected Database(Logger log)
	{
		this.log = log;
	}

	public abstract boolean initialize();

	public abstract boolean connect();
        
        public void close()
        {
            try
            {
                this.con.close();
                this.con = null;
            }
            catch(SQLException ex)
            {
                log("An exception occurred while closing the connection: %s", ex.getMessage());
            }
        }

	public boolean isReady()
	{
		if(this.con == null)
		{
			this.log("Tried to execute a query or to prepare a statement while the connection was not ready.");
			return false;
		}
		return true;
	}

	public abstract boolean checkTable(String name);

	public abstract boolean createTable(String name, Map<String, PropertyList> columns);

	public abstract ResultSet executeQuery(String query);

	public abstract PreparedStatement prepare(String query);

	protected void log(String msg, Object...o)
	{
		this.log.log(Level.SEVERE, String.format(msg, o));
	}
}
