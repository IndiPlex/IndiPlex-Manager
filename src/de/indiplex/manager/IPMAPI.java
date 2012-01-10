package de.indiplex.manager;

import de.indiplex.manager.util.Config;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author IndiPlex
 */
public class IPMAPI {
    
    private Plugin plugin;
    private Manager IPM;   
    private Config config;
    
    public IPMAPI(Manager IPM, Plugin plugin) {
        this.IPM = IPM;
        this.plugin = plugin;
        this.config = IPM.getIPMConfig();
    }
    
    public File getDataFolder() {
        return getDataFolder(plugin);
    }
    
    private File getDataFolder(Plugin plugin) {
        File f = new File(IPM.getDataFolder(), "/config/"+plugin.getDescription().getName());
        if (!f.exists()) f.mkdirs();
        return f;
    }
    
    public YamlConfiguration getConfig() {
        return getConfig(plugin);
    }
    
    private YamlConfiguration getConfig(Plugin plugin) {
        File f = new File(IPM.getDataFolder(), "/config/"+plugin.getDescription().getName()+".yml");
        //File f = new File("lol.yml");
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            if (!f.exists()) f.createNewFile();
            yaml.load(f);
        } catch (Exception ex) {
            yaml = null;
        }
        return yaml;
    }
    
    public boolean saveConfig(Plugin plugin, YamlConfiguration yaml) {
        File f = new File(IPM.getDataFolder(), "/config/"+plugin.getDescription().getName()+".yml");
        //File f = new File("lol.yml");
        try {
            yaml.save(f);
        } catch (IOException ex) {
            return false;
        }
        return true;
    }
    
    public void registerAPI(API api) {
        registerAPI(plugin.getDescription().getName(), api);
    }
    
    public void registerAPI(String alias, API api) {
        IPM.getApis().put(alias, api);
    }
    
    public API getAPI(String pName) {
        return IPM.getApis().get(pName);
    }        
    
    public boolean putData(String category, String key, Object value) {
        return config.getStHandler().put(plugin.getDescription().getName(), category, key, value);
    }
    
    public boolean removeData(String category, String key) {
        return config.getStHandler().remove(plugin.getDescription().getName(), category, key);
    }
    
    public Object getData(String category, String key) {
        return config.getStHandler().get(plugin.getDescription().getName(), category, key);
    }
    
    /**
     * Pointless ^^
     * @param value 
     * @return value
     */
    public Object getData(Object value) {
        return value;
    }
    
    public Connection getDBConnection() {
        return config.getStHandler().getConnection();
    }
    
}
