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

package nu.nethome.home.items.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class ResourceServlet extends HttpServlet {

    private final static int CACHE_AGE = 60 * 60 * 24;
    private String urlPathRoot;
    private String resourcePathRoot;

    public ResourceServlet(String urlPathRoot, String resourcePathRoot) {
        this.urlPathRoot = removeTrailingSlash(urlPathRoot);
        this.resourcePathRoot = removeTrailingSlash(resourcePathRoot);
    }

    public String getPathSpecification() {
        return urlPathRoot + "/*";
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long expiry = new Date().getTime() + CACHE_AGE *1000;
        response.setDateHeader("Expires", expiry);
        response.setHeader("Cache-Control", "max-age="+ CACHE_AGE);

        String localResourcePath = extractLocalResourcePath(request.getRequestURI());
        String resourceName = extractFileName(localResourcePath);
        InputStream sourceStream = this.getClass().getClassLoader()
                .getResourceAsStream(resourcePathRoot + "/" + localResourcePath);
        String mimeType = getServletContext().getMimeType(resourceName);

        if (sourceStream == null || mimeType == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            // Not setting content length since I don't have it. Hope this is ok
            response.setContentType(mimeType);
            copyAndClose(sourceStream, response.getOutputStream());
        }
    }

    private void copyAndClose(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int count;
        while ((count = in.read(buf)) >= 0) {
            out.write(buf, 0, count);
        }
        in.close();
        out.close();
    }

    String extractFileName(String localResourcePath) {
        String pathParts[] = localResourcePath.split("/");
        return pathParts.length > 0 ? pathParts[pathParts.length - 1] : "";
    }

    String removeTrailingSlash(String url) {
        if (url.length() == 0 || url.charAt(url.length() - 1) != '/') {
            return url;
        }
        return url.substring(0, url.length() - 1);
    }

    String extractLocalResourcePath(String path) {
        if (!(path.length() > urlPathRoot.length())) {
            return "";
        }
        return path.substring(urlPathRoot.length() + 1);
    }
}

