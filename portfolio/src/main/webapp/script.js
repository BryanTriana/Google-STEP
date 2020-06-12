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
 * Displays the contact form is the user is logged in, otherwise
 * prompts the user to login.
 */
async function allowContactForm() {
  try {
    const loginResponse = await fetch('/login-data');
    const isUserLoggedIn = await loginResponse.json();

    if (!isUserLoggedIn) {
      $('#contact-section').addClass('invisible');
      return;
    }

    $('#login-section').addClass('invisible');
  } catch (err) {
    $('#contact-section').addClass('invisible');

    console.log('failed to fetch login status: ' + err);
  }
}

/**
 * Displays a div to post a comment if the user is logged in and has a nickname,
 * if not then it displays prompts to login and to choose a nickname.
 */
async function allowPostComment() {
  try {
    const loginResponse = await fetch('/login-data');
    const isUserLoggedIn = await loginResponse.json();

    if (!isUserLoggedIn) {
      $('#nickname-section').addClass('invisible');
      $('#post-comment').addClass('invisible');
      return;
    }

    $('#login-section').addClass('invisible');
  } catch (err) {
    $('#nickname-section').addClass('invisible');
    $('#post-comment').addClass('invisible');

    console.log('failed to fetch login status: ' + err);

    return;
  }

  try {
    const nicknameResponse = await fetch('/nickname-data');
    const nickname = await nicknameResponse.json();

    if (nickname === null) {
      $('#post-comment').addClass('invisible');
      return;
    }

    $('#nickname-section').addClass('invisible');
  } catch (err) {
    $('#post-comment').addClass('invisible');

    console.log('failed to fetch nickname: ' + err);
  }
}

/**
 * Fetches comments from CommentServlet and adds them to the comments section.
 */
async function getComments() {
  try {
    const commentsResponse = await fetch(
        '/comment-data?commentLimit=' + $('#comment-limit').val() +
        '&languageCode=' + $('#language-selector').val());

    const comments = await commentsResponse.json();

    const commentsContainer = $('#comments-container');
    commentsContainer.empty();

    for (comment of comments) {
      commentsContainer.append(createComment(
          comment.name, comment.message, moment(comment.timestampMillis)));
    }
  } catch (err) {
    console.log('failed to fetch comments: ' + err);
  }
}

/**
 * Creates a list element that displays information about the sender's name,
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

/**
 * Fetches markers data from MapsServlet and adds them to a map.
 */
function createMap() {
  fetch('/maps-data').then((response) => response.json()).then((markers) => {
    const map = new google.maps.Map($('#map')[0], {
      zoom: 2,
      center: {lat: 0, lng: 0},
    });

    for (marker of markers) {
      new google.maps.Marker({
        position: {lat: marker.latitude, lng: marker.longitude},
        map: map,
      });
    }
  });
}

google.charts.load('current', {packages: ['corechart']});
google.charts.setOnLoadCallback(drawProjectChart);

/**
 * Fetches project idea voting data from ProjectServlet and draws column chart
 * showing the amount of votes each project has.
 */
function drawProjectChart() {
  fetch('/project-data')
      .then((response) => response.json())
      .then((projectVoteMap) => {
        const projectTable = new google.visualization.DataTable();
        projectTable.addColumn('string', 'Project');
        projectTable.addColumn('number', 'Votes');

        Object.keys(projectVoteMap).forEach((project) => {
          projectTable.addRow([project, projectVoteMap[project]]);
        });

        const options = {
          title: 'Next Project Ideas',
          height: 500,
          width: $('#container').width(),
          titleTextStyle: {
            bold: true,
            fontSize: 24,
          },
        };

        const chart =
            new google.visualization.ColumnChart($('#project-chart')[0]);
        chart.draw(projectTable, options);
      });
}
