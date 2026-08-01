# Documentation guide

This folder is the fastest way to understand how Sisyphus Academy is built and
how contributors are expected to work.

## Start here

If you are new to the project, read the documents in this order:

1. [`../README.md`](../README.md) — product overview, repository structure, and
   major local commands
2. [`DEVELOPMENT.md`](DEVELOPMENT.md) — day-to-day setup and development flow
3. [`ARCHITECTURE.md`](ARCHITECTURE.md) — backend structure, boundaries, and
   request flow
4. the topic guide that matches your task

## Pick the right document

| If you want to... | Read |
| --- | --- |
| understand the project at a high level | [`../README.md`](../README.md) |
| run the project or work locally | [`DEVELOPMENT.md`](DEVELOPMENT.md) |
| understand backend responsibilities and flow | [`ARCHITECTURE.md`](ARCHITECTURE.md) |
| work on authentication or OAuth | [`OAUTH_SETUP.md`](OAUTH_SETUP.md) |
| check security expectations | [`SECURITY.md`](SECURITY.md) |
| deploy the project | [`DEPLOYMENT.md`](DEPLOYMENT.md) |
| understand web UI rules and tokens | [`design/ui-foundation.md`](design/ui-foundation.md) |
| understand shared UI component behavior | [`design/component-contracts.md`](design/component-contracts.md) |
| understand product wording and UX language | [`product/ux-language.md`](product/ux-language.md) |
| contribute changes consistently | [`CONTRIBUTING.md`](CONTRIBUTING.md) |

## UI documentation reading order

When changing the web UI, read these three documents together:

1. [`design/ui-foundation.md`](design/ui-foundation.md)
   Product direction, layout rules, responsive behavior, design tokens, and
   accessibility baseline.
2. [`design/component-contracts.md`](design/component-contracts.md)
   Shared component boundaries, button/form/state rules, and feedback patterns.
3. [`product/ux-language.md`](product/ux-language.md)  
   Product terminology, action wording, and screen-by-screen UX expectations.

## Quick mental model

- `README.md` explains **what** the project is.
- `ARCHITECTURE.md` explains **how** the backend is organized.
- `DEVELOPMENT.md` explains **how to work** in the repository.
- `docs/design/*` explains **how the product should look and behave**.
- `docs/product/*` explains **what words and UX patterns users should see**.

If you are unsure where to start, open the root `README.md` first and come back
here for the matching deep-dive document.
