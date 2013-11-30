/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.home.items.web.servergui;

/*
 Java HTML Tidy - JTidy
 HTML parser and pretty printer

 Copyright (c) 1998-2000 World Wide Web Consortium (Massachusetts
* Institute of Technology, Institut National de Recherche en
* Informatique et en Automatique, Keio University). All Rights
* Reserved.
*
* Contributing Author(s):
*
* Dave Raggett <dsr@w3.org>
* Andy Quick <ac.quick@sympatico.ca> (translation to Java)
* Gary L Peskin <garyp@firstech.com> (Java development)
* Sami Lempinen <sami@lempinen.net> (release management)
* Fabrizio Giustina <fgiust at users.sourceforge.net>
* Vlad Skarzhevskyy <vlads at users.sourceforge.net> (JTidy servlet development)
*
* The contributing author(s) would like to thank all those who
* helped with testing, bug fixes, and patience. This wouldn't
* have been possible without all of you.
*
* COPYRIGHT NOTICE:
*
* This software and documentation is provided "as is," and
* the copyright holders and contributing author(s) make no
* representations or warranties, express or implied, including
* but not limited to, warranties of merchantability or fitness
* for any particular purpose or that the use of the software or
* documentation will not infringe any third party patents,
* copyrights, trademarks or other rights.
*
* The copyright holders and contributing author(s) will not be
* liable for any direct, indirect, special or consequential damages
* arising out of any use of the software or documentation, even if
* advised of the possibility of such damage.
*
* Permission is hereby granted to use, copy, modify, and distribute
* this source code, or portions hereof, documentation and executables,
* for any purpose, without fee, subject to the following restrictions:
*
* 1. The origin of this source code must not be misrepresented.
* 2. Altered versions must be plainly marked as such and must
* not be misrepresented as being the original source.
* 3. This Copyright notice may not be removed or altered from any
* source or altered source distribution.
*
* The copyright holders and contributing author(s) specifically
* permit, without fee, and encourage the use of this source code
* as a component for supporting the Hypertext Markup Language in
* commercial products. If you use this source code in a product,
* acknowledgment is not required but would be appreciated.
*
*/

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;


/**
 * Converts a String to HTML by converting all special characters to HTML-entities.
 *
 * @author Vlad Skarzhevskyy <a href="mailto:skarzhevskyy@gmail.com">skarzhevskyy@gmail.com </a>
 * @version $Revision: 1.5 $ ($Author: vlads $)
 */

public class HTMLEncode {

    /**
     * j2se 1.4 encode method, used by reflection if available.
     */
    private static Method encodeMethod14;

    static {
// URLEncoder.encode(String) has been deprecated in J2SE 1.4.
// Take advantage of the new method URLEncoder.encode(String, enc) if J2SE 1.4 is used.
        try {
            Class urlEncoderClass = Class.forName("java.net.URLEncoder");
            encodeMethod14 = urlEncoderClass.getMethod("encode", new Class[]{String.class, String.class});
// encodeMethod14 will be null if exception
        } catch (ClassNotFoundException e) {
            // Leave as null
        } catch (NoSuchMethodException e) {
            // Leave as null
        }
    }

    /**
     * Utility class, don't instantiate.
     */
    private HTMLEncode() {
// unused
    }

    private static final String[] ENTITIES = {
            ">",
            "&gt;",
            "<",
            "&lt;",
            "&",
            "&amp;",
            "\"",
            "&quot;",
            "'",
            "&#039;",
            "\\",
            "&#092;",
            "\u00a9",
            "&copy;",
            "\u00ae",
            "&reg;"};

    private static Hashtable entityTableEncode = null;

    protected static synchronized void buildEntityTables() {
        entityTableEncode = new Hashtable(ENTITIES.length);

        for (int i = 0; i < ENTITIES.length; i += 2) {
            if (!entityTableEncode.containsKey(ENTITIES[i])) {
                entityTableEncode.put(ENTITIES[i], ENTITIES[i + 1]);
            }
        }
    }

    /**
     * Converts a String to HTML by converting all special characters to HTML-entities.
     */
    public final static String encode(String s) {
        return encode(s, "\n");
    }

    /**
     * Converts a String to HTML by converting all special characters to HTML-entities.
     */
    public final static String encode(String s, String cr) {
        if (entityTableEncode == null) {
            buildEntityTables();
        }
        if (s == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer(s.length() * 2);
        char ch;
        for (int i = 0; i < s.length(); ++i) {
            ch = s.charAt(i);
            if ((ch >= 63 && ch <= 90) || (ch >= 97 && ch <= 122) || (ch == ' ')) {
                sb.append(ch);
            } else if (ch == '\n') {
                sb.append(cr);
            } else {
                String chEnc = encodeSingleChar(String.valueOf(ch));
                if (chEnc != null) {
                    sb.append(chEnc);
                } else {
// Not 7 Bit use the unicode system
                    sb.append("&#");
                    sb.append(new Integer(ch).toString());
                    sb.append(';');
                }
            }
        }
        return sb.toString();
    }

    /**
     * Converts a single character to HTML
     */
    private static String encodeSingleChar(String ch) {
        return (String) entityTableEncode.get(ch);
    }

    /**
     * Converts a String to valid HTML HREF by converting all special characters to HTML-entities.
     *
     * @param url url to be encoded
     * @return encoded url.
     */
    protected static String encodeHREFParam(String url) {
        if (encodeMethod14 != null) {
            Object[] methodArgs = new Object[2];
            methodArgs[0] = url;

            methodArgs[1] = "UTF8";

            try {
                return (String) encodeMethod14.invoke(null, methodArgs);
            } catch (InvocationTargetException e) {
                // Ok, will fall back
            } catch (IllegalAccessException e) {
                // Ok, will fall back
            }
        }

// must use J2SE 1.3 version
        return URLEncoder.encode(url);

    }

    protected static String encodeHREFParamJava13(String value) {
        return URLEncoder.encode(value);
    }

    public static String encodeQuery(String url, String[] args) {
        return encodeHREFQuery(url, args, false);
    }

    public static String encodeHREFQuery(String url, String[] args) {
        return encodeHREFQuery(url, args, true);
    }

    public static String encodeHREFQuery(String url, String[] args, boolean forHtml) {
        StringBuffer out = new StringBuffer(128);
        out.append(url);

        if ((args != null) && (args.length > 0)) {
            out.append("?");
            for (int i = 0; i < (args.length + 1) / 2; i++) {
                int k = i * 2;
                if (k != 0) {
                    if (forHtml) {
                        out.append("&amp;");
                    } else {
                        out.append("&");
                    }
                }
                out.append(encodeHREFParam(args[k]));
                if (k + 1 < args.length) {
                    out.append("=");
                    out.append(encodeHREFParam(args[k + 1]));
                }
            }
        }
        return out.toString();
    }

    public static String encodeHREFQuery(String url, Map args, boolean forHtml) {
        StringBuffer out = new StringBuffer(128);
        out.append(url);

        if ((args != null) && (args.size() > 0)) {
            out.append("?");
            int k = 0;
            for (Iterator i = args.keySet().iterator(); i.hasNext(); ) {
                if (k != 0) {
                    if (forHtml) {
                        out.append("&amp;");
                    } else {
                        out.append("&");
                    }
                }
                String name = (String) i.next();
                out.append(encodeHREFParam(name));
                out.append("=");
                out.append(encodeHREFParam((String) args.get(name)));
                k++;
            }
        }
        return out.toString();
    }
}

