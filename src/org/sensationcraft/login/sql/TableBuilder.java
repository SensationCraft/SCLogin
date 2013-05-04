package org.sensationcraft.login.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author DarkSeraphim
 */
public class TableBuilder 
{
    private final String name;
    
    private final Map<String, PropertyList> columns = new HashMap<String, PropertyList>();
    
    private String pkey;
    
    public TableBuilder(String name)
    {
        this.name = name;
    }
    
    public PropertyList addColumn(String name, String type)
    {
        PropertyList list = new PropertyList(type);
        this.columns.put(name, list);
        return list;
    }
    
    public void setPrimaryKey(String field)
    {
        if(this.columns.get(field) == null)
        {
            // This might need some cleaning up, like an actual Logger reference
            System.out.println(String.format("Field '%s' does not exist in table '%s'", field, this.name));
            return;
        }
        this.pkey = field;
    }
    
    public Map<String, PropertyList> getColumns()
    {
        return this.columns;
    }
    
    public String getTableName()
    {
        return this.name;
    }
    
    public boolean createTable(Database db)
    {
        return db.createTable(getTableName(), getColumns());
    }
    
}
