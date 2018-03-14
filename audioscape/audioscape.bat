cd F:\devel\misc\audioscape\audioscape
copy target\audioscape-1.0-SNAPSHOT.jar audioscape.jar
copy target\audioscape-1.0-SNAPSHOT-jar-with-dependencies.jar audioscape-dependencies.jar
SET GOOGLE_APPLICATION_CREDENTIALS=F:\devel\misc\audioscape\audioscape\keys\google_speech.json
java -Xmx2G -cp audioscape-dependencies.jar;audioscape.jar org.quilombo.audioscape.AudioScape