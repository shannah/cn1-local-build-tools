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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    
    
    
    protected Set<String> getCurrentXcodeFiles(String pbxprojContent){
        Set<String> out = new HashSet<String>();
        Scanner scanner = new Scanner(pbxprojContent);
        int state = 0;
        Pattern regex = Pattern.compile("\\d+ /\\* ([^ ]+) \\*/");
        while ( scanner.hasNextLine()){
            String line = scanner.nextLine();
            if ( state == 0 && line.indexOf("/* Application */ = {") >= 0){
                state = 1;
            } else if ( state == 1 ){
                Matcher m = regex.matcher(line);
                if ( m.find()){
                    String path = m.group(1);
                    if ( path.indexOf("/") < 0 ){
                        out.add(path);
                    }
                } else if ( ");".equals(line.trim()) ){
                    return out;
                }
            }
            
        }
        throw new RuntimeException("Could not find the end of the file reference section");
    }
    
    protected Set<String> getCurrentAppSrcFiles(){
        File appDir = new File(getOut(), "build/xcode/src/app");
        Set<String> out = new HashSet<String>();
        out.addAll(Arrays.asList(appDir.list()));
        return out;
        
    }
    
    protected void updatePbxProj() throws IOException{
        String contents = FileUtils.readFileToString(getPbxProjectFile());
        contents = updatePbxProj(contents);
        FileUtils.writeStringToFile(getPbxProjectFile(), contents);
        
    }
    
    protected String updatePbxProj(String pbxProjContent){
        Set<String> inProject = getCurrentXcodeFiles(pbxProjContent);
        Set<String> inFileSystem = getCurrentAppSrcFiles();
        Set<String> missingFromProject = new HashSet<String>();
        missingFromProject.addAll(inFileSystem);
        missingFromProject.removeAll(inProject);
        Set<String> missingFromFileSystem = new HashSet<String>();
        missingFromFileSystem.addAll(inProject);
        missingFromFileSystem.removeAll(inFileSystem);
        
        File appDir = new File(getOut(), "build/xcode/src/app");
        
        if ( !missingFromProject.isEmpty()){
            System.out.println("Found "+missingFromProject.size()+" missing from the Xcode project.  Adding them now...");
            System.out.println(missingFromProject);
            List<File> filesToAdd = new ArrayList<File>();
            for ( String s : missingFromProject ){
                filesToAdd.add(new File(appDir, s));
            }
            pbxProjContent = this.injectFilesIntoXcodeProject(pbxProjContent, filesToAdd.toArray(new File[0]));
        }
        
        if ( !missingFromFileSystem.isEmpty() ){
            System.out.println("Found "+missingFromFileSystem.size()+" missing from the fileSystem.  Removing them in Xcode...");
            System.out.println(missingFromFileSystem);
            List<File> filesToRemove = new ArrayList<File>();
            for ( String s : missingFromFileSystem ){
                filesToRemove.add(new File(appDir, s));
            }
            pbxProjContent = removeFilesFromXcodeProject(pbxProjContent, filesToRemove.toArray(new File[0]));
        }
        return pbxProjContent;
        
    }
    
    
    protected String removeFilesFromXcodeProject(String projFileContent, File[] filesToRemove){
        StringBuilder sb = new StringBuilder();
        Scanner scanner = new Scanner(projFileContent);
        while ( scanner.hasNextLine()){
            String line = scanner.nextLine();
            boolean found = false;
            for ( File f : filesToRemove ){
                if ( line.indexOf("/* "+f.getName()+" */") >= 0){
                    found = true;
                }
            }
            if ( !found ){
                sb.append(line).append("\n");
            }
        }
        
        
        return sb.toString();
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
        boolean doingClean = clean || !ios.exists();
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
        
        if ( !doingClean ){
            try {
                // Let's add all of the missing files
                updatePbxProj();
            } catch (IOException ex) {
                Logger.getLogger(CodenameOneIOSBuildTask.class.getName()).log(Level.SEVERE, null, ex);
            }
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
    
    
    
    private static class FileResource {
        static Map<String,String> sourcefiles = new HashMap<String, String>();
        static Map<String,String> hiddensourcefiles = new HashMap<String, String>();
        static {
            
           //sourcefiles.put("a", "library");
           sourcefiles.put("c", "sourcecode.c.c");
           sourcefiles.put("m", "sourcecode.c.objc");
           sourcefiles.put("mm", "sourcecode.cpp.objcpp");
           sourcefiles.put("c++", "sourcecode.cpp.cpp");
           sourcefiles.put("cpp", "sourcecode.cpp.cpp");
           sourcefiles.put("xib", "sourcecode.xib.xib");

           
           hiddensourcefiles.put("h", "sourcecode.c.h");
        }
        
            private String  type        = null;
            private boolean isSource    = false;
            private boolean isValid     = false;
            private boolean isBuildable = false;


            private FileResource(String fname) {
                if (fname == null)
                    return;
                int point = fname.lastIndexOf(".");
                if (point < 0 || point == fname.length())
                    return;

                String ext = fname.substring(point + 1);

                type = sourcefiles.get(ext);
                if (type != null) {
                    isValid = true;
                    isBuildable = true;
                    isSource = true;
                    return;
                }

                type = hiddensourcefiles.get(ext);
                if (type != null) {
                    isValid = true;
                    isSource = true;
                    return;
                }
            }

            @Override
            public String toString() {
                return "[type=" + type + (isSource ? ", Source" : "") + (isValid ? ", Valid" : "")
                        + (isBuildable ? ", Buildable" : "") + "]";
            }
        }
    
    private File getPbxProjectFile(){
        return new File(this.getOut(), "dist/"+this.getBundleDisplayName()+".xcodeproj/project.pbxproj" );
        
    }
    
    private void injectFilesIntoXcodeProject(File[] files) throws IOException {
        injectFilesIntoXcodeProject(getPbxProjectFile(), files);
    }
    
    private void injectFilesIntoXcodeProject(File projectFile, File[] files) throws IOException{
        String contents = FileUtils.readFileToString(projectFile);
        contents = injectFilesIntoXcodeProject(contents, files);
        FileUtils.writeStringToFile(projectFile, contents);
    }
    
    /**
     * Based on https://github.com/shannah/cn1/blob/master/Ports/iOSPort/xmlvm/src/xmlvm/org/xmlvm/proc/out/build/XCodeFile.java
     * @param template
     * @param filter 
     */
    private String injectFilesIntoXcodeProject(String template, File[] files) {

        int nextid = 0;
        Pattern idPattern = Pattern.compile(" (\\d+) ");
        Matcher m = idPattern.matcher(template);
        while ( m.find() ){
            int curr = Integer.parseInt(m.group(1));
            if ( curr > nextid ){
                nextid = curr;
            }
        }
        nextid++;

        StringBuilder filerefs = new StringBuilder();
        StringBuilder buildrefs = new StringBuilder();
        StringBuilder display = new StringBuilder();
        StringBuilder source = new StringBuilder();
        StringBuilder resource = new StringBuilder();


        for (File f : files) {
            String fname = f.getName();
            if ( template.indexOf(" "+fname+" ") >= 0 ){
                continue;
            }
            FileResource fres = new FileResource(fname);
            if (f.exists()) {
                filerefs.append("\t\t").append(nextid);
                filerefs.append(" /* ").append(fname).append(" */");
                filerefs.append(" = { isa = PBXFileReference; fileEncoding = 4; lastKnownFileType = ");
                filerefs.append(fres.type).append("; path = \"");
                filerefs.append(fname).append("\"; sourceTree = \"<group>\"; };");
                filerefs.append('\n');

                display.append("\t\t\t\t").append(nextid);
                display.append(" /* ").append(fname).append(" */");
                display.append(",\n");

                if (fres.isBuildable) {
                    int fileid = nextid;
                    nextid++;
                    buildrefs.append("\t\t").append(nextid);
                    buildrefs.append(" /* ").append(fname);
                    buildrefs.append(" in ").append(fres.isSource ? "Sources" : "Resources");
                    buildrefs.append(" */ = {isa = PBXBuildFile; fileRef = ").append(fileid);
                    buildrefs.append(" /* ").append(fname);
                    buildrefs.append(" */; };\n");

                    if (fres.isSource) {
                        source.append("\t\t\t\t").append(nextid);
                        source.append(" /* ").append(fname).append(" */");
                        source.append(",\n");
                    }
                }
                nextid++;
            }
        }
        String data = template;
        data = data.replace("/* End PBXFileReference section */", filerefs.toString() + "/* End PBXFileReference section */");
        data = data.replace("/* End PBXBuildFile section */", buildrefs.toString() + "/* End PBXBuildFile section */");

        // The next two we probably shouldn't do by regex because there is no clear pattern.
        Stack<String> buffer = new Stack<String>();

        Stack<String> backtrackStack = new Stack<String>();

        Scanner scanner = new Scanner(data);
        while ( scanner.hasNextLine()){
            String line = scanner.nextLine();
            if ( line.indexOf("/* End PBXSourcesBuildPhase section */") >= 0 ){
                // Found the end, let's backtrack
                while ( !buffer.isEmpty()){
                    String l = buffer.pop();
                    backtrackStack.push(l);
                    if ( ");".equals(l.trim())){
                        // This is the closing of the sources list
                        // we can insert the sources here
                        buffer.push(source.toString());
                        while ( !backtrackStack.isEmpty()){
                            buffer.push(backtrackStack.pop());
                        }
                        break;
                    }
                }
            } else if ( line.indexOf("name = Application;") >= 0 ){
                while ( !buffer.isEmpty()){
                    String l = buffer.pop();
                    backtrackStack.push(l);
                    if ( ");".equals(l.trim())){
                        buffer.push(display.toString());
                        while ( !backtrackStack.isEmpty()){
                            buffer.push(backtrackStack.pop());
                        }
                        break;
                    }
                }
            }
            buffer.push(line);
        }
        StringBuilder sb = new StringBuilder();
        String[] lines = buffer.toArray(new String[0]);
        for ( String line : lines ){
            sb.append(line).append("\n");
        }
        data = sb.toString();
        return data;
    }
    
}
