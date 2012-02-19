$(function() {
  var ProjectRouter = Backbone.Router.extend({
    routes: {
      'projects': 'home'
    },

    home: function() {
      $('ul.nav:not(.nav-tabs) li').removeClass('active').filter('.projects').addClass('active');
      var projects = new ProjectCollection([{
        id: 1,
        name: 'Econ 101',
        files: [{name: 'a/b/c'}],
        users: [{
          username: 'geowa4',
          email: 'g.w.adams.iv@gmail.com',
          loggedIn: false
        }, {
          username: 'geowa4',
          email: 'g.w.adams.iv@gmail.com',
          loggedIn: false
        }, {
          username: 'geowa4',
          email: 'g.w.adams.iv@gmail.com',
          loggedIn: false
        }, {
          username: 'geowa4',
          email: 'g.w.adams.iv@gmail.com',
          loggedIn: false
        }, {
          username: 'geowa4',
          email: 'g.w.adams.iv@gmail.com',
          loggedIn: false
        }, {
          username: 'geowa4',
          email: 'g.w.adams.iv@gmail.com',
          loggedIn: false
        }, {
          username: 'geowa4',
          email: 'g.w.adams.iv@gmail.com',
          loggedIn: false
        }, {
          username: 'geowa4',
          email: 'g.w.adams.iv@gmail.com',
          loggedIn: false
        }, {
          username: 'geowa4',
          email: 'g.w.adams.iv@gmail.com',
          loggedIn: false
        }, {
          username: 'geowa4',
          email: 'g.w.adams.iv@gmail.com',
          loggedIn: false
        }, {
          username: 'geowa4',
          email: 'g.w.adams.iv@gmail.com',
          loggedIn: false
        }, {
          username: 'geowa4',
          email: 'g.w.adams.iv@gmail.com',
          loggedIn: false
        }, {
          username: 'geowa4',
          email: 'g.w.adams.iv@gmail.com',
          loggedIn: false
        }, {
          username: 'geowa4',
          email: 'g.w.adams.iv@gmail.com',
          loggedIn: false
        }, {
          username: 'geowa4',
          email: 'g.w.adams.iv@gmail.com',
          loggedIn: false
        }, {
          username: 'geowa4',
          email: 'g.w.adams.iv@gmail.com',
          loggedIn: false
        }, {
          username: 'geowa4',
          email: 'g.w.adams.iv@gmail.com',
          loggedIn: false
        }, {
          username: 'geowa4',
          email: 'g.w.adams.iv@gmail.com',
          loggedIn: false
        }]
      }, {
        id: 2,
        name: 'CS 201',
        files: [{name: 'd/e/f'}],
        users: [{
          username: 'gadams',
          email: 'george.adams@oracle.com',
          loggedIn: true
        }, {
          username: 'gadams',
          email: 'george.adams@oracle.com',
          loggedIn: true
        }, {
          username: 'gadams',
          email: 'george.adams@oracle.com',
          loggedIn: true
        }, {
          username: 'gadams',
          email: 'george.adams@oracle.com',
          loggedIn: true
        }]
      }]);

      $('#projects').projectList({
        projects: projects
      });
    }
  });

  new ProjectRouter;
})