# LifeHub — DESIGN.md

## Visual world
**Quiet Ledger** — minimal product UI. Soft light surfaces, 1px borders, one ink accent, no decoration for decoration’s sake. The tool disappears into the task.

Incumbent anti-reference: Night Ops Cockpit (navy + volt + emoji + gradients). Replaced.

## Palette (light default)
| Token | Value | Role |
|---|---|---|
| canvas | `#f7f7f8` | page |
| surface | `#ffffff` | cards / panels |
| surface-2 | `#f4f4f5` | hover / wells |
| surface-3 | `#e4e4e7` | badges |
| line | `#e4e4e7` | borders |
| line-strong | `#d4d4d8` | strong edges |
| ink | `#18181b` | primary text |
| ink-2 | `#3f3f46` | secondary |
| muted | `#71717a` | tertiary |
| accent | `#18181b` | primary actions (ink) |
| success | `#16a34a` | only when meaning requires |
| warning | `#d97706` | only when meaning requires |
| danger | `#dc2626` | only when meaning requires |

Dark theme: zinc surfaces (`#09090b` / `#18181b` / `#27272a`), same semantic meanings, ink inverted.

## Typography
- Inter for all UI
- Mono only for money, timers, measurements
- Fixed rem scale; labels 0.72–0.78; body 0.88–0.92; titles 1.15–1.5
- `tabular-nums` on numeric data
- No decorative tracking tricks

## Layout
- Sidebar 248px, light surface, 1px border
- Cards: 10–12px radius, 1px border, **no** colored left/top accent bars
- Spacing 4-based rhythm
- Density over airiness

## Elevation
Almost none. Prefer borders. Shadow only for overlays (dropdown, modal, toast).

## Motion
120–200ms. State feedback only. No bounce, no glow pulse.

## Icons
SVG stroke or text labels. **No emoji** in chrome, badges, empty states, or toasts.

## Banned
- Emoji as UI icons
- Navy/neon cockpit palette
- Gradients on buttons/brand/backgrounds
- Colored thick borders on cards
- Glow shadows
- Nested cards
