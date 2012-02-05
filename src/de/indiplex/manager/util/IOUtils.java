/*
 * IndiPlexManager
 * Copyright (C) 2012 IndiPlex
 * 
 * IndiPlexManager is free software: you can redistribute it and/or modify
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author IndiPlex <Cartan12@indiplex.de>
 */
public class IOUtils {
    
    public static void copyFile(File from, File to) throws IOException {
        if (!from.exists() || !to.exists()) {
            return;
        }
        copyAndCloseStrams(new FileInputStream(from), new FileOutputStream(to));
    }
    
    public static void copyAndCloseStrams(InputStream in, OutputStream out) throws IOException {
        copy(in, out);
        in.close();
        out.close();
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        int r = in.read();
        while (r != -1) {
            out.write(r);
            r = in.read();
        }
    }
}
