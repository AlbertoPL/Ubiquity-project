$(function() {
  var User = Backbone.Model.extend({
    defaults: {
      username: 'Anonymous',
      email: 'anon@ymo.us',
      loggedIn: false,
      joined: new Date,
      pic: 'http://www.gravatar.com/avatar/443f71885827eac7afa2a87bb70be610'
    },

    initialize: function() {
      if(!_.isDate(this.get('joined'))) {
        this.set({
          joined: new Date(this.get('joined'))
        });
      }
    }
  });

  var UserCollection = Backbone.Collection.extend({
    model: User
  });

  window['User'] = User;
  window['UserCollection'] = UserCollection;
  window['currentUser'] = new User;

  $('form#login').on('submit', function(evt) {
    var login = $(this), userData = {
      username: login.find('.username').val(),
      loggedIn: true
    }, user = new User(userData);

    login.find(':input').attr('disabled', 'disabled');

    //TODO: use ajax; this would be the ajax success block
    login.hide();
    window['currentUser'] = user;
    $('ul#user').find('.username').text(user.get('username')).end()
      .add('.login-required').css('display', 'block');
    return false;
  });

  $('ul#user a.dropdown-toggle').dropdown();
});