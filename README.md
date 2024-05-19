# CPD project 2 - UNO server/client

In this assignment, we've implemented a server capable of handling multiple games of UNO at the same time. We leverage Java's Virtual Threads, introduced in Java 21, so that, parallelization is achieved in a more lightweight way. It has no hard cap on how many games can run at the same time, as the protocol and the logic try to be stateless and asynchronous as much as possible.

**NOTE:** we used what we thought was the best version of UNO rules, we know that there are many, but all other options are wrong :) 

## How to run

You'll need a version of `Gradle` that can run `.kts` configuration files and also compile `Java 21` (you'll also need Java 21 installed).

### Server

```sh
gradle server:run --console=plain
```

However, you can specify the number of players you want per game (default is 4):

```sh
gradle server:run --console=plain --args="<num_of_players>"
```

**NOTE:** using `gradle server:run` will delete the old _"database"_ files, for ease of the development process. If you wish to persist them, you can run it manually using Gradle's built jars, in the server directory.


### Client


```sh
gradle client:run --console=plain
```

However, if you wish for the clients to operate automatically (it joins a queue and plays random moves without user intervention):

```sh
gradle server:run --console=plain --args="<username> <password> <normal/ranked>"
```

## Project structure

This gradle is subdivided into 3 projects:

- `game` project contains shared code between the client and server: the game logic and the protocol;
- `client` project contains the client application;
- `server` project contains the server logic and application.