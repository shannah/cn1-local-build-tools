/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.xmlvm;

import ca.weblite.codename1.ios.CodenameOneIOSBuildTask;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.types.Path;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

/**
 *
 * @author shannah
 */
public class XMLVMTest {
    
    public XMLVMTest() {
    }

    @Test
    public void testSomeMethod() throws IOException {
        
        org.apache.tools.ant.Project proj = new org.apache.tools.ant.Project();
        
        
        File basedir = new File("/Volumes/Windows VMS/NetbeansProjects/HelloWorldCN1");
        proj.setBasedir(basedir.getAbsolutePath());
        XMLVM xmlvm = new XMLVM();
        xmlvm.setProject(proj);
        File dest = new File(basedir, "cout");
        dest.mkdir();
        
        Path src = new Path(proj,new File(basedir,"src").getAbsolutePath());
        xmlvm.setSrc(src);
        xmlvm.setDest(dest);
        
        File buildDir = new File(basedir, "build");
        buildDir = new File(buildDir, "classes");
        
        xmlvm.setJavaBuildDir(buildDir);
        
        File iOSPort = new File(basedir, "iOSPort");
        
        
        Path classPath = new Path(proj);
        classPath.add(new Path(proj, buildDir.getAbsolutePath() ));
        src.add(new Path(proj, new File("/Volumes/Windows VMS/src/codenameone-read-only/CodenameOne/src").getAbsolutePath()));
        src.add(new Path(proj, new File(iOSPort, "src").getAbsolutePath()));
        xmlvm.setClassPath(classPath);
        
        xmlvm.setTarget("c");
        //xmlvm.doCleanBuild();
        //if ( true ) return;
        
        File myApp = new File(basedir, "src/com/mycompany/myapp/MyApplication.java");
        FileUtils.touch(myApp);
        System.out.println("Doing regular build now");
        xmlvm.doRegularBuild();
        
    }
    
    
    @Test public void testCollectDirtyClasses() throws IOException{
        org.apache.tools.ant.Project proj = new org.apache.tools.ant.Project();
        
        
        File basedir = new File("/Volumes/Windows VMS/NetbeansProjects/HelloWorldCN1");
        proj.setBasedir(basedir.getAbsolutePath());
        XMLVM xmlvm = new XMLVM();
        xmlvm.setProject(proj);
        File dest = new File(basedir, "cout");
        dest.mkdir();
        
        Path src = new Path(proj,new File(basedir,"src").getAbsolutePath());
        xmlvm.setSrc(src);
        xmlvm.setDest(dest);
        
        File buildDir = new File(basedir, "build");
        buildDir = new File(buildDir, "classes");
        
        xmlvm.setJavaBuildDir(buildDir);
        
        File iOSPort = new File(basedir, "iOSPort");
        
        
        Path classPath = new Path(proj);
        classPath.add(new Path(proj, buildDir.getAbsolutePath() ));
        src.add(new Path(proj, new File("/Volumes/Windows VMS/src/codenameone-read-only/CodenameOne/src").getAbsolutePath()));
        src.add(new Path(proj, new File(iOSPort, "src").getAbsolutePath()));
        xmlvm.setClassPath(classPath);
        
        xmlvm.setTarget("c");
        
        HashSet<String> changedClasses = new HashSet<>();
        changedClasses.add("com.mycompany.myapp.MyApplication");
        Set<String> dirty = xmlvm.collectDirtyClasses(changedClasses);
        System.out.println(dirty.size()+" classes are dirty");
        System.out.println(dirty);
    }
    
    @Test 
    public void testCreateXMLVMClassStub() throws Exception {
        
            File input = File.createTempFile("input", ".xmlvm");
            File output = File.createTempFile("output", ".xmlvm");
            
            XMLVM xmlvm = new XMLVM();
            
            FileUtils.copyURLToFile(this.getClass().getResource("resources/com_codename1_cloud_CloudObject.xmlvm"), input);
            xmlvm.createXMLVMClassStub(input, output);
            
            System.out.println(FileUtils.readFileToString(output));
    
    }
    
    @Test
    public void testSingleXMLVM() throws Exception {
        org.apache.tools.ant.Project proj = new org.apache.tools.ant.Project();
        File basedir = new File("/Volumes/Windows VMS/NetbeansProjects/HelloWorldCN1");
        proj.setBasedir(basedir.getAbsolutePath());
        XMLVM xmlvm = new XMLVM();
        xmlvm.setProject(proj);
        File srcdir = File.createTempFile("input", "dir");
        srcdir.delete();
        srcdir.mkdir();
        File comDir = new File(srcdir, "com");
        File myCompanyDir = new File(comDir, "mycompany");
        File myappDir = new File(myCompanyDir, "myapp");
        myappDir.mkdirs();
        File inputClassFile = new File(myappDir, "MyApplication.class");
        
        FileUtils.copyURLToFile(this.getClass().getResource("resources/MyApplication.clazz"), inputClassFile);
        
        
        
        
        File destDir = File.createTempFile("output", "dir");
        destDir.delete();
        destDir.mkdir();
        
        //XMLVM xmlvm = new XMLVM();
        File[] results = xmlvm.generateSingleXMLVM(srcdir, inputClassFile, destDir);
        assertEquals(2, results.length);
        assertEquals("com_mycompany_myapp_MyApplication.xmlvm", results[0].getName());
        assertEquals("org_xmlvm_ConstantPool.xmlvm", results[1].getName());
        
        
        
        
    }
    
    
    @Test public void testGetRequiredClasses() throws ParserConfigurationException, SAXException, IOException{
        org.apache.tools.ant.Project proj = new org.apache.tools.ant.Project();
        File basedir = new File("/Volumes/Windows VMS/NetbeansProjects/HelloWorldCN1");
        proj.setBasedir(basedir.getAbsolutePath());
        XMLVM xmlvm = new XMLVM();
        xmlvm.setProject(proj);
        
        File input = File.createTempFile("input", ".xmlvm");
           
           
            
        FileUtils.copyURLToFile(this.getClass().getResource("resources/com_codename1_cloud_CloudObject.xmlvm"), input);
        String[] classes = xmlvm.getRequiredClasses(input, false);
        assertEquals(2, classes.length);
        assertEquals(21, xmlvm.getRequiredClasses(input, true).length);
    }
    
}
