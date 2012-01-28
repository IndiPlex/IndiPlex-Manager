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
    private boolean api;

    public IPMPluginInfo(String name, String uri, String description, Version version, String depends, boolean fupdate, boolean fdownload, boolean api) {
        this.name = name;
        this.uri = uri;
        this.description = description;
        this.version = version;
        this.depends = depends.split(";");
        this.fupdate = fupdate;
        this.fdownload = fdownload;
        this.api = api;
    }

    public boolean isApi() {
        return api;
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IPMPluginInfo)) {
            return false;
        }
        return obj.hashCode()==hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 97 * hash + (this.version != null ? this.version.hashCode() : 0);
        return hash;
    }
    
}
