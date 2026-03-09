# Backend API Reference

Base URL: `http://localhost:8080`
Interactive docs: `http://localhost:8080/api/docs` (Swagger UI)

All endpoints except `/api/auth/**` and `/api/docs/**` require a Bearer JWT token in the `Authorization` header.

## Authentication

### Discord OAuth2 Flow

1. Frontend calls `GET /api/auth/discord/url` to get the Discord authorization URL.
2. User authenticates on Discord and is redirected to `/login/callback?code=...`.
3. Frontend sends the code to `POST /api/auth/discord` and receives a JWT.
4. All subsequent requests include `Authorization: Bearer <token>`.

### Token Details

| Claim | Description |
|-------|-------------|
| `sub` | User UUID |
| `iss` | `ark-resolver` |
| `iat` | Issued-at timestamp |
| `exp` | Expiration (24 hours from issuance) |
| `discordId` | Discord user ID |
| `username` | Discord username |
| `avatar` | Discord avatar hash |

Tokens are invalidated server-side by comparing `iat` against the `lastIssuedAt` field on the User entity. Issuing a new token (login or refresh) supersedes all previous tokens.

---

## Endpoints

### Auth (`/api/auth`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/api/auth/discord/url` | No | Returns Discord OAuth2 authorization URL |
| `POST` | `/api/auth/discord` | No | Exchanges Discord auth code for JWT |
| `GET` | `/api/auth/refresh` | Yes | Issues a new JWT, invalidating the previous one |

#### `GET /api/auth/discord/url`

**Response** `200`
```json
{ "url": "https://discord.com/api/oauth2/authorize?client_id=...&redirect_uri=...&response_type=code&scope=identify" }
```

#### `POST /api/auth/discord`

**Request**
```json
{ "code": "a6ML2UdpsqBozEXBKDjJaARBfqpqm1" }
```

**Response** `200`
```json
{ "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." }
```

#### `GET /api/auth/refresh`

**Response** `200`
```json
{ "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." }
```

---

### User Profile (`/api/users`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/api/users/me` | Yes | Get current user profile |
| `PUT` | `/api/users/me/nickname` | Yes | Update display nickname |
| `DELETE` | `/api/users/me` | Yes | Delete account and all associated data |
| `POST` | `/api/users/me/sync` | Yes | Sync profile with Discord (placeholder) |

#### `GET /api/users/me`

**Response** `200`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "discordId": "123456789012345678",
  "username": "Player",
  "nickname": "CoolAdventurer",
  "avatar": "a_abc123def456"
}
```

#### `PUT /api/users/me/nickname`

**Request**
```json
{ "nickname": "CoolAdventurer" }
```

---

### Groups (`/api/groups`)

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| `GET` | `/api/groups` | Yes | Any | List groups the user belongs to |
| `POST` | `/api/groups` | Yes | Any | Create a new group (user becomes MANAGER) |
| `GET` | `/api/groups/{groupId}` | Yes | Member | Get group details |
| `PUT` | `/api/groups/{groupId}` | Yes | MANAGER | Update group name/description |
| `DELETE` | `/api/groups/{groupId}` | Yes | MANAGER | Delete group and all associated data |
| `POST` | `/api/groups/join` | Yes | Any | Join a group using an invite code |

#### `POST /api/groups`

**Request**
```json
{
  "name": "Friday Night Static",
  "description": "Weekly raids and horizontal content."
}
```

**Response** `200`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Friday Night Static",
  "description": "Weekly raids and horizontal content."
}
```

#### `POST /api/groups/join`

**Request**
```json
{ "code": "ARK-ABCD-1234" }
```

---

### Group Members (`/api/groups/{groupId}/members`)

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| `GET` | `/api/groups/{groupId}/members` | Yes | Member | List group roster |
| `DELETE` | `/api/groups/{groupId}/members/{targetUserId}` | Yes | Self or MANAGER | Remove member or leave group |
| `PATCH` | `/api/groups/{groupId}/members/{targetUserId}/role` | Yes | MANAGER | Change member role (AUDITOR or MEMBER) |
| `POST` | `/api/groups/{groupId}/members/{targetUserId}/transfer` | Yes | MANAGER | Transfer MANAGER role to target |

#### `GET /api/groups/{groupId}/members`

**Response** `200`
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "CoolAdventurer",
    "role": "MANAGER",
    "discordId": "123456789012345678",
    "avatar": "a_abc123def456"
  }
]
```

#### `PATCH /api/groups/{groupId}/members/{targetUserId}/role`

**Request**
```json
{ "role": "AUDITOR" }
```

Valid roles for this endpoint: `AUDITOR`, `MEMBER`. Use the transfer endpoint for `MANAGER`.

---

### Invite Codes (`/api/groups/{groupId}/invites`)

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| `POST` | `/api/groups/{groupId}/invites` | Yes | MANAGER, AUDITOR | Generate a new invite code |
| `GET` | `/api/groups/{groupId}/invites` | Yes | MANAGER, AUDITOR | List active (non-expired, non-exhausted) codes |
| `DELETE` | `/api/groups/{groupId}/invites/{code}` | Yes | MANAGER, AUDITOR | Revoke an invite code |

#### `POST /api/groups/{groupId}/invites`

**Request**
```json
{
  "maxUsage": 5,
  "expirationDays": 7
}
```

**Response** `200`
```json
{
  "code": "ARK-ABCD-1234",
  "max": 5,
  "used": 0,
  "expires": "2026-03-16"
}
```

---

### Schedules (`/api/groups/{groupId}/schedules`, `/api/schedules`)

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| `POST` | `/api/groups/{groupId}/schedules` | Yes | MANAGER, AUDITOR | Create a schedule |
| `GET` | `/api/groups/{groupId}/schedules` | Yes | Member | List schedules for a group |
| `GET` | `/api/schedules/{scheduleId}` | Yes | Member | Get schedule details |
| `PUT` | `/api/groups/schedules/{scheduleId}` | Yes | MANAGER, AUDITOR | Update schedule |
| `DELETE` | `/api/groups/schedules/{scheduleId}` | Yes | MANAGER, AUDITOR | Delete schedule |

#### `POST /api/groups/{groupId}/schedules`

**Request**
```json
{
  "title": "March Week 1 Reset",
  "startTime": "2026-03-04T10:00:00Z",
  "endTime": "2026-03-11T05:00:00Z"
}
```

**Response** `200`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "March Week 1 Reset",
  "start": "2026-03-04T10:00:00Z",
  "end": "2026-03-11T05:00:00Z",
  "status": "ACTIVE"
}
```

Schedule `status` is computed at response time: `PLANNED` (before start), `ACTIVE` (between start and end), `PAST` (after end).

---

### Availability (`/api/schedules/{scheduleId}/availability`)

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| `PUT` | `/api/schedules/{scheduleId}/availability/me` | Yes | Member | Submit/overwrite availability blocks |
| `GET` | `/api/schedules/{scheduleId}/availability/me` | Yes | Member | Get own availability |
| `GET` | `/api/schedules/{scheduleId}/availability` | Yes | Member | Get all members' availability (heatmap data) |

#### `PUT /api/schedules/{scheduleId}/availability/me`

**Request**
```json
{
  "blocks": [
    { "start": "2026-03-04T18:00:00Z", "end": "2026-03-04T19:00:00Z" },
    { "start": "2026-03-05T20:00:00Z", "end": "2026-03-05T22:00:00Z" }
  ]
}
```

Blocks must fall within the schedule's start/end time range.

#### `GET /api/schedules/{scheduleId}/availability`

**Response** `200`
```json
[
  {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "blocks": [
      { "start": "2026-03-04T18:00:00Z", "end": "2026-03-04T19:00:00Z" }
    ]
  }
]
```

---

### Parties (`/api/schedules/{scheduleId}/parties`, `/api/parties`)

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| `POST` | `/api/schedules/{scheduleId}/parties` | Yes | MANAGER, AUDITOR | Create a party |
| `GET` | `/api/schedules/{scheduleId}/parties` | Yes | Member | List parties in schedule |
| `POST` | `/api/parties/{partyId}/join` | Yes | Member | Join a party |
| `POST` | `/api/parties/{partyId}/leave` | Yes | Member | Leave a party |
| `DELETE` | `/api/parties/{partyId}` | Yes | MANAGER, AUDITOR | Delete a party |
| `PATCH` | `/api/parties/{partyId}/complete` | Yes | MANAGER, AUDITOR | Set completion status |

#### `POST /api/schedules/{scheduleId}/parties`

**Request**
```json
{
  "title": "Valtan HM Party 1",
  "raidType": "Valtan Hard",
  "maxMembers": 8,
  "startTime": "2026-03-05T20:00:00Z"
}
```

**Response** `200`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "scheduleId": "660e8400-e29b-41d4-a716-446655440000",
  "title": "Valtan HM Party 1",
  "raidType": "Valtan Hard",
  "members": 0,
  "max": 8,
  "status": "Planned",
  "start": "2026-03-05T20:00:00Z",
  "joinedMembers": []
}
```

Party `status` is computed: `Planned` (before start), `On-going` (after start), `Done` (marked complete).

#### `PATCH /api/parties/{partyId}/complete`

**Request**
```json
{ "completed": true }
```

---

## Error Responses

All errors return a consistent JSON structure:

```json
{
  "message": "Human-readable error description",
  "status": 404,
  "resourceId": "550e8400-e29b-41d4-a716-446655440000"
}
```

`resourceId` is optional and included when the error relates to a specific entity.

| Status | Exception | Meaning |
|--------|-----------|---------|
| `400` | `ValidationException` | Invalid input or business rule violation |
| `401` | `BadCredentialsException` | Missing, expired, or invalidated JWT |
| `403` | `ForbiddenException` | Insufficient permissions (not a member or wrong role) |
| `404` | `ResourceNotFoundException` | Entity not found |
| `500` | Unhandled exception | Internal server error |

---

## Database Schema

### Tables

| Table | Description |
|-------|-------------|
| `users` | Registered users (Discord-linked) |
| `groups` | Group collectives (statics, guilds) |
| `group_members` | User-Group membership with role |
| `invite_codes` | Group invite codes with usage tracking |
| `schedules` | Scheduled time blocks per group |
| `member_availability` | Per-user availability blocks per schedule |
| `parties` | Raid parties within schedules |
| `party_members` | User-Party membership |

### Entity Relationships

- `User` 1:N `GroupMember` N:1 `Group` (many-to-many via join table)
- `Group` 1:N `InviteCode`
- `Group` 1:N `ScheduleInstance`
- `ScheduleInstance` 1:N `MemberAvailability`
- `ScheduleInstance` 1:N `Party`
- `Party` 1:N `PartyMember` N:1 `User`

All primary keys are UUIDs. Cascade deletes propagate from parent to children (deleting a group removes all members, schedules, parties, availability, and invite codes).

---

## Configuration Profiles

### Default (`application.yml`)
- MariaDB connection via environment variables
- JWT configuration
- Discord OAuth2 credentials
- Swagger UI at `/api/docs`

### Development (`application-dev.yml`)
- H2 in-memory database (`jdbc:h2:mem:arkdb`)
- Schema auto-created and dropped on shutdown
- SQL logging enabled
- H2 console at `/h2-console`
- Debug logging for Spring Web and Security

Activate with: `./gradlew bootRun --args='--spring.profiles.active=dev'`

---

## Plugin System (PF4J)

The backend supports runtime plugins via PF4J.

### Extension Points

Defined in `plugin-api/`:

```java
public interface GreetingProvider extends ExtensionPoint {
    String getGreeting();
}
```

### Creating a Plugin

1. Create a Gradle module depending on `plugin-api`.
2. Implement a `Plugin` subclass with `start()`/`stop()` lifecycle methods.
3. Implement extension point interfaces annotated with `@Extension`.
4. Configure manifest attributes in `build.gradle.kts`:
   - `Plugin-Class`, `Plugin-Id`, `Plugin-Version`, `Plugin-Provider`
5. Build as a JAR and place in the `plugins/` directory.

See `sample-plugin/` for a working reference implementation.
