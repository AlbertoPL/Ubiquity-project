(function($) {
  var deviceTemplate = _.template('<li data-id="<%= id %>"><a href="#"><%= name %></a></li>');
  var fileTemplate = _.template('<tr data-id="<%= id %>"><td class="name"><a href="#"><%= name %></a></td><td class="size"><%= size %></td><td class="owner"><%= owner %></td></tr>');
  var breadcrumbTemplate = _.template('<li><a href="#"><%= name %></a><span class="divider">/</span></li>');

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
      this._reset();
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
      this.deviceFileCollectionStack = {};
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
      var self = this, newCache = {}, newDeviceFileCollectionStack = {};
      this.options.devices.each(function(device) {
        var deviceElem = self.deviceElements[device.id];
        if(deviceElem === undefined) {
          deviceElem = $(deviceTemplate({
            id: device.id,
            name: device.get('name')
          })).appendTo(self.deviceContainer);
        } else {
          delete self.deviceElements[device.id];
          delete self.deviceFileCollectionStack[device.id];
        }
        newCache[device.id] = deviceElem;
        newDeviceFileCollectionStack[device.id] = [device.get('root')];
      });
      _.keys(this.deviceElements, function(key) {
        self.deviceElements[key].remove();
        delete self.deviceElements[key];
        delete self.deviceFileCollectionStack[device.id];
      });
      this.deviceElements = newCache;
      this.deviceFileCollectionStack = newDeviceFileCollectionStack;
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

    _stopListening: function() {
      this.breadcrumb.off('click', 'a:not(.active)');
      this.fileTreeContainer.off('click', 'a');
    },

    _renderTree: function() {
      var self = this;
      this._stopListening();
      this.fileTreeContainer.fadeOut('fast', function() {
        self.breadcrumb.fadeOut('fast', function() {
          self._renderBreadcrumb();
          self._renderFiles();
          self.breadcrumb.fadeIn('fast', function() {
            self.fileTreeContainer.fadeIn('fast');
          });
          self.fileTreeContainer.on('click', 'a', _.bind(self._fileOpenHandler, self));
        });
      });
    },

    _renderBreadcrumb: function() {
      var self = this;
      this.breadcrumb.empty();
      _.each(this.deviceFileCollectionStack[this.activeDevice.id], function(directory) {
        $(breadcrumbTemplate({name: directory.get('name')})).appendTo(self.breadcrumb);
      });
      this.breadcrumb.find('li:last').addClass('active');
      this.breadcrumb.on('click', 'a:not(.active)', _.bind(this._goUpDirectory, this));
    },

    _renderFiles: function() {
      var self = this;
      this.fileTreeBody.empty();
      _.last(this.deviceFileCollectionStack[this.activeDevice.id]).get('children').each(function(file) {
        var fileEl = $(fileTemplate({
          id: file.id,
          name: file.get('name'),
          size: file.get('size'),
          owner: file.get('owner')
        }))
        if(file.get('isDirectory')) {
          fileEl.addClass('directory');
        }
        fileEl.appendTo(self.fileTreeBody);
      });
    },

    _fileOpenHandler: function(evt) {
      var target = $(evt.target), currentDir = null, selectedFile = null;
      if(target.closest('tr').hasClass('directory')) {
        currentDir = _.last(this.deviceFileCollectionStack[this.activeDevice.id]);
        selectedFile = currentDir.get('children').find(function(directory) {
          return directory.get('name') === $.trim(target.text());
        });
        this.deviceFileCollectionStack[this.activeDevice.id].push(selectedFile);
        this._renderTree();
      }
      return false;
    },

    _goUpDirectory: function(evt) {
      var targetLink = $(evt.target);
      console.log(targetLink.text());
      return false;
    }
  });
})(jQuery);
