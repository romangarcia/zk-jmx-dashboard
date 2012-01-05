resolvers ++= Seq(
	"Web plugin repo" at "http://siasia.github.com/maven2",
	ScalaToolsSnapshots
)

// libraryDependencies += "de.element34" %% "sbt-eclipsify" % "0.10.0-SNAPSHOT"
	
libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % ("0.1.1-"+v))