/*var express = require('express');
var app = express();
var server = require('http').createServer(app);
var io = require('socket.io').listen(server);
users = [];
connections = [];

app.use(express.static(__dirname + '/'));

server.listen(process.env.PORT || 3000);
console.log('Server running......');



app.get('/', function (req, res) {
    res.sendFile(__dirname + '/index.html');
});

io.sockets.on('connection', function(socket){
    connections.push(socket);
    console.log('Connected: %s sockets connected', connections.length);

    socket.on('disconnect', function(data){
        //Disconnect
        connections.splice(connections.indexOf(socket), 1);
        console.log('Disconnected: %s sockets connected', connections.length);
    });
	
    socket.on('message', function(data) {
		console.log(data);
        socket.broadcast.emit('message', data);
    });
});*/
var clients = [ ];
var oldConnection;
var express = require('express');
var app = express();
app.use(express.static(__dirname + '/'));

var WebSocketServer = require('websocket').server;
var http = require('http');

var server = http.createServer(app);

server.listen(process.env.PORT || 3000);
console.log('Server running......');

app.get('/', function (req, res) {
    res.sendFile(__dirname + '/index.html');
});
// create the server
wsServer = new WebSocketServer({
    httpServer: server
});
// WebSocket server
wsServer.on('request', function(request) {
	
    var connection = request.accept(null, request.origin);
	var index = clients.push(connection) - 1;
	console.log(request);
	//clients[""] = connection;
    // This is the most important callback for us, we'll handle
    // all messages from users here.
    connection.on('message', function(data) {
        /*if (message.type === 'utf8') {
            // process WebSocket message
        }*/
		for (var i=0; i < clients.length; i++) {
        	clients[i].send(data.utf8Data);
        }
    });

    connection.on('close', function(connection) {
		clients.splice(index, 1);
    });
});