(function($) {
  var projectTemplate = _.template('<li data-id="<%= id %>"><a href="#"><%= name %></a></li>');
  var fileTemplate = _.template('<tr data-id="<%= id %>">' + 
    '<td class="name span6"><a href="#"><%= name %></a></td>' + 
    '<td class="projects span1"><div class="btn-group pull-right">' + 
      '<a href="#" class="btn dropdown-toggle" data-toggle="dropdown">Options <span class="caret"></span></a>' + 
      '<ul class="dropdown-menu"><li class="remove-from-project"><a href="#">Remove from Project</a></li></ul></div></td></tr>');

  $.widget('ubiquity.projectList', {
    options: {
      projects: null
    },

    _setOption: function(key, value) {
      $.Widget.prototype._setOption.apply(this, arguments);
      //jQuery UI 1.9: this._super( "_setOption", key, value );

      switch(key) {
        case 'projects':
          this.refresh();
          break;
      }
    },

    _create: function() {
      this.projectContainer = this.element.find('.project-list')
        .on('click', 'a', _.bind(this._changeProjectHandler, this));
      this.fileTreeContainer = this.element.find('.project-files');
      this.fileTreeBody = this.fileTreeContainer.find('tbody');
      this._reset();
      this.refresh();
    },

    _changeProjectHandler: function(evt) {
      var targetLi = $(evt.target).closest('li');
      if(targetLi.hasClass('active')) return false;
      this.projectContainer.children('li').removeClass('active');
      var projectId = new Number(
          targetLi.addClass('active').attr('data-id')).valueOf();
      this.selectProject(this.options.projects.find(function(project) {
        return projectId === project.id;
      }));
      return false;
    },

    _reset: function() {
      this.projectElements = {};
      this.activeProject = null;
      this.projectContainer.empty();
      this.fileTreeBody.empty();
    },

    refresh: function() {
      if(this.options.projects === null || this.options.projects.length === 0) {
        this._reset();
      } else {
        this._renderProjects();
      }
    },

    _renderProjects: function() {
      var self = this, newCache = {};
      this.options.projects.each(function(project) {
        var projectElem = self.projectElements[project.id];
        if(projectElem === undefined) {
          projectElem = $(projectTemplate({
            id: project.id,
            name: project.get('name')
          })).appendTo(self.projectContainer);
        } else {
          delete self.projectElements[project.id];
        }
        newCache[project.id] = projectElem;
      });
      _.keys(this.projectElements, function(key) {
        self.projectElements[key].remove();
        delete self.projectElements[key];
      });
      this.projectElements = newCache;
      if(this.activeProject === null) {
        this.selectProject(this.options.projects.first());
      }
    },

    selectProject: function(project) {
      this.projectContainer.children('li').removeClass('active');
      var projectElement = this.projectElements[project.id].addClass('active');
      if(projectElement.size() > 0) {
        this.activeProject = project;
        this._renderTree();
      }
    },

    _stopListening: function() {
      this.fileTreeContainer.off('click', '.name a');
    },

    _renderTree: function() {
      var self = this;
      this._stopListening();
      this.fileTreeContainer.fadeOut('fast', function() {
        self._renderFiles();
        self.fileTreeContainer.on('click', '.name a', _.bind(self._fileOpenHandler, self));
        self.fileTreeContainer.fadeIn('fast');
      });
    },

    _renderFiles: function() {
      var self = this;
      this.fileTreeBody.empty();
      this.activeProject.get('files').each(function(file) {
        var fileEl = $(fileTemplate({
          id: file.id,
          name: file.get('name')
        }));
        fileEl.appendTo(self.fileTreeBody);
      });
    },

    _fileOpenHandler: function(evt) {
      var target = $(evt.target), currentDir = null, selectedFile = null;
      if(target.closest('tr').hasClass('directory')) {
        currentDir = _.last(this.deviceFileCollectionStack[this.activeProject.id]);
        selectedFile = currentDir.get('files').find(function(directory) {
          return directory.get('name') === $.trim(target.text());
        });
        this.deviceFileCollectionStack[this.activeProject.id].push(selectedFile);
        this._renderTree();
      }
      return false;
    }
  });
})(jQuery);
