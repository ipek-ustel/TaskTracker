# Task Tracker CLI

A Command Line Interface (CLI) application built with Java to manage tasks and track your to-do list. All data is persisted locally in a `tasks.json` file.

This project was built without any external libraries or frameworks (no Jackson, Gson, etc.), relying solely on native Java standard libraries (`java.nio.file`, `java.time`, and `java.util.regex`).

---

## Prerequisites

- **Java Development Kit (JDK 17+)** installed on your machine.
- A command-line terminal (macOS/Linux Terminal, Windows Command Prompt, or PowerShell).

---

## Setup & Compilation

You **must** navigate into the `src` directory before compiling and executing the application.

### 1. Step into the Source Directory
From the root of the project:

cd src

### 2. Compile the Code
Compile TaskCli.java using the standard Java compiler:

javac TaskCli.java

**Important:** Always make sure you are inside the src folder when executing javac TaskCli.java. If you modify TaskCli.java, re-run this command before launching the application.

## Commands

### 1. Adding a Task
Creates a new task with a status of todo, generates a unique ID, and logs the current creation and update timestamps.

**Syntax:** task-cli add "\<description>"

**Example:**

task-cli add "Buy groceries"

---
### 2. Listing Tasks
Inspect all tracked tasks or filter them by their execution status.

***List all tasks:***

task-cli list

***List tasks that are to be done (todo):***

task-cli list todo

***List tasks currently in progress (in-progress):***

task-cli list in-progress

***List tasks that are finished (done):***

task-cli list done

---
### 3. Updating a Task Description
Modifies the description of an existing task by its ID and refreshes the updatedAt timestamp.

**Syntax:** task-cli update \<id> "\<new description>"

**Example:**

task-cli update 1 "Buy groceries and cook dinner"

---
### 4. Updating Task Status
Progress tasks across their lifecycle states.

***Mark task as in-progress:***

**Syntax:** task-cli mark-in-progress \<id>

**Example:**

task-cli mark-in-progress 1

***Mark task as completed:***

**Syntax:** task-cli mark-done \<id>

**Example:**

task-cli mark-done 1

---
### 5. Deleting a Task
Removes a task permanently from the storage file by its ID.

**Syntax:** task-cli delete \<id>

**Example:**

task-cli delete 1
