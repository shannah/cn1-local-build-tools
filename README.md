#Codename One Local iOS Build Ant Task

This repository contains an ANT task to build [Codename One](http://www.codenameone.com) applications locally.  This project is targetted specifically at iOS builds.

----
###IMPORTANT:

This ANT task is designed to be used **for development builds only**.  It includes modifications to XMLVM to allow generated classes to be more self-contained, and thus optimized for faster compilation (when changes are made).  These changes are experimental and may not work correctly in all cases.  You are advised to use this module only as a tool to speed your development process.

**PLEASE USE THE [Codename One Build Server](http://www.codenameone.com/build-server.html) for all of your production builds, and for anything you wish to distribute for use by other users. **

**ALSO NOTE:*** This project is **not affiliated with Codename One, the company**.  It is a community project maintained by [Steve Hannah](http://sjhannah.com).  Builds produced by this project are **not supported by Codename One**.  

Therefore, if you have questions or issues related to applications you have built using this tool, and you post questions to the Codename One mailing list, Stack Overflow, or any other forum, **please state clearly that the build was produced using this project, and not with the build server**.  You will likely be asked to try building with the build server before asking for support. 

If you find bugs, or have questions relating to this library, please post them to [the issue tracker](https://github.com/shannah/cn1-local-build-tools/issues).

----

##Requirements

* Mac OS X Running Xcode 5
* Java 7
* An existing Codename One Netbeans project

----


##Features
* **Clean and Build** - Create a new Xcode project from scratch.  Building an app from scratch to finally compiling to an iOS binary takes about 10 to 15 minutes.
* **Build only Changed** - The default build option only regenerates those classes that have changed (and classes that are affected by changed classes).  This should reduce build times to between 10 seconds and 1 minute depending on the number of classes that were changed.
* **Self-contained** - The Codename One and iOSPort code is included inside the jar file so you don't need to download any source separately.  Just make sure the jar file is in your ant classpath.
* **Specify Custom Locations for Codename One Sources** - If you want to build against a specific version of Codename One you can specify custom locations for the Codename One and iOS port sources.

----


##Installation

* Download the [CN1LocalIOSBuild.jar](https://github.com/shannah/cn1-local-build-tools/raw/master/dist/CN1LocalIOSBuild.jar) file and add it to your ANT class path.  
* You will also need to add the [commons-io-2.4.jar](https://github.com/shannah/cn1-local-build-tools/raw/master/dist/lib/commons-io-2.4.jar) and [xmlvm.jar](https://github.com/shannah/cn1-local-build-tools/raw/master/dist/lib/xmlvm.jar) files to your classpath since this is required by the task.  *Note that this version of xmlvm.jar is a slightly different version of XMLVM than you will find in either the Codename One repository or the [XMLVM website](http://www.xmlvm.org). It has been modified to support building only changed files.  You can find the source [here](https://github.com/shannah/cn1/tree/master/Ports/iOSPort/xmlvm).*  

##Usage

1. Inside your ANT script, first define the task: 

~~~~
<property name="libs.cn1-ios-all.classpath"
          value="path/to/CN1LocalIOSBuild.jar:path/to/commons-io-2.4.jar:path/to/xmlvm.jar"
/>

<taskdef name="buildios" 
         classpath="${libs.cn1-ios-all.classpath}:${javac.classpath}"
         classname="ca.weblite.codename1.ios.CodenameOneIOSBuildTask"
/>	
~~~~

2. Use the `buildios` task that was defined:

~~~~

<buildios xmlvmclasspath="${libs.cn1-ios-all.classpath}"/>

~~~~

Note: the `xmlvmclasspath` paramter is required, and it needs to *at least* include the path to the `xmlvm.jar` file.

###Options 

Name | Description | Default| Required 
----|----|----|----
xmlvmclasspath       | A classpath containing at least xmlvm.jar        |  -          | Yes
iosport  |    Path to the iOSPort directory.  If this is omitted, it will just use the bundled iOSPort, which will be extracted to $PROJECT_DIR/iOSPort   | $PROJECT_DIR/iOSPort | No
codenameonesrc | Path to the CodenameOne sources.  If this is omitted, it will just use the bundled Codename One sources extracted to $PROJECT_DIR/CodenameOneSrc | - | No
clean | Whether to build it clean or not | false | No

###Other Tasks

In addition to the default task (`ca.weblite.codename1.ios.CodenameOneIOSBuildTask`), there are two other tasks defined in this module:

1. `ca.weblite.codename1.ios.SetupXcodeProject` - A wrapper task that only creates the Xcode project, and only if it doesn't exist or the "clean" parameter is passed.  This takes the same parameters as `ca.weblite.codename1.ios.CodenameOneIOSBuildTask`.
2. `ca.weblite.codename1.ios.OpenXcodeProject` - A task to open the previously generated Xcode project.

~~~~

<taskdef name="openxcode" 
         classpath="${libs.cn1-ios-all.classpath}:${javac.classpath}"
         classname="ca.weblite.codename1.ios.OpenXcodeProject"
/>
<openxcode/>

~~~~

----

##Build Instructions

Building from source is most easily done by cloning the repository, opening the project in Netbeans, and just building the project normally.  The jars will be saved in the dist directory.

The github repo includes ZIP files of all of the Codename One sources that will be included by default. 

###Re-bundling Codename One Sources

1. Obtain the Codename One sources from either the [official Codename One SVN repository](https://code.google.com/p/codenameone/source/checkout) or from [this fork](https://github.com/shannah/cn1).  Both should be fine, but the github fork includes some changes to XMLVM improve support for offline building.  Noraml bundling of sources does not involve bundling XMLVM so this point is moot.  The official SVN repository will always give you the latest so that is probably the getter course if you are unsure.
2. Modify the [`build.xml` file](https://github.com/shannah/cn1-local-build-tools/blob/master/build.xml) in your local copy so that all of the paths in the `bundle-iosport` target point to your local Codename One sources that you downloaded in step 1.
3. Execute the 	`bundle-iosport` target:

~~~~
$ ant bundle-iosport
~~~~

----

##Status

**Alpha**. Currently the code is a mess and really needs a refactor. That said, it should work for simple apps.

----

##License

This project bundles the entire Codename One codebase in it, which is licensed under the GPL with Classpath exception.  See [the Codename One website](http://www.codenameone.com) for more details on the license.

----

##Supported Codename One Features

* Should support all features except those listed in the unsupported features section below.

##Unsupported Codename One Features
* *Native Interfaces* -  Hopefully these will be supported in the near future.
* *Embedded Fonts* - For some reason these don't work right now..  Hopefully I'll be able to figure this out.

**If you spot anything else that doesn't work, please post in the [issue tracker](https://github.com/shannah/cn1-local-build-tools/issues)**.

----
##Credits

1. Thanks to **Shai Almog** and **Chen Fishbein** of Codename One for developing and maintaining [Codename One](http://www.codenameone.com), a powerful, cross-platform, mobile application toolkit and framework.
2. Thanks for **Arno Puder** and the XMLVM team for developing and maintaining [XMLVM](http://xmlvm.org), a rich and wonderful tool for translating between different programming languages - in this case for the Java to C translator.
3. This project is maintained by **[Steve Hannah](http://sjhannah.com)**.


