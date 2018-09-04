# A Dummy OAuth2 Authorization Server for issuing JWT Tokens

## Table of Contents
  - [About](#about)
  - [Java Build](#java-build)
  - [Docker Credential Helper Setup](#docker-credential-helper-setup)
  - [Deploy to Kyma](#deploy-to-kyma)
  - [Try out on Kyma](#try-out-on-kyma)


## About

This Application is a dummy OAuth2 Authorization server issuing JWT Tokens. It can be used to test Authorization scenarios. It leverages Spring Boot and is available as a docker container. There is also a Kubernetes Manifest available (Targeting Kyma https://kyma-project.io).

As this is very lightweight, there is no plan to support more than one instance at a time (Key is generated locally and is not shared accross instances).



## Java Build

Project is built using: mvn clean package or mvn clean install. It uses jib (https://github.com/GoogleContainerTools/jib/tree/master/jib-maven-plugin) to build and push to a docker registry (which does not require a local docker install). You **must** use the following maven properties to adapt to your local installation: 

* docker.repositoryname: Docker repository that the image will be published to
* jib.credentialhelper: docker credential helper that will be used to acquire docker hub (adapt to YOUR Operating System, pass or secretservice for Linuy, wincred for Windows and osxkeychain for Linux)


You **can** use the following maven properties to adapt to your local installation: 

* project.version: Tag that will be assigned to docker image 
* jib.version: Version of the jib plugin that will be used
credentials (see: https://docs.docker.com/engine/reference/commandline/login/ heading: "Credential helper protocol")

For editing the code I recommend either Eclipse with Spring plugins installed or Spring Tool Suite (https://spring.io/tools/sts/all). You will also need to install the Lombok plugin (https://projectlombok.org/setup/overview). Lombok is used to generate getters/setters and sometimes constructors. It keeps the code lean and neat.

## Docker Credential Helper Setup

Docker credential helpers can be downloaded from https://github.com/docker/docker-credential-helpers. There are various versions for different Operating Systems. If you want to use docker-credential-pass please ensure that gpg and pass are installed. A detailed walkthrough is available under https://github.com/docker/docker-credential-helpers/issues/102 (Steps 1 to 10).

To provide your credentials create a json file like the one below:

```
{ 
    "ServerURL": "registry.hub.docker.com",
    "Username": "<username>", 
    "Secret": "<password>" 
}
```

To push this file into the credentials helper enter the following statement under Linux:

`cat credentials.json | docker-credential-pass store`

Windows:

`type credentials.json | docker-credential-wincred store`

To delete a set of credentials:

`echo <ServerURL> | docker-credential-pass erase`

To read a set of credentials:

`echo <ServerURL> | docker-credential-pass get`



## Deploy to Kyma 

Deployment to kyma requires to change and apply kubernetes-kyma.yaml. 

```
kubectl apply -f kubernetes-kyma.yaml -n <Your Namespace>
```

## Try out on Kyma

After deployment you can access the swagger documentation under https://{kymahost}/swagger-ui.html. This also allows you to try it out. 


## Docker Repository

You can find this in Docker Hub as well: https://hub.docker.com/r/andy008/jwt_issuer/ 

Run locally: `docker run -d --rm -p 8080:8080 --name jwt_issuer andy008/jwt_issuer:1.0.0`