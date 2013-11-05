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

import org.gradle.api.Plugin
import org.gradle.api.Project

class XcodePlugin implements Plugin<Project> {

    static final String XCODE_GROUP_NAME = "Xcode"


    void apply(Project project) {
        configureDependencies(project)
        applyExtensions(project)
        applyTasks(project)
    }

    void applyExtensions(final Project project) {
        project.extensions.create('xcode', XcodePluginExtension, project)
    }

    void applyTasks(final Project project) {

        XcodeCleanTask xcodeCleanTask = project.tasks.create("xcodeClean", XcodeCleanTask)
        xcodeCleanTask.group = XCODE_GROUP_NAME
        xcodeCleanTask.description = "Clean project and delete build directory"
        xcodeCleanTask.outputs.upToDateWhen { false }

        XcodeBuildTask xcodeBuildTask = project.tasks.create("xcodeBuild", XcodeBuildTask)
        xcodeBuildTask.group = XCODE_GROUP_NAME
        xcodeBuildTask.description = "Build project"
        xcodeBuildTask.output = project.buildDir
        xcodeBuildTask.outputs.upToDateWhen { false }

        XcodeTestTask xcodeTestTask = project.tasks.create("xcodeTest", XcodeTestTask)
        xcodeTestTask.group = XCODE_GROUP_NAME
        xcodeTestTask.description = "Run tests"
        xcodeTestTask.output = project.buildDir
        xcodeTestTask.outputs.upToDateWhen { false }

        GHUnitTestTask ghUnitTestTask = project.tasks.create("ghunitTest", GHUnitTestTask)
        ghUnitTestTask.group = XCODE_GROUP_NAME
        ghUnitTestTask.description = "Run GHUnit Tests"
        ghUnitTestTask.outputs.upToDateWhen { false }

        ClangScanTask clangScanTask = project.tasks.create("clangScanBuild", ClangScanTask)
        clangScanTask.group = XCODE_GROUP_NAME
        clangScanTask.description = "Run clang scan-build"
        clangScanTask.outputs.upToDateWhen { false }
        clangScanTask.output = project.buildDir


    }

    void configureDependencies(final Project project) {
        project.repositories {
            mavenCentral()
        }
    }

}