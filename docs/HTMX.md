## HTMX Out-Of-Bound Swap

Example usage: [ticket-details.html](../src/main/resources/templates/ticket/ticket-details.html)

OOB Swap is a HTMX feature that allows updating multiple elements.

It works like this:

In a normal scenario HTMX takes what HTML it receives from backend and use it to replace `hx-target`. HTML element can have only one "hx-target" attribute.

And if we want to update multiple sections on a page using the same HTMX trigger we can use OOB Swap. We do it by:

* Putting `hx-swap-oob` on elements we want to update along with `hx-target`. 
    * We can use it together with `th:` to enable or disable update based on some variable value that will be evaluated on backend: `th:hx-swap-oob="${isOobSwap} ? 'true' : null"`.
        * By resolving to null, Thymeleaf completely omits the hx-swap-oob attribute from the generated HTML string.

* We then ensure that our `@Controller` returns all relevant fragments when OOB replacement is needed. And the beautiful part is that those relevant fragments can be returned as a virtual bundle, just as we did with `reply-composite-bundle`. 
    * So we can analyze possible scenarios and decide something like: "For outcome A sections S1,S2 must be updated" and "For outcome B sections S4,S5,S6 must be updated". We then compose bundle `th:fragments="bundle-a"` and `th:fragments="bundle-b"` containing `th:insert` with relevant fragments.
    * We put those bundles as `th:block`s at the end of the target template (just before the closing `</body>`), and while it looks like HTML duplicating it is not, because in `@Controller` we return not a full template but specific fragment reference. Thymeleaf will compose HTML containing only fragments that are part of the bundle.
        * There is no duplication even when serving a full page, Thymeleaf ignores the `th:fragment` declarations and simply evaluates the virtual `<th:block>` content, effectively doing nothing because they are just references to blocks that **already rendered natively above**.
    * HTMX is clever enough to scan through the HTML it gets from backend for potential `hx-swap-oob` markers. It understands that sections marked with `hx-swap-oob` are not for primary target, they are separate sections that present somewhere on the page.
        * When HTMX finds a block with hx-swap-oob="true", it plucks it out of the incoming HTML stream completely.
        * It uses that block to update the matching ID element elsewhere on your page.
        * The remaining HTML code—the pieces without the OOB attribute—is what gets injected into your primary hx-target.

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