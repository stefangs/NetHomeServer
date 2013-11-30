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

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * User: Stefan
 * Date: 2012-05-06
 * Time: 18:43
 */
public class ResourceServletTest {

    ResourceServlet servlet;

    @Before
    public void setUp() throws Exception {
        servlet = new ResourceServlet("/web", "nu/nethome");
    }

    @Test
    public void removeTrailingSlash() {
        assertThat(servlet.removeTrailingSlash("foo"), is("foo"));
        assertThat(servlet.removeTrailingSlash("/foo/"), is("/foo"));
        assertThat(servlet.removeTrailingSlash("/"), is(""));
    }

    @Test
    public void extractFileName() {
        assertThat(servlet.extractFileName(""), is(""));
        assertThat(servlet.extractFileName("/foo/test.png"), is("test.png"));
        assertThat(servlet.extractFileName("test.png"), is("test.png"));
    }

    @Test
    public void extractLocalResourcePath() {
        assertThat(servlet.extractLocalResourcePath("/web/foo"), is("foo"));
        assertThat(servlet.extractLocalResourcePath("/web"), is(""));
        assertThat(servlet.extractLocalResourcePath("/web/test.png"), is("test.png"));
        assertThat(servlet.extractLocalResourcePath("/web/foo/test.png"), is("foo/test.png"));
    }
}
