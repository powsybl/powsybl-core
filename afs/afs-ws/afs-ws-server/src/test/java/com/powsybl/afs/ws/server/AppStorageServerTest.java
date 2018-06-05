package com.powsybl.afs.ws.server;

import com.powsybl.afs.ws.storage.RemoteListenableAppStorage;
import com.powsybl.commons.exceptions.UncheckedUriSyntaxException;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import static org.junit.Assert.assertEquals;
import java.io.FileReader;

import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Model;




import com.powsybl.afs.ws.storage.RemoteAppStorage;
import java.net.URISyntaxException;

import org.jboss.arquillian.container.test.api.RunAsClient;
import com.powsybl.afs.storage.AbstractAppStorageTest;
import org.jboss.arquillian.junit.Arquillian;
import com.powsybl.afs.storage.ListenableAppStorage;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.net.URI;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.io.File;
import java.util.List;

/**
 * @author Ali Tahanout <ali.tahanout at rte-france.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@RunWith(Arquillian.class)
@RunAsClient
public class AppStorageServerTest extends AbstractAppStorageTest {

    @ArquillianResource
    private URL baseUrl;

    @Deployment
    public static WebArchive createTestArchive() {
    	List<String> listDependencies  = null;
    	List<File> lf = new ArrayList<>();
    	try {
    		listDependencies  = getListDependencies(new File("pom.xml"));
    		
    		for (String dp : listDependencies) {
    			try {
        			List<File> filesLib0 = Maven
    						.configureResolver()
    						.useLegacyLocalRepo(true)
    						.withMavenCentralRepo(false)
    						.withClassPathResolution(true)
    						.loadPomFromFile("pom.xml")
    						.importRuntimeDependencies()
        					.resolve(dp)
        					.withTransitivity()
        					.asList(File.class);
        			
                    for (File f : filesLib0) {
                    	if (!f.getName().equals("resteasy-jaxrs-3.1.4.Final.jar") && !f.getName().equals("slf4j-simple-1.7.22.jar")) {
                        	lf.add(f);
                    	}
                    }
    	    	} catch (Exception e0) {System.out.println(dp+" ---> "+e0.getMessage());}
    		}
    		
    	} catch (IOException e) {}
/*        File[] filesLib = Maven
        						.configureResolver()
        						.useLegacyLocalRepo(true)
        						.withMavenCentralRepo(false)
        						.withClassPathResolution(true)
        						.loadPomFromFile("pom.xml")
        						.importRuntimeDependencies()
        						.resolve("org.mockito:mockito-all","com.powsybl:powsybl-afs-mapdb")
        						.withTransitivity()
        						.asFile();
*/    
    	File[] slf = new File[lf.size()];
    	int i=0;
    	for (File ulf : lf) {
    		slf[i++] = ulf;
    	}
        return ShrinkWrap.create(WebArchive.class, "afs-ws-server-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addPackage(AppStorageServerTest.class.getPackage())
                .addAsLibraries(slf );
        		//filesLib);
    }
    static List<String> getListDependencies(File pomFile) throws IOException {
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(pomFile);
            Model model = mavenReader.read(fileReader);
            model.setPomFile(pomFile);
            MavenProject project = new MavenProject(model);
            project.setFile(pomFile);
            
            List<String> listDependencies = new ArrayList<>();
            model.getDependencies().forEach(a -> listDependencies.add(a.getGroupId()+":"+a.getArtifactId()));

            return listDependencies;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (fileReader != null) {
                fileReader.close();
            }
        }
    }
    
    private URI getRestUri() {
        try {
            return baseUrl.toURI();
        } catch (URISyntaxException e) {
            throw new UncheckedUriSyntaxException(e);
        }
    }

    @Override
    protected ListenableAppStorage createStorage() {
        URI restUri = getRestUri();
        RemoteAppStorage storage = new RemoteAppStorage(AppDataBeanMock.TEST_FS_NAME, restUri);
        return new RemoteListenableAppStorage(storage, restUri);
    }

    @Test
    public void getFileSystemNamesTest() {
        List<String> fileSystemNames = RemoteAppStorage.getFileSystemNames(getRestUri());
        assertEquals(Collections.singletonList(AppDataBeanMock.TEST_FS_NAME), fileSystemNames);
    }
}