package org.sensationcraft.login.sql;

import java.io.File;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.logging.Logger;

public class H2 extends Database
{

	private enum Type
	{
		INSERT("INSERT"),
		UPDATE("UPDATE"),
		DELETE("DELETE"),
		SELECT("SELECT"),
		CREATE("CREATE"),
		NONE("");

		private final String sql;

		private Type(String sql)
		{
			this.sql = sql;
		}

		public String getSQL()
		{
			return this.sql;
		}

		public static H2.Type getType(String keyword)
		{
			for(H2.Type t : values())
			{
				if(t.getSQL().equalsIgnoreCase(keyword)) return t;
			}
			return H2.Type.NONE;
		}
	}

	private File parent;
        
        private final String dbname;
        
        private final String user;
        
        private final String pass;

        public H2(Logger log, File parent, String dbname, String username)
        {
            this(log, parent, dbname, username, "");
        }
        
	public H2(Logger log, File parent, String dbname, String username, String password)
	{
		super(log);
		this.parent = parent;
                this.dbname = dbname;
                this.user = username;
                this.pass = password;
		if(!this.parent.exists())
		{
                    try
                    {
                            if(!this.parent.mkdirs())
                                    throw new IOException("Failed to create file");
                    }
                    catch(IOException ex)
                    {
                            this.log("Failed to find (and create) parent directory at %s", this.parent.getPath());
                            this.parent = null;
                    }
		}
	}

	@Override
	public boolean initialize()
	{
		try
		{
			Class.forName("org.h2.Driver");
			return this.parent != null && this.parent.exists();
		}
		catch(ClassNotFoundException ex)
		{
			this.log("H2 library not found!");
			return false;
		}
	}

	@Override
	public boolean connect()
	{
		if(this.initialize())
		{
			try
			{
				this.con = DriverManager.getConnection(String.format("jdbc:h2:%s%s%s;MODE=MySQL;IGNORECASE=TRUE",this.parent,File.separator,this.dbname), this.user, this.pass);
			}
			catch (SQLException ex)
			{
                                ex.printStackTrace();
				this.log("Failed to establish a H2 connection, SQLException: ", ex.getMessage());
			}
		}
		return this.con != null;
	}

	@Override
	public boolean checkTable(String name)
	{
		if(!this.isReady()) return false;
		try
		{
			DatabaseMetaData meta = this.con.getMetaData();
			ResultSet result = meta.getTables(null, null, name, null);
			return result.next();
		}
		catch(SQLException ex)
		{
			// Swallow the exception, as it is a conditional
			//log("Table %s does not exist", name);
		}
		// Check
		return false;
	}

	@Override
	public void createTable(TableBuilder builder)
	{
		if(!this.isReady()) return;

		StringBuilder table = new StringBuilder("CREATE TABLE `").append(builder.getTableName()).append("`(");
		for(Map.Entry<String, PropertyList> property : builder.getColumns().entrySet())
		{
			table.append(property.getKey()).append(" ").append(property.getValue().getProperties()).append(",");
		}
                
                String pkey = builder.getPrimaryKey();
                if(pkey != null)
                    table.append(String.format("PRIMARY KEY(%s),", pkey));

                for(Map.Entry<String, Reference> reference : builder.getReferences().entrySet())
                {
                    if(reference.getValue() != null)
                    table.append(String.format("FOREIGN KEY %s REFERENCES `%s`(%s),", reference.getKey(), reference.getValue().getTable(), reference.getValue().getColumn()));
                }
                
                // Delete the last comma
		if(builder.getColumns().size() > 0) {
			table.deleteCharAt(table.length() - 1);
		}

		String query = table.append(");").toString();

		this.executeQuery(query);
	}

	@Override
	public ResultSet executeQuery(String query)
	{
		if(!this.isReady()) return null;

		ResultSet result = null;

		try
		{
			Statement stmt = this.con.createStatement();
			switch(this.getQueryType(query))
			{
			case INSERT:
			case UPDATE:
			case DELETE:
				stmt.executeUpdate(query);
				break;
			case CREATE:
				break;
			default:
				result = stmt.executeQuery(query);
				break;
			}
		}
		catch(SQLException ex)
		{
			this.log("An exception has occurred while executing query '%s': %s", query, ex.getMessage());
		}

		return result;
	}

	@Override
	public PreparedStatement prepare(String query)
	{
		if(!this.isReady()) return null;
		PreparedStatement stmt = null;
		try
		{
			stmt = this.con.prepareStatement(query);
		}
		catch(SQLException ex)
		{
			this.log("An exception has occurred while preparing query '%s': %s", query, ex.getMessage());
		}
		return stmt;
	}

	private H2.Type getQueryType(String query)
	{
		String typename = query.split(" ")[0];
		return H2.Type.getType(typename);
	}

}
