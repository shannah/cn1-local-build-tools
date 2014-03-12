/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.codename1.ios;

// IMPORTANT! You need to compile this class against ant.jar.
// The easiest way to do this is to add ${ant.core.lib} to your project's classpath.
// For example, for a plain Java project with no other dependencies, set in project.properties:
// javac.classpath=${ant.core.lib}
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author shannah
 */
public class OpenXcodeProject extends Task {

    public @Override
    void execute() throws BuildException {
        try {
            File base = getProject().getBaseDir();
            File build = new File(base, "build");
            File ios = new File(build, "ios");
            File iosDist = new File(ios, "dist");
            File xmlvmPropertiesFile = new File(ios, "xmlvm.properties");
            if ( !xmlvmPropertiesFile.exists() ){
                throw new BuildException("Could not find "+xmlvmPropertiesFile);
            }
            
            Properties xmlvmProperties = new Properties();
            xmlvmProperties.load(new FileInputStream(xmlvmPropertiesFile));
            String displayName = xmlvmProperties.getProperty("bundle.displayname");
            if ( displayName == null ){
                throw new BuildException("Display name not set.");
            }
            
            File xcodeProj = new File(iosDist, displayName+".xcodeproj");
            if ( !xcodeProj.exists() ){
                throw new BuildException("The Xcode project could not be found at location "+xcodeProj+".  Please first create the xcode project.  Then you can open it.");
            }
            
            Desktop.getDesktop().open(xcodeProj);
            
        } catch (IOException ex) {
            Logger.getLogger(OpenXcodeProject.class.getName()).log(Level.SEVERE, null, ex);
            throw new BuildException(ex);
        }
        
    }
    
}
