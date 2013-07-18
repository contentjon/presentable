var ui = presentable
var mu = Mustache

var views = function(ps) {
  return _.map(ps, ui.view)
}

ui.presenter("app", {

    gutters: "half-gutters", // the ink gutter class to use

    factory: function(p) {
      var view =
        $(mu.render(
            '<div class="ink-grid"> \
               <div class="column-group {{gutters}}"/> \
             </div>',
            p))
      $('.column-group', view).append( views(p.children) )
      return view
    }
})

ui.presenter("column", { 

    width: 50, // width in percent of this column
    
    factory: function(p) {
      return $('<div class="large-' + p.width + '">').append( views(p.children) )
    }
})

ui.presenter("menu", {

    entries: [],
    
    factory: function(p) {
        return $(mu.render(
          '<div class="ink-navigation"> \
             <ul class="menu horizontal red{{#styles}} {{.}}{{/styles}}"> \
               {{#entries}} \
                 <li><a href="#">{{.}}</a></li> \
               {{/entries}} \
             </ul> \
           </div>', 
            p))
    }
})

var class_setter = function(classes) {
    return function(p, evt) {
        evt.preventDefault()
        $(p.view)
            .removeClass(classes,join(" "))
            .addClass($(evt.target).text())
    }
}

ui.behavior("change-color", class_setter(["red" "green" "blue"]))
ui.behavior("change-style", class_setter(["flat" "rounded"]))

// declarative ui description. this could also be loaded from a back
// end data source

var color_menu = ui.make('menu', {
    entries: ["red", "green", "blue"],
    on: {
        "click": ["change-color"]
    }
})

var border_menu = ui.make('menu', {
    entries: ["rounded", "flat"],
    on: {
        "click": ["change-style"]
    }
})

var app = ui.make('app', {
    children: [
        [ ui.make('column', {
            width: 100, 
            children: [ color_menu, border_menu ]
        })]
    ]})

$(function() {
  $('#content').append(ui.view(app))
})
