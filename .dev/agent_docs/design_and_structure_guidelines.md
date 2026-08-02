# Design and architecture guidelines

* follow "package-by-feature" approach
    - typical package structure for the project:

    ```
    /com.mygordienko.brennon
        /feature1           // Modulith module
            /adapter        // controllers, repositories etc. (in and out adapters)
            /di             // spring configurations and configuration properties
            ...             // feature domain classes and managers, API exposed by module

        /feature2
            /adapter
            /di
            ...
    ```

    - there can be sub-folders under `/adapter` to separate adapters by technology, ex: `/rest`, `/ws`, `/jdbc`
    - API exposed by feature module must be in feature's root package

* Prefer defining Spring beans in `@Configuration` files. Avoid using @Component annotation on service/manager beans.

* follow Modulith best practices
* Thymeleaf template/js directory `/resources/templates/` MUST have a subfolder structure that matches feature structure in backend, ex: if a template or js library belongs to feature1 it must be put in `/resources/templates/feature1` folder