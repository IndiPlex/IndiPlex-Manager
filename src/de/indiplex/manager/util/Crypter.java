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

/**
 *
 * @author IndiPlex <Cartan12@indiplex.de>
 */
public class Crypter {

    public static String encode(String s) {
        String result = new String();
        int laenge = s.length();
        char tmp;
        for (int i = 1; i <= laenge; i++) {
            tmp = new Character(s.charAt(i - 1));
            tmp += 3;
            result += tmp;
        }
        char[] tokens = result.toCharArray();
        for (int vtausch = 0; vtausch < laenge - 1; vtausch += 2) {
            tmp = tokens[vtausch];
            tokens[vtausch] = tokens[vtausch + 1];
            tokens[vtausch + 1] = tmp;
        }
        result = "";
        for (char etwas : tokens) {
            result += etwas;
        }

        return result.trim();
    }

    public static String decode(String s) {
        String result = new String();
        int laenge = s.length();
        char tmp;
        for (int i = 1; i <= laenge; i++) {
            tmp = new Character(s.charAt(i - 1));
            tmp -= 3;
            result += tmp;
        }
        char[] tokens = result.toCharArray();
        for (int vtausch = 0; vtausch < laenge - 1; vtausch += 2) {
            tmp = tokens[vtausch];
            tokens[vtausch] = tokens[vtausch + 1];
            tokens[vtausch + 1] = tmp;
        }
        result = "";
        for (char etwas : tokens) {
            result += etwas;
        }

        return result.trim();

    }
}
