# CLINK ANTIGRAVITY AUTONOMY & PROJECT SCOPE POLICY

## Purpose

This file defines the operating permissions and boundaries for the AI coding agent working on the CLINK project.

These rules apply to all normal development work performed by the AI agent.

---

## 1. DO NOT ASK FOR PERMISSION FOR NORMAL COMMANDS

The AI agent MUST NOT repeatedly ask the user for permission before running normal development commands or using normal development tools.

The user has already granted standing permission for normal development operations within the CLINK project.

The agent should execute the required commands autonomously as part of its workflow.

Do NOT ask questions such as:

- "Can I run this command?"
- "Should I execute Gradle?"
- "May I inspect this file?"
- "Can I run the tests?"
- "Should I install/update a project dependency?"
- "Can I use the emulator?"
- "May I run Git status/log/diff?"
- "Can I create or modify this project file?"

unless the operation falls under the critical exceptions listed below.

---

## 2. PROJECT-ONLY ACCESS

The AI agent's tools and commands are authorized ONLY for the CLINK project.

The project root is the directory containing this file.

The agent may freely:

- inspect files inside the CLINK project
- create files inside the CLINK project
- modify files inside the CLINK project
- delete files inside the CLINK project when required by the current task
- run Gradle commands for CLINK
- run tests for CLINK
- run lint/static analysis for CLINK
- run local project scripts
- inspect Git status, diff, history, and branches for CLINK
- create commits for CLINK
- use the Android SDK/emulator as required to build and test CLINK
- resolve/download normal development dependencies required by CLINK
- use development tools required to implement, build, test, debug, and verify CLINK

The agent MUST NOT intentionally operate on unrelated projects, files, applications, repositories, or system resources.

Do not use CLINK authorization as permission to perform unrelated system administration.

---

## 3. SCOPE THE COMMANDS

Whenever possible, commands MUST be scoped to the CLINK project directory.

Prefer:

```bash
cd <CLINK_PROJECT_ROOT>
./gradlew test
```

over commands that search or modify unrelated parts of the machine.

Do not perform broad system-wide searches, cleanup, modification, or configuration changes unless explicitly required for CLINK and permitted by the critical-operation rules.

---

## 4. AUTONOMOUS DEVELOPMENT LOOP

For normal CLINK work, follow this loop without asking for approval:

```text
UNDERSTAND
→ INSPECT
→ PLAN
→ EXECUTE
→ BUILD
→ TEST
→ DEBUG
→ VERIFY
→ DOCUMENT
→ UPDATE .ai MEMORY
→ COMMIT
→ REPORT
```

If a normal command fails, diagnose and fix the project rather than immediately asking the user whether the command may be retried.

---

## 5. CRITICAL OPERATIONS THAT STILL REQUIRE USER APPROVAL

The agent MUST stop and ask the user before performing any of the following:

### Destructive operations outside the CLINK project

Examples:

- deleting unrelated files
- modifying unrelated repositories
- system-wide cleanup
- changing unrelated system configuration
- formatting drives
- partition changes

### Destructive Git operations

Examples:

- force push
- deleting important remote history
- rewriting shared history
- destructive reset that could discard user work
- deleting the repository

Normal Git operations such as:

- status
- diff
- log
- branch creation
- add
- commit
- normal pull/push when appropriate

do NOT require permission when they are strictly related to CLINK, unless they would destroy or overwrite user work.

### Secrets and credentials

Ask before handling or creating:

- production credentials
- private keys
- API secrets
- payment secrets
- signing keys
- passwords
- tokens that grant sensitive external access

Never expose secrets in source code, logs, reports, or commits.

### Real money and production systems

Ask before:

- making a real-money transaction
- connecting to production payment infrastructure
- deploying to production
- modifying a production database
- publishing/releasing the app publicly

---

## 6. DO NOT STOP FOR NORMAL FAILURES

A command failure is not automatically a reason to ask permission.

For example:

```bash
./gradlew test
```

fails.

The agent should:

1. inspect the error
2. determine the cause
3. make the necessary CLINK project changes
4. rerun the command
5. continue until verified

Only ask the user if the required fix crosses one of the critical boundaries above or if genuinely necessary user input is unavailable.

---

## 7. DO NOT EXPAND SCOPE

Autonomy does NOT mean unrestricted access.

The agent must remain focused on the current CLINK task.

Do not:

- modify unrelated projects
- browse unrelated personal files
- reorganize the user's system
- install unrelated software
- change unrelated applications
- perform system cleanup
- access personal data that is not needed for CLINK
- make unrelated configuration changes

---

## 8. USER WORK HAS PRIORITY

Never overwrite or discard user work without understanding it.

Before potentially destructive project changes:

- inspect the current state
- preserve existing functionality
- avoid unnecessary rewrites
- make focused changes

If a destructive action would affect user work and cannot be safely avoided, ask the user first.

---

## 9. SECURITY PRINCIPLE

Use the minimum access necessary to complete the current CLINK task.

The authorization in this file means:

> "Do not ask permission for normal CLINK development commands."

It does NOT mean:

> "Do whatever you want on the computer."

Project autonomy and system-wide autonomy are different.

---

## 10. TASK REPORTING

Do not ask permission between ordinary implementation steps.

At the end of a task, report:

- implementation status
- important files changed
- tests
- build result
- lint result
- runtime verification
- known issues
- memory updates
- Git commit/status

Then stop and wait for the next task.

---

## FINAL RULE

For normal CLINK development:

**EXECUTE, DON'T ASK.**

For critical or system-wide operations:

**STOP, EXPLAIN, ASK.**

All tools and commands should remain strictly limited to what is necessary for the CLINK project.
