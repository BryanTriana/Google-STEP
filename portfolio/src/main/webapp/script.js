/**
 * Loads a selector from another HTML file so that it can be used in
 * the current DOM
 *
 * @param { string } selectorID - ID that will be used across the document to refer to the HTML element
 * @param { string } filename - Filename of HTML file that is used to load the element
 *
 * @example loadSelector("navbar", "navbar.html")
 */
function loadSelector(selectorID, filename) {
    $(document).ready(function() {
        $('#' + selectorID).load(filename);
    });
}

/**
 * Highlights the current active link on the navigation bar. The links used
 * to navigate the website should be enclosed by an element with ID 'link-container'.
 * This function should be called after the navigation bar is defined in the DOM tree
 */
function highlightActivePage() {
    const linkContainer = document.getElementById('link-container');
    const links = linkContainer.getElementsByTagName('a');

    for(let i = 0; i < links.length; ++i) {
        if(links[i].href === window.location.href) {
            links[i].classList.add('active');
        }
    }
}
