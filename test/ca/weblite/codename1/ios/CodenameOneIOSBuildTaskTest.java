/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.codename1.ios;

import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author shannah
 */
public class CodenameOneIOSBuildTaskTest {
    
    public CodenameOneIOSBuildTaskTest() {
    }

    @Test
    public void testSomeMethod() {
        org.apache.tools.ant.Project proj = new org.apache.tools.ant.Project();
        
        
        
        proj.setBasedir("/Volumes/Windows VMS/NetbeansProjects/HelloWorldCN1");
        CodenameOneIOSBuildTask build = new CodenameOneIOSBuildTask();
        build.setProject(proj);
        build.setCodenameOneSrc(new File("/Volumes/Windows VMS/src/codenameone-read-only/CodenameOne/src"));
        build.setCldc(new File("/Volumes/Windows VMS/src/codenameone-read-only/CodenameOne/CLDC11.jar"));
        
        //build.clean = true;
        build.execute();
    }
    
}
