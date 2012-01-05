package dridco.jmx.web
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.server.Server
import java.io.File

object JettyLauncher extends App { // this is my entry object as specified in sbt project definition
        
	val server = new Server(8080);

	val context = new WebAppContext();

	val cd = new File(".").getAbsolutePath()
	context.setDescriptor(cd + "/WEB-INF/web.xml");
	context.setResourceBase(cd + "/src/main/webapp");
	context.setContextPath("/");
	context.setParentLoaderPriority(true);	context.setExtraClasspath(cd + "/src/main/resources")
	server.setHandler(context);
    server.start();
    server.join();    }