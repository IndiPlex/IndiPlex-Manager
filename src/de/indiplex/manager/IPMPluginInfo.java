package de.indiplex.manager;

import de.indiplex.manager.util.Version;
import java.io.Serializable;

/**
 *
 * @author Cartan12
 */
public class IPMPluginInfo implements Serializable {
    
    private String name;
    private String uri;
    private String description;
    private Version version;
    private String[] depends; 
    private boolean fupdate;
    private boolean fdownload;

    public IPMPluginInfo(String name, String uri, String description, Version version, String depends, boolean fupdate, boolean fdownload) {
        this.name = name;
        this.uri = uri;
        this.description = description;
        this.version = version;
        this.depends = depends.split(";");
        this.fupdate = fupdate;
        this.fdownload = fdownload;
    }

    public String[] getDepends() {
        return depends;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public Version getVersion() {
        return version;
    }

    public boolean isFdownload() {
        return fdownload;
    }

    public boolean isFupdate() {
        return fupdate;
    }
    
    @Override
    public String toString() {
        return "[" + "name=" + name + ", uri=" + uri + ", description=" + description + ", version=" + version + ']';
    }        
    
}
