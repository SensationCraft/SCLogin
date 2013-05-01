package org.sensationcraft.login.sql;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 *
 * @author s129977
 */
public class SQLite extends Database
{
        
    public SQLite(String prefix, Logger log, File dbfile)
    {
        super(prefix, log);
    }
    
    @Override
    public boolean initialize()
    {
        try
        {
            Class.forName("org.sqlite.JDBC");
            return true;
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
                this.con = DriverManager.getConnection("");
            }
            catch (SQLException ex)
            {
                log("");
            }
        }
        return null;
    }

    @Override
    protected boolean checkTable(String name)
    {
        if(!isReady()) return false;
        // Check
        return true;
    }

    @Override
    protected boolean createTable()
    {
        if(!isReady()) return false;
        return true;
    }

    @Override
    protected ResultSet query(String query)
    {
        if(!isReady()) return null;
        
        ResultSet result = null;
        
        return result;
    }

    @Override
    protected PreparedStatement prepare(String query)
    {
        if(!isReady()) return null;
        PreparedStatement stmt = null;
        return stmt;
    }

}
