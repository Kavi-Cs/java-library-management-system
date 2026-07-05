# Java Library Management System

## Overview

This project is the Java translation of the previous Python-based Library Management System assignment. It keeps the same two-version structure:

- **Version A:** Traditional object-oriented programming using basic Java classes and `ArrayList`.
- **Version B:** AI-assisted improved version using Java records, Streams API, custom exceptions, validation, and file persistence.

The selected problem is a small Library Management System that allows a librarian to add books, register members, borrow books, return books, list records, and search books.

## Project Structure

```text
Java_Library_Management_System/
├── README.md
├── source_code/
│   ├── version_a_traditional/
│   │   └── LibrarySystem.java
│   └── version_b_ai_assisted/
│       └── LibrarySystemAI.java
└── uml/
    ├── activity_diagram.puml
    ├── class_diagram.puml
    └── sequence_diagram.puml
```

## Requirements

- Java Development Kit (JDK) 17 or later is recommended.
- No external libraries are required.

## Compile and Run Version A

Version A is the traditional OOP implementation. It uses simple classes, `ArrayList`, and direct method calls.

```bash
cd source_code/version_a_traditional
javac LibrarySystem.java
java LibrarySystem
```

## Compile and Run Version B

Version B is the AI-assisted implementation. It uses records, streams, custom exceptions, and standard file I/O persistence.

```bash
cd source_code/version_b_ai_assisted
javac LibrarySystemAI.java
java LibrarySystemAI
```

Version B automatically creates `library_data.txt` in the same folder. This file stores books and members between program runs.

## Version Comparison

| Area | Version A | Version B |
|---|---|---|
| Data model | Normal Java classes | Java `record` classes |
| Storage | In-memory only | File persistence with `library_data.txt` |
| Collections | `ArrayList` | `LinkedHashMap`, `List`, Streams |
| Error handling | Return message strings | Custom `LibraryException` and try-catch |
| Search | Not included | Streams-based search by ID, title, or author |
| Code style | Beginner-friendly OOP | More maintainable service-based design |

## AI Prompts Used

1. Convert my Python Library Management System assignment into Java while keeping Version A and Version B.
2. Create a traditional Java OOP implementation using classes, ArrayLists, and simple menu logic.
3. Improve the Java implementation using records, Streams API, custom exceptions, and persistence.
4. Add comments beginning with `// AI Suggestion:` to identify AI-assisted improvements.
5. Generate PlantUML class, activity, and sequence diagrams for the Java Library Management System.

## Notes

- Version A is intentionally simple and does not save data after the program exits.
- Version B saves data automatically after adding, borrowing, or returning books.
- The persistence format uses standard text file I/O so it can run without third-party JSON libraries.
