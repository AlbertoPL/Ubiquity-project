$(function() {
  var Project = Backbone.Model.extend({
    defaults: {
      name: 'Project0',
      files: null
    },

    initialize: function() {
      var files = this.get('files');
      if(_.isArray(files)) {
        files = new FileCollection(files);
      } else {
        files = new FileCollection;
      }
      this.set({
        files: files
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
