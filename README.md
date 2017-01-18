# Link Two Containers Java - Postgres

Steps to connect a JDBC App to an Postgres database through docker containers.

The main idea is to have two containers which will be connected together, one for Java App and the second for the database.

The Java container will share a volume with the host. The volume is used to share a specific folder on the host to the container file system. For this example we will create a volume to the source Maven project.

## Requirements
* [Docker](https://www.docker.com)

Please check your docker installation by executing: `command -v docker` or `which docker`.

--> [To download Docker](https://www.docker.com/products/overview) <--

## Structure folder

```
├── Client
│   ├── Client.iml
│   ├── Dockerfile  <--
│   ├── pom.xml
│   ├── src
│   └── target
├── Database
│   └── Dockerfile  <--
└── README.md
```

* ***Client*** folder contains the JDBC App using [Maven](https://maven.apache.org) and the Dockerfile which will execute the Java App.

* ***Database*** folder contains the Dockerfile to create the Postgres image.

## Dockerfile Details

### Linux & Java & Maven

```bash
FROM alpine:3.5

MAINTAINER Ysee Monnier <yseemonnier@gmail.com>

# Java Version and other ENV
ENV JAVA_VERSION_MAJOR=8 \
    JAVA_VERSION_MINOR=102 \
    JAVA_VERSION_BUILD=14 \
    JAVA_PACKAGE=jdk \
    JAVA_JCE=standard \
    JAVA_HOME=/opt/jdk \
    PATH=${PATH}:/opt/jdk/bin \
    GLIBC_VERSION=2.23-r3 \
    LANG=C.UTF-8

RUN apk upgrade --update && \
    apk add --update libstdc++ curl ca-certificates bash && \
    for pkg in glibc-${GLIBC_VERSION} glibc-bin-${GLIBC_VERSION} glibc-i18n-${GLIBC_VERSION}; do curl -sSL https://github.com/andyshinn/alpine-pkg-glibc/releases/download/${GLIBC_VERSION}/${pkg}.apk -o /tmp/${pkg}.apk; done && \
    apk add --allow-untrusted /tmp/*.apk && \
    rm -v /tmp/*.apk && \
    ( /usr/glibc-compat/bin/localedef --force --inputfile POSIX --charmap UTF-8 C.UTF-8 || true ) && \
    echo "export LANG=C.UTF-8" > /etc/profile.d/locale.sh && \
    /usr/glibc-compat/sbin/ldconfig /lib /usr/glibc-compat/lib && \
    mkdir /opt && \
    curl -jksSLH "Cookie: oraclelicense=accept-securebackup-cookie" -o /tmp/java.tar.gz \
      http://download.oracle.com/otn-pub/java/jdk/${JAVA_VERSION_MAJOR}u${JAVA_VERSION_MINOR}-b${JAVA_VERSION_BUILD}/${JAVA_PACKAGE}-${JAVA_VERSION_MAJOR}u${JAVA_VERSION_MINOR}-linux-x64.tar.gz && \
    gunzip /tmp/java.tar.gz && \
    tar -C /opt -xf /tmp/java.tar && \
    ln -s /opt/jdk1.${JAVA_VERSION_MAJOR}.0_${JAVA_VERSION_MINOR} /opt/jdk && \
    if [ "${JAVA_JCE}" == "unlimited" ]; then echo "Installing Unlimited JCE policy" >&2 && \
      curl -jksSLH "Cookie: oraclelicense=accept-securebackup-cookie" -o /tmp/jce_policy-${JAVA_VERSION_MAJOR}.zip \
        http://download.oracle.com/otn-pub/java/jce/${JAVA_VERSION_MAJOR}/jce_policy-${JAVA_VERSION_MAJOR}.zip && \
      cd /tmp && unzip /tmp/jce_policy-${JAVA_VERSION_MAJOR}.zip && \
      cp -v /tmp/UnlimitedJCEPolicyJDK8/*.jar /opt/jdk/jre/lib/security; \
    fi && \
    sed -i s/#networkaddress.cache.ttl=-1/networkaddress.cache.ttl=30/ $JAVA_HOME/jre/lib/security/java.security && \
    apk del curl glibc-i18n && \
    rm -rf /opt/jdk/*src.zip \
           /opt/jdk/lib/missioncontrol \
           /opt/jdk/lib/visualvm \
           /opt/jdk/lib/*javafx* \
           /opt/jdk/jre/plugin \
           /opt/jdk/jre/bin/javaws \
           /opt/jdk/jre/bin/jjs \
           /opt/jdk/jre/bin/orbd \
           /opt/jdk/jre/bin/pack200 \
           /opt/jdk/jre/bin/policytool \
           /opt/jdk/jre/bin/rmid \
           /opt/jdk/jre/bin/rmiregistry \
           /opt/jdk/jre/bin/servertool \
           /opt/jdk/jre/bin/tnameserv \
           /opt/jdk/jre/bin/unpack200 \
           /opt/jdk/jre/lib/javaws.jar \
           /opt/jdk/jre/lib/deploy* \
           /opt/jdk/jre/lib/desktop \
           /opt/jdk/jre/lib/*javafx* \
           /opt/jdk/jre/lib/*jfx* \
           /opt/jdk/jre/lib/amd64/libdecora_sse.so \
           /opt/jdk/jre/lib/amd64/libprism_*.so \
           /opt/jdk/jre/lib/amd64/libfxplugins.so \
           /opt/jdk/jre/lib/amd64/libglass.so \
           /opt/jdk/jre/lib/amd64/libgstreamer-lite.so \
           /opt/jdk/jre/lib/amd64/libjavafx*.so \
           /opt/jdk/jre/lib/amd64/libjfx*.so \
           /opt/jdk/jre/lib/ext/jfxrt.jar \
           /opt/jdk/jre/lib/ext/nashorn.jar \
           /opt/jdk/jre/lib/oblique-fonts \
           /opt/jdk/jre/lib/plugin.jar \
           /tmp/* /var/cache/apk/*

# Maven
ENV MAVEN_VERSION 3.3.9
ENV MAVEN_HOME /usr/lib/mvn
ENV PATH $MAVEN_HOME/bin:$PATH

RUN wget http://ftp.fau.de/apache/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz && \
    tar -zxvf apache-maven-$MAVEN_VERSION-bin.tar.gz && \
    rm apache-maven-$MAVEN_VERSION-bin.tar.gz && \
    mv apache-maven-$MAVEN_VERSION /usr/lib/mvn
```

I chose to use [AlpineLinux](https://www.alpinelinux.org) because of its lightness. On this Dockerfile, you can find all commands to install Java, Maven and other components.

### Database (Postgres)
```bash
FROM postgres:latest
MAINTAINER YMonnier <contact@yseemonnier.com>

ENV POSTGRES_USER root
ENV POSTGRES_PASSWORD root  
ENV POSTGRES_DB app
```

## How to use it?

* `git clone git@github.com:YMonnier/docker-java-postgres.git`
* `cd docker-java-postgres`

#### Images

docker build [OPTIONS] PATH : Build an image from a Dockerfile

Creating of Postgres image with a tag `ymonnier/psql:1.0`:
* `docker build Database/ -t ymonnier/psql:1.0`.

Creating of our AlpineLinux&Java image with a tag `ymonnier/java-mini:1.0`:
* `docker build Client/ -t ymonnier/java-mini:1.0`

*tag format*: 'name:tag'

#### Containers
For the database:
* `docker run -d -t -i --name database ymonnier/psql:1.0`
Our Java app:
* `docker run --rm --name MyApp -t -i --link database:database --volume $(pwd)/Client:/home/app/ ymonnier/mini-java:1.0 bash`
Execute java project:
When you are into the `ymonnier/java-mini:1.0`container you can run:
* `cd /home/app/ && mvn package && mvn exec:java -Dexec.mainClass="com.yseemonnier.dbDocker.App"`
You will see the maven compilation and the execution:
```
Hello World from Docker Container!
Deleting table 'Clients' if exists.
Creating table 'Clients' if not exists.
Inserting a client...
Selecting all clients...
Client Pierre - 45
```
##### JDBC Settings
Now in your App you can use `database`(`--link database:database` when creating the Java container) as DB URL connection and use the user(`POSTGRES_USER, POSTGRES_PASSWORD`), database name(`POSTGRES_DB`) created into the `Database/Dockerfile`.

Example of JDBC Connection:
```java
    DriverManager.getConnection("jdbc:postgresql://database/app", "root", "root");
```
jdbc:postgresql://*database*/*app*.

*database* refer to `--link` option.

*app* refer to the postgres environment, the same for the user and password.

Contributor
------------
* [@YMonnier](https://github.com/YMonnier)

License
-------
docker-java-postgres is available under the MIT license. See the [LICENSE](https://github.com/YMonnier/docker-java-postgres/blob/master/LICENSE) file for more info.
