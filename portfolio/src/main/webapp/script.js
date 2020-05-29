/**
 * Loads a selector from another HTML file so that it can be used in the current DOM
 *
 * @param { string } selector - Selector that will be used across the document to refer to the HTML element
 * @param { string } filename - Filename of HTML file that is used to load the element
 *
 * @example loadSelector("#navbar", "navbar.html")
 */
function loadSelector(selector, filename) {
    $(document).ready(function() {
        $(selector).load(filename);
    });
}

/**
 * Highlights the current active link on the navigation bar by finding the 'nav' tag and then searching
 * if its children anchor links have the same 'href' attribute
 */
function highlightActivePage() {
    $(document).ready(function() {
        const links = $('nav a');

        for (let i = 0; i < links.length; ++i) {
            if (links[i].href === window.location.href) {
                links[i].classList.add('active');
                return;
            }
        }
    });
}
