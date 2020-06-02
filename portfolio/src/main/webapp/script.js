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

/**
 * Fetches comments from CommentServlet and adds them to the comment section in the DOM
 */
function getComments() {
    fetch('/comment-data').then(response => response.json()).then((comments) => {
        const commentsContainer = $('#comments-container');
        
        for(comment of comments) {
            commentsContainer.append(createComment(comment.name, comment.message));
        };
    });
}

/**
 * Creates a list element that displays information about the name and the message
 * of a comment
 * 
 * @param { string } name - The name of the person posting the comment
 * @param { string } message - The message included in the comment
 * @returns { jQuery } The jQuery object that holds a list element representing a comment
 */
function createComment(name, message) {
    const commentElem = $('<li></li>');
    commentElem.append($('<h3>' + name + '</h3>'));
    commentElem.append($('<p>' + message + '</p>'));

    return commentElem;
}
