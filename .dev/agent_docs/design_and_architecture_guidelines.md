# Design and architecture guidelines

* follow "package-by-feature" approach
    - typical package structure for the project:

    ```
    /com.mygordienko.brennon
        /feature1           // root directory for a module
            /adapter        // controllers, repositories etc. (in and out adapters)
            /di             // spring configurations and configuration properties
            /internal       // classes not supposed to be used by other modules
            ...             // root feature package, feature domain classes and API exposed by module

        /feature2
            /adapter
            /di
            /internal
            ...
    ```

    - there can be sub-folders under `/adapter` to separate adapters by technology, ex: `/rest`, `/ws`, `/jdbc`
    - API exposed by feature module must be in feature's root package

* Avoid using `@Component`, prefer defining Spring beans in `@Configuration` files.
* Thymeleaf template/js directory `/resources/templates/` MUST have a subfolder structure that matches feature structure in backend, ex: if a template or js library belongs to `feature1` it must be put in `/resources/templates/feature1` folder.