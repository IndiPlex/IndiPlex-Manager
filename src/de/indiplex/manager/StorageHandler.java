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

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 *
 * @author IndiPlex <kahabrakh@indiplex.de>
 */
public class StorageHandler {

    private static final String SQLite_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ipm_storage ("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "plugin varchar(20),"
            + "category varchar(20),"
            + "name varchar(20),"
            + "object BLOB"
            + ")";
    private static final String SQL_INSERT_OBJECTS = "INSERT INTO ipm_storage ("
            + "plugin, category, name, object) VALUES ("
            + "?,?,?,?)";
    private static final String SQL_DELETE_OBJECTS = "DELETE FROM ipm_storage";
    private static final String SQL_READ_OBJECTS = "SELECT * FROM ipm_storage";
    private HashMap<String, HashMap<String, HashMap<String, Object>>> data = new HashMap<String, HashMap<String, HashMap<String, Object>>>();
    private Connection c;
    private Type type;

    public void save() {
        try {
            PreparedStatement stmtSave = c.prepareStatement(SQL_INSERT_OBJECTS);
            Statement stmtDel = c.createStatement();
            stmtDel.executeUpdate(SQL_DELETE_OBJECTS);
            stmtDel.close();
            for (String p : data.keySet()) {
                HashMap<String, HashMap<String, Object>> h1 = data.get(p);
                for (String cat : h1.keySet()) {
                    HashMap<String, Object> h2 = h1.get(cat);
                    for (String name : h2.keySet()) {
                        Object o = h2.get(name);
                        if (!(o instanceof Serializable)) {
                            System.out.println("Can't save " + p + ":" + cat + ":" + name + " because it's not serializable!");
                        } else {
                            stmtSave.setString(1, p);
                            stmtSave.setString(2, cat);
                            stmtSave.setString(3, name);
                            stmtSave.setObject(4, o);
                            stmtSave.addBatch();
                        }
                    }
                }
            }
            stmtSave.executeBatch();
            stmtSave.close();
            c.commit();
            c.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            ex.getNextException().printStackTrace();
        }

    }

    public void load() {
        try {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(SQL_READ_OBJECTS);
            while (rs.next()) {
                String plugin = rs.getString("plugin");
                String cat = rs.getString("category");
                String name = rs.getString("name");
                Object o = rs.getObject("object");
                put(plugin, cat, name, o);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            ex.getNextException().printStackTrace();
        }
    }

    public StorageHandler(Type t, String database, String user, String password, String host) {
        this.type = t;
        String url = "";
        try {
            Class.forName(t.getClassName());
            url = "jdbc:" + t + ":";
            if (t.equals(Type.SQLITE)) {
                url += database;
                c = DriverManager.getConnection(url);
            } else {
                url += "//" + host + "/" + database;
                c = DriverManager.getConnection(url, user, password);
            }
            Statement stmt = c.createStatement();
            int res = stmt.executeUpdate(SQLite_CREATE_TABLE);
            stmt.close();
            c.setAutoCommit(false);
        } catch (SQLException ex) {
            System.out.println(Manager.pre + "Can't load database with url " + url);
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            System.out.println(Manager.pre + "Can't find driver for " + t);
        }
    }

    public boolean put(String plugin, String category, String key, Object value) {
        HashMap<String, HashMap<String, Object>> h1 = data.get(plugin);
        if (h1 == null) {
            HashMap<String, HashMap<String, Object>> ht = new HashMap<String, HashMap<String, Object>>();
            data.put(plugin, ht);
            h1 = ht;
        }
        HashMap<String, Object> h2 = h1.get(category);
        if (h2 == null) {
            HashMap<String, Object> ht = new HashMap<String, Object>();
            h1.put(category, ht);
            h2 = ht;
        }
        Object o = h2.put(key, value);
        return o == null;
    }

    public Object get(String plugin, String category, String key) {
        if (data.get(plugin) == null) {
            return null;
        } else if (data.get(plugin).get(category) == null) {
            return null;
        }
        return data.get(plugin).get(category).get(key);
    }

    public enum Type {

        SQLITE("org.sqlite.JDBC"),
        MYSQL("com.mysql.jdbc.Driver"),
        POSTGRESQL("org.postgresql.Driver");
        String className;

        private Type(String className) {
            this.className = className;
        }

        public String getClassName() {
            return className;
        }
    }

    public Connection getConnection() {
        return c;
    }
    
    public boolean remove(String plugin, String category, String key) {
        HashMap<String, HashMap<String, Object>> h1 = data.get(plugin);
        if (h1==null) {
            return false;
        }
        HashMap<String, Object> h2 = h1.get(category);
        if (h2==null) {
            return false;
        }
        return h2.remove(key)!=null;
    }
    
}
