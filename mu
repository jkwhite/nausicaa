org:  excelsiorg
proj: nausicaa
ver:  1.0
lang: Java
type:
    - app
uses:
    - bsh-2.0b4.jar
    - javassist-3.4.jar
    - graph-0.1.jar
    - jgraph-5.1-jkw.jar
    - jgraphaddons-1.0.jar
    - trove-3.0.3.jar
    - Jama-1.0.2.jar
    - rlyehian-1.0.jar
    - jyaml-1.3.jar
    - solace-1.0.jar
    - gimmal-1.0.jar
    - groovy-all-1.8.9.jar
    - log4j-1.2.15.jar
    - snakeyaml-1.21.jar
    - swingxbuilder-0.1.5.jar
    - swingx-0.9.1.jar
    - batik-1.7.jar
    - batik-awt-util-1.7.jar
    - imgscalr-lib-4.2.jar
    - Fxyz-Core-1.0.jar
    - Fxyz-lib-1.0.jar
    - gson-2.8.5.jar
    - slf4j-api-1.7.7.jar
    - slf4j-log4j12-1.7.7.jar
    - log4j-1.2.15.jar
#main: org.excelsi.gimmal.AppFactory
#main: org.excelsi.nausicaa.NViewer
#main: org.excelsi.nausicaa.JfxUniverse
main: org.excelsi.nausicaa.Nausicaa
#main: org.excelsi.ca.Viewer
#sysargs: -XX:-OmitStackTraceInFastThrow -Dcom.sun.management.jmxremote -Xmx12g -d64
sysargs: -Xmx12g -XX:-OmitStackTraceInFastThrow -Dcom.sun.management.jmxremote -d64
# -agentlib:hprof=heap=sites,cpu=samples,depth=8
