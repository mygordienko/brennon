# AI Agent Instructions

This file provides guidance on how to work with code in the Brennon repository.

## Project overview

* Spring Boot 4 MVC Thymeleaf + HTMX. 
* PostgreSQL as a database.
* Gradle as a build system.

## Git conventions

* Each commit MUST follow Conventional Commits 1.0.0 specification.
* Branches MUST be named as follows:
    - For new features: `feature/feature-name-with-hyphens`
    - For fixes: `fix/fix-name-with-hyphens`
    - For other tasks: `task/task-name-with-hyphens`

NO changes allowed in `main` branch.
DO NOT push.

## Development Workflow

When asked to implement a new feature or change existing code stick with the following workflow:

Understand → Plan → Implement → Test → Review → Commit

Ask for confirmation and permission to proceed AFTER EACH step. DO NOT proceed without permission.
The steps are described below:

* Understand: Read the task thoroughly before writing any code. Clarify scope, acceptance criteria, and edge cases. If the issue is ambiguous, ask the user for clarification rather than guessing. Read the relevant docs in `.dev/agent_docs` folder (if available) to get project-specific guidelines — do not read them all upfront.
* Plan: Identify affected packages/modules/files in the repository and outline the approach. Present the plan to the user before implementing. Consider side effects on other parts of the system.
* Implement: NO changes allowed in `main` branch. Create a new branch before introducing any changes.
* Test: Run the specific tests affected by your changes. Write new tests for new behavior. Never run the full test suite — CI handles comprehensive testing.
* Review: Before committing, delegate the review to the code-reviewer sub-agent (if available). The sub-agent has its own context window and reviews the uncommitted diff without bias from the implementation history. Maximum one round of review. Fix reported issues and proceed to commit.
* Commit: see "Git conventions" above