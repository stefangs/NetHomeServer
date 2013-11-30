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

package nu.nethome.home.impl;

import nu.nethome.util.plugin.SelectivePluginScanner;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * This is the boot strap loader of the server application. It reads command line parameters
 * and starts the server.
 *
 * @author Stefan
 */
public class HomeManager {
    private static final int NUMBER_OF_LOGFILES = 2;
    private static final int LOG_FILE_SIZE = 1000000;
    private static final String DEFAULT_XML = "default.xml";
    private Logger logger = Logger.getLogger(HomeManager.class.getName());


    public static void main(String[] args) {
        HomeManager me = new HomeManager();
        me.go(args);
    }

    public final void go(String[] args) {

        createFileLogger();

        // Check arguments - if no arguments, load the demo configuration
        int i = 0;
        List<String> pluginDirectories = new LinkedList<String>();
        pluginDirectories.add("../../plugins");
        while (i < args.length && args[i].startsWith("-")) {
            if (args[i].startsWith("-p")) {
                pluginDirectories.add(args[i].substring(2));
            }
            i++;
        }

        // Create Plugin scanner and scan for plugins
        SelectivePluginScanner pluginProvider = new SelectivePluginScanner(".hmp", "3dparty");
        try {
            List<File> files = new LinkedList<File>();
            for (String name : pluginDirectories) {
                files.add(new File(name));
            }
            pluginProvider.scanForPlugins(files);
        } catch (IOException e) {
            logger.warning("Could not open plugin directory");
        }

        // Create the factory objects to use
        PluginHomeItemFactory factory = new PluginHomeItemFactory(pluginProvider);
        HomeItemLoader loader = new HomeItemFileLoader();

        // Create the server, the only hard coded HomeItem instance
        HomeServer server = new HomeServer();
        server.setName("Home Server");
        //server.registerInstance(server);

        // Check if a configuration file name is supplied
        if (i == args.length) {
            // No, Check if the default configuration file exists, in that case, use that
            File configuration = new File(DEFAULT_XML);
            if (configuration.exists()) {
                server.setFileName(DEFAULT_XML);
            } else {
                server.setFileName("demo.xml");
            }
        } else {
            // Yes, a configuration file name was supplied, use that
            server.setFileName(args[0]);
        }

        // Run the server
        server.run(factory, loader, pluginProvider);
    }

    private void createFileLogger() {
        try {
            // Initialize logging ( http://java.sun.com/j2se/1.4.2/docs/api/java/util/logging/package-summary.html )
            Handler fh = new FileHandler("%h/HomeManager%g.log", LOG_FILE_SIZE, NUMBER_OF_LOGFILES, true);
            Formatter fm = new LogFormatter();
            fh.setFormatter(fm);
            Logger.getLogger("").addHandler(fh);
            logger.info("**Starting HomeManager " + HomeManager.class.getPackage().getImplementationVersion() + "**");
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
