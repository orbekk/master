# Distributed Shared Objects for Android
A platform that provides shared objects for ad-hoc networks of Android devices.

## Goal
Fault-tolerant, distributed, fast synchronization of application state across
several Android phones. A suitable library for programmers to interact with the
system.

## Feature list
An incomplete list of implemented and planned features.

* Fault-tolerance with automatic master selection. (Planned)
* Network discovery with directory service. (Planned)
* Ability to share variables with other peers. (Implemented, 0.1)
* Network discovery with UDP broadcast. (Implemented, 0.1)

## Example
With Same you can share variables easily, like so:

```Java
// First: Some setup to get all your devices connected to the same network.
// Then, in your Activity:
ClientInterfaceBridge client = new ClientInterfaceBridge(this);
client.connect();
try {
    // A VariableFactory is used to create distributed objects.
    VariableFactory variableFactory = client.createVariableFactory();
    Variable<String> myVariable = variableFactory.createString("MyVariable");

    // Set the variable.
    myVariable.set("Hello, Same!");

    // Run update() to get the most recent version.
    myVariable.update();

    // Get the current value.
    Log.i("The current value is: " + myVariable.get());
} finally {
    // Always remember to disconnect.
    client.disconnect();
}
```

## Release log
Pre-1.0: A release is a milestone in the project. Whenever the system seems to
be working fairly well, a commit is tagged as the release version.

### 0.2
* Tag: 0.2-new-main-screen
* Many Android related bugs were fixed. App is generally stable.
* More sophisticated state handling in the demo Activities.
* Benchmark activity shows reasonable performance on Galaxy S.
* README file that makes sense.

### 0.1
* Tag: working-graphics-0.1
* An interactive graphics demo with several participants.
* Web management on http://ip:port/_/state
