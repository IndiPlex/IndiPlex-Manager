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
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.UnknownDependencyException;

/**
 *
 * @author IndiPlex <kahabrakh@indiplex.de>
 */
public class Config {

    private Manager IPM;
    public static final Logger log = Logger.getLogger("Minecraft");
    private boolean online = true;
    private int versionDepth;
    private boolean autoUpdate;
    private HashMap<String, Version> versions = new HashMap<String, Version>();
    private StorageHandler stHandler;
    private File pluginFolder;
    private ArrayList<String> requiredAPIs = new ArrayList<String>();
    private boolean saveChangelogs;

    public void init(Manager IPM) {
        this.IPM = IPM;
        pluginFolder = new File(IPM.getDataFolder().getAbsolutePath() + "/plugins");
        readVersions();
        if (versions.get("IndiPlexManager") == null) {
            versions.put("IndiPlexManager", Version.NULL);
        }
        getOptions();
        if (isOnline()) {
            checkOnline();
        }
    }

    private void checkOnline() {
        try {
            URL url = new URL("http://hosting.indiplex.de/plugins/plugins.php");
            URLConnection conn = url.openConnection();
            conn.connect();
        } catch (IOException ex) {
            setOffline(ex);
        }
    }

    public boolean isOnline() {
        return online;
    }

    public void setOffline(Exception ex) {
        log.warning(Manager.pre + "Can't reach API(" + ex.toString() + ")...");
        online = false;
    }

    private void getOptions() {
        FileConfiguration config = IPM.getConfig();

        if (config.getString("options.online") == null) {
            config.set("options.online", true);
        }

        online = config.getBoolean("options.online", true);

        if (config.getString("options.versiondepth") == null) {
            config.set("options.versiondepth", 1);
        }
        versionDepth = config.getInt("options.versiondepth", 1);

        if (config.getString("options.autoupdate") == null) {
            config.set("options.autoupdate", true);
        }
        autoUpdate = config.getBoolean("options.autoupdate", true);

        if (config.getString("options.savechangelogs") == null) {
            config.set("options.savechangelogs", true);
        }
        saveChangelogs = config.getBoolean("options.savechangelogs", true);

        String type = config.getString("options.database.type");
        String db = config.getString("options.database.dbname");
        String user = config.getString("options.database.user");
        String pass = config.getString("options.database.password");
        String host = config.getString("options.database.host");

        if (type == null) {
            config.set("options.database.type", "sqlite");
            type = "sqlite";
        } else if (!type.equalsIgnoreCase("sqlite")) {
            if (user == null) {
                config.set("options.database.user", "minecraft");
                user = "minecraft";
            }
            if (pass == null) {
                config.set("options.database.password", "PW");
                pass = "minecraft";
            }
            if (host == null) {
                config.set("options.database.host", "localhost");
                host = "localhost";
            }
        }
        if (db == null) {
            config.set("options.database.dbname", "IPM.db");
            db = "IPM.db";
        }
        stHandler = new StorageHandler(StorageHandler.Type.valueOf(type.toUpperCase()), db, user, pass, host);
        IPM.saveConfig();
    }

    public void update() {
        FileConfiguration config = IPM.getConfig();
        List<String> keys = new ArrayList<String>();
        for (IPMPluginInfo plugin : IPM.getPluginInfos()) {
            if (plugin.isApi()) {
                if (plugin.isFdownload()) {
                    requiredAPIs.add(plugin.getName());
                }
                continue;
            }
            if (config.getString(plugin.getName() + ".enabled") == null) {
                config.set(plugin.getName() + ".enabled", plugin.isFdownload() || plugin.isFupdate());
            }
            if (plugin.isFdownload()) {
                config.set(plugin.getName() + ".enabled", true);
            }
            if (plugin.isFupdate()) {
                config.set(plugin.getName() + ".autoupdate", true);
            }
            config.set(plugin.getName() + ".version.actual", plugin.getVersion().toString());

            keys.add(plugin.getName());
        }
        for (IPMPluginInfo plugin : IPM.getPluginInfos()) {
            if (config.getBoolean(plugin.getName() + ".enabled", false)) {
                for (String dep : plugin.getDepends()) {
                    config.set(dep + ".enabled", true);
                }
            }
        }
        keys.add("options");
        Set<String> ks = config.getKeys(false);
        ks.removeAll(keys);
        for (String k : ks) {
            config.set(k, null);
        }
        IPM.saveConfig();
    }

    public boolean isSaveChangelogs() {
        return saveChangelogs;
    }

    public HashMap<IPMPluginInfo, Plugin> load() {
        try {
            FileConfiguration config = IPM.getConfig();
            List<String> keys = new ArrayList<String>(config.getKeys(false));
            keys.remove("options");
            keys.addAll(requiredAPIs);
            HashMap<IPMPluginInfo, Plugin> plugs = new HashMap<IPMPluginInfo, Plugin>();
            if (!online) {
                File pluginsFirst = new File(IPM.getDataFolder().getAbsolutePath() + "/pluginsfirst");
                if (pluginsFirst.exists()) {
                    for (String p : pluginsFirst.list()) {
                        Plugin plug = IPM.getServer().getPluginManager().loadPlugin(new File(pluginsFirst, p));
                        if (plug == null) {
                            log.severe(Manager.pre + "PLUGIN " + p + " IS INVALID!!!");
                        }
                        PluginDescriptionFile des = plug.getDescription();
                        IPMPluginInfo info = new IPMPluginInfo(des.getName(), "", des.getDescription(), Version.parse(des.getVersion() + ".0.0"), "", false, false, false);
                        plugs.put(info, plug);
                    }
                }
                if (pluginFolder.exists()) {
                    for (String p : pluginFolder.list()) {
                        Plugin plug = IPM.getServer().getPluginManager().loadPlugin(new File(pluginFolder, p));
                        if (plug == null) {
                            log.severe(Manager.pre + "PLUGIN " + p + " IS INVALID!!!");
                        }
                        PluginDescriptionFile des = plug.getDescription();
                        IPMPluginInfo info = new IPMPluginInfo(des.getName(), "", des.getDescription(), Version.parse(des.getVersion() + ".0.0"), "", false, false, false);
                        plugs.put(info, plug);
                    }
                }
                return plugs;
            }
            for (String s : keys) {
                IPMPluginInfo info = IPM.getPluginInfoByPluginName(s);
                if (info == null || (!info.isFdownload() && !config.getBoolean(s + ".enabled", false))) {
                    continue;
                }
                Plugin plug = getPlugin(info);
                if (plug != null) {
                    plugs.put(info, plug);
                }
            }
            return plugs;
        } catch (Exception e) {
            e.printStackTrace();
            log.warning(Manager.pre + e.toString());
            return null;
        }
    }

    public Plugin getPlugin(IPMPluginInfo info) throws InvalidPluginException, InvalidDescriptionException, UnknownDependencyException {
        FileConfiguration config = IPM.getConfig();
        File pluginFile = new File(pluginFolder, info.getName() + ".jar");
        Version actual_version = null;

        actual_version = info.getVersion();
        Version installed_version = versions.get(info.getName());
        if (actual_version == null) {
            log.warning(Manager.pre + "Can't parse version of plugin " + info.getName() + "(version:" + info.getVersion() + ")");
        }

        boolean auto_update = autoUpdate;
        if (info.isFupdate()) {
            auto_update = true;
        } else if (config.getString(info.getName() + ".autoupdate") != null) {
            auto_update = config.getBoolean(info.getName() + ".autoupdate", true);
        }
        int vDep = versionDepth;
        if (config.getString(info.getName() + ".versiondepth") != null) {
            vDep = config.getInt(info.getName() + ".versiondepth", versionDepth);
        }
        if (installed_version == null || actual_version == null || (auto_update && installed_version.isNewer(vDep, actual_version))) {
            IPM.downloadPlugin(info);
            log.info(Manager.pre + info.getName() + " was updated to v" + actual_version);
        } else if (installed_version.isNewer(vDep, actual_version)) {
            log.info(Manager.pre + "New version of " + info.getName() + " found(v" + installed_version + " is installed, v" + actual_version + " is the actual), but autoupdate is off...");
        } else if (!pluginFile.exists()) {
            log.info(Manager.pre + info.getName() + " is missing");
            IPM.downloadPlugin(info);
        }
        Plugin plug = IPM.getServer().getPluginManager().loadPlugin(pluginFile);
        if (plug == null) {
            log.severe(Manager.pre + " CANT LOAD PLUGIN " + info.getName() + "!!!");
        }
        return plug;
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

    public void setVersion(String p, Version v) {
        versions.put(p, v);
    }

    private void readVersions() {
        try {
            File vers = new File(IPM.getDataFolder(), "versions");
            if (!vers.getParentFile().exists()) {
                vers.getParentFile().mkdirs();
            }
            if (!vers.exists()) {
                vers.createNewFile();
            }

            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(vers));
                int l = ois.readInt();
                for (int i = 0; i < l; i++) {
                    try {
                        String key = ois.readUTF();
                        key = Crypter.decode(key);
                        int s = ois.readInt();
                        int b = ois.readInt();
                        int a = ois.readInt();
                        Version v = new Version(s, b, a);
                        versions.put(key, v);
                    } catch (Exception e) {
                    }
                }
                ois.close();
            } catch (EOFException e) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveVersions() {
        try {
            File vers = new File(IPM.getDataFolder(), "versions");
            if (!vers.getParentFile().exists()) {
                vers.getParentFile().mkdirs();
            }
            if (!vers.exists()) {
                vers.createNewFile();
            }

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(vers));
            oos.writeInt(versions.size());
            for (String p : versions.keySet()) {
                try {
                    Version v = versions.get(p);
                    if (v != null) {
                        oos.writeUTF(Crypter.encode(p));
                        oos.writeInt(v.getStable());
                        oos.writeInt(v.getBeta());
                        oos.writeInt(v.getAlpha());
                    }
                } catch (Exception e) {
                }
            }
            oos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Version getVersion(String p) {
        return versions.get(p);
    }
}
