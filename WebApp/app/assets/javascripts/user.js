$(function() {
  var User = Backbone.Model.extend({
    defaults: {
      username: 'Anonymous',
      email: 'anon@ymo.us',
      loggedIn: false,
      joined: new Date
    },

    initialize: function() {
      if(!_.isDate(this.get('joined'))) {
        this.set({
          joined: new Date(this.get('joined'))
        });
      }
    }
  });

  window['user'] = new User;

  $('form#login').on('submit', function(evt) {
    var login = $(this), userData = {
      username: login.find('.username').val(),
      loggedIn: true
    }, user = new User(userData);

    login.find(':input').attr('disabled', 'disabled');

    //TODO: use ajax; this would be the ajax success block
    login.hide();
    window['currentUser'] = user;
    $('ul#user').find('.username').text(user.get('username')).end().show();
    return false;
  });

  window['currentUser'] = new User;

  $('ul#user').dropdown();
});