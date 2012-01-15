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
package de.indiplex.manager.util;

import de.indiplex.manager.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;

/**
 *
 * @author IndiPlex <kahabrakh@indiplex.de>
 */
public class Config {

    private Manager IPM;
    public static final Logger log = Logger.getLogger("Minecraft");
    public boolean online = true;
    private int versionDepth;
    private boolean autoUpdate;
    private HashMap<String, String> versions = new HashMap<String, String>();
    private StorageHandler stHandler;

    public void init(Manager IPM) {
        this.IPM = IPM;
        readVersions();
        if (versions.get("IndiPlexManager") == null) {
            versions.put("IndiPlexManager", "0.0.0");
        }
        getOptions();
    }

    private void getOptions() {
        Configuration config = IPM.getConfiguration();
        config.load();

        if (config.getString("options.online") == null) {
            config.setProperty("options.online", true);
        }

        online = config.getBoolean("options.online", true);

        if (config.getString("options.versiondepth") == null) {
            config.setProperty("options.versiondepth", 1);
        }
        versionDepth = config.getInt("options.versiondepth", 1);

        if (config.getString("options.autoupdate") == null) {
            config.setProperty("options.autoupdate", true);
        }
        autoUpdate = config.getBoolean("options.autoupdate", true);

        String type = config.getString("options.database.type");
        String db = config.getString("options.database.dbname");
        String user = config.getString("options.database.user");
        String pass = config.getString("options.database.password");
        String host = config.getString("options.database.host");

        if (type == null) {
            config.setProperty("options.database.type", "sqlite");
            type = "sqlite";
        } else if (!type.equalsIgnoreCase("sqlite")) {
            if (user == null) {
                config.setProperty("options.database.user", "minecraft");
                user = "minecraft";
            }
            if (pass == null) {
                config.setProperty("options.database.password", "PW");
                pass = "minecraft";
            }
            if (host == null) {
                config.setProperty("options.database.host", "localhost");
                host = "localhost";
            }
        }
        if (db == null) {
            config.setProperty("options.database.dbname", "IPM.db");
            db = "IPM.db";
        }
        stHandler = new StorageHandler(StorageHandler.Type.valueOf(type.toUpperCase()), db, user, pass, host);
        config.save();
    }

    public void update() {
        Configuration config = IPM.getConfiguration();
        List<String> keys = new ArrayList<String>();
        for (IPMPluginInfo plugin : IPM.getPluginInfos()) {
            if (config.getString(plugin.getName() + ".enabled") == null) {
                config.setProperty(plugin.getName() + ".enabled", plugin.isFdownload() || plugin.isFupdate());
            }
            if (plugin.isFdownload()) {
                config.setProperty(plugin.getName() + ".enabled", true);
            }
            if (plugin.isFupdate()) {
                config.setProperty(plugin.getName() + ".autoupdate", true);
            }
            config.setProperty(plugin.getName() + ".version.actual", plugin.getVersion().toString());

            keys.add(plugin.getName());
        }
        for (IPMPluginInfo plugin : IPM.getPluginInfos()) {
            if (config.getBoolean(plugin.getName() + ".enabled", false)) {
                for (String dep : plugin.getDepends()) {
                    config.setProperty(dep + ".enabled", true);
                }
            }
        }
        keys.add("options");
        List<String> ks = config.getKeys();
        ks.removeAll(keys);
        for (String k : ks) {
            config.removeProperty(k);
        }
        config.save();
    }

    public ArrayList<Plugin> load() {
        try {
            Configuration config = IPM.getConfiguration();
            List<String> keys = new ArrayList<String>(config.getKeys());
            keys.remove("options");
            File pluginFolder = new File(IPM.getDataFolder().getAbsolutePath() + "/plugins");
            ArrayList<Plugin> plugs = new ArrayList<Plugin>();
            if (!online) {
                File pluginsFirst = new File(IPM.getDataFolder().getAbsolutePath() + "/pluginsfirst");
                if (pluginsFirst.exists()) {
                    for (String p : pluginsFirst.list()) {
                        Plugin plug = IPM.getServer().getPluginManager().loadPlugin(new File(pluginsFirst, p));
                        if (plug == null) {
                            log.severe(Manager.pre + "PLUGIN " + p + " IS INVALID!!!");
                        }
                        plugs.add(plug);
                    }
                }
                if (pluginFolder.exists()) {
                    for (String p : pluginFolder.list()) {
                        Plugin plug = IPM.getServer().getPluginManager().loadPlugin(new File(pluginFolder, p));
                        if (plug == null) {
                            log.severe(Manager.pre + "PLUGIN " + p + " IS INVALID!!!");
                        }
                        plugs.add(plug);
                    }
                }
                return plugs;
            }
            for (String s : keys) {
                if (!config.getBoolean(s + ".enabled", false)) {
                    continue;
                }
                IPMPluginInfo info = IPM.getPluginInfoByPluginName(s);
                File pluginFile = new File(pluginFolder, s + ".jar");
                Version installed_version = null;
                Version actual_version = null;

                actual_version = info.getVersion();
                String v = versions.get(info.getName());
                if (v != null) {
                    installed_version = Version.parse(v);
                }
                if (actual_version == null) {
                    log.warning(Manager.pre + "Can't parse version of plugin " + info.getName() + "(version:" + info.getVersion() + ")");
                }

                boolean auto_update = autoUpdate;
                if (config.getString(info.getName() + ".autoupdate") != null) {
                    auto_update = config.getBoolean(info.getName() + ".autoupdate", true);
                }
                int vDep = versionDepth;
                if (config.getString(s + ".versiondepth") != null) {
                    vDep = config.getInt(s + ".versiondepth", versionDepth);
                }
                if (installed_version == null || actual_version == null || (auto_update && installed_version.isNewer(vDep, actual_version))) {
                    IPM.queueDownloadPlugin(s);
                    log.info(Manager.pre + info.getName() + " was updated to v" + actual_version);
                } else if (installed_version.isNewer(vDep, actual_version)) {
                    log.info(Manager.pre + "New version of " + info.getName() + " found(v" + installed_version + " is installed, v" + actual_version + " is the actual), but autoupdate is off...");
                } else if (!pluginFile.exists()) {
                    log.info(Manager.pre + info.getName() + " is missing");
                    IPM.queueDownloadPlugin(s);
                }
                Plugin plug = IPM.getServer().getPluginManager().loadPlugin(pluginFile);
                if (plug == null) {
                    log.severe(Manager.pre + "PLUGIN " + s + " IS INVALID!!!");
                }
                plugs.add(plug);
            }
            return plugs;
        } catch (Exception e) {
            e.printStackTrace();
            log.warning(Manager.pre + e.toString());
            return null;
        }
    }

    public void save() {
        stHandler.save();
    }

    public StorageHandler getStHandler() {
        return stHandler;
    }

    public int getVersionDepth() {
        return versionDepth;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public void setVersion(String p, String v) {
        versions.put(p, v);
    }

    private void readVersions() {
        try {
            File vers = new File(IPM.getDataFolder(), "versions");
            if (!vers.getParentFile().exists()) {
                vers.mkdirs();
            }
            if (!vers.exists()) {
                vers.createNewFile();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(new Base64InputStream(new FileInputStream(vers))));
            while (br.ready()) {
                String line = br.readLine();
                String[] parts = line.split("\\:");
                if (parts.length != 2) {
                    continue;
                }
                versions.put(parts[0], parts[1]);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveVersions() {
        try {
            PrintStream ps = new PrintStream(new Base64OutputStream(new FileOutputStream(new File(IPM.getDataFolder(), "versions"))));
            for (String p : versions.keySet()) {
                ps.println(p + ":" + versions.get(p));
            }
            ps.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public String getVersion(String p) {
        return versions.get(p);
    }
}
