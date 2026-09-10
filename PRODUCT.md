# LifeHub — PRODUCT.md

## What it is
Personal life cockpit: tasks (list + Kanban), habits, goals, finances (accounts, transactions, installments, transfers), calendar with sports events, quick notes, Pomodoro, and a daily dashboard.

## Users
Individuals who want one dark-first productivity + money + sports screen instead of five apps. Primary use is daily and weekly scanning, then short actions (check habit, log expense, complete task).

## Platform
Web SPA served by Spring Boot static resources. Vanilla HTML/CSS/JS. No React. PWA-capable (`manifest.json`, service worker). Desktop-first with mobile sidebar drawer.

## Surfaces
- Dashboard / cockpit (KPIs, alerts, habits, goals, sports, notes)
- Tasks (create, list, Kanban, trash, stats, Pomodoro)
- Finances (summary cards, accounts, transactions, filters)
- Calendar (month grid, day details, events)
- Goals (grid, create/edit, aportes)
- Auth modal, onboarding, shortcuts, many detail modals

## Product truths
- Portuguese (pt-BR) UI copy
- Dark theme default; light "day ops" theme available
- Currency BRL; money formatted with `toLocaleString`
- Guest/test login path exists
- Keyboard shortcuts (N, F, 1–3, ?)

## Mode
**Operate** — the tool must disappear into the task. Scanability, consistency, and daily-use speed outrank spectacle.
