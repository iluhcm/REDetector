/*
 * RED: RNA Editing Detector
 *     Copyright (C) <2014>  <Xing Li>
 *
 *     RED is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     RED is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xl.preferences;

import java.io.*;
import java.util.Properties;

/**
 * A set of RedPreferences, both temporary and permanent which are used throughout RED. Permanent RedPreferences can be loaded from and saved to a
 * RedPreferences file allowing persistence between sessions.
 */
public class RedPreferences {
    /**
     * The keys of the preferences.
     */
    public static final String PROXY = "Proxy";
    public static final String CHECK_FOR_UPDATE = "CheckForUpdate";
    /**
     * The single instantiated instance of redPreferences
     */
    private static RedPreferences redPreferences = new RedPreferences();
    private LocationPreferences locationPreferences = LocationPreferences.getInstance();
    private DatabasePreferences databasePreferences = DatabasePreferences.getInstance();
    /**
     * The preferences file.
     */
    private File preferencesFile = null;
    /**
     * Whether we're using a network proxy
     */
    private boolean useProxy = false;
    /**
     * The proxy host.
     */
    private String proxyHost = "";
    /**
     * The proxy port.
     */
    private int proxyPort = 0;
    /**
     * Whether we should check for updates every time we're launched.
     */
    private boolean checkForUpdates = true;

    /**
     * Instantiates a redPreferences object. Only ever called once from inside this class. External access is via the getInstnace() method.
     */
    private RedPreferences() {
        try {
            preferencesFile = new File(locationPreferences.getProjectSaveLocation() + File.separator + "red_prefs.txt");
            if (preferencesFile.exists()) {
                /** Loading redPreferences from file... */
                loadPreferences();
            } else {
                savePreferences();
            }
            updateProxyInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the single instance of RedPreferences.
     *
     * @return single instance of RedPreferences
     */
    public static RedPreferences getInstance() {
        return redPreferences;
    }

    /**
     * Load preferences from a saved file
     */
    private void loadPreferences() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader(preferencesFile));
        setCheckForUpdates(Boolean.parseBoolean(properties.getProperty(CHECK_FOR_UPDATE)));
        String[] proxys = properties.getProperty(PROXY).split(",");
        if (proxys.length == 2) {
            setProxy(proxys[0], Integer.parseInt(proxys[1]));
        } else {
            setProxy(proxyHost, proxyPort);
        }
        locationPreferences.loadPreferences(properties);
        databasePreferences.loadPreferences(properties);
    }

    /**
     * Save RedPreferences.
     *
     * @throws IOException
     */
    public void savePreferences() throws IOException {
        PrintWriter p = new PrintWriter(new FileWriter(preferencesFile));

        Properties properties = new Properties();
        properties.setProperty(PROXY, proxyHost + "," + proxyPort);
        properties.setProperty(CHECK_FOR_UPDATE, Boolean.toString(checkForUpdates));
        locationPreferences.savePreferences(properties);
        databasePreferences.savePreferences(properties);
        properties.store(p, "RED Preferences. DO NOT Edit This File Individually.");
        p.close();
    }

    /**
     * Asks whether we should check for updated versions of RED
     *
     * @return true, if we should check for updates
     */
    public boolean checkForUpdates() {
        return checkForUpdates;
    }

    /**
     * Sets the flag to say if we should check for updates
     *
     * @param checkForUpdates Check if there is any updated version.
     */
    public void setCheckForUpdates(boolean checkForUpdates) {
        this.checkForUpdates = checkForUpdates;
    }

    /**
     * Flag to say if network access should go through a proxy
     *
     * @return true, if a proxy should be used
     */
    public boolean useProxy() {
        return useProxy;
    }

    /**
     * Proxy host.
     *
     * @return The name of the proxy to use. Only use this if the useProxy flag is set.
     */
    public String proxyHost() {
        return proxyHost;
    }

    /**
     * Proxy port.
     *
     * @return The port to access the proxy on. Only use this if the useProxy flag is set
     */
    public int proxyPort() {
        return proxyPort;
    }

    /**
     * Sets proxy information
     *
     * @param host The name of the proxy
     * @param port The port to access the proxy on
     */
    public void setProxy(String host, int port) {
        proxyHost = host;
        proxyPort = port;
        updateProxyInfo();
    }

    /**
     * Applies the stored proxy information to the environment of the current session so it is picked up automatically by any network calls made within the
     * program. No further configuration is required within classes requiring network access.
     */
    private void updateProxyInfo() {
        if (useProxy) {
            System.getProperties().put("proxySet", "true");
            System.getProperties().put("proxyHost", proxyHost);
            System.getProperties().put("proxyPort", "" + proxyPort);
        } else {
            System.getProperties().put("proxySet", "false");
        }
    }


}