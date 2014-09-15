PariSolve
=========

PariSolve has been developed by Arne Schröder as part of his Master's thesis 'Algorithmen für Paritätsspiele'. If you want to use or try PariSolve and have any questions, please do not hesitate to ask.

# Building PariSolve
PariSolve is built with Java 8 using ANT and ANT-Contrib.

- Install the JDK of Java 8 or above. (e.g. http://www.oracle.com/technetwork/java/javase/downloads/) make sure that you can run java and javac.
- Install ant (https://ant.apache.org/manual/install.html), make sure you can run ant.
- Copy ant-contrib (ant-contrib-1.0b3.jar from http://sourceforge.net/projects/ant-contrib/files/ant-contrib/1.0b3/ant-contrib-1.0b3-bin.zip/download) into the lib-folder in your ant-folder (on Linux usually /usr/share/ant/lib/)
- run 'ant' inside the root-folder of PariSolve
- under 'target' there should now be jar files parisolve_*.jar.

# Running PariSolve
- to run parisolve run 'java -jar target/parisolve_<your_system>.jar'
- further options like the cli are available through switches
  - n cli-mode
  - t measure and display time when solving parity games
  - b <batch-file> execute commands from batch-file
  - bench do benchmarking
- in cli-mode several commands are available, to get an overview, run ? or help in the open repl.

# Developing PariSolve
- PariSolve was developed using the Eclipse environment and therefore has .project- and .classpath-files which allow it to be easily imported into Eclipse. Please note, that PariSolve was developed in Java 8 and thus requires Eclipse 4.4 or above or Eclipse 4.3 plus the Java 8 patch.
