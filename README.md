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
