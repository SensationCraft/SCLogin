package org.sensationcraft.login.sql;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author s129977
 */
public class SQLite extends Database
{
        
    private File dbfile;
    
    public SQLite(String prefix, Logger log, File dbfile)
    {
        super(prefix, log);
        this.dbfile = dbfile;
        if(!this.dbfile.exists())
        {
            try
            {
                if(!this.dbfile.createNewFile())
                {
                    throw new IOException("Failed to create file");
                }
            }
            catch(IOException ex)
            {
                log("Failed to find (and create) file at %s", this.dbfile.getPath());
                this.dbfile = null;
            }
        }
    }
    
    @Override
    public boolean initialize()
    {
        try
        {
            Class.forName("org.sqlite.JDBC");
            return this.dbfile != null && this.dbfile.exists();
        }
        catch(ClassNotFoundException ex)
        {
            log("SQLite library not found!");
            return false;
        }
    }
    
    @Override
    public Connection connect()
    {
        if(initialize())
        {
            try
            {
                this.con = DriverManager.getConnection("jdbc:sqlite:" + this.dbfile.getAbsolutePath());
            }
            catch (SQLException ex)
            {
                log("Failed to establish a SQLite connection, SQLException: ", ex.getMessage());
            }
        }
        return this.con;
    }

    @Override
    protected boolean checkTable(String name)
    {
        if(!isReady()) return false;
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
    protected boolean createTable(String name, Map<String, PropertyList> columns)
    {
        if(!isReady()) return false;
        
        StringBuilder table = new StringBuilder("CREATE TABLE `").append(name).append("`(");
        for(Map.Entry<String, PropertyList> property : columns.entrySet())
        {
            table.append(property.getKey()).append(" ").append(property.getValue().getProperties()).append(",");
        }
        
        // Delete the last comma
        if(columns.size() > 0) table.deleteCharAt(table.length() - 1);
        
        String query = table.append(");").toString();
        
        ResultSet result = executeQuery(query);
        
        boolean returns = false;
        try
        {
            returns = result != null && result.next();
        }
        catch(SQLException ex)
        {
            // Swallow the exception
        }
        
        return returns;
    }

    @Override
    protected ResultSet executeQuery(String query)
    {
        if(!isReady()) return null;
        
        ResultSet result = null;
        
        try
        {
            Statement stmt = this.con.createStatement();
            result = stmt.executeQuery(query);
        }
        catch(SQLException ex)
        {
            log("An exception has occurred while executing query '%s': %s", query, ex.getMessage());
        }
        
        return result;
    }

    @Override
    protected PreparedStatement prepare(String query)
    {
        if(!isReady()) return null;
        PreparedStatement stmt = null;
        try
        {
            stmt = con.prepareStatement(query);
        }
        catch(SQLException ex)
        {
            log("An exception has occurred while preparing query '%s': %s", query, ex.getMessage());
        }
        return stmt;
    }

}
