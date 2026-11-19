## Pure streaming upload

By default Spring has its default `ServletMultipartResolver` which does trigger file content caching when request body is accessed in any way (ex. via Spring Security). Also with the default multipart handling the request body will be cached in temp files or memory (if small enough) when using `@RequestParam` with `MultipartFile`. To achieve pure streaming file upload the default Spring's multipart data processing must be disabled (globally via properties or for specific endpoints via custom bean.

```java
@Bean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
public MultipartResolver multipartResolver() {
    return new StandardServletMultipartResolver() {
        @Override
        public boolean isMultipart(HttpServletRequest request) {
            if (request.getRequestURI().endsWith("/api/v1/tickets/attachments/upload")
                    || request.getRequestURI().endsWith("/api/v1/profile/images/upload")) {
                return false;
            }
            return super.isMultipart(request);
        }
    };
}
```

**NOTE:** There can be only one `MultipartResolver` bean.

With default multipart handling disabled the file upload must be handled manuallym using `JakartaServletFileUpload` from `org.apache.commons:commons-fileupload2-jakarta-servlet6:2.0.0-M5` which allows iterating over form fields and reading `file` ones.

**NOTE:** Disabling default multipart handling also disables default file size limit checks (`spring.servlet.multipart.max-file-size`), so limits must be explicitly configured for `JakartaServletFileUpload`.

## Large file upload and early server response

Scenario: 5MB limit is configured on the server (Spring Boot) and a client tries to upload 8MB file.

Tomcat has `max-swallow-size` property which is 2MB by default. By default, an HTTP/1.1 client opens a socket, sends the request headers (POST /upload), and **immediately** starts streaming the file body without pausing to read from the socket. If Tomcat stops reading early and closes the socket while unread bytes are still in transit, the OS kernel triggers a TCP RST (Reset) packet. The client see it as a network error and retries or fails reporting network problem, **it does not get server error response**!

Some command-line tools and HTTP clients (most notably cURL when sending payloads over 1 KB) automatically add an `Expect: 100-continue` header to the request. The client sends only the HTTP headers (including Content-Length) and pauses before sending the file bytes. That makes the client get an error response if the server sends one.

The `max-swallow-size` kicks in after Tomcat receives error signal (ex. exception) from request handler (controller). Tomcat tries to read up to `max-swallow-size` bytes more from the stream, if it is enough to completely drain the stream then the client will get the normal error response from controller, if it is not enough then Tomcat just drops the connection with `RST` (our scenario).