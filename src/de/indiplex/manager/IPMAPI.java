/*
 * IndiPlexManager
 * Copyright (C) 2011 IndiPlex
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.indiplex.manager;

import de.indiplex.manager.util.Config;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author IndiPlex <kahabrakh@indiplex.de>
 */
public class IPMAPI {
    
    private IPMPluginInfo plugin;
    private Manager IPM;   
    private Config config;
    
    /**
     * Creates a new instance of the IPMAPI
     * @param IPM Assigns the instance of the IndiPlex-Manager
     * @param plugin Assigns the instance of the plugin to the IPMAPI 
     */
    public IPMAPI(Manager IPM, IPMPluginInfo plugin) {
        this.IPM = IPM;
        this.plugin = plugin;
        this.config = IPM.getIPMConfig();
    }
    
    /**
     * Gets the folder where you can save your things
     * @return File Your data folder
     */
    public File getDataFolder() {
        return getDataFolder(plugin);
    }
    
    private File getDataFolder(IPMPluginInfo plugin) {
        File f = new File(IPM.getDataFolder(), "/config/"+plugin.getName());
        if (!f.exists()) f.mkdirs();
        return f;
    }
    
    /**
     * Gets your plugins configuration
     * @return YamlConfiguration Your config
     */
    public YamlConfiguration getConfig() {
        return getConfig(plugin);
    }
    
    private YamlConfiguration getConfig(IPMPluginInfo plugin) {
        File f = new File(IPM.getDataFolder(), "/config/"+plugin.getName()+".yml");
        Manager.log.info(Manager.pre+"Reqested config for "+plugin.getName()+" file "+f);
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            if (!f.exists()) f.createNewFile();
            yaml.load(f);
        } catch (Exception ex) {
            yaml = null;
        }
        return yaml;
    }
    
    /**
     * Saves the config
     * @param yaml YamlConfiguration to save
     * @return boolean True for success otherwise false
     */
    public boolean saveConfig(YamlConfiguration yaml) {
        File f = new File(IPM.getDataFolder(), "/config/"+plugin.getName()+".yml");
        try {
            yaml.save(f);
            Manager.log.info(Manager.pre+"Saved config of "+plugin.getName()+" to "+f);
        } catch (IOException ex) {
            return false;
        }
        return true;
    }
    
    /**
     * Registers your API under your plugins name
     * @param api Custom API to assign
     */
    public void registerAPI(API api) {
        registerAPI(plugin.getName(), api);
    }
    
    /**
     * Registers your API and assigns your custom name
     * @param alias Name under which you API will be assigned
     * @param api Custom API to assign
     */
    public void registerAPI(String alias, API api) {
        IPM.getApis().put(alias, api);
    }
    
    /**
     * Get API of registered plugins
     * @param pName Name of the plugin to search for
     * @return API Returns the API, if plugin not found returns null
     */
    public API getAPI(String pName) {
        return IPM.getApis().get(pName);
    }        
    
    /**
     * Add one item to the plugins database
     * @param category Category to find the item
     * @param key Key to find the item
     * @param value Value to assign to the key and category
     * @return boolean True for success otherwise false
     */
    public boolean putData(String category, String key, Object value) {
        return config.getStHandler().put(plugin.getName(), category, key, value);
    }
    
    /**
     * Remove one item out of the database
     * @param category Category to look for
     * @param key Key to look for
     * @return boolean True for success otherwise false
     */
    public boolean removeData(String category, String key) {
        return config.getStHandler().remove(plugin.getName(), category, key);
    }
    
    public String test() {
        return plugin.getName();
    }
    
    /**
     * Get one item out of the database
     * @param category Category to look for
     * @param key Key to look for
     * @return Object Return the object specified by the category and key
     */
    public Object getData(String category, String key) {
        return config.getStHandler().get(plugin.getName(), category, key);
    }
    
    /**
     * Pointless ^^ Returns argument
     * @param value The value to return
     * @return value Returns the parameter value
     */
    public Object getData(Object value) {
        return value;
    }
    
    /**
     * Get the IndiPlex-Manager database connection
     * @return Connection The database connection 
     */
    public Connection getDBConnection() {
        return config.getStHandler().getConnection();
    }
    
}
