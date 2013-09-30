# gradle-xcode-plugin 

A gradle plugin for building xcode projects.

## Basic usage

Add to your build.gradle

```gradle
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'de.felixschulze.gradle:gradle-xcode-plugin:1.0-SNAPSHOT'
    }
}

apply plugin: 'xcode'
```

## Advanced usage

Add to your build.gradle

```gradle
xcode {
    teamCityLog = true
    xcodeWorkspace = "AutoScout.xcworkspace"
    xcodeScheme = "AutoScout24"
    xcodeSdk = "iphonesimulator7.0"
    xcodeConfiguration = "Debug"
    teamCityLog = true
    iosSimPath = "/usr/local/bin/ios-sim"
    ghunitAppName = "AutoScout24ViewTests"
    ghunitXcodeScheme = "AutoScout24ViewTests"
}
```

* `teamCityLog`: Add features for [TeamCity](http://www.jetbrains.com/teamcity/)
* `xcodeWorkspace`: Path to `.xcworkspace` file
* `xcodeScheme`: The scheme name
* `xcodeSdk`: The SDK to build for
* `xcodeConfiguration`: The Configuration to build for
* `iosSimPath`: Path to [ios-sim](https://github.com/phonegap/ios-sim)
* `ghunitAppName`: The app name from the gh-unit test target
* `ghunitXcodeScheme`: The scheme name for the gh-unit test

## Changelog

[Releases](https://github.com/x2on/gradle-xcode-plugin/releases)

## License

gradle-xcode-plugin is available under the MIT license. See the LICENSE file for more info.
