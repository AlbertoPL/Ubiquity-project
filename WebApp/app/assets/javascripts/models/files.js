$(function() {
  var File = Backbone.Model.extend({
    defaults: {
      name: '~',
      size: '10MB',
      owner: 'You',
      isDirectory: false,
      children: null,
      projects: []
    },

    initialize: function() {
      var children = this.get('children');
      if(_.isArray(children)) {
        children = new FileCollection(children);
      } else {
        children = new FileCollection;
      }
      this.set({
        children: children
      });
    },
    
    url: function() {
      return '/' + window['currentUser'].get('username') + '/devices/' + this.id + '.json'
    }
  });

  var FileCollection = Backbone.Collection.extend({
    model: File
  });

  window['File'] = File;
  window['FileCollection'] = FileCollection;
})