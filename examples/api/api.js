var ui = presentable
var mu = Mustache

var views = function(ps) {
  return _.map(ps, ui.view)
}

ui.presenter("column", { 

    width: 50, // width in percent of this column
    
    factory: function(p) {
      return $('<div class="large-' + p.width + '">').append( views(p.children) )
    }
})

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

ui.presenter("menu", {

    entries:  [],
    styles:   [],
    
    factory: function(p) {
        return $(mu.render(
          '<div class="ink-navigation"> \
             <ul class="menu red{{#styles}} {{.}}{{/styles}}"> \
               {{#entries}} \
                 <li><a href="#">{{.}}</a></li> \
               {{/entries}} \
             </ul> \
           </div>', p))
    }
})

ui.behavior("change-color",
  function(p, evt) {
      $(p.view)
          .removeClass("red blue green")
          .addClass($(evt.target).text())
  })

// declaratively define a menu that switches colors when items
// get clicked. this description could come from anywhere (loaded from
// a server f.e.)

var menu = ui.make('menu', {
    entries: ["red", "green", "blue"], 
    styles:  ["horizontal"]
    on: {
        "click": ["change-color"]
    }
})

var app = ui.make('app', {
    children: [
        [ ui.make('column', {
            width: 100, 
            children: [ menu ]
        })]
    ]})

$(function() {
  $('#content').append(ui.view(app))
})
