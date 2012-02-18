$(function() {
  var DeviceRouter = Backbone.Router.extend({
    routes: {
      'devices': 'home'
    },

    home: function() {
      $('ul.nav li').removeClass('active').filter('.devices').addClass('active');
      var devices = new window.DeviceCollection([{
        id: 1,
        name: 'Home Desktop',
        root: {id: 100, isDirectory:true, children: [{id: 1, name: 'a', projects: ['foo', 'bar']}, {id: 2, name: 'b'}, {id: 3, name: 'c'}]}
      }, {
        id: 2,
        name: 'Work Laptop',
        root: {
          id: 101, 
          isDirectory: true, 
          children: [
            {id: 4, name: 'd', projects: ['foo', 'bar']}, 
            {id: 5, name: 'e'}, 
            {id: 6, name: 'f', isDirectory: true, children: [{id: 7, name: 'g', isDirectory: true}, {id: 8, name: 'h'}]}
          ]
        }
      }]);

      $('#devices').deviceList({
        devices: devices
      });
    }
  });

  new DeviceRouter;
})