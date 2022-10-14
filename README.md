# URL shortening application

## Configuration 

See `src/main/resources/application.yml` for configuration options

## Running

Use the gradle wrapper to run the application using the Spring Boot Gradle 
Plugin. The following commands assume a *nix environment but if running on 
Windows then use `gradlew.bat` instead.

Running with HSQL database:

```
./gradlew -Pe=hsql bootrun
```

Running with PostgreSQL database:

```
./gradlew -Pe=psql bootrun
```

Debugging

```
./gradlew -Pe=hsql bootrun -Pdebug
```

Run specific test class: 

```
./gradlew test --tests net.orfdev.Base62Test 
```