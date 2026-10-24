To build for 1.21.11: copy build.gradle and gradle.properties from this folder over the root ones, then in
src/main/resources/fabric.mod.json set "minecraft": "~1.21.11" and "java": ">=21". Use JDK 21.
Fix any compile errors in the version-sensitive files listed in the root README (e.g. Camera.position() vs getPosition()).
