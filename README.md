# EBAD

EBAD is a project to run your batchs on remote server from a central place.

## Getting Started

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
