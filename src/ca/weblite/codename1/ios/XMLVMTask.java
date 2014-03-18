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

import ca.weblite.xmlvm.XMLVM;
import ca.weblite.xmlvm.XmlvmHelper;
import com.simontuffs.onejar.Boot;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.Replace;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ZipFileSet;


/**
 * @author shannah
 */
public class XMLVMTask extends Task {
    
    /**
     * Nested javac task
     */
    private Javac javac;
    
    
    /**
     *  The type of project skeleton to build.
     */ 
    private String skeleton="iphone";
    
    /**
     * The app name.
     */
    private String appName;
    private File out;
    private String bundleIdentifier;
    private String bundleVersion;
    private String bundleDisplayName;
    private Path nativeSources;
    private String nativeIncludes = "**/*.h **/*.c **/*.m **/*.a";
    private Path javaSources;
    private String lib;
    protected boolean clean = false;
    
    public void setClean(boolean clean){
        this.clean = clean;
    }
    
    public boolean getClean(){
        return this.clean;
    }
    
    /**
     * Path where to look for the xmlvm.jar file which is used
     * for external execution of xmlvm.  If we try to execute it
     * internally the onejar structure limits us to only one 
     * run.
     */
    private Path xmlvmClasspath;

    @Override
    public void init() throws BuildException {
        super.init(); //To change body of generated methods, choose Tools | Templates.
         if ( getOut() == null ){
            File build = new File(getProject().getBaseDir(), "build");
            if ( !build.exists() ){
                build.mkdir();
            }
            File ios = new File(build, "ios");
            if ( !ios.exists() ){
                ios.mkdir();
            }
            setOut(ios);
            
            
        }
        
        
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
    
    
    
    protected String[] buildXMLVMArgs(){
        ArrayList<String> l = new ArrayList<String>();
        //l.add("java");
        //l.add("-jar");
        //l.add("xmlvm.jar");
        Path sourcePath = getJavac().getSourcepath();
        
       
        
        if ( getSkeleton() != null ){
            l.add("--skeleton="+getSkeleton());
        }
        
        if ( getOut() != null ){
            l.add("--out="+getOut().getAbsolutePath());
        }
        
        if ( getAppName() != null ){
            System.out.println("Setting app name to "+getAppName());
            l.add("--app-name="+getAppName());
        } 
        
        if ( getBundleIdentifier() != null ){
            l.add("-DBundleIdentifier="+getBundleIdentifier());
        }
        
        if ( getBundleVersion() != null ){
            l.add("-DBundleVersion="+getBundleVersion());
        }
        
        if ( getBundleDisplayName() != null ){
            l.add("-DBundleDisplayName="+getBundleDisplayName());
        }
        
        
        StringBuilder sb = new StringBuilder();
        if ( getLib() != null ){
            sb.append(getLib()).append(",");
        }
    
        //for ( String lib : findNativeLibs()){
        //    sb.append(lib).append(",");
        //}
        if ( sb.length() > 0 ){  
            
            l.add("--lib="+sb.substring(0, sb.length()-1));
        }
        
        System.out.println(l);
        
        return l.toArray(new String[0]);
    }
    
    protected List<String> findNativeLibs(){
        List<String> out = new ArrayList<String>();
        for ( File f : getNativeLibsPath().listFiles()){
            if (f.getName().endsWith(".a")){
                out.add(f.getName());
            }
        }
        return out;
    }
    
    protected void createSkeletonProject() throws Exception{
        
        String[] args = buildXMLVMArgs();
        XmlvmHelper.runXmlvm(getProject(), xmlvmClasspath, args );
        //Boot.main(args);
        
        File src = new File(out, "src");
        if ( src.exists() ){
            File javaSrc = new File(src, "java");
            if ( javaSrc.exists() ){
                File myF = new File(javaSrc, "my");
                if ( myF.exists()){
                    Delete del = (Delete)getProject().createTask("delete");
                    del.setDir(myF);
                    del.execute();
                }
            }
        }   
    }
    
    
    protected void setup(){
         
    }
    
    protected File getSkeletonPropertiesFile(){
        return new File(getOut(), "xmlvm.properties");
    }
    
    
    
    
    protected void fixSkeletonProperties() throws IOException  {
        Properties p = new Properties();
        
        p.load(new FileInputStream(getSkeletonPropertiesFile()));
        if ( getBundleIdentifier() != null ){
            p.setProperty("bundle.identifier", getBundleIdentifier());
        }
        
        if ( getBundleVersion() != null ){
            p.setProperty("bundle.version", getBundleVersion());
        }
        
        p.setProperty("xmlvm.backend", "c");
        //p.setProperty("xmlvm.resource", "btres/");
        
        p.store(new FileOutputStream(getSkeletonPropertiesFile()), "Fixed properties");
    }
    
    protected File getNBProjectDir(){
        return new File(getOut(), "nbproject");
    }
    
    protected File getProjectPropertiesFile(){
        return new File(getNBProjectDir(), "project.properties");
    }
    
    protected void fixProjectProperties() throws IOException {
        Properties p = new Properties();
        p.load(new FileInputStream(getProjectPropertiesFile()));
        String runClassPath = p.getProperty("run.classpath");
        if ( runClassPath == null ){
            runClassPath = "";
        }
        runClassPath = getNativeIOSJarFile().getAbsolutePath()+":"+runClassPath;
        p.setProperty("run.classpath", runClassPath);
        p.store(new FileOutputStream(getProjectPropertiesFile()), "Updated the run classpath");
        
        Replace repl = (Replace)getProject().createTask("replace");
        File buildXcode = new File(getNBProjectDir(), "build-Xcode.xml");
        repl.setFile(buildXcode);
        repl.setToken("-Xmx512m");
        repl.setValue("-Xmx2G");
        
        repl.execute();
        
        repl = (Replace)getProject().createTask("replace");
        //File buildXcode = new File(getNBProjectDir(), "build-Xcode.xml");
        repl.setFile(buildXcode);
        repl.setToken("<arg value=\"-DSupportedInterfaceOrientations=${orientations.supported}\"/>");
        repl.setValue("<arg value=\"-DSupportedInterfaceOrientations=${orientations.supported}\"/>\n<arg value=\"--disable-vtable-optimizations\"/>");
        
        repl.execute();
        
        
                
    }
    
    
    private File getJavacDestDir(){
        File buildDir = new File(getOut(), "build");
        if ( !buildDir.exists() ){
            buildDir.mkdir();
        }
        File classes = new File(buildDir, "classes");
        if ( !classes.exists() ){
            classes.mkdir();
        }
        return classes;
    }
    
    
    private void setupJavac(){
        getJavac().setDestdir(getJavacDestDir());
    }
    
    private void executeJavac(){
        getJavac().execute();
    }
    
    protected File getResourcesPath(){
        File res = new File(getOut(), "resources");
        if ( !res.exists() ){
            res.mkdir();
        }
        return res;
    }
    
    protected File getNativeLibsPath(){
        File libs = new File(getOut(), "btres");
        if ( !libs.exists() ){
            libs.mkdir();
        }
        return libs;
    }
    
    protected void copyNativeLibraries(){
        String libraryIncludes = "**/*.a";
        //copyFromPath(getJavac().getSrcdir(), getNativeLibsPath(), libraryIncludes, null);
        copyFromPath(getNativeSources(), getNativeLibsPath(), libraryIncludes, null);
    }
    
    
    
    protected void copyResources(){
        copyFromClassPath(getResourcesPath(), null, "**/*.java");
        copyFromPath(getNativeSources(), getResourcesPath(), null, nativeIncludes);
        copyFromPath(getJavac().getSrcdir(), getResourcesPath(), null, "**/*.java");
    }
    
    protected void copyClassFiles(){
        copyFromClassPath(getJavacDestDir(), "**/*.class", null);
    }
    
    protected void copyFromPath(Path inPath, File toDir, String includes, String excludes){
        Copy copy = (Copy)getProject().createTask("copy");
        copy.setTodir(toDir);
        if ( !toDir.exists() ){
            if ( !toDir.mkdir() ){
                throw new RuntimeException("Failed to create directory "+toDir);
            } else {
                System.out.println("Successfully created directory "+toDir);
            }
        }
        System.out.println("Setting up copy to dir "+toDir);
        ArrayList<File> inputPaths = new ArrayList<File>();
        
       
        
        for ( String path : inPath.list()){
            File f = new File(path);
            if ( f.exists() ){
                inputPaths.add(f);
            }
        }
        
        
        if ( inputPaths.isEmpty() ){
            return;
        }
        boolean empty = true;
        
        for (File f : inputPaths){
            System.out.println("Adding file to copy set : "+f);
            if ( f.isDirectory()){
                
                FileSet fs = new FileSet();
                fs.setDir(f);
                fs.setExcludes(excludes);
                fs.setErrorOnMissingDir(false);
                if ( excludes != null){
                    fs.setExcludes(excludes);
                }
                if ( includes != null ){
                    fs.setIncludes(includes);
                }
                copy.addFileset(fs);
                empty = false;
            } else if ( f.getName().endsWith(".jar")){
                Expand unzip = (Expand)getProject().createTask("unzip");
                unzip.setSrc(f);
                unzip.setDest(toDir);
                PatternSet pattern = new PatternSet();
                pattern.setIncludes(includes);
                pattern.setExcludes(excludes);
                unzip.addPatternset(pattern);
                unzip.execute();
                //copy.addFileset(zs);
                empty = false;
            }
        }
        if ( empty ){
            return;
        }
        //copy.setVerbose(true);
        copy.execute();
    }
    
    protected void copyFromClassPath(File toDir, String includes, String excludes ){
        Path p = new Path(getProject());
        p.add(getJavac().getClasspath());
        if ( getJavac().getBootclasspath() != null ){
            p.add(getJavac().getBootclasspath());
        }
        
        copyFromPath(p, toDir, includes, excludes);
        
        
    }
    
    
    
    private File getNativeIOSJarFile() throws IOException{
        File iosJar = new File(getOut(), "nativeios.jar");
        if ( !iosJar.exists() ){
            
            File tmp = File.createTempFile("native", "dir");
            tmp.delete();
            tmp.mkdir();
            
            
            File objc = new File(tmp, "objc");
            
            Copy copy = (Copy)getProject().createTask("copy");
            copy.setTodir(objc);
            if ( getNativeSources() != null ){
                for ( String root : getNativeSources().list()){
                    File f = new File(root);
                    FileSet fs = null;
                    if ( f.isDirectory() ){
                        fs = new FileSet();
                        fs.setDir(f);
                    } else if ( f.getName().endsWith(".jar")){
                        fs = new ZipFileSet();
                        ((ZipFileSet)fs).setSrc(f);
                    }
                    if ( fs != null ){
                        fs.setIncludes(getNativeIncludes());
                        fs.setErrorOnMissingDir(false);
                        copy.addFileset(fs);
                    }
                }
            }
            copy.execute();
            
            
            Zip zip = (Zip)getProject().createTask("zip");
            zip.setDestFile(iosJar);
            zip.setBasedir(tmp);
            zip.setIncludes("objc/**");
            zip.execute();
            
            Delete del = (Delete)getProject().createTask("delete");
            del.setDir(tmp);
            del.execute();
            
            
        }
        return iosJar;
    }
    
    
    public void copyMainStub(){
        
    }
   
    
    public void runXMLVMProject(){
        Project p = new Project();
        
        File build = new File(getProject().getBaseDir(), "build");
        File ios = new File(build, "ios");
        File buildXml = new File(ios, "build.xml");
        p.setBaseDir(ios);
        p.setUserProperty("ant.file", buildXml.getAbsolutePath());
        p.init();
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        p.addReference("ant.projectHelper", helper);
        helper.parse(p, buildXml);
        p.executeTarget(p.getDefaultTarget());
        fixGCC();
        
        
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
        init();
        try {
            if ( clean || out == null || !out.exists()){
                createSkeletonProject();

                fixSkeletonProperties();
                fixProjectProperties();
                //fixGCC();
                setupJavac();
                copyNativeLibraries();
                copyResources();
                copyClassFiles();
                executeJavac();
                runXMLVMProject();
                
                // Step 3: Generate Dependency graph
                XMLVM xmlvm = new XMLVM();
                xmlvm.setProject(getProject());
                xmlvm.setXmlvmClasspath(xmlvmClasspath);
                File xmlvmDir = xmlvm.getXmlvmCacheDir("xmlvm");
                xmlvm.setJavac(this.getJavac());
                xmlvm.setClassPath(this.getJavac().getClasspath());
                xmlvm.setJavaBuildDir(new File(getProject().getBaseDir(), "build/classes"));
                xmlvm.createDependencyGraph(xmlvm.getJavaBuildDir(), xmlvmDir);
                
                
            } else {
                // The project already exists, we're just updating some stuff.
                System.out.println("Project already exists.  Let's just update the source files.");
                XMLVM xmlvm = new XMLVM();
                xmlvm.setXmlvmClasspath(xmlvmClasspath);
                xmlvm.setJavac(this.getJavac());
                xmlvm.setProject(getProject());
                xmlvm.setClassPath(this.getJavac().getClasspath());
                xmlvm.setJavaBuildDir(new File(getProject().getBaseDir(), "build/classes"));
                File srcDest = new File(out, "build/xcode/src/app" );
                xmlvm.setDest(srcDest);
                if ( !javac.getSrcdir().equals(xmlvm.getSrc())){
                    xmlvm.setSrc(javac.getSrcdir());
                }
                
                xmlvm.doRegularBuild();
                
            }
            
            
        } catch (Exception ex) {
            throw new BuildException(ex);
        }
        
        
        
        
    }

    
    private void fixGCC(){
        File dist = new File(out, "dist");
        for ( File child : dist.listFiles() ){
            if ( child.getName().endsWith(".xcodeproj")){
                File proj = new File(child, "project.pbxproj");
                
                Replace repl = (Replace)getProject().createTask("replace");
                
                repl.setToken("GCC_VERSION = \"com.apple.compilers.llvmgcc42\";");
                repl.setValue("GCC_VERSION = \"\";");
                repl.setFile(proj);
                repl.execute();
            }
        }
        
    }
    /**
     * @return the javac
     */
    public Javac getJavac() {
        return javac;
    }

    /**
     * @param javac the javac to set
     */
    public void setJavac(Javac javac) {
        this.javac = javac;
    }

    

    /**
     * @return the skeleton
     */
    public String getSkeleton() {
        return skeleton;
    }

    /**
     * @param skeleton the skeleton to set
     */
    public void setSkeleton(String skeleton) {
        this.skeleton = skeleton;
    }

    /**
     * @return the appName
     */
    public String getAppName() {
        return appName;
    }

    /**
     * @param appName the appName to set
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * @return the out
     */
    public File getOut() {
        return out;
    }

    /**
     * @param out the out to set
     */
    public void setOut(File out) {
        this.out = out;
    }

    /**
     * @return the bundleIdentifier
     */
    public String getBundleIdentifier() {
        return bundleIdentifier;
    }

    /**
     * @param bundleIdentifier the bundleIdentifier to set
     */
    public void setBundleIdentifier(String bundleIdentifier) {
        this.bundleIdentifier = bundleIdentifier;
    }

    /**
     * @return the bundleVersion
     */
    public String getBundleVersion() {
        return bundleVersion;
    }

    /**
     * @param bundleVersion the bundleVersion to set
     */
    public void setBundleVersion(String bundleVersion) {
        this.bundleVersion = bundleVersion;
    }

    /**
     * @return the bundleDisplayName
     */
    public String getBundleDisplayName() {
        return bundleDisplayName;
    }

    /**
     * @param bundleDisplayName the bundleDisplayName to set
     */
    public void setBundleDisplayName(String bundleDisplayName) {
        this.bundleDisplayName = bundleDisplayName;
    }

    /**
     * @return the nativeSources
     */
    public Path getNativeSources() {
        return nativeSources;
    }

    /**
     * @param nativeSources the nativeSources to set
     */
    public void setNativeSources(Path nativeSources) {
        this.nativeSources = nativeSources;
    }

    /**
     * @return the nativeIncludes
     */
    public String getNativeIncludes() {
        return nativeIncludes;
    }

    /**
     * @param nativeIncludes the nativeIncludes to set
     */
    public void setNativeIncludes(String nativeIncludes) {
        this.nativeIncludes = nativeIncludes;
    }

    /**
     * @return the javaSources
     */
    public Path getJavaSources() {
        return javaSources;
    }

    /**
     * @param javaSources the javaSources to set
     */
    public void setJavaSources(Path javaSources) {
        this.javaSources = javaSources;
    }

    /**
     * @return the lib
     */
    public String getLib() {
        return lib;
    }

    /**
     * @param lib the lib to set
     */
    public void setLib(String lib) {
        this.lib = lib;
    }
    
}
