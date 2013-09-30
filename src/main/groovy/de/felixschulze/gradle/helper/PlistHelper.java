/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Felix Schulze
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.felixschulze.gradle.helper;

import de.felixschulze.teamcity.TeamCityStatusMessageHelper;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xmlwise.Plist;
import xmlwise.XmlParseException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class PlistHelper {

    private static final String BUNDLE_IDENTIFIER_KEY = "CFBundleIdentifier";
    private static final String BUNDLE_DISPLAY_NAME_KEY = "CFBundleDisplayName";
    private static final String BUNDLE_VERSION_KEY = "CFBundleVersion";

    private static final Logger LOG = LoggerFactory.getLogger(PlistHelper.class);

    public static void changesValuesInPlist(File basedir, File infoPlist, String bundleIdentifierSuffix, String bundleDisplayNameSuffix, Boolean bundleVersionFromGit, Boolean teamCityLog) {
        try {
            Map<String, Object> properties = Plist.load(infoPlist);

            Boolean changeInPlist = false;

            if (bundleIdentifierSuffix != null) {

                if (properties.containsKey(BUNDLE_IDENTIFIER_KEY)) {

                    String identifier = String.valueOf(properties.get(BUNDLE_IDENTIFIER_KEY));
                    if (!identifier.endsWith(bundleIdentifierSuffix)) {
                        LOG.info("Add suffix: \"" + bundleIdentifierSuffix + "\" for: \"" + BUNDLE_IDENTIFIER_KEY + "\"");
                        identifier = identifier.concat(bundleIdentifierSuffix);
                        changeInPlist = true;
                        properties.put(BUNDLE_IDENTIFIER_KEY, identifier);
                    }
                }
            }
            if (bundleDisplayNameSuffix != null) {

                if (properties.containsKey(BUNDLE_DISPLAY_NAME_KEY)) {

                    String displayName = String.valueOf(properties.get(BUNDLE_DISPLAY_NAME_KEY));
                    if (!displayName.endsWith(bundleDisplayNameSuffix)) {
                        LOG.info("Add suffix: \"" + bundleDisplayNameSuffix + "\" for: \"" + BUNDLE_DISPLAY_NAME_KEY + "\"");
                        displayName = displayName.concat(bundleDisplayNameSuffix);
                        changeInPlist = true;
                        properties.put(BUNDLE_DISPLAY_NAME_KEY, displayName);
                    }
                }
            }
            if (bundleVersionFromGit) {
                if (properties.containsKey(BUNDLE_VERSION_KEY)) {
                    try {
                        File gitDir = new File(basedir, ".git");
                        if (gitDir.exists()) {
                            GitHelper gitHelper = new GitHelper(gitDir);
                            int numberOfCommits = gitHelper.numberOfCommits();
                            String uniqueShortId = gitHelper.currentHeadRef();

                            if (numberOfCommits > 0 && uniqueShortId != null) {
                                String version = String.valueOf(properties.get(BUNDLE_VERSION_KEY));
                                String versionSuffix = "-" + numberOfCommits + "-" + uniqueShortId;
                                if (!version.contains(versionSuffix)) {
                                    version = version.concat(versionSuffix);
                                    LOG.info("Change version to: " + version);
                                    changeInPlist = true;
                                    properties.put(BUNDLE_VERSION_KEY, version);
                                }
                                if (teamCityLog) {
                                    LOG.info(TeamCityStatusMessageHelper.buildNumberString(version));
                                }
                            }
                        }
                    } catch (GitAPIException e) {
                        LOG.warn("Error while getting version number from git: " + e);
                    }
                }
            }
            if (changeInPlist) {
                Plist.store(properties, infoPlist);
            }
        } catch (XmlParseException e) {
            LOG.warn("Error while parsing plist: " + e);
        } catch (IOException e) {
            LOG.warn("Can't find plist: " + e);
        }
    }
}
