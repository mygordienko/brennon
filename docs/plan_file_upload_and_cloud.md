# Adding "Attachments" and gradually migrating to a cloud environment

1. client JS request upload link from backend, providing file's mime-type
2. Backend prepares a temporary place to accept the file (calls `ObjectStore` and instructs it to prepare the space and generate upload link), and sends the link back to client
3. client uploads file using the link and receives back `uploadedFileId`
    - `ObjectStore` does not immediately start file processing/validation, but waits for an explicit command.
4. client sends update request with ticketId (or create ticket request) with relevant ticket data and `uploadedFileId`'s
5. Backend 
    - updates ticket DB by adding a new record for each attachment with `PENDING` status
    - `@TransactionalListener` fires `AttachmentAddedEvent`
    - redirects the client to the result page.
6. Client gets redirected and sees "Pending.." status for the attachments it has just added.
7. Ticket subsystem upon receiving `AttachmentAddedEvent`:
    - request ObjectStore to validate and copy file to the final place (`ProcessUploadCommand`)
8. When `ObjectStore` is done validating and copying the file it nootifies Ticket sybsystem (ex. via message queue).


**NOTE:** Attachment status is necessary if file processing (validation, malware scan, etc.) takes significant ammount of time and so MUST be done after uploaded file associated with a ticket. If we restrict attachment size to ~5MB and do processing immediately after upload (block user from proceeding with ticket update) then we can go without statuses.

Another important aspect is that this design allows gradual migration to the cloud, here are the steps:

- [x] Stage 1: Single instance for local deployment.
    - `ObjectStore` is implemented as a `LocalFileSystemObjectStore` that is backed by whatever disk is available on current machine
    - No message queue, async communication between Ticket subsystem and `ObjectStore` is done purely with Spring domain events.

- Stage 2: Multiple instances on local machine
    - introduce RabbitMQ to transfer messages between `ObjectStore` and Ticket subsystem (`AttachmentAddedEvent` and `ProcessUploadCommand`)

- Stage 3: Cloud deployment with simple storage
    - Use managed RabbitMQ service from Cloud provider
    - Use shared network drive attached to all instances of the app

- Stage 4: Cloud deployment with robust and scalable storage
    - Use managed RabbitMQ service from Cloud provider
    - `ObjectStore` is implemented to use S3 or similar service from Cloud provider