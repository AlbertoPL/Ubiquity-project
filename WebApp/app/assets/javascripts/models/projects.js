$(function() {
  var Project = Backbone.Model.extend({
    defaults: {
      name: 'Project0',
      files: null,
      users: null
    },

    initialize: function() {
      var files = this.get('files'), users = this.get('users');
      if(_.isArray(files)) {
        files = new FileCollection(files);
      } else {
        files = new FileCollection;
      }
      if(_.isArray(users)) {
        users = new FileCollection(users);
      } else {
        users = new FileCollection;
      }
      this.set({
        files: files,
        users: users
      });
    }
  });

  var ProjectCollection = Backbone.Collection.extend({
    model: Project,
    url: function() {
      '/' + window['currentUser'].get('username') + '/projects.json'
    }
  });

  window['ProjectCollection'] = ProjectCollection;
});
