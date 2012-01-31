$(function() {
  var deviceList = $('#device-list');
  deviceList.on('click', 'a', function() {
    deviceList.children('li').removeClass('active');
    $(this).closest('li').addClass('active');
    return false;
  });
});