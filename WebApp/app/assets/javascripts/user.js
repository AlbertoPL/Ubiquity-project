$(function() {
  var User = Backbone.Model.extend({
    defaults: {
      username: 'Anonymous',
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
    //TODO: ajax login, spinner while deciding
    var login = $(this), userData = {
      username: login.find('.username').val(),
      loggedIn: true
    }, user = new User(userData);
    login.hide();
    window['user'] = user;
    $('ul#user').show().find('.username').text(user.get('username'));
    return false;
  });

  $('ul#user').dropdown();
});