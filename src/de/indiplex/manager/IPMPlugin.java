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

import de.indiplex.manager.util.Version;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 * @author IndiPlex <Cartan12@indiplex.de>
 */
public abstract class IPMPlugin extends JavaPlugin {
    
    private IPMPluginInfo info;
    private static IPMAPI API;
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
