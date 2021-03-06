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
package com.xl.net.genomes;

import com.xl.exception.NetworkException;
import com.xl.main.Global;
import com.xl.utils.NetworkDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

/**
 * The UpdateChecker allows the program to check on the homepage of RED to determine if a newer version of the program has been released so we can prompt the
 * user to get the update.
 */
public class UpdateChecker {
    private final static Logger logger = LoggerFactory.getLogger(UpdateChecker.class);

    private static String latestVersion = null;

    /**
     * Checks if an is update available.
     */
    public static void isUpdateAvailable(final IUpdateCheck listener) {
        NetworkDetector.isNetworkAvailable(new NetworkDetector.INetwork() {
            @Override
            public void onSuccess() {
                try {
                    if (latestVersion == null) {
                        getLatestVersionNumber();
                    }
                    if (listener != null) {
                        listener.onSuccess(isNewer(Global.VERSION, latestVersion));
                    }
                } catch (NetworkException e) {
                    if (listener != null) {
                        listener.onFailed();
                    }
                }
            }

            @Override
            public void onFailed() {
                if (listener != null) {
                    listener.onFailed();
                }
            }
        });
    }

    /**
     * Compares a local and remote version string to see if the remote version is newer.
     *
     * @param thisVersion   The version string from the currently running program
     * @param remoteVersion The version string from the latest remote version
     * @return true, if the remote version is newer
     */
    private static boolean isNewer(String thisVersion, String remoteVersion) {
        logger.info("Current version: " + thisVersion + "\tRemote version: " + remoteVersion);
        String[] thisSections = thisVersion.split("[ \\.]");
        String[] remoteSections = remoteVersion.split("[ \\.]");

        for (int i = 0; i < Math.min(thisSections.length, remoteSections.length); i++) {

            int thisNumber = Integer.parseInt(thisSections[i]);
            int remoteNumber = Integer.parseInt(remoteSections[i]);

            if (remoteNumber > thisNumber) {
                // The remote version is higher
                return true;
            } else if (thisNumber > remoteNumber) {
                // This version is higher
                logger.error("Local version (" + thisVersion + ") is higher than the remote (" + remoteVersion + ")");
                return false;
            }
        }

        // If we get to here then all of the common sections were the same. The remote version is therefore newer if it's longer than the local version
        if (remoteSections.length > thisSections.length) {
            return true;
        }

        if (thisSections.length > remoteSections.length) {
            logger.error("Local version (" + thisVersion + ") is higher than the remote (" + remoteVersion + ")");
        }
        return false;
    }

    /**
     * Gets the latest version number from the main RED site
     *
     * @return The version string from the remote site
     * @throws NetworkException if the remote version couldn't be retrieved
     */
    public static String getLatestVersionNumber() throws NetworkException {
        logger.info("Try to get the latest version of RED");
        try {
            URL updateURL = new URL(Global.VERSION_PAGE);

            URLConnection connection = updateURL.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setUseCaches(false);

            DataInputStream d = new DataInputStream(new BufferedInputStream(connection.getInputStream()));

            byte[] data = new byte[255]; // A version number should never be more than 255 bytes
            int bytesRead = d.read(data);

            byte[] actualData = Arrays.copyOfRange(data, 0, bytesRead);

            latestVersion = new String(actualData);
            return latestVersion.replaceAll("[\\r\\n]", "").trim();
        } catch (IOException e) {
            throw new NetworkException("Couldn't contact the update server to check for updates");
        }
    }

    public interface IUpdateCheck {
        void onSuccess(boolean isUpdateAvailable);

        void onFailed();
    }
}
