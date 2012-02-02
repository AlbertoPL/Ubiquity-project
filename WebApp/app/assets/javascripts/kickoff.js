$(function() {
  if(Backbone.history !== undefined) {
    Backbone.history.start({pushState: true});
  }
});