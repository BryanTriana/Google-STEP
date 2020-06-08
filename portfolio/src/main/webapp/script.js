/**
 * Loads a selector from another HTML file so that it can be used in the current
 * DOM.
 *
 * @param { string } selector - Selector that will be used across the document
 *     to refer to the HTML element
 * @param { string } filename - Filename of HTML file that is used to load the
 *     element
 *
 * @example loadSelector("#navbar", "navbar.html")
 */
function loadSelector(selector, filename) {
  $(document).ready(function() {
    $(selector).load(filename);
  });
}

/**
 * Highlights the current active link on the navigation bar by finding the 'nav'
 * tag and then searching if its children anchor links have the same 'href'
 * attribute.
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
 * Fetches comments from CommentServlet and adds them to the comments section.
 */
async function getComments() {
  const commentsResponse =
      await fetch('/comment-data?commentLimit=' + $('#comment-limit').val());
  const comments = await commentsResponse.json();

  const commentsContainer = $('#comments-container');
  commentsContainer.empty();

  for (comment of comments) {
    commentsContainer.append(createComment(
        comment.name, comment.message, moment(comment.timestampMillis)));
  }
}

/**
 * Displays a div to post a comment if the user is logged in and has a nickname,
 * if not then it displays prompts to login and to choose a nickname.
 */
async function allowPostComment() {
  const loginResponse = await fetch('/login-data');
  const isUserLoggedIn = await loginResponse.json();

  if (!isUserLoggedIn) {
    $('#nickname-section').addClass('invisible');
    $('#post-comment').addClass('invisible');
    return;
  }

  const nicknameResponse = await fetch('/nickname-data');
  const nickname = await nicknameResponse.json();

  if (nickname === '') {
    $('#post-comment').addClass('invisible');
  } else {
    $('#nickname-section').addClass('invisible');
  }

  $('#login-section').addClass('invisible');
}

/**
 * Creates a list element that displays information about the sender's email,
 * submission time, and the message of a comment.
 *
 * @param { string } name - The name of the person posting the comment
 * @param { string } message - The message included in the comment
 * @param { moment } submissionMoment The moment the comment was sent
 * @returns { jQuery } The jQuery object that holds a list element representing
 *     a comment
 */
function createComment(name, message, submissionMoment) {
  const commentElem = $('<li class="list-group-item"></li>');
  commentElem.append($('<h3>' + name + '</h3>'));
  commentElem.append(
      $('<p class="text-secondary">' +
        submissionMoment.format('MMMM Do YYYY, h:mm:ss a') + '</p>'));
  commentElem.append($('<p>' + message + '</p>'));

  return commentElem;
}
