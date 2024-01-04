PROJECT_NAME=java-common

all: java_install
build: java_install
clean: java_clean

java_install_root:
	echo "Installing root pom ..."
	mvn $(MVN_ARGS) install -f ../pom.xml -N

include ../../Makefile.maven

.PHONY: all build clean
