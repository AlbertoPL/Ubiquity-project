(function($) {
  var deviceTemplate = _.template('<li data-id="<%= id %>"><a href="#"><%= name %></a></li>');
  var fileTemplate = _.template('<tr data-id="<%= id %>"><td><a href="#"><%= name %></a></td><td><%= size %></td><td><%= owner %></td></tr>');
  var breadcrumbTemplate = _.template('<li><a href="#"><%= name %></a> <span class="divider">/</span></li>');

  $.widget('ubiquity.deviceList', {
    options: {
      devices: null
    },

    _setOption: function(key, value) {
      $.Widget.prototype._setOption.apply(this, arguments);
      //jQuery UI 1.9: this._super( "_setOption", key, value );

      switch(key) {
        case 'devices':
          this.refresh();
          break;
      }
    },

    _create: function() {
      this.deviceContainer = this.element.find('.device-list')
        .on('click', 'a', _.bind(this._changeDeviceHandler, this));
      this.breadcrumb = this.element.find('.device-breadcrumb').hide();
      this.fileTreeContainer = this.element.find('.device-files');
      this.fileTreeBody = this.fileTreeContainer.find('tbody');
      this.deviceElements = {};
      this.activeDevice = null;
      this.refresh();
    },

    _changeDeviceHandler: function(evt) {
      var targetLi = $(evt.target).closest('li');
      if(targetLi.hasClass('active')) return false;
      this.deviceContainer.children('li').removeClass('active');
      var deviceId = new Number(
          targetLi.addClass('active').attr('data-id')).valueOf();
      this.selectDevice(this.options.devices.find(function(device) {
        return deviceId === device.id;
      }));
      return false;
    },

    _reset: function() {
      this.deviceElements = {};
      this.activeDevice = null;
      this.deviceContainer.empty();
      this.fileTreeBody.empty();
    },

    refresh: function() {
      if(this.options.devices === null || this.options.devices.length === 0) {
        this._reset();
      } else {
        this._renderDevices();
      }
    },

    _renderDevices: function() {
      var self = this;
      var newCache = {};
      this.options.devices.each(function(device) {
        var deviceElem = self.deviceElements[device.id];
        if(deviceElem === undefined) {
          deviceElem = $(deviceTemplate({
            id: device.id,
            name: device.get('name')
          })).appendTo(self.deviceContainer);
        } else {
          delete self.deviceElements[device.id];
        }
        newCache[device.id] = deviceElem;
      });
      _.keys(this.deviceElements, function(key) {
        self.deviceElements[key].remove();
        delete self.deviceElements[key];
      });
      this.deviceElements = newCache;
      if(this.activeDevice === null) {
        this.selectDevice(this.options.devices.first());
      }
    },

    selectDevice: function(device) {
      this.deviceContainer.children('li').removeClass('active');
      var deviceElement = this.deviceElements[device.id].addClass('active');
      if(deviceElement.size() > 0) {
        this.activeDevice = device;
        this._renderTree();
      }
    },

    _renderTree: function() {
      var self = this;
      this.fileTreeContainer.fadeOut(function() {
        self.breadcrumb.fadeOut(function() {
          $(breadcrumbTemplate({
            name: self.activeDevice.get('root')
          })).addClass('active').appendTo(self.breadcrumb.empty());
          self.fileTreeBody.empty();
          self.activeDevice.get('files').each(function(file) {
            $(fileTemplate({
              id: file.id,
              name: file.get('name'),
              size: file.get('size'),
              owner: file.get('owner')
            })).appendTo(self.fileTreeBody);
          });
          self.breadcrumb.fadeIn('slow', function() {
            self.fileTreeContainer.fadeIn('slow');
          });
        });
      });
    }
  });
})(jQuery);
