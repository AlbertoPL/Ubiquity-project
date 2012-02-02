$(function() {
  var File = Backbone.Model.extend({
    defaults: {
      name: 'File0',
      isDirectory: false,
      contents: null
    },

    initialize: function() {
      var contents = this.get('contents');
      if(_.isArray(contents)) {
        contents = new FileCollection(contents);
      } else {
        contents = new FileCollection;
      }
      this.set({
        contents: contents
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
