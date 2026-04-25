# Computer Networks Socket Programming

Java socket-programming project for computer networks coursework. This repository demonstrates core networking concepts through two practical tasks: a custom HTTP web server and a UDP client/server multiplayer game.

## Project Overview

- Task 1: Build and run a Java web server that serves static HTML, CSS, and image files, supports Arabic/English pages, and handles search with redirect behavior.
- Task 2: Implement a UDP multiplayer number game using Java client/server sockets.
- Goal: Practice HTTP request handling, UDP communication, and end-to-end network application flow.

## Repository Structure

```text
Task1/
	WebServer.java
	css/
		style.css
	html/
		event_details_ar.html
		event_details.html
		main_ar.html
		main_en.html
	imgs/

Task2/
	Client.java
	Server.java
```

## Prerequisites

- Java JDK 8 or newer
- Terminal (PowerShell, Command Prompt, or VS Code terminal)

## How to Run

### Task 1: Web Server

1. Go to the Task1 folder.
2. Compile:

```bash
javac WebServer.java
```

3. Run:

```bash
java WebServer
```

4. Open your browser and visit the server address/port configured in `WebServer.java`.

### Task 2: UDP Client/Server Game

1. Go to the Task2 folder.
2. Compile both files:

```bash
javac Server.java Client.java
```

3. Run the server first:

```bash
java Server
```

4. In a second terminal, run each client:

```bash
java Client
```

5. Enter a username when prompted and follow round instructions.

## Learning Outcomes

- Understand UDP socket communication and game-state coordination.
- Practice handling HTTP requests, responses, and redirects.
- Gain hands-on experience with Java networking APIs.

## Notes

- This project is educational and focused on fundamentals.
- File names and task layout are kept simple for coursework clarity.
