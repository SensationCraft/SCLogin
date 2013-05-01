package org.sensationcraft.login.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author s129977
 */
public abstract class Database 
{
    
    protected Connection con;
    
    private final Logger log;
    
    private final String prefix;
    
    protected Database(String prefix, Logger log)
    {
        this.log = log;
        this.prefix = prefix;
    }
    
    protected abstract boolean initialize();
    
    protected abstract Connection connect();
    
    protected boolean isReady()
    {
        if(this.con == null)
        {
            log("Tried to execute a query or to prepare a statement while the connection was not ready.");
            return false;
        }
        return true;
    }
    
    protected abstract boolean checkTable(String name);
    
    protected abstract boolean createTable(String name, Map<String, PropertyList> columns);
    
    protected abstract ResultSet executeQuery(String query);
    
    protected abstract PreparedStatement prepare(String query);
    
    protected void log(String msg, Object...o)
    {
        log.log(Level.SEVERE, prefix+msg, o);
    }
}
