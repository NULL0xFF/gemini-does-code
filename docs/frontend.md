# Frontend Architecture

## Overview

The frontend is a SvelteKit 2 single-page application built with Svelte 5, deployed as static HTML/CSS/JS via `@sveltejs/adapter-static`. It uses Tailwind CSS 4 with daisyUI 5 for styling and communicates with the backend REST API using JWT bearer tokens stored in `localStorage`.

## Tech Stack

| Technology | Purpose |
|-----------|---------|
| SvelteKit 2 | Routing, build tooling, static adapter |
| Svelte 5 | UI framework (runes: `$state`, `$derived`, `$props`) |
| Tailwind CSS 4 | Utility-first CSS |
| daisyUI 5 | Component class library (buttons, cards, modals, etc.) |
| Vite 7 | Dev server and bundler |
| TypeScript 5 | Type checking |

## Project Structure

```
frontend/src/
├── app.css                              Global styles (Tailwind + daisyUI + fonts)
├── app.html                             HTML shell
├── app.d.ts                             TypeScript declarations
├── lib/
│   ├── api.ts                           HTTP client (fetchApi, fetchJson, ApiError)
│   ├── index.ts                         Library barrel export
│   ├── assets/
│   │   └── favicon.svg
│   ├── components/
│   │   ├── ConfirmationModal.svelte     Reusable confirmation dialog
│   │   ├── ThemeToggle.svelte           Light/dark mode toggle
│   │   └── ToastContainer.svelte        Toast notification renderer
│   └── stores/
│       └── toast.svelte.ts              Toast notification state (Svelte 5 runes)
└── routes/
    ├── +layout.svelte                   Root layout (favicon, ToastContainer)
    ├── +layout.ts                       Prerender configuration
    ├── +page.svelte                     Landing page (/)
    ├── login/
    │   ├── +page.svelte                 Discord OAuth login (/login)
    │   └── callback/+page.svelte        OAuth callback handler (/login/callback)
    ├── dashboard/
    │   └── +page.svelte                 Main dashboard (/dashboard)
    ├── profile/
    │   └── +page.svelte                 Edit profile (/profile)
    ├── groups/
    │   ├── create/+page.svelte          Create group (/groups/create)
    │   ├── join/+page.svelte            Join via invite code (/groups/join)
    │   └── [id]/
    │       ├── +page.svelte             Group detail (/groups/:id)
    │       ├── settings/+page.svelte    Group settings (/groups/:id/settings)
    │       ├── invites/+page.svelte     Invite management (/groups/:id/invites)
    │       └── schedules/
    │           └── create/+page.svelte  Create schedule (/groups/:id/schedules/create)
    └── schedules/
        └── [id]/
            ├── availability/+page.svelte  Availability heatmap grid (/schedules/:id/availability)
            └── parties/
                └── create/+page.svelte    Create party with heatmap (/schedules/:id/parties/create)
```

## Authentication Flow

1. User clicks "Login with Discord" on `/login`.
2. Frontend fetches `GET /api/auth/discord/url` and redirects user to Discord.
3. Discord redirects back to `/login/callback?code=...`.
4. Callback page sends code to `POST /api/auth/discord` and stores the returned JWT in `localStorage` as `ark_token`.
5. User is redirected to `/dashboard`.

All protected pages check for `ark_token` in `localStorage` on mount and redirect to `/login` if absent. The `fetchApi` client automatically attaches the token as a `Bearer` header and handles 401 responses by clearing the token and redirecting.

## API Client (`src/lib/api.ts`)

### `fetchApi(endpoint, options?)`
Low-level fetch wrapper that:
- Auto-injects `Authorization: Bearer <token>` from `localStorage`
- Sets `Content-Type: application/json` for string bodies
- On 401: clears token and redirects to `/login?error=session_invalid`
- On error: throws `ApiError` with status, parsed response data, and message

### `fetchJson<T>(endpoint, options?)`
Calls `fetchApi` and parses the JSON response. Returns `{} as T` for 204 No Content.

### `ApiError`
Custom error class with `status: number` and `data: any` properties.

## Pages

### Landing (`/`)
Public page with branding and a login CTA. Redirects to `/dashboard` if already authenticated.

### Dashboard (`/dashboard`)
Central hub showing:
- User profile card (avatar, name, Discord ID, edit profile link)
- "My Groups" list with links to each group
- "Join a Group" card linking to `/groups/join`
- "New Group" button linking to `/groups/create`

### Group Detail (`/groups/:id`)
Displays group information with tabbed sections:
- **Schedules**: list of schedules with status badges (ACTIVE/PLANNED/PAST), links to availability and party creation
- **Parties**: list of parties per schedule with join/leave buttons, member count, status
- **Members**: group roster with role badges

Admins (MANAGER/AUDITOR) see additional controls: create schedule, create party, rename/delete schedule.

### Group Settings (`/groups/:id/settings`)
MANAGER-only controls:
- Edit group name and description
- Member management: change roles, kick members
- Transfer MANAGER role to another member
- Leave group / delete group (with confirmation modals)

### Invite Management (`/groups/:id/invites`)
MANAGER/AUDITOR controls:
- Generate invite codes with configurable max usage and expiration
- List active codes with usage stats
- Copy code to clipboard
- Revoke codes

### Availability (`/schedules/:id/availability`)
Interactive 8-day x 24-hour heatmap grid:
- Click/drag to select available time blocks
- Save/clear controls
- Responsive layout (rotated grid on mobile)

### Party Creation (`/schedules/:id/parties/create`)
Form to create a raid party:
- Title, raid type, max members, start time
- Availability heatmap overlay showing member counts per time slot
- Click heatmap cells to see which members are available

## Components

### `ConfirmationModal`
Generic confirmation dialog for destructive actions.

**Props**: `id`, `title`, `message`, `confirmText`, `type` (error/warning/info)
**Methods**: `show()`, `close()` (bind with `bind:this`)

### `ThemeToggle`
Light/dark mode toggle stored in `localStorage`. Sets `data-theme` on `<html>`.

### `ToastContainer`
Renders toast notifications from the `toast` store. Auto-dismisses after timeout.

## State Management

Uses Svelte 5 runes exclusively:
- `$state()` for reactive local state
- `$derived()` for computed values (e.g., `isManager`, `isAuditor`, `isAdmin`)
- `$props()` for component props

No external state management library. Each page manages its own state via `onMount` data fetching.

### Toast Store (`src/lib/stores/toast.svelte.ts`)
Shared notification system:
- `toast.success(message)`
- `toast.error(message)`
- `toast.warning(message)`
- `toast.info(message)`

## Role-Based UI

Authorization is computed per-page from the member roster:

```typescript
let isManager = $derived(members.find(m => m.id === currentUser?.id)?.role === 'MANAGER');
let isAuditor = $derived(members.find(m => m.id === currentUser?.id)?.role === 'AUDITOR');
let isAdmin = $derived(isManager || isAuditor);
```

UI elements are conditionally rendered:
- Create schedule/party buttons: visible when `isAdmin`
- Member management controls: visible when `isManager`
- Kick/transfer actions: visible when `isManager`

## Styling

### Theme System
- daisyUI provides light and dark themes via `data-theme` attribute
- `ThemeToggle` component persists preference in `localStorage`

### Fonts
- **Ubuntu**: primary heading font
- **NanumSquareNeo**: body text

### Responsive Design
- Tailwind breakpoints: `sm`, `md`, `lg`
- Mobile-first layout with grid adjustments for availability heatmap

## Build & Deployment

### Development
```bash
npm run dev          # Start Vite dev server on :5173
npm run check        # TypeScript + Svelte validation
npm run check:watch  # Watch mode
```

### Production
```bash
npm run build        # Output to /build
npm run preview      # Preview production build locally
```

### Static Adapter Config
- `fallback: '404.html'` for SPA client-side routing
- `BASE_PATH` environment variable for subdirectory deployments
- Output is a fully static site deployable to any static host (Vercel, Netlify, GitHub Pages, S3, etc.)
