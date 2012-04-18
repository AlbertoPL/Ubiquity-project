$(function() {
  function displayLoggedInUser(userJson) {
    $('form#login').hide();
    window['currentUser'] = new User(userJson);
    $('ul#user').find('.username').text(window['currentUser'].get('name')).end()
      .add('.login-required').css('display', 'block');
  }

  var User = Backbone.Model.extend({
    defaults: {
      name: 'Anonymous',
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

  if(!_.isUndefined(window['bootstrap']) && 
      !_.isUndefined(window['bootstrap']['currentUser']) && 
      window['bootstrap']['currentUser'] !== null) {

    displayLoggedInUser(window['bootstrap']['currentUser']);
  } else {
    window['currentUser'] = new User;

    $('form#login').on('submit', function(evt) {
      var login = $(this), userData = {
        'username': login.find('.username').val(),
        'password': login.find('.password').val()
      };

      login.find(':input').attr('disabled', 'disabled');

      $.ajax({
        url: '/login', 
        type:'POST', 
        dataType: 'json',
        data: {
          email: userData['username'], 
          password: userData['password']
        }, 
        error: function() {
          //TODO 
          console.log(arguments);
          login.find(':input').removeAttr('disabled');
        }, 
        success: displayLoggedInUser
      });
      
      return false;
    });
  }

  $('ul#user a.dropdown-toggle').dropdown();
});