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
    
    public Map<String, PropertyList> getColumns()
    {
        return this.columns;
    }
    
}
