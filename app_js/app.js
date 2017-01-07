var express = require('express');
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

    socket.on('drawClick', function(data) {
        socket.broadcast.emit('draw', {
            x: data.x,
            y: data.y,
            type: data.type
        });
        console.log(data);
    });
});