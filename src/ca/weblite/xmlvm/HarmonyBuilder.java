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
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;



/**
 *
 * @author shannah
 */
public class HarmonyBuilder  extends Task {
    
    private Path xmlvmClasspath = null;
    private File codenameOneDir = new File("/Volumes/Windows VMS/src/codenameone-read-only");
    
    public void buildCodenameOne() throws IOException{
        org.apache.tools.ant.Project proj = new org.apache.tools.ant.Project();
        
        
        
        proj.setBasedir(new File(".").getAbsolutePath());
        setProject(proj);
        
        
        File buildDir = new File(proj.getBaseDir(), "build");
        File classesDir = new File(buildDir, "cn1Build");
        if ( classesDir.exists() ){
            FileUtils.deleteDirectory(classesDir);
        } 
        classesDir.mkdir();
        
        File cDir = new File(buildDir, "c");
        if ( cDir.exists() ){
            FileUtils.deleteDirectory(cDir);
        }
        cDir.mkdir();
        
        File cn1SourceDir = new File(codenameOneDir, "CodenameOne/src");
        File cn1PortDir = new File(codenameOneDir, "Ports/iOSPort/src");
        
        Path javaSourcesPath = new Path(proj, cn1SourceDir.getAbsolutePath());
        javaSourcesPath.add(new Path(proj, cn1PortDir.getAbsolutePath()));
        
        Javac javac = (Javac)proj.createTask("javac");
        javac.setSrcdir(javaSourcesPath);
        javac.setDestdir(classesDir);
        javac.setTarget("1.5");
        javac.execute();
        
        
        //xmlvm.setClassPath(new Path(proj, harmonyDir.getAbsolutePath()));
        
        runXmlvm(new String[]{
            //"-XmX=2G",
            "--in="+classesDir.getAbsolutePath(),
            "--out="+cDir.getAbsolutePath(),
            "--target=c",
            "--load-dependencies",
            "--disable-vtable-optimizations",
            "--c-source-extension=m"
            
        });
        
        
        ConstantPoolHelper.removeConstantPoolDependencies(cDir);
        VtableHelper.fixVtableReferences(proj, cDir, cDir);
        
        
        
        
    }
    
    public static void main(String[] args) throws IOException{
        HarmonyBuilder builder = new HarmonyBuilder();
        builder.buildCodenameOne();
    }
    
    private void runXmlvm(String[] args) {
        Path path = new Path(getProject(), System.getProperty("java.class.path"));
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
        runXmlvm(args, new File(xmlvm));

    }

    private void runXmlvm(String[] args, File xmlvmJar) {
        try {
            //System.out.println(getProject().getProperties());
            //System.out.println(this.getClassPath());
            //System.out.println(System.getProperties());
            Java j = (Java) getProject().createTask("java");
            j.setMaxmemory("4G");
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
            Logger.getLogger(HarmonyBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
