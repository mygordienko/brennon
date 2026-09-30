package com.mygordienko.brennon.ticket.adapter.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.ticket.FileId;
import com.mygordienko.brennon.ticket.FileMeta;
import com.mygordienko.brennon.ticket.TicketAttachmentStorageException;
import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.internal.ResourceBundle;
import com.mygordienko.brennon.ticket.internal.TicketAttachmentStoragePort;

public class LocalFileSystemTicketAttachmentStorage implements TicketAttachmentStoragePort {
    
    public LocalFileSystemTicketAttachmentStorage(Path baseFolder) {
        this.baseFolder = baseFolder;
    }

    private static final String ORIG_FILE_NAME_CUSTOM_ATTRIBUTE = "user:orig.filename";

    private final Path baseFolder;
    private final Path tmpBaseFolder = Paths.get("tmp");
    private final Path ticketsBaseFolder = Paths.get("tickets");

    @Override
    public void saveTemp(BrennonUserId user, FileId fileId, InputStream is, String fileName) throws TicketAttachmentStorageException {
        var target = getTempPathForUser(user, fileId);

        try {
            Files.createDirectories(target.getParent());
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
            writeCustomAttribute(target, ORIG_FILE_NAME_CUSTOM_ATTRIBUTE, fileName);
        } catch (IOException e) {
            throw new TicketAttachmentStorageException(e);
        }
    }

    @Override
    public void moveToFinal(BrennonUserId user, FileId fileId, TicketId ticketId) throws TicketAttachmentStorageException {
        var source = getTempPathForUser(user, fileId);
        var target = getFinalPathForTicket(ticketId, fileId);

        try (var is = Files.newInputStream(source, StandardOpenOption.READ)) {
            Files.createDirectories(target.getParent());
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
            // NOTE: StandardCopyOption.COPY_ATTRIBUTES does not guarantee copying of custom user-defined attributes
            copyCustomAttributes(source, target);
        } catch (IOException e) {
            throw new TicketAttachmentStorageException(e);
        }
    }

    @Override
    public void delete(FileId fileId, TicketId ticketId) throws TicketAttachmentStorageException {
        var target = getFinalPathForTicket(ticketId, fileId);

        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new TicketAttachmentStorageException(e);
        }
    }

    @Override
    public ResourceBundle getDownloadResource(FileId fileId, TicketId ticketId) {
        try {
            var target = getFinalPathForTicket(ticketId, fileId);
            Resource resource = new UrlResource(target.toUri());

            if (resource.isReadable()) {
                var originalFilename = readCustomAttribute(target, ORIG_FILE_NAME_CUSTOM_ATTRIBUTE);

                return new ResourceBundle(resource, new FileMeta(originalFilename));
            } else {
                throw new TicketAttachmentStorageException("File not readable: %s".formatted(fileId));
            }
        } catch (IOException e) {
            throw new TicketAttachmentStorageException("Could not read file: %s".formatted(fileId), e);
        }
    }

    @Override
    public FileMeta getTempMeta(BrennonUserId userId, FileId fileId) throws TicketAttachmentStorageException {
        var target = getTempPathForUser(userId, fileId);

        try {
            var originalFilename = readCustomAttribute(target, ORIG_FILE_NAME_CUSTOM_ATTRIBUTE);
            return new FileMeta(originalFilename);
        } catch (IOException e) {
            throw new TicketAttachmentStorageException(e);
        }
    } 

    private final Path getTempPathForUser(BrennonUserId userId, FileId fileId) {
        return baseFolder.resolve(tmpBaseFolder).resolve(userId.toString()).resolve(fileId.toString());
    }

    private final Path getFinalPathForTicket(TicketId ticketId, FileId fileId) {
        return baseFolder.resolve(ticketsBaseFolder).resolve(ticketId.toString()).resolve(fileId.toString());
    }

    private void copyCustomAttributes(Path source, Path target) throws IOException {
        var fileName = readCustomAttribute(source, ORIG_FILE_NAME_CUSTOM_ATTRIBUTE);
        writeCustomAttribute(target, ORIG_FILE_NAME_CUSTOM_ATTRIBUTE, fileName);
    }
    
    private final void writeCustomAttribute(Path target, String attribute, String value) throws IOException {
        Files.setAttribute(target, attribute, StandardCharsets.UTF_8.encode(value));
    }

    private final String readCustomAttribute(Path target, String attribute) throws IOException {
        byte[] bytes = (byte[]) Files.getAttribute(target, attribute);
        return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes)).toString();
    }
}
