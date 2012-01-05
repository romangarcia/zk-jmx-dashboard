// set the name of the project
name := "Jmx-Dashboard"

version := "1.0"

organization := "dridco"

// set the Scala version used for the project
scalaVersion := "2.9.1"

// test dependencies
libraryDependencies ++= Seq(
    "org.scalatest" % "scalatest_2.9.1" % "1.6.1",
    "junit" % "junit" % "4.8.2"
)   

// compile dependencies
libraryDependencies ++= Seq(
    "org.scala-tools.time" % "time_2.9.0-1" % "0.4",
 	"org.zkoss.zk" % "zk" % "5.0.9",
    "org.zkoss.zk" % "zul" % "5.0.9",
 	"org.zkoss.zk" % "zkplus" % "5.0.9",
 	"org.zkoss.zk" % "zhtml" % "5.0.9",
    "org.zkoss.common" % "zcommon" % "5.0.9",
    "org.zkoss.common" % "zweb" % "5.0.9",
    "org.zkoss.theme" % "breeze" % "5.0.9",
	"rhino" % "js" % "1.7R1",
	"org.zkoss.zk" % "zkex" % "3.6.3",
	"org.zkoss.zk" % "zkmax" % "3.6.3",
    "commons-lang" % "commons-lang" % "2.4",
    "commons-logging" % "commons-logging" % "1.1.1",
    "commons-io" % "commons-io" % "1.3.1",
    "javax.servlet" % "servlet-api" % "2.5" % "provided",
    "javax.annotation" % "jsr250-api" % "1.0"    
)
//	"org.zkoss.zkforge.el" % "zcommons-el" % "5.0.7",

// jetty dependencies
libraryDependencies ++= Seq (
    "org.eclipse.jetty" % "jetty-distribution" % "7.5.1.v20110908" % "provided, jetty", 
    "org.eclipse.jetty" % "jetty-webapp" % "7.5.1.v20110908" % "provided, jetty", 
    "org.eclipse.jetty" % "jetty-http" % "7.5.1.v20110908" % "provided, jetty", 
    "org.eclipse.jetty" % "jetty-server" % "7.5.1.v20110908" % "provided, jetty", 
    "org.eclipse.jetty" % "jetty-io" % "7.5.1.v20110908" % "provided, jetty", 
    "org.eclipse.jetty" % "jetty-util" % "7.5.1.v20110908" % "provided, jetty", 
    "org.eclipse.jetty" % "jetty-websocket" % "7.5.1.v20110908" % "provided, jetty", 
    "org.eclipse.jetty" % "jetty-continuation" % "7.5.1.v20110908" % "provided, jetty"
)


// reduce the maximum number of errors shown by the Scala compiler
maxErrors := 20

// increase the time between polling for file changes when using continuous execution
pollInterval := 1000

// append several options to the list of options passed to the Java compiler
javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

// append -deprecation to the options passed to the Scala compiler
scalacOptions += "-deprecation"

// fork a new JVM for 'run' and 'test:run'
fork := true

// fork a new JVM for 'test:run', but not 'run'
fork in Test := true

// add a JVM option to use when forking a JVM for 'run'
javaOptions += "-Xmx512m"

// don't aggregate clean (See FullConfiguration for aggregation details)
aggregate in clean := false

// only show warnings and errors on the screen for compilations.
//  this applies to both test:compile and compile and is Info by default
logLevel in compile := Level.Info

// only show warnings and errors on the screen for all tasks (the default is Info)
//  individual tasks can then be more verbose using the previous setting
logLevel := Level.Info

// only show 10 lines of stack traces
traceLevel := 10

// only show stack traces up to the first sbt stack frame
// traceLevel := 0

// publish test jar, sources, and docs
publishArtifact in Test := true

// Copy all managed dependencies to <build-root>/lib_managed/
//   This is essentially a project-local cache and is different
//   from the lib_managed/ in sbt 0.7.x.  There is only one
//   lib_managed/ in the build root (not per-project).
retrieveManaged := false

// Add WebSettings?
seq(com.github.siasia.WebPlugin.webSettings :_*)