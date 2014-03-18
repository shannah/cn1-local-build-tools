/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.xmlvm;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;

/**
 *
 * @author shannah
 */
public class XmlvmHelper {
     public static void runXmlvm(Project project, Path xmlvmClasspath, String[] args) {
        Path path = new Path(project, System.getProperty("java.class.path"));
        if ( xmlvmClasspath != null ){
            path.add(xmlvmClasspath);
        }
        System.out.println("Checking classpath for xmlvm.jar: "+path);
        String xmlvm = null;
        for (String p : path.list()) {
            if (p.equals("xmlvm.jar") || p.endsWith(File.separator + "xmlvm.jar") ) {
                xmlvm = p;
            }
        }
        if (xmlvm == null) {
            throw new RuntimeException("Could not find XMLVM ");

        }
        runXmlvm(project, args, new File(xmlvm));

    }

    public static void runXmlvm(Project project, String[] args, File xmlvmJar) {
        try {
            //System.out.println(getProject().getProperties());
            //System.out.println(this.getClassPath());
            //System.out.println(System.getProperties());
            Java j = (Java) project.createTask("java");
            j.setMaxmemory("2G");
            j.setJar(xmlvmJar);
            //j.setFailonerror(true);
            File log = File.createTempFile("foo", "bar");
            j.setOutput(log);
            j.setFork(true);
            for (String arg : args) {
                Commandline.Argument a = j.createArg();
                a.setValue(arg);
                
            }
            System.out.println("About to execute XMLVM...");
            j.execute();
            System.out.println(FileUtils.readFileToString(log));
            System.out.println("Finished execution of XMLVM");
        } catch (IOException ex) {
            Logger.getLogger(XMLVM.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
