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
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

/**
 * @author shannah
 */
public class SetupXcodeProject extends Task {
    
    private File codenameOneSrc;
    private Path xmlvmClasspath;
    private boolean clean;

    public @Override
    void execute() throws BuildException {
        File base = getProject().getBaseDir();
        File build = new File(base, "build");
        File ios = new File(build, "ios");
        File iosDist = new File(ios, "dist");
        
        if ( !iosDist.exists() || clean ){
            CodenameOneIOSBuildTask builder = CodenameOneIOSBuildTask.create(getProject().getBaseDir().getAbsolutePath());
            builder.setCodenameOneSrc(getCodenameOneSrc());
            builder.setXmlvmClasspath(getXmlvmClasspath());
            builder.setClean(true);
            builder.execute();
            
        }
        
    }

    /**
     * @return the codenameOneSrc
     */
    public File getCodenameOneSrc() {
        return codenameOneSrc;
    }

    /**
     * @param codenameOneSrc the codenameOneSrc to set
     */
    public void setCodenameOneSrc(File codenameOneSrc) {
        this.codenameOneSrc = codenameOneSrc;
    }

    /**
     * @return the xmlvmClasspath
     */
    public Path getXmlvmClasspath() {
        return xmlvmClasspath;
    }

    /**
     * @param xmlvmClasspath the xmlvmClasspath to set
     */
    public void setXmlvmClasspath(Path xmlvmClasspath) {
        this.xmlvmClasspath = xmlvmClasspath;
    }

    /**
     * @return the clean
     */
    public boolean isClean() {
        return clean;
    }

    /**
     * @param clean the clean to set
     */
    public void setClean(boolean clean) {
        this.clean = clean;
    }
    
}
