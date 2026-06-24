# Agent Instructions

This repository keeps canonical AI instructions in `.ai/`.

Before making code changes:

1. Read `.ai/README.md`.
2. Follow the progressive loading rules from that file.
3. Load only the instruction, skill, and harness files relevant to the current task.
4. Always follow `.ai/instructions/coding-guidelines.md` for Kotlin code.
5. Always follow `.ai/instructions/testing.md` when creating, changing, or reviewing tests.

Do not duplicate the full AI instructions in this file. Keep this file as the stable entry point for tools that discover `AGENTS.md`.
