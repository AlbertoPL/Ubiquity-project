resolvers += Classpaths.typesafeResolver

libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v+"-0.2.4"))

addSbtPlugin("com.typesafe.sbtscalariform" % "sbt-scalariform" % "0.1.4")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse" % "1.4.0")

libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-proguard-plugin" % (v+"-0.1.1"))