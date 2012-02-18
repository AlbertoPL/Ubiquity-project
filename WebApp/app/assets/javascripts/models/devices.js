$(function() {
  var Device = Backbone.Model.extend({
    defaults: {
      name: 'Device0',
      root: null
    },

    initialize: function() {
      var root = this.get('root');
      if(_.isObject(root)) {
        root = new File(root);
      } else {
        root = new File;
      }
      this.set({
        root: root
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
