# AI Agent Instructions

This file provides guidance on how to work with code in the Brennon repository.

## Project overview

This is a Spring Boot MVC project. Gradle is used as a build system.
Frontend is implemented with Thymeleaf.

## Git conventions

* Each commit MUST follow Conventional Commits 1.0.0 specification.
* Branches MUST be named as follows:
    - For new features: `feature/feature-name-with-hyphens`
    - For fixes: `fix/feature-name-with-hyphens`
    - For other tasks: `chore/feature-name-with-hyphens`

NO changes allowed in `main` branch.
DO NOT push.

## Development Workflow

When asked to implement a new feature or change existing code stick with the following
workflow:

Understand → Research → Plan → Branch → Implement → Test → Commit → Review

Ask for confirmation and permission to proceed AFTER EACH step. DO NOT proceed without permission.

The steps are described below:

### 1. Understand

Read the task thoroughly before writing any code. Clarify scope, acceptance criteria, and edge cases. If the issue is ambiguous, ask the user for clarification rather than guessing.

### 2. Research

If unsure, search for best practices and guidelines to implement what was asked.

If during research you discover the scope was wrong (e.g. you overlooked dependencies between modules or forgot security aspects), update the scope explicitly and ask for approval.

### 3. Plan

Identify affected packages/modules/files in the repository and outline the approach.

Read the relevant docs in `.dev/agent_docs` folder (if available) to get project-specific guidelines — do not read them all upfront.

Present the plan to the user before implementing. Consider side effects on other parts of the system.


### 4. Branch

NO changes allowed in `main` branch. Create a new branch before introducing any changes.

Branches MUST be named as follows:
    - For new features: `feature/feature-name-with-hyphens`
    - For fixes: `fix/feature-name-with-hyphens`
    - For other tasks: `chore/feature-name-with-hyphens`


### 5. Implement

Read the relevant implementation-related docs in `.dev/agent_docs` folder (if available) before starting implementation — do not read them all upfront.

### 6. Test

Run the specific tests affected by your changes. Write new tests for new behavior. Never run the full test suite — CI handles comprehensive testing.

### 7. Review

Before committing, delegate the review to the code-reviewer sub-agent (if available). The sub-agent has its own context window and reviews the uncommitted diff without bias from the implementation history.

If the sub-agent reports no findings, proceed to commit.

### 8. Commit

Each commit MUST follow Conventional Commits 1.0.0 specification.