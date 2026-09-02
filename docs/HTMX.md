## HTMX Out-Of-Bound Swap

Example usage: [ticket-details.html](../src/main/resources/templates/ticket/ticket-details.html)

OOB Swap is a HTMX feature that allows updating multiple elements.

It works like this:

In a normal scenario HTMX takes what HTML it receives from backend and use it to replace `hx-target`. HTML element can have only one "hx-target" attribute.

And if we want to update multiple sections on a page using the same HTMX trigger we can use OOB Swap. We do it by:

* Putting `hx-swap-oob` on elements we want to update along with `hx-target`. 
    * We can use it together with `th:` to enable or disable update based on some variable value that will be evaluated on backend: `th:hx-swap-oob="${isOobSwap} ? 'true' : null"`.
        * By resolving to null, Thymeleaf completely omits the hx-swap-oob attribute from the generated HTML string.

* We then ensure that our `@Controller` returns all relevant fragments when OOB replacement is needed. And the beautiful part is that those relevant fragments can be returned as a virtual bundle, just as we did with `details-composite-bundle`. 
    * So we can analyze possible scenarios and decide something like: "For outcome A sections S1,S2 must be updated" and "For outcome B sections S4,S5,S6 must be updated". We then compose bundle `th:fragments="bundle-a"` and `th:fragments="bundle-b"` containing `th:insert` with relevant fragments.
    * We put those bundles as `th:block`s at the end of the target template (just before the closing `</body>`), and while it looks like HTML duplicating it is not, because in `@Controller` we return not a full template but specific fragment reference. Thymeleaf will compose HTML containing only fragments that are part of the bundle.
        * There is no duplication even when serving a full page, Thymeleaf ignores the `th:fragment` declarations and simply evaluates the virtual `<th:block>` content, effectively doing nothing because they are just references to blocks that **already rendered natively above**.
    * HTMX is clever enough to scan through the HTML it gets from backend for potential `hx-swap-oob` markers. It understands that sections marked with `hx-swap-oob` are not for primary target, they are separate sections that present somewhere on the page.
        * When HTMX finds a block with hx-swap-oob="true", it plucks it out of the incoming HTML stream completely.
        * It uses that block to update the matching ID element elsewhere on the page.
        * The remaining HTML code—the pieces without the OOB attribute—is what gets injected into the primary hx-target.

**IMPORTANT:** Top-Level Elements Only. The `hx-swap-oob` attribute must be on the top-level element of the returned HTML chunk.

This Will Fail:

```html
<th:block th:fragment="broken-bundle">
    <!-- Main Target -->
    <div id="reply-section">...</div>
    
    <!-- HTMX will NOT find the OOB attribute here because it is wrapped inside a generic wrapper -->
    <div class="some-wrapper">
        <div id="reply-form-section" hx-swap-oob="true">...</div>
    </div>
</th:block>
```

**IMPORTANT:** When returning multiple HTML fragments, the one which is for primary target MUST NOT have `hx-swap-oob`. As was described above, HTMX plucks such fragments out of the incoming HTML stream, so if the "primary" fragment has OOB attribute it will never reach `hx-target`. That might be a problem when a page has multiple dependent forms (ex. updating different parts of the same aggregate). One technique that helps to reduce complexity is having a top-level fragment, which can be replaced by HTMX is some cases (ex. handling "Cancel" buttons), see `page-content` in [ticket-details.html](../src/main/resources/templates/ticket/ticket-details.html).



## HTMX `hx-swap`

`hx-swap` can be used along with `hx-target` to specify HOW to insert the received HTML into target container.
    Ex: in [ticket-list.html](../src/main/resources/templates/ticket/ticket-list.html) we use `hx-swap="outerHTML"`
    to fully replace target <div> with the HTML received from backend, and for that we ensure that the root element returned by the server is the one we are targeting with `hx-target`: `id="page-content" th:fragment="page-content-block"`


## HTMX `HX-Request` header

The problem:

* The "Ticket list" decorates a common layout (sidebar, navigation).
* We want the table to have a basic sorting that is triggered by clicking on a column header.
* We want the page URL to update accordingly (reflecting sorting property and direction).
* We DO NOT want the whole page to reload (bad UX).

Wrong solutions:

* HTMX. `hx-get` to a separate endpoint `/tickets/my-partial` that serves Thymeleaf fragment; `hx-push-url` to update URL.
    * What is wrong: The URL remembers `/tickets/my-partial`, F5 breaks the page (only the fragment is loaded).

* Option 2: Still HTMX but with single endpoint `/tickets/my` that serves either a full template or a fragment depending on special parameter `mode`. `hx-get` with `hx-push-url` that calls `/tickets/my` with `mode=partial`.
    * the same problem as with previous solution.

Correct solution:

* HTMX uses a special header `HX-Request`, we can check it in our `@Controller` and return either a full template or just a fragment.


## HTMX `HX-Trigger` headers

The problem:

* Multiple forms on the same page (ex: reply and details edit)
* After one form updates the ticket, the new version must be propagated to other forms, otherwise users won't be able to update subject right after posting a reply.

OOB + virtual bundle do not always help. Ex: in [ticket-details.html](../src/main/resources/templates/ticket/ticket-details.html) "Ticket details editing form" is dynamic, it is not always present in the DOM, but loaded on demand (user hits "Edit"), therefore trying to reload it via OOB each time a new reply getss added is unsafe, we do not know if user is editing or just viewing.

The right solution: 

Use HX-Trigger response header to fire a JS event and use custom listener to dynamically lookup all existing version inputs across the document and update their value.

## Enabling HTMX logs

```
<script>
        htmx.logger = function(elt, event, data) {
            if(console) {
                console.log("HTMX Log -> Event: " + event, data);
            }
        }
</script>
```

## HTMX `HX-Refresh` header and proper session expiration handling

The problem:

By default Spring Security does cache request that triggrs authentication in order to
get the client back to the requested resource after authentication is done.

That is not what we want with HTMX. HTMX requests made with expired session must trigger full
page reload, otherwise users would see just a fragment after re-login.

That is exactly what `HX-Refresh` header is used for.