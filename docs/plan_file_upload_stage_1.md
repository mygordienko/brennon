# Implementation Plan — Ticket Attachments (Stage 1)

## Decisions locked in
- `Ticket` owns a `ticket_attachment_refs` link table; `TicketAttachment` is a separate aggregate root.
- Separate **`TicketAttachmentConverters.java`** for JDBC converters.
- `TicketAttachment` has **`created` and `modified`** fields, handled like `Ticket`'s timestamps (set on `createPending`, `modified` bumped on `markVerified`).
- **Edit `1_init.sql` in place** (+ its `tableExists` precondition in `main-changelog.yaml`) — no new changeSet.

## 1. Database (`src/main/resources/db/changelog/sql/1_init.sql`)
Append two tables + a sequence-free varchar PK for attachments:
```sql
create table ticket_attachments (
    id varchar not null primary key,
    status varchar not null,
    created timestamptz not null,
    modified timestamptz not null,
    version int not null default 0
);

create table ticket_attachment_refs (
    ticket_id bigint not null,
    attachment_id varchar not null,
    constraint fk_att_ref_ticket foreign key(ticket_id) references tickets(id)
);
create unique index ix_att_ref on ticket_attachment_refs (ticket_id, attachment_id);
```
`main-changelog.yaml`: add `not tableExists: ticket_attachments` (and `ticket_attachment_refs`) to changeSet 1's preconditions.

## 2. Domain value types (`com.mygordienko.brennon.ticket`)
- **`TicketAttachmentId`** — wraps `String value`; private ctor; `static of(String)`; `getValue()`; `toString()` returns value; `@EqualsAndHashCode(of="value")`. Also `static newId()` → `of(UUID.randomUUID().toString())` used by the store.
- **`TicketAttachmentStatus`** — enum `PENDING("0010")`, `VERIFIED("0020")` with `getCode()`/`static of(code)` (mirrors `TicketStatus`).
- **`ProcessNewAttachmentCommandEvent`** — `record(TicketAttachmentId attachmentId, TicketId ticketId) implements BrennonDomainEvent`.
- **`RemoveAttachmentCommandEvent`** — `record(TicketAttachmentId attachmentId) implements BrennonDomainEvent`.

## 3. Aggregates & collections (`...ticket.internal`)
- **`TicketAttachment`** — `@Table("ticket_attachments") extends AbstractAggregateRoot<TicketAttachment> implements Persistable<TicketAttachmentId>`. Fields: `@Id id`, `status`, `created`, `modified`, `@Version version`, `@Transient explicitNew`. Factory `createPending(TicketAttachmentId id, OffsetDateTime now)` (sets PENDING, created=modified=now, explicitNew=true). Method `markVerified(OffsetDateTime now)` (status=VERIFIED, modified=now). `isNew()` like `Ticket`.
- **`TicketAttachmentRef`** — `@Table("ticket_attachment_refs")` value element holding `@Column("attachment_id") TicketAttachmentId attachmentId` (no `@Id`; mapped as a child `Set` on `Ticket` via `@MappedCollection(idColumn="ticket_id")`).
- **`Ticket`** — add `@MappedCollection(idColumn="ticket_id") Set<TicketAttachmentRef> attachmentRefs`. Add:
  - `acceptAttachment(TicketAttachmentId id, OffsetDateTime updated)` → guard not CLOSED, add ref, `this.updated = updated`, `registerEvent(new ProcessNewAttachmentCommandEvent(id, this.id))`.
  - `removeAttachment(TicketAttachmentId id, OffsetDateTime updated)` → remove matching ref, `this.updated = updated`, `registerEvent(new RemoveAttachmentCommandEvent(id))`.
  - `getAttachmentIds()` → sorted/immutable list of ids from refs.

## 4. Ports & services (`...ticket.internal`)
- **`TicketAttachmentStore`** (port): `moveToFinalStorage(TicketAttachmentId, TicketId)`, `delete(TicketAttachmentId)`, `UploadUrl getUploadUrl(MimeType type, String origName)` (record `UploadUrl(TicketAttachmentId attachmentId, String url)`), `String getDownloadUrl(TicketAttachmentId)`, `void upload(TicketAttachmentId, MultipartFile)` (controller-facing), `Resource download(TicketAttachmentId)`.
- **`TicketAttachmentStoragePort`** (DB port): `save(TicketAttachment)`, `findById(TicketAttachmentId)`, `deleteById(TicketAttachmentId)`.
- **`TicketAttachmentService`**:
  - `@Transactional createPending(TicketAttachmentId id)` — saves a PENDING `TicketAttachment` (via CRUD-repo-backed port; `created=modified=now`).
  - `@Async @TransactionalEventListener(phase=AFTER_COMMIT)` `onProcessNew(ProcessNewAttachmentCommandEvent)` — `store.moveToFinalStorage(id, ticketId)`, load attachment, `markVerified(now)`, save (own tx).
  - `@Async @TransactionalEventListener(phase=AFTER_COMMIT)` `onRemove(RemoveAttachmentCommandEvent)` — in a tx: `storagePort.deleteById(id)` then `store.delete(id)`.

## 5. Adapters
- **jdbc**: `TicketAttachmentCrudRepository extends CrudRepository<TicketAttachment, TicketAttachmentId>` (fires domain events on save); `JdbcTicketAttachmentStorage implements TicketAttachmentStoragePort` (delegates to CRUD repo). New **`TicketAttachmentConverters`**: `TicketAttachmentId↔String`, `TicketAttachmentStatus↔String`. Register in `TicketDataJdbcConfiguration`.
- **storage**: `LocalFileSystemTicketAttachmentStore` — ctor `(Path rootDirectory)`; `@PostConstruct init()` → `Files.createDirectories(tmp)`, `Files.createDirectories(final)` (idempotent, multi-instance safe). `getUploadUrl` → new id + url `/api/v1/upload/{id}`. `getDownloadUrl` → `/api/v1/getDownloadUrl/{id}` (temp). `upload` → write `tmp/{id}`. `moveToFinalStorage` → ensure `final/{ticketId}/`, `Files.move(tmp/{id}, final/{ticketId}/{id}, ATOMIC_MOVE fallback REPLACE_EXISTING)`. `delete` → delete from final (searching ticket dirs) or tmp. `download` → `Resource` from tmp.
- **web**: `TicketAttachmentController` (`@RestController`): `GET /api/v1/getUploadUrl` (params `type`, `origName`) → `{attachmentId,url}`; `POST /api/v1/upload/{attachmentId}` (multipart `file`) → `{attachmentId}`; `GET /api/v1/getDownloadUrl/{attachmentId}` → `{url}`; `GET /api/v1/download/{attachmentId}` → file bytes. All under `user:write` authority via existing security (authenticated). CSRF: these are POST/GET; upload POST needs CSRF token (sent from JS).

## 6. Commands, DTOs, view, service wiring
- `TicketCreateCommand` += `List<TicketAttachmentId> attachmentIds`.
- `TicketEditCommand` += `List<TicketAttachmentId> newAttachmentIds`, `List<TicketAttachmentId> removedAttachmentIds`.
- `TicketCreateCommandDto` += `List<String> attachmentIds` (default empty; `toCommand` maps).
- `TicketEditCommandDto` += `List<String> newAttachmentIds`, `List<String> removedAttachmentIds`.
- `TicketAttachmentDto(String id, String status)`; `TicketDto` += `List<TicketAttachmentDto> attachments`; `composeDto` populates from view.
- `TicketDetailsView` += `List<AttachmentView>` (record `id`,`status`); `TicketService.getTicket` loads attachments (via storage port using the ticket's attachment ids) and maps.
- `TicketService.createTicket`: after building ticket, for each id → `attachmentService.createPending(id)` + `ticket.acceptAttachment(id, created)`, then save (events fire).
- `TicketService.editTicketDitails`: for each `newAttachmentIds` → `createPending` + `acceptAttachment`; for each `removedAttachmentIds` → `removeAttachment`; then save.
- Controller: `createTicket` POST and `editTicketDetails` PUT already pass DTO→command; will forward new lists.

## 7. DI / config
- `TicketServiceConfiguration`: add beans `TicketAttachmentStore` (root dir from `@Value("${brennon.attachments.root-directory}")`), `TicketAttachmentStoragePort`, `TicketAttachmentService`; inject `TicketAttachmentService` into `TicketService`.
- `TicketDataJdbcConfiguration`: register `TicketAttachmentConverters`.
- New `AsyncConfiguration` with `@EnableAsync` + `ThreadPoolTaskExecutor` bean (needed — none exists today).
- `application.yaml`: add `brennon.attachments.root-directory: ${BRENNON_ATTACHMENTS_DIR:./data/attachments}`.

## 8. UI (Uppy headless, no Dashboard)
- `frontend/src/index.js`: import `@uppy/core`, `@uppy/file-input`, `@uppy/xhr-upload`. Export `initTicketAttachments(rootEl)`:
  - Uppy instance with `XHRUpload` where each file's endpoint is resolved from `GET /api/v1/getUploadUrl` (per-file), sending CSRF header (read from a meta tag / hidden input). Field name `file`.
  - On `upload-success`, read returned `attachmentId`, append hidden `<input name="attachmentIds"|"newAttachmentIds" value="id">` + a styled list row with a remove button (Tailwind classes matching existing controls).
  - Remove (pending upload): delete hidden input + row.
  - For edit page existing attachments: a "remove" button toggles a hidden `<input name="removedAttachmentIds" value="id">` and visually marks the row.
  - Auto-init on `DOMContentLoaded` and on `htmx:afterSwap` for `[data-attachments]` containers (edit form loads via HTMX).
- `create-ticket.html`: add attachments section (Add-file button `<input type=file>` styled, list container, `data-attachments` with `data-field="attachmentIds"`). Add CSRF meta if needed (create form already has `th:object`; add a hidden csrf or meta tag in layout head).
- `edit-ticket.html`: same section with `data-field="newAttachmentIds"`; render `th:each` over `ticket.attachments` showing status + remove toggle producing `removedAttachmentIds`.
- Rebuild bundle: `npm run build` in `src/main/frontend` → updates `static/dist/app.bundle.js`.

## 9. Tests (only affected)
- `TicketTest`: `acceptAttachment` adds ref + registers `ProcessNewAttachmentCommandEvent` + bumps `updated`; `removeAttachment` registers `RemoveAttachmentCommandEvent`.
- New `LocalFileSystemTicketAttachmentStoreTest` (JUnit `@TempDir`): dir creation, upload→move→download→delete, url/id generation.
- New `TicketAttachmentServiceTest` (Mockito): process listener moves file + sets VERIFIED + saves; remove listener deletes DB then file.
- Run only these via Gradle test filters (`--tests`), per AGENTS.md (never full suite).

## 10. Workflow / git
Per AGENTS.md: create branch `feature/ticket-attachments` (no commits on `main`, no push). Implement → run targeted tests + `npm run build` → self-review the diff → single Conventional Commit `feat(ticket): add two-stage file attachments for tickets` (only after your go-ahead for the commit step).
