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

package de.felixschulze.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.InputFile

class XcodePluginExtension {

    def String xcodeProject

    def String xcodeWorkspace

    def String xcodeConfiguration

    def String xcodeScheme

    def String xcodeSdk

    def String provisioningProfile

    def String codeSignIdentity

    def Boolean teamCityLog = false

    def String bundleIdentifierSuffix

    def String bundleDisplayNameSuffix

    def Boolean bundleVersionFromGit = false

    def String infoPlist

    def String ghunitAppName

    def String ghunitTestDevice

    def String ghunitXcodeScheme

    def Boolean ghunitAutoExit = true

    def Boolean ghunitTestDeviceRetina = true

    def Boolean ghunitTestDeviceTall = true

    def String iosSimPath


    private final Project project

    public XcodePluginExtension(Project project) {
        this.project = project
    }
}
