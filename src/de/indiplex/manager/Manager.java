package de.indiplex.manager;

import de.indiplex.manager.util.Config;
import de.indiplex.manager.util.Version;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author IndiPlex
 */
public class Manager extends JavaPlugin {

    public static final Logger log = Logger.getLogger("Minecraft");
    public static final String pre = "[IPM] ";
    private static boolean loaded;
    private ArrayList<IPMPluginInfo> pluginInfos = new ArrayList<IPMPluginInfo>();
    private HashMap<String, API> apis = new HashMap<String, API>();
    private Config config = new Config();

    public HashMap<String, API> getApis() {
        return apis;
    }

    public ArrayList<IPMPluginInfo> getPluginInfos() {
        return pluginInfos;
    }
    
    public Config getIPMConfig() {
        return config;
    }
    
    @Override
    public void onDisable() {
        Plugin[] plugs = getServer().getPluginManager().getPlugins();
        for (Plugin p : plugs) {
            if (p instanceof IPMPlugin) {
                getServer().getPluginManager().disablePlugin(p);
            }
        }
        loaded = false;
        config.save();
    }

    @Override
    public void onEnable() {
        log.info(pre + "Setting up API...");

        config.init(this);                

        if(config.online) {
            log.info(pre + "Reading Online API..");
            loadPluginXML();
            log.info(pre + "Creating descriptions...");
            createDescriptions();
            log.info(pre + "Updating config...");
            config.update();
        } else {
            log.warning(pre + "You are in offline mode!");
            log.warning(pre + "The dependency management is deactivated. It could happen, that the plugins can not load, so place the dependencies in the folder /plugins/IndiPlexManager/pluginsfirst");
        }
        log.info(pre + "Loading config...");
        ArrayList<Plugin> plugs = config.load();
        if(config.online) {
            log.info(pre + "Loading Plugins...");
        }
        loadPlugins(plugs);

        log.info(pre + "Finished!");
        loaded = true;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    private void loadPluginXML() {
        try {
            pluginInfos.clear();
            String b[] = getServer().getVersion().split("b");
            String bversion = b[b.length - 1].split("j")[0];
            String xmlDocURL = "http://hosting.indiplex.de/plugins/plugins.php?version=" + bversion;
            Document D = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlDocURL);
            Element mainNode = D.getDocumentElement();
            NodeList pluginsNodes = mainNode.getChildNodes();

            for (int i = 0; i < pluginsNodes.getLength(); i++) {
                Node node = pluginsNodes.item(i);
                
                IPMPluginInfo plugin = null;
                String name = null;
                String uri = null;
                String description = null;
                Version version = null;
                String depends = null;
                boolean fupdate = false;
                boolean fdownload = false;
                
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element plug = (Element) node;
                NodeList pluginNodes = plug.getChildNodes();
                for (int j = 0; j < pluginNodes.getLength(); j++) {
                    Node pluginNode = pluginNodes.item(j);
                    if (pluginNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }
                    Element ele = (Element) pluginNode;
                    if (ele.getNodeName().equals("name")) {
                        name = ele.getTextContent();
                    } else if (ele.getNodeName().equals("uri")) {
                        uri = ele.getTextContent();
                    } else if (ele.getNodeName().equals("description")) {
                        description = ele.getTextContent();
                    } else if (ele.getNodeName().equals("version")) {
                        version = Version.parse(ele.getTextContent());
                    } else if (ele.getNodeName().equals("depends")) {
                        depends = ele.getTextContent();
                    } else if (ele.getNodeName().equals("fupdate")) {
                        fupdate = !ele.getTextContent().equals("0");
                    } else if (ele.getNodeName().equals("fdownload")) {
                        fdownload = !ele.getTextContent().equals("0");
                    }
                }
                plugin = new IPMPluginInfo(name, uri, description, version, depends, fupdate, fdownload);
                pluginInfos.add(plugin);
            }

            for (IPMPluginInfo plugin : pluginInfos) {
                log.info(pre + "Found plugin " + plugin.getName());
            }
        } catch (Exception ex) {
            config.online = false;
            log.warning(pre + "Can't reach API(" + ex.toString() + ")...");
        }
    }

    private void createDescriptions() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        File desFile = new File(getDataFolder(), "descriptions.txt");
        if (desFile.exists()) {
            desFile.delete();
        }
        try {
            desFile.createNewFile();
            PrintWriter pw = new PrintWriter(desFile);
            for (int i = 0; i < pluginInfos.size(); i++) {
                IPMPluginInfo plugin = pluginInfos.get(i);
                pw.println("################################");
                pw.println(plugin.getName() + " Version " + plugin.getVersion());
                pw.println("################################");
                pw.println(plugin.getDescription());
                if (i < pluginInfos.size() - 1) {
                    pw.println();
                    pw.println();
                }
            }
            pw.close();
        } catch (IOException ex) {
            log.warning(pre + "Can't create descriptions file!");
        }
    }

    private void loadPlugins(ArrayList<Plugin> plugs) {
        ArrayList<Plugin> queuePlugins = new ArrayList<Plugin>();
        if (config.online) {
            for (Plugin p : plugs) {
                loadPlugin(plugs, queuePlugins, p);
            }
        } else {
            queuePlugins = plugs;
        }
        getConfiguration().load();
        for (Plugin p : queuePlugins) {
            if (!(p instanceof IPMPlugin)) {
                log.warning(pre + "Can't load " + p.getDescription().getName() + "!");
                continue;
            }
            IPMPlugin plug = (IPMPlugin) p;
            IPMPluginInfo info = getPluginInfoByPluginName(plug.getDescription().getName());
            if (!config.online) {
                PluginDescriptionFile des = p.getDescription();
                info = new IPMPluginInfo(des.getName(), "", des.getDescription(), Version.parse(des.getVersion() + ".0.0"), "", false, false);
            }
            plug.init(info, new IPMAPI(this, plug));
            plug.onLoad();
        }
        getConfiguration().save();
        for (Plugin p : queuePlugins) {
            getServer().getPluginManager().enablePlugin(p);
            log.info(pre + "Loaded " + p.getDescription().getName());
        }
    }

    private void loadPlugin(ArrayList<Plugin> plugs, ArrayList<Plugin> queuePlugins, Plugin plugin) {
        IPMPluginInfo ipmPlug = getPluginInfoByPluginName(plugin.getDescription().getName().replaceAll(" ", ""));
        if (ipmPlug == null) {
            log.severe(pre + "CAN'T FIND PLUGIN " + plugin.getDescription().getName().replaceAll(" ", "") + "!!!");
        }
        for (String s : ipmPlug.getDepends()) {
            if (s.equals("")) {
                continue;
            }
            Plugin p = getBukkitPluginByPluginName(plugs, s);
            if (p == null) {
                log.severe(pre + "PLUGIN " + s + " INVALID!!!");
            }
            if (!queuePlugins.contains(p)) {
                loadPlugin(plugs, queuePlugins, p);
            }
        }
        if (!queuePlugins.contains(plugin)) {
            queuePlugins.add(plugin);
        }

    }

    private Plugin getBukkitPluginByPluginName(List<Plugin> plugs, String name) {
        for (Plugin plugin : plugs) {
            if (plugin.getDescription().getName().equals(name)) {
                return plugin;
            }
        }
        return null;
    }

    private boolean downloadPlugin(String name) {
        File pluginFolder = new File(getDataFolder().getAbsolutePath() + "/plugins");
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }
        IPMPluginInfo info = getPluginInfoByPluginName(name);
        String uri = info.getUri();
        uri += "/" + info.getVersion().toString() + ".jar";
        log.info(pre + "Downloading: " + uri);
        try {
            FileOutputStream fos = new FileOutputStream(new File(pluginFolder, name + ".jar"));
            InputStream is = new URI(uri).toURL().openStream();

            int t = is.read();
            while (t != -1) {
                fos.write(t);
                t = is.read();
            }
            fos.close();
            is.close();
            getConfiguration().setProperty(info.getName() + ".version.installed", info.getVersion().toString());
            getConfiguration().save();
            return true;
        } catch (Exception e) {
            log.info(e.toString());
            return false;
        }
    }
    
    public boolean enqueueDownloadPlugin(String name) {
        return downloadPlugin(name);
    }

    public IPMPluginInfo getPluginInfoByPluginName(String name) {
        name = name.replaceAll(" ", "");
        for (IPMPluginInfo plugin : pluginInfos) {
            if (plugin.getName().equals(name)) {
                return plugin;
            }
        }
        return null;
    }
}