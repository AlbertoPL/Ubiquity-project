$(function() {
  var File = Backbone.Model.extend({
    defaults: {
      name: 'File0',
      size: '10MB',
      owner: 'You',
      isDirectory: false,
      children: null
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

  var Device = Backbone.Model.extend({
    defaults: {
      name: 'Device0',
      root: '~',
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

  var DeviceCollection = Backbone.Collection.extend({
    model: Device,
    url: function() {
      '/' + window['currentUser'].get('username') + '/devices.json'
    }
  });

  window['DeviceCollection'] = DeviceCollection;
});
