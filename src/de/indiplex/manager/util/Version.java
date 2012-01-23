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

import java.io.Serializable;

/**
 *
 * @author IndiPlex <kahabrakh@indiplex.de>
 */
public class Version implements Serializable {

    private final int stable;
    private final int beta;
    private final int alpha;
    
    public static final Version UNKNOWN = new Version(-1, -1, -1);

    private Version(int stable, int beta, int alpha) {
        this.stable = stable;
        this.beta = beta;
        this.alpha = alpha;
    }

    public static Version parse(String version) {
        String[] versions = version.split("\\.");
        if (versions.length != 3 || version.equals("?")) {
            return Version.UNKNOWN;
        }
        try {
            int stable = Integer.parseInt(versions[0]);
            int beta = Integer.parseInt(versions[1]);
            int alpha = Integer.parseInt(versions[2]);
            
            return new Version(stable, beta, alpha);
        } catch(Exception e) {
            e.printStackTrace();
            return Version.UNKNOWN;
        }
    }

    public int[] getVersion() {
        int[] version = null;
        for (int i = 0; i < 2; i++) {
            version[i] = i == 0 ? this.stable : i == 1 ? this.beta : i == 2 ? this.alpha : null;
        }
        return version;
    }

    public int getAlpha() {
        return alpha;
    }

    public int getBeta() {
        return beta;
    }

    public int getStable() {
        return stable;
    }

    @Override
    public String toString() {
        if (stable==-1) {
            return "?.?.?";
        }
        return stable+"."+beta+"."+alpha;
    }        
    
    public boolean isNewer(int level, Version oVersion) {
        switch(level) {
            case 1:
                return oVersion.getStable()>stable;
            case 2:
                return oVersion.getStable()>stable || oVersion.getBeta()>beta;
            case 3:
                return oVersion.getStable()>stable || oVersion.getBeta()>beta || oVersion.getAlpha()>alpha;
            default:
                return false;
        }
    }
    
}
