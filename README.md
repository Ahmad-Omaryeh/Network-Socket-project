# Computer Networks Socket Programming

Java socket-programming project for computer networks coursework. This repository demonstrates core networking concepts through two practical tasks: a custom web server and a TCP client/server application.

## Project Overview

- Task 1: Build and run a Java web server that serves static HTML, CSS, and image files.
- Task 2: Implement socket communication between a Java TCP server and client.
- Goal: Practice request handling, socket programming, and end-to-end network application flow.

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

### Task 2: Client/Server Sockets

1. Go to the Task2 folder.
2. Compile both files:

```bash
javac Server.java Client.java
```

3. Run the server first:

```bash
java Server
```

4. In a second terminal, run the client:

```bash
java Client
```

## Learning Outcomes

- Understand TCP socket creation and communication flow.
- Practice handling basic HTTP-style requests and responses.
- Gain hands-on experience with Java networking APIs.

## Notes

- This project is educational and focused on fundamentals.
- File names and task layout are kept simple for coursework clarity.
