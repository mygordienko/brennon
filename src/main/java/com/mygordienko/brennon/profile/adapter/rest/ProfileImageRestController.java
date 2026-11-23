package com.mygordienko.brennon.profile.adapter.rest;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.core.FileUploadSizeException;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.common.ResourceBundle;
import com.mygordienko.brennon.core.common.adapter.FileContentValidator;
import com.mygordienko.brennon.profile.internal.ProfileImageService;

import jakarta.servlet.http.HttpServletRequest;

@RequestMapping("/api/v1/profile")
@RestController
public class ProfileImageRestController {
    public ProfileImageRestController(
            ProfileImageService profileImageService,
            FileContentValidator fileContentValidator,
            @Value("${spring.servlet.multipart.max-file-size:5MB}") DataSize maxFileSize,
            @Value("${spring.servlet.multipart.max-request-size:5MB}") DataSize maxRequestSize) {
        this.profileImageService = profileImageService;
        this.fileContentValidator = fileContentValidator;
        this.maxFileSize = maxFileSize;
        this.maxRequestSize = maxRequestSize;
    }

    private static final Logger log = LoggerFactory.getLogger("com.mygordienko.brennon.profile.adapter.rest.ProfileImageRestController");

    /**
     * NOTE: profile images are cached by the browser only, see `ProfileImageHttpCachingConfiguration`
     * for the security part of the caching policy. The image URL is stable, so a freshly uploaded
     * image may be served from the browser cache until it expires.
     */
    private static final Duration PROFILE_IMAGE_CACHE_MAX_AGE = Duration.ofMinutes(10);

    private final ProfileImageService profileImageService;
    private final FileContentValidator fileContentValidator;
    private final DataSize maxFileSize;
    private final DataSize maxRequestSize;

    /**
     * NOTE: Custom multipart processing to achieve pure streaming without storing request into temp files.
     */
    @PostMapping("/images/upload")
    public UploadedFileDto upload(HttpServletRequest request) {
        var fileId = profileImageService.generateNextFileId();
        boolean fileUploaded = false;

        try {
            @SuppressWarnings("rawtypes")
            JakartaServletFileUpload upload = new JakartaServletFileUpload();
            upload.setMaxFileSize(maxFileSize.toBytes());
            upload.setMaxSize(maxRequestSize.toBytes());
            upload.setMaxFileCount(1);
            FileItemInputIterator iter = upload.getItemIterator(request);

            while (iter.hasNext()) {
                FileItemInput item = iter.next();
                if (!item.isFormField()) {
                    String fileName = item.getName();
                    checkFileName(fileName);

                    try (var is = fileContentValidator.checkRealFileType(item.getInputStream())) {
                        log.info("Uploading profile image {}", fileName);
                        profileImageService.saveTemp(fileId, is, fileName);
                    }
                    fileUploaded = true;
                    // NOTE: we expect single file per request
                    break;
                }
            }
        } catch (FileUploadSizeException e) {
            throw new IllegalArgumentException("Profile image is too large");
        } catch (IOException e) {
            throw new RuntimeException("Error uploading profile image", e);
        }

        if (!fileUploaded) {
            throw new IllegalArgumentException("No profile image provided");
        }

        return new UploadedFileDto(fileId.toString());
    }

    @GetMapping("/images/me")
    public ResponseEntity<Resource> getCurrentUserImage() {
        return profileImageService.getCurrentUserProfileImage()
                .map(ProfileImageRestController::toResponse)
                .orElseGet(ProfileImageRestController::notFoundResponse);
    }

    @GetMapping("/images/{profileId}")
    public ResponseEntity<Resource> getImage(@PathVariable String profileId) {
        return profileImageService.getProfileImage(BrennonUserId.of(profileId))
                .map(ProfileImageRestController::toResponse)
                .orElseGet(ProfileImageRestController::notFoundResponse);
    }

    private static ResponseEntity<Resource> notFoundResponse() {
        return ResponseEntity.notFound()
                .cacheControl(CacheControl.noStore())
                .build();
    }

    private static ResponseEntity<Resource> toResponse(ResourceBundle bundle) {
        var fileName = bundle.meta().fileName();
        MediaType mediaType = MediaTypeFactory.getMediaType(fileName)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(PROFILE_IMAGE_CACHE_MAX_AGE).cachePrivate())
                // NOTE: image is per user, make sure another user of the same browser gets its own image
                .varyBy(HttpHeaders.COOKIE)
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(fileName).build().toString())
                .body(bundle.resource());
    }

    private static final Pattern ILLEGAL_FILENAME_CHARACTERS = Pattern.compile("[^a-zA-Z0-9._-]");

    private void checkFileName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("File to upload has no name");
        }

        var lowerCaseFileName = originalFilename.toLowerCase(Locale.ROOT);
        if (lowerCaseFileName.endsWith(".svg") || lowerCaseFileName.endsWith(".svgz")) {
            throw new IllegalArgumentException("SVG images are not supported");
        }

        Matcher matcher = ILLEGAL_FILENAME_CHARACTERS.matcher(originalFilename);
        if (matcher.find()) {
            throw new IllegalArgumentException("File to upload has invalid file name");
        }
    }

    public record UploadedFileDto(String fileId) {
    }
}
