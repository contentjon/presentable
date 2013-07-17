var ui = presentable

var children_in = function(p, el) {
  return _.reduce(
    p.children,
    function(s, child) {
      return s.append(ui.view(child))
    },
    $(el))
}

ui.presenter(
  "grid",
  { factory: function(p) {
      return children_in(p, '<div class="ink-grid">')
  }})

ui.presenter(
  "group",
  { factory: function(p) {
      return children_in(p, '<div class="column-group gutters">')
  }})

ui.presenter(
  "column",
  { width:   50,
    factory: function(p) {
      return children_in(p, '<div class="large-' + p.width + '">')
  }})

var node = function(n) {
  return function() {
    return ui.make('grid', {children:arguments})
  }
}

var grid  = node('grid')
var group = node('group')

var app = grid( group( ui.make('column'), ui.make('column') ) )

$(function() {
  $('#content').append(ui.view(app))
})
