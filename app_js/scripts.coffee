# setup our application with its own namespace
App = {}
dragging = false
###
	Init
###
App.init = ->
  App.canvas = document.createElement 'canvas' #create the canvas element
  App.canvas.height = 400
  App.canvas.width = $('.canvas-container').width()  #size it up
  document.querySelector('div.canvas-container').appendChild(App.canvas) #append it into the DOM

  App.ctx = App.canvas.getContext("2d") # Store the context
  # set some preferences for our line drawing.
  App.ctx.fillStyle = "solid"
  App.ctx.strokeStyle = "#bada55"
  App.ctx.lineWidth = 5
  App.ctx.lineCap = "round"

  # Clear function
  App.clear = () ->
    App.ctx.clearRect(0, 0, App.canvas.width, App.canvas.height)

  # Draw Function
  App.draw = (x,y,type) ->
    if type is "dragstart"
      App.ctx.beginPath()
      App.ctx.moveTo(x,y)
    else if type is "drag"
      App.ctx.lineTo(x,y)
      App.ctx.stroke()
    else
      App.ctx.closePath()
  return



# Sockets!
App.socket = io.connect('http://sr-94933.onmodulus.net:3000')

App.socket.on 'draw', (data) ->
  App.draw(data.x,data.y,data.type)

App.init();

$('canvas').on 'mousemove', (e) ->
  if(dragging)
    offset = $(this).offset()
    e.offsetX = e.pageX - offset.left
    e.offsetY = e.pageY - offset.top
    x = e.offsetX
    y = e.offsetY
    App.draw(x,y,'drag')
    App.socket.emit('drawClick', { x : x, y : y, type : 'drag'})
  return

$('canvas').on 'mousedown', (e) ->
  offset = $(this).offset()
  e.offsetX = e.pageX - offset.left
  e.offsetY = e.pageY - offset.top
  x = e.offsetX
  y = e.offsetY
  App.draw(x,y,'dragstart')
  App.socket.emit('drawClick', { x : x, y : y, type : 'dragstart'})
  dragging = true

$('canvas').on 'mouseup', (e) ->
  offset = $(this).offset()
  e.offsetX = e.pageX - offset.left
  e.offsetY = e.pageY - offset.top
  x = e.offsetX
  y = e.offsetY
  App.draw(x,y,'dragend')
  App.socket.emit('drawClick', { x : x, y : y, type : 'dragend'})
  dragging = false

$('#clear').on 'click', (e) ->
  App.clear()