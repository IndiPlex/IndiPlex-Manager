package de.indiplex.manager;

import de.indiplex.manager.util.Version;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author IndiPlex
 */
public abstract class IPMPlugin extends JavaPlugin {
    
    private IPMPluginInfo info;
    private static IPMAPI API; //TODO: Maybe the API should be static and every class in the plugin can use: (e.g.) IPMAPI api = MyIPMPlugin.getIPMAPI();
    protected boolean online = true;
    public static final Logger log = Logger.getLogger("Minecraft");

    void init(IPMPluginInfo info, IPMAPI API) {
        this.info = info;
        checkInfo();
        IPMPlugin.API = API;
    }
    
    private void checkInfo() {
        if (info==null) {
            online = false;
            info = new IPMPluginInfo(getDescription().getFullName(), "", getDescription().getDescription(), Version.UNKNOWN, "", false, false);
            log.warning(Manager.pre+getDescription().getFullName()+" is not in the IndiPlex Manager plugins dir!");
        }
    }
    
    public static IPMAPI getAPI() {
        return API;
    }
    
    public String getDes() {
        return info.getDescription();
    }

    public String getName() {
        return info.getName();
    }

    public String getUri() {
        return info.getUri();
    }

    public Version getVersion() {
        return info.getVersion();
    }

    public String[] getDepends() {
        return info.getDepends();
    }

    @Override
    public String toString() {
        return "IPMPlugin [" + "description " + getDes() + " " + "name " + getName() + " " + "uri " + getName() + " " + "version " + getVersion() + "]";
    }
    
    protected void printEnabled(String pre) {
        checkInfo();
        log.info(pre + getName()+ " v"+ getVersion() + " was enabled!");
    }
    
    protected void printDisabled(String pre) {
        checkInfo();
        log.info(pre + getName()+ " v"+ getVersion() + " was disabled!");
    }
}
