# EBAD
[![Build Status](https://travis-ci.org/informatique-cdc/ebad.svg?branch=master)](https://travis-ci.org/informatique-cdc/ebad)
![Codecov](https://img.shields.io/codecov/c/github/informatique-cdc/ebad)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=informatique-cdc_ebad&metric=alert_status)](https://sonarcloud.io/dashboard?id=informatique-cdc_ebad)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=informatique-cdc_ebad&metric=bugs)](https://sonarcloud.io/dashboard?id=informatique-cdc_ebad)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=informatique-cdc_ebad&metric=ncloc)](https://sonarcloud.io/dashboard?id=informatique-cdc_ebad)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=informatique-cdc_ebad&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=informatique-cdc_ebad)


EBAD is a project to run your batchs on remote server from a central place.

## Getting Started Usage
These instructions womm get you a working application.

### Configuration
To configure your application create application.yml file

#### Database
```
spring:
  datasource:
    url: jdbc:postgresql://<url>:<port>/<database>?stringtype=unspecified
    username: <username>
    password: <password>
```
Replace <xxx> with your values.

For initialize data (user and roles) add

```
spring:
  datasource:
    initialization-mode: always
```

#### SSH Connection
For the ssh connection edit this values :

```
ebad:
  ssh:
    login: <login>
    private_key_path: </path/to/key>
```
Replace <xxx> with your values.

#### Authentication JWT
If you use JWT authentication edit the JWT Secret
```
ebad:
  security:
    authentication:
      jwt:
        secret: <my-secret-token-to-change-in-production>
```
Replace <xxx> with your values.

In case you want use JWT (standalone mode) run with "jwt" Spring profile 
```
java -jar <downloaded-release>.jar --spring.profiles.active=jwt --spring.config.additional-location=<path-to-your-application.yml>
```

By default this application run on port 10300 and context ebad : http://localhost:10300/ebad

Then, your back is running, after this run [ebad-front](http://github.com/informatique-cdc/ebad-front) project.
The user created is admin / admin .

#### Authentication OAUTH2
Authentication OAUTH2 getting started is comming soon.



## Getting Started Development

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

the project needs Java 8 and a PostgreSQL database.
To build the project, Maven 3+ is required.

### Installing

This project uses Spring Boot.  
To build EBAD, use this command:

```
mvn clean package
```

And to run EBAD, use this command:

```
java -jar <generated-artifact>.jar
```


## Running the tests

To run tests, use this command:

```
mvn clean verify
```

### Break down into end to end tests

We use cypress for end to end tests with other ebad-front project


## Deployment

Add additional notes about how to deploy this on a live system


## Built With

* [Maven](https://maven.apache.org/) - Dependency Management


## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.


## Versioning

For the available versions, see the [releases on this repository](https://github.com/informatique-cdc/ebad/releases). 


## Authors

* **Damien Trouillet** - *Initial work* - [dtrouillet](https://github.com/dtrouillet)

See also the list of [contributors](https://github.com/informatique-cdc/ebad/contributors) who participated in this project.


## License

This project is licensed under the Apache 2.0 License - see the [LICENSE.md](LICENSE.md) file for details
