# Camp Chat API

The API uses the existing Keycloak bearer-token authentication. Every route is under `/api/camp`; callers must send `Authorization: Bearer <access-token>`. A player is eligible only when their authenticated `UserProfile` owns a survivor. The backend selects the one camp (`shared`) and derives the sender from that survivor; request bodies cannot select a camp or sender.

## Start A Visit

`POST /api/camp/visits` accepts no body and returns `201`:

```json
{
  "visitId": "uuid",
  "startedAt": "2026-10-02T16:30:00Z"
}
```

Every call creates a distinct visit, including a page refresh. Visits expire 24 hours after creation and are deleted by the hourly cleanup job. Opening another tab does not invalidate an existing tab's visit. The client should keep the visit ID in that tab's memory and start a new visit on a fresh Camp entry.

## Read And Send Messages

`GET /api/camp/visits/{visitId}/messages?limit=50&cursor=<cursor>` returns chronological pages:

```json
{
  "items": [
    {
      "id": "uuid",
      "content": "The east gate is holding.",
      "senderName": "Mara",
      "createdAt": "2026-10-02T16:31:12.345Z"
    }
  ],
  "nextCursor": null
}
```

`limit` defaults to 50 and must be between 1 and 100. The opaque cursor advances by `(createdAt, id)`; send the returned cursor unchanged to fetch the next page. History is constrained by the server-recorded visit start, with no client timestamp accepted.

`POST /api/camp/visits/{visitId}/messages` accepts `{ "content": "..." }` and returns the persisted message with `201`. Content is trimmed at both ends, must contain non-whitespace text, and may contain at most 500 Unicode code points after trimming. Supplementary characters such as emoji count as one code point each. Internal whitespace is preserved. Content is stored and returned as plain text; render it as text, never as HTML.

Unknown request properties are ignored by the request DTO; only `content` is used. Errors follow the existing `{ "status": 400, "message": "..." }` response format. An unauthorized or expired/foreign visit is not found for visit operations.

## Realtime

`GET /api/camp/visits/{visitId}/events` returns an authenticated Server-Sent Events stream. Use `fetch` or an EventSource-compatible fetch client that can set the bearer `Authorization` header; the native browser `EventSource` constructor cannot set it. No token or ticket is placed in the URL.

Events are named `message.created`, with the same message object as HTTP. The SSE event ID is the opaque history cursor for that message. On reconnect, fetch history using the last received event ID as `cursor` to recover missed committed messages, then resume the stream. The SQL history is authoritative; clients should deduplicate by message ID.

The current broadcaster is in-process. Run one backend instance for cross-player live delivery; multi-instance deployment needs a shared broker or database-backed fan-out. Persisted history and authorization work across instances regardless.

## Setup And Verification

Hibernate creates/updates the new tables using the existing `spring.jpa.hibernate.ddl-auto=update` policy. The shared camp is inserted idempotently by `data/03_camp.sql` during normal SQL initialization. No separate migration command or new environment variable is required. Tests/build use `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew test` and `... ./gradlew build` on this macOS development environment.

There is no existing rate-limiting mechanism in this backend. The initial implementation does not add a process-local limiter; apply the deployment's gateway limit to message POSTs and event connections before exposing a multi-instance public deployment.

## Frontend Adapter Changes

In `camp-chat-service.js`, replace the in-memory mock with:

1. `POST /api/camp/visits` on each Camp entry; retain `visitId` in page/tab state.
2. `GET /api/camp/visits/{visitId}/messages` with the bearer header; follow `nextCursor` for older pages.
3. `POST /api/camp/visits/{visitId}/messages` with `{content}` and the bearer header.
4. An authenticated fetch-based SSE client at `/api/camp/visits/{visitId}/events`; handle `message.created`, store each event ID as the latest cursor, and reconnect by fetching after that cursor before reopening the stream.

The message mapping already matches the current React shape (`id`, `content`, `senderName`, `createdAt`); IDs are UUID strings. Visit creation is new client behavior. Abort the stream when leaving Camp and reconnect only while that tab's visit remains valid.