/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.codename1.ios;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.Replace;
import org.apache.tools.ant.types.Path;


/**
 * @author shannah
 */
public class CodenameOneIOSBuildTask extends XMLVMIOSTask {
    
    File iOSPort;
    private File codenameOneSrc;
    private File cldc;
    private boolean debug = true;
    
    
    
    public static CodenameOneIOSBuildTask create(String basePath){
        org.apache.tools.ant.Project proj = new org.apache.tools.ant.Project();
        
        
        
        proj.setBasedir(basePath);
        CodenameOneIOSBuildTask build = new CodenameOneIOSBuildTask();
        build.setProject(proj);
        return build;
        
    }
    
    
    private void addIOSPortToClasspath() throws IOException{
        if ( iOSPort == null ){
            iOSPort = new File(getProject().getBaseDir(), "iOSPort");
            if ( !iOSPort.exists() ){
                extractIOSPort();
            }
        }
        File javaSources = new File(iOSPort, "src");
        Path jsp = new Path(getProject(), javaSources.getPath());
        Path srcDir = getJavac().getSrcdir();
        if ( srcDir == null ){
            srcDir = new Path(getProject());
            getJavac().setSrcdir(srcDir);
        }
        getJavac().getSrcdir().add(jsp);
        
        File nativeSources = new File(iOSPort, "nativeSources");
        
        Replace repl = (Replace)getProject().createTask("replace");
        repl.setToken("#define INCLUDE_ZOOZ");
        repl.setValue("#define NOT_INCLUDE_ZOOZ");
        repl.setFile(new File(nativeSources, "CodenameOne_GLViewController.h"));
        repl.execute();
        
        Path np = new Path(getProject(), nativeSources.getPath());
        Path nativeSourcesPath = this.getJavaSources();
        if ( nativeSourcesPath == null ){
            nativeSourcesPath = new Path(getProject());
            this.setNativeSources(nativeSourcesPath);
        }
        this.getNativeSources().add(np);
        
        
    }
    
    private void runPreJavac(){
        Javac javac = (Javac)getProject().createTask("javac");
        Path cp = javac.createClasspath();
        //cp.add(new Path(getProject(), "src"));
        File lib = new File(getProject().getBaseDir(), "lib");
        
        File impl = new File(lib, "impl");
        if ( !impl.exists() ){
            impl.mkdir();
        }
        
        File cls = new File(impl, "cls");
        if ( !cls.exists() ){
            cls.mkdir();
        }
        
        File build = new File(getProject().getBaseDir(), "build");
        if ( !build.exists()){
            build.mkdir();
        }
        
        File classes = new File(build, "classes");
        if ( !classes.exists() ){
            classes.mkdir();
        }
        javac.setSrcdir(new Path(getProject(),"src" ));
        
        if ( codenameOneSrc != null && codenameOneSrc.exists() ){
            javac.getSrcdir().add(new Path(getProject(), codenameOneSrc.getAbsolutePath()));
        } else { 
            cp.add(new Path(getProject(), "lib/CodenameOne.jar"));
        }
        cp.add(new Path(getProject(), "lib/impl/cls"));
        cp.add(new Path(getProject(), "build/classes"));
        
        
        
        Path bcp = javac.createBootclasspath();
        if ( getCldc() == null || !getCldc().exists()){
            bcp.add(new Path(getProject(), "lib/CLDC11.jar"));
        } else {
            bcp.add(new Path(getProject(), getCldc().getAbsolutePath()));
        }
        
        
        javac.setDestdir(classes);
        
        
        javac.execute();
        
        
    }
    
    
    
    
    private File getIOSPortDir(){
        return new File(getProject().getBaseDir(), "iOSPort");
        
    }
    
    
    
    private void extractIOSPort() throws IOException{
        File dir = getIOSPortDir();
        if ( !dir.exists() ){
            dir.mkdir();
            
            URL src = getClass().getResource("resources/iOSPort.zip");
            
            File tmp = File.createTempFile("iosport", "zip");
            tmp.delete();
            FileUtils.copyURLToFile(src, tmp);
            Expand unzip = (Expand)getProject().createTask("unzip");
            unzip.setTaskType("unzip");
            
            unzip.setSrc(tmp);
            unzip.setDest(iOSPort);
            unzip.execute();
        }
        
    }
    
    private void addLibs(){
        String lib = getLib();
        String toAdd = "libxml2.2.dylib,Security.framework,libzbar.a";
        if ( lib == null ){
            lib = "";
        }
        if ( lib.length() == 0 ){
            lib = toAdd;
            
        } else {
            lib += ","+toAdd;
        }
        
        setLib(lib);
    }
    
    
    private void setupProjectInfo() throws IOException{
        File basedir = getProject().getBaseDir();
        File props = new File(basedir, "codenameone_settings.properties");
        Properties p = new Properties();
        p.load(new FileInputStream(props));
        
        this.setAppName(p.getProperty("codename1.displayName"));
        this.setBundleDisplayName(this.getAppName());
        this.setBundleIdentifier(p.getProperty("codename1.ios.appid"));
        this.setBundleVersion(p.getProperty("codename1.version"));
        
    }
    
    
    public void setupJavac() throws IOException{
        Javac javac = (Javac)getProject().createTask("javac");
        setJavac(javac);
        Path cp = javac.createClasspath();
        //cp.add(new Path(getProject(), "src"));
        Path src = javac.getSrcdir();
        if ( src == null ){
            src = new Path(getProject());
            getJavac().setSrcdir(src);
        }
        src.add(new Path(getProject(), "src"));
        getJavac().setClasspath(cp);
        addIOSPortToClasspath();
        if ( codenameOneSrc != null && codenameOneSrc.exists() ){
            
            src.add(new Path(getProject(), codenameOneSrc.getAbsolutePath()));
        } else {
            cp.add(new Path(getProject(), "lib/CodenameOne.jar"));
        }
        cp.add(new Path(getProject(), "lib/impl/cls"));
        cp.add(new Path(getProject(), "build/classes"));
        //javac.setClasspath(cp);
        
        //Path bcp = javac.createBootclasspath();
        //bcp.add(new Path(getProject(), "lib/CLDC11.jar"));
        //javac.setBootclasspath(bcp);
        //cp.add(new Path(getProject(), "lib/CLDC11.jar"));
        
        //javac.setSrcdir(new Path(getProject(),"src" ));
        File build = new File(getProject().getBaseDir(), "build");
        File classes = new File(build, "classes");
        getJavac().setDestdir(classes);
        
        setJavac(javac);
        
        setupMainStub();
        
    }
    
    
    
    
    
    private void setupMainStub() throws IOException{
        File build = new File(getProject().getBaseDir(), "build");
        File generated = new File(build, "generated-src");
        if ( !generated.exists() ){
            generated.mkdir();
        }
        
        File comD = new File(generated, "com");
        File codename1D = new File(comD, "codename1");
        File implD = new File(codename1D, "impl");
        File iosD = new File(implD, "ios");
        if ( !iosD.exists()){
            iosD.mkdirs();
        }
        
        File main = new File(iosD, "Main.java");
        URL mainStub = this.getClass().getResource("resources/Main.java.tpl");
        FileUtils.copyURLToFile(mainStub, main);
        
        
        
        Properties p = new Properties();
        File cn1Props = new File(getProject().getBaseDir(), "codenameone_settings.properties");
        p.load(new FileInputStream(cn1Props));
        StringBuilder mainClass = new StringBuilder();
        String pkgName = p.getProperty("codename1.packageName");
        if ( pkgName != null && !"".equals(pkgName) ){
            mainClass.append(pkgName).append(".");
        }
        mainClass.append(p.getProperty("codename1.mainName"));
        
        
        Replace repl = (Replace)getProject().createTask("replace");
        repl.setEncoding("UTF-8");
        repl.setFile(main);
        repl.setToken("#MAIN_CLASS#");
        repl.setValue(mainClass.toString());
        repl.execute();
        
        repl = (Replace)getProject().createTask("replace");
        repl.setEncoding("UTF-8");
        repl.setFile(main);
        repl.setToken("#CODENAMEONE_REGISTER_NATIVE_STUBS#");
        repl.setValue("");
        repl.execute();
       
        getJavac().getSrcdir().add(new Path(getProject(), generated.getAbsolutePath()));
        
        
        
    }

    @Override
    protected void fixSkeletonProperties() throws IOException {
        super.fixSkeletonProperties(); //To change body of generated methods, choose Tools | Templates.
        Properties p = new Properties();
        p.load(new FileInputStream(this.getSkeletonPropertiesFile()));
        String lib = p.getProperty("xmlvm.lib");
        if ( lib == null ){
            lib = "";
        }
        if ( lib.length() > 0 ){
            lib += ",libzbar.a";
        } else {
            lib = "libzbar.a";
        }
        p.setProperty("xmlvm.lib", lib);
        p.store(new FileOutputStream(this.getSkeletonPropertiesFile()), "Updated xmlvm.lib");
    }
    
    
    
    
    
    

   // TODO customize method names to match custom task
    // property and type (handled by inner class) names

    /* For a simple option:
     private boolean opt;
     public void setOpt(boolean b) {
     opt = b;
     }
     // <customtask opt="true"/>
     */

    /* For a simple property based on a string:
     private String myprop;
     public void setMyprop(String s) {
     myprop = s;
     }
     // <customtask myprop="some text here"/>
     */

    /* For a simple property based on a file:
     private File myfile;
     public void setMyfile(File f) {
     // Note: f will automatically be absolute (resolved from project basedir).
     myfile = f;
     }
     // <customtask myfile="foo.txt"/>
     */

    /* Custom nested elements:
     public static class Nestme {
     String val; // accessible from execute()
     public void setVal(String s) {
     val = s;
     }
     }
     private List<Nestme> nestmes = new LinkedList<Nestme>();
     public Nestme createNestme() {
     Nestme n = new Nestme();
     nestmes.add(n);
     return n;
     }
     // Or:
     public void addNestme(Nestme n) {
     nestmes.add(n);
     }
     // <customtask>
     //     <nestme val="something"/>
     // </customtask>
     */

    /* To add embedded filesets:
     private List<FileSet> filesets = new LinkedList<FileSet>();
     public void addFileset(FileSet fs) {
     filesets.add(fs);
     }
     // <customtask>
     //     <fileset dir="foo">
     //         <include name="*.txt"/>
     //     </fileset>
     // </customtask>
     // In execute() you can do:
     for (FileSet fs : filesets) {
     DirectoryScanner ds = fs.getDirectoryScanner(project);
     File basedir = ds.getBasedir();
     for (String file : ds.getIncludedFiles()) {
     // process it...
     }
     }
     */

    /* For nested text:
     private StringBuilder text;
     public void addText(String raw) {
     String s = getProject().replaceProperties(raw.trim());
     if (text == null) {
     text = new StringBuilder(s);
     } else {
     text.append(s);
     }
     }
     // <customtask>
     //     Some text...
     // </customtask>
     */

    /* Some sort of path (like classpath or similar):
     private Path path;
     public void setPath(Path p) {
     if (path == null) {
     path = p;
     } else {
     path.append(p);
     }
     }
     public Path createPath () {
     if (path == null) {
     path = new Path(project);
     }
     return path.createPath();
     }
     public void setPathRef(Reference r) {
     createPath().setRefid(r);
     }
     // <customtask path="foo:bar"/>
     // <customtask>
     //     <path>
     //         <pathelement location="foo"/>
     //     </path>
     // </customtask>
     // Etc.
     */

    /* One of a fixed set of choices:
     public static class FooBieBletch extends EnumeratedAttribute { // or use Java 5 enums
     public String[] getValues() {
     return new String[] {"foo", "bie", "bletch"};
     }
     }
     private String mode = "foo";
     public void setMode(FooBieBletch m) {
     mode = m.getValue();
     }
     // <customtask mode="bletch"/>
     */
    public @Override
    void execute() throws BuildException {
        File build = new File(getProject().getBaseDir(), "build");
        if ( !build.exists() ){
            build.mkdir();
        }
        File ios = new File(build, "ios");
        if ( ios.exists() && clean ){
            try {
                FileUtils.deleteDirectory(ios);
            } catch (IOException ex) {
                Logger.getLogger(CodenameOneIOSBuildTask.class.getName()).log(Level.SEVERE, null, ex);
                throw new BuildException(ex);
            }
            ios.mkdirs();
        }
        
        
        
        //runPreJavac();
        try {
            
            setupJavac();
                    
            setupProjectInfo();
        } catch (IOException ex) {
           throw new BuildException(ex);
        }
        addLibs();
        
        super.execute();
        
        
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

    @Override
    protected void copyNativeLibraries() {
        super.copyNativeLibraries(); //To change body of generated methods, choose Tools | Templates.
        if ( debug ){
            try {
                // We need to copy the fat version of libzbar so that it works in the simulator
                File nativeLibDir = this.getNativeLibsPath();
                File dest = new File(nativeLibDir, "libzbar.a");
                URL src = getClass().getResource("resources/libzbar.a");
                FileUtils.copyURLToFile(src, dest);
            } catch (IOException ex) {
                Logger.getLogger(CodenameOneIOSBuildTask.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }

    
    
    /**
     * @return the cldc
     */
    public File getCldc() {
        return cldc;
    }

    /**
     * @param cldc the cldc to set
     */
    public void setCldc(File cldc) {
        this.cldc = cldc;
    }
    
}
