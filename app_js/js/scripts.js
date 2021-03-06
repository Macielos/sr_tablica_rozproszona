// Generated by CoffeeScript 1.12.2
(function() {
  $(document).ready(function () {

	window.WebSocket = window.WebSocket || window.MozWebSocket;

    //var connection = new WebSocket('ws://sr-94933.onmodulus.net');
	var connection = new WebSocket('ws://localhost:3000');
	
  var App, dragging;
  var apiData;
  
  App = {};
  oldCordX = null;
  oldCordY = null;
  dragging = false;
    
  var url = "http://srcloudboardserver.azurewebsites.net/api/Boards";
    
  $.get(url,function(data){
    apiData = data;
  }).done(function(data) {
		$.each(data,function( index, value) {
		
			$('#boardsList').append('<a href="#" class="list-group-item" data-ip="'+value.ipAddress+'">'+value.board.name+'</a>');
		
		});
	  
	  
	  $('#makeBoard').click(function(e){
		e.preventDefault();
		$.ajax({
			  type: "POST",
			  url: url,
			  // The key needs to match your method's input parameter (case-sensitive).
			  data: JSON.stringify({"ipAddress": $('#boardIP').val(), "boardName": $('#boardName').val()}),
			  contentType: "application/json",
			  dataType: "json",
			  success: function(data){
				location.reload();  
				},
			  failure: function(errMsg) {
				alert(errMsg);
			  }
		});
	  });
	  App.init = function() {
		App.canvas = document.createElement('canvas');
		App.canvas.height = 400;
		//App.canvas.width = 600;
		App.canvas.width = $('.canvas-container').width();
		document.querySelector('div.canvas-container').appendChild(App.canvas);
		App.ctx = App.canvas.getContext("2d");
		App.ctx.fillStyle = "solid";
		App.ctx.strokeStyle = "#bada55";
		App.ctx.lineWidth = 5;
		App.ctx.lineCap = "round";
		App.clear = function() {
		  return App.ctx.clearRect(0, 0, App.canvas.width, App.canvas.height);
		};
		App.draw = function(oldX, oldY, x, y, type) {
		  if (type === "dragstart") {
			//App.ctx.beginPath();
			//return App.ctx.moveTo(x, y);
		  } else if (type === "drag") {
			App.ctx.beginPath();
			if(oldCordX == null)
				App.ctx.moveTo(x, y);
			else
				App.ctx.moveTo(oldX, oldY);
				
			App.ctx.lineTo(x, y);
			//oldCordX = x;
			//oldCordY = y;
			App.ctx.stroke();
			App.ctx.closePath();
		  } else {
			//return App.ctx.closePath();
		  }
		};
	  };
		//console.log(apiData);
	  //App.socket = io.connect(apiData[0].ipAddress);
	  //App.socket = io.connect('http://localhost:3000');
	  //App.socket = io.connect("http://sr-94933.onmodulus.net");
	  
	  connection.onmessage = function (data) {
        // try to decode json (I assume that each message from server is json)
        try {
			console.log(data.data);
            var newData = JSON.parse(data.data);
			return App.draw(newData.oldX, newData.oldY, newData.x, newData.y, newData.type);
        } catch (e) {
            
        }
        // handle incoming message
	};
	
	  App.init();
	
	  $('canvas').on('mousemove', function(e) {
		var offset, x, y;
		if (dragging) {
		  offset = $(this).offset();
		  e.offsetX = e.pageX - offset.left;
		  e.offsetY = e.pageY - offset.top;
		  x = e.offsetX;
		  y = e.offsetY;
		  App.draw(oldCordX, oldCordY, x, y, 'drag');
		  var newJSON = {
			x: x,
			y: y,
			oldX: oldCordX,
			oldY: oldCordY,
			type: 'drag'
		  }
		  connection.send(JSON.stringify(newJSON));
		  oldCordX = x;
		  oldCordY = y;
		}
	  });
	
	  $('canvas').on('mousedown', function(e) {
		var offset, x, y;
		offset = $(this).offset();
		e.offsetX = e.pageX - offset.left;
		e.offsetY = e.pageY - offset.top;
		x = e.offsetX;
		y = e.offsetY;
		//App.draw(x, y, 'dragstart');
		//App.socket.emit('drawClick', {
		 // x: x,
		 // y: y,
		 // oldX: oldCordX,
		 // oldY: oldCordY,
		 // type: 'dragstart'
		//});
		oldCordX = x;
		oldCordY = y;
		return dragging = true;
	  });
		
	  $('canvas').on('mouseup', function(e) {
		var offset, x, y;
		offset = $(this).offset();
		e.offsetX = e.pageX - offset.left;
		e.offsetY = e.pageY - offset.top;
		x = e.offsetX;
		y = e.offsetY;
		//App.draw(x, y, 'dragend');
		//App.socket.emit('drawClick', {
		//  x: x,
		//  y: y,
		//  oldX: oldCordX,
		 // oldY: oldCordY,
		//  type: 'dragend'
		//});
		return dragging = false;
	  });
	
	  $('#clear').on('click', function(e) {
		return App.clear();
	  });
	  });
   

    
  });
}).call(this);

//# sourceMappingURL=scripts.js.map
