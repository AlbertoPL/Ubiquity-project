$(function() {
  var DeviceRouter = Backbone.Router.extend({
    routes: {
      'devices': 'home'
    },

    home: function() {
      var devices = new window.DeviceCollection([{
        id: 1,
        name: 'Home Desktop',
        files: [{id: 1, name: 'a'}, {id: 2, name: 'b'}, {id: 3, name: 'c'}]
      }, {
        id: 2,
        name: 'Work Laptop',
        files: [{id: 4, name: 'd'}, {id: 5, name: 'e'}, {id: 6, name: 'f'}]
      }]);

      $('#devices').deviceList({
        devices: devices
      });
    }
  });

  new DeviceRouter;
})