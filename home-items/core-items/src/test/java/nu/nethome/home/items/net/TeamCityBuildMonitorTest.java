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

package nu.nethome.home.items.net;

import nu.nethome.home.items.MockServiceConnection;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class TeamCityBuildMonitorTest {

    private class TestBuildMonitor extends TeamCityBuildMonitor {

        public int call = 0;
        public List<String> restArgument = new ArrayList<String>();
        public List<String> restReply = new ArrayList<String>();
        public List<String> actions = new ArrayList<String>();

        @Override
        public String restGet(String url, String username, String password) throws Exception {
            restArgument.add(url);
            call++;
            return restReply.get(call - 1);
        }

        @Override
        protected void performCommand(String commandString) {
            actions.add(commandString);
        }
    }

    TestBuildMonitor monitor;
    private HomeService server;

    @Before
    public void setup() {
        server = new MockServiceConnection();
        monitor = new TestBuildMonitor();
        monitor.activate(server);
        monitor.setSuccessAction("S");
        monitor.setFailAction("F");
        monitor.setInconclusiveAction("I");
    }

    @Test
    public void parseSuccessBuildStatus() throws Exception {
        String result = monitor.parseLatestBuildStatus(successOnLast);
        assertEquals("SUCCESS", result);
    }

    @Test
    public void parseSuccessBuildStatusWithOneBuild() throws Exception {
        String result = monitor.parseLatestBuildStatus(oneSuccess);
        assertEquals("SUCCESS", result);
    }

    @Test
    public void parseFailedBuildStatus() throws Exception {
        String result = monitor.parseLatestBuildStatus(failureOnLast);
        assertEquals("FAIL", result);
    }

    @Test
    public void parseOneSuccessBuildStatus() throws Exception {
        monitor.restReply.add(successOnLast);
        monitor.setrestURL("http://teamcity.hq.assaabloy.org/TeamCity");
        monitor.setBuilds("bt1385");
        monitor.checkAllBuildStatus();
        assertEquals(1, monitor.restArgument.size());
        assertEquals("http://teamcity.hq.assaabloy.org/TeamCity/httpAuth/app/rest/buildTypes/id:bt1385/builds/",
                monitor.restArgument.get(0));
        assertEquals("SUCCESS", monitor.getLatestResult());
        assertEquals(1, monitor.actions.size());
        assertEquals("S", monitor.actions.get(0));
    }

    @Test
    public void minuteEventOneSuccessBuildStatus() throws Exception {
        monitor.restReply.add(successOnLast);
        monitor.setrestURL("http://teamcity.hq.assaabloy.org/TeamCity");
        monitor.setBuilds("bt1385");
        monitor.receiveEvent(server.createEvent(HomeService.MINUTE_EVENT_TYPE, ""));
        assertEquals(1, monitor.restArgument.size());
        assertEquals("http://teamcity.hq.assaabloy.org/TeamCity/httpAuth/app/rest/buildTypes/id:bt1385/builds/",
                monitor.restArgument.get(0));
        assertEquals("SUCCESS", monitor.getLatestResult());
        assertEquals(1, monitor.actions.size());
        assertEquals("S", monitor.actions.get(0));
    }

    @Test
    public void minuteEventNoActionIfNotActivated() throws Exception {
        TestBuildMonitor inactiveMonitor = new TestBuildMonitor();
        inactiveMonitor.restReply.add(successOnLast);
        inactiveMonitor.setrestURL("http://teamcity.hq.assaabloy.org/TeamCity");
        inactiveMonitor.setBuilds("bt1385");
        inactiveMonitor.receiveEvent(server.createEvent(HomeService.MINUTE_EVENT_TYPE, ""));
        assertEquals(0, inactiveMonitor.restArgument.size());
        assertEquals(0, inactiveMonitor.actions.size());
    }

    @Test
    public void minuteEventNoActionIfNotActive() throws Exception {
        monitor.doDeactivate();
        monitor.restReply.add(successOnLast);
        monitor.setrestURL("http://teamcity.hq.assaabloy.org/TeamCity");
        monitor.setBuilds("bt1385");
        monitor.receiveEvent(server.createEvent(HomeService.MINUTE_EVENT_TYPE, ""));
        assertEquals(0, monitor.restArgument.size());
        assertEquals(0, monitor.actions.size());
    }

    @Test
    public void parseTwoOneFail() throws Exception {
        monitor.restReply.add(successOnLast);
        monitor.restReply.add(failureOnLast);
        monitor.setrestURL("http://teamcity.hq.assaabloy.org/TeamCity");
        monitor.setBuilds("bt1385,bt1386");
        monitor.checkAllBuildStatus();
        assertEquals(2, monitor.restArgument.size());
        assertEquals("http://teamcity.hq.assaabloy.org/TeamCity/httpAuth/app/rest/buildTypes/id:bt1385/builds/",
                monitor.restArgument.get(0));
        assertEquals("http://teamcity.hq.assaabloy.org/TeamCity/httpAuth/app/rest/buildTypes/id:bt1386/builds/",
                monitor.restArgument.get(1));
        assertEquals("Error: bt1386", monitor.getLatestResult());
        assertEquals(1, monitor.actions.size());
        assertEquals("F", monitor.actions.get(0));
    }

    static final String oneSuccess =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?> \n" +
                    "<builds count=\"1\"> \n" +
                    "<build id=\"253687\" number=\"21974\" status=\"SUCCESS\" buildTypeId=\"bt1385\" href=\"/httpAuth/app/rest/builds/id:253687\" \n" +
                    "       webUrl=\"http://teamcity.hq.assaabloy.org/TeamCity/viewLog.html?buildId=253687 buildTypeId=bt1385\"/> \n" +
                    "</builds> \n";

    static final String successOnLast =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?> \n" +
                    "<builds count=\"6\"> \n" +
                    "<build id=\"253687\" number=\"21974\" status=\"SUCCESS\" buildTypeId=\"bt1385\" href=\"/httpAuth/app/rest/builds/id:253687\" \n" +
                    "       webUrl=\"http://teamcity.hq.assaabloy.org/TeamCity/viewLog.html?buildId=253687 buildTypeId=bt1385\"/> \n" +
                    "<build id=\"253438\" number=\"21950\" status=\"FAIL\" buildTypeId=\"bt1385\" href=\"/httpAuth/app/rest/builds/id:253438\" \n" +
                    "      webUrl=\"http://teamcity.hq.assaabloy.org/TeamCity/viewLog.html?buildId=253438 buildTypeId=bt1385\"/> \n" +
                    "<build id=\"253163\" number=\"21923\" status=\"FAIL\" buildTypeId=\"bt1385\" href=\"/httpAuth/app/rest/builds/id:253163\" \n" +
                    "       webUrl=\"http://teamcity.hq.assaabloy.org/TeamCity/viewLog.html?buildId=253163 buildTypeId=bt1385\"/> \n" +
                    "<build id=\"252799\" number=\"21902\" status=\"FAIL\" buildTypeId=\"bt1385\" href=\"/httpAuth/app/rest/builds/id:252799\" \n" +
                    "       webUrl=\"http://teamcity.hq.assaabloy.org/TeamCity/viewLog.html?buildId=252799 buildTypeId=bt1385\"/> \n" +
                    "<build id=\"252445\" number=\"21884\" status=\"FAIL\" buildTypeId=\"bt1385\" href=\"/httpAuth/app/rest/builds/id:252445\" \n" +
                    "       webUrl=\"http://teamcity.hq.assaabloy.org/TeamCity/viewLog.html?buildId=252445 buildTypeId=bt1385\"/> \n" +
                    "<build id=\"251442\" number=\"21863\" status=\"FAIL\" buildTypeId=\"bt1385\" href=\"/httpAuth/app/rest/builds/id:251442\" \n" +
                    "       webUrl=\"http://teamcity.hq.assaabloy.org/TeamCity/viewLog.html?buildId=251442 buildTypeId=bt1385\"/> \n" +
                    "</builds> \n";

    static final String failureOnLast =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?> \n" +
                    "<builds count=\"6\"> \n" +
                    "<build id=\"253687\" number=\"21974\" status=\"FAIL\" buildTypeId=\"bt1385\" href=\"/httpAuth/app/rest/builds/id:253687\" \n" +
                    "       webUrl=\"http://teamcity.hq.assaabloy.org/TeamCity/viewLog.html?buildId=253687 buildTypeId=bt1385\"/> \n" +
                    "<build id=\"253438\" number=\"21950\" status=\"SUCCESS\" buildTypeId=\"bt1385\" href=\"/httpAuth/app/rest/builds/id:253438\" \n" +
                    "      webUrl=\"http://teamcity.hq.assaabloy.org/TeamCity/viewLog.html?buildId=253438 buildTypeId=bt1385\"/> \n" +
                    "<build id=\"253163\" number=\"21923\" status=\"SUCCESS\" buildTypeId=\"bt1385\" href=\"/httpAuth/app/rest/builds/id:253163\" \n" +
                    "       webUrl=\"http://teamcity.hq.assaabloy.org/TeamCity/viewLog.html?buildId=253163 buildTypeId=bt1385\"/> \n" +
                    "<build id=\"252799\" number=\"21902\" status=\"SUCCESS\" buildTypeId=\"bt1385\" href=\"/httpAuth/app/rest/builds/id:252799\" \n" +
                    "       webUrl=\"http://teamcity.hq.assaabloy.org/TeamCity/viewLog.html?buildId=252799 buildTypeId=bt1385\"/> \n" +
                    "<build id=\"252445\" number=\"21884\" status=\"SUCCESS\" buildTypeId=\"bt1385\" href=\"/httpAuth/app/rest/builds/id:252445\" \n" +
                    "       webUrl=\"http://teamcity.hq.assaabloy.org/TeamCity/viewLog.html?buildId=252445 buildTypeId=bt1385\"/> \n" +
                    "<build id=\"251442\" number=\"21863\" status=\"SUCCESS\" buildTypeId=\"bt1385\" href=\"/httpAuth/app/rest/builds/id:251442\" \n" +
                    "       webUrl=\"http://teamcity.hq.assaabloy.org/TeamCity/viewLog.html?buildId=251442 buildTypeId=bt1385\"/> \n" +
                    "</builds> \n";

}
