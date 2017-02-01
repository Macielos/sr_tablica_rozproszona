using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Networking.Sockets;
using Windows.Storage.Streams;
using CloudBoard.Uwp.Models;
using UnicodeEncoding = Windows.Storage.Streams.UnicodeEncoding;

namespace CloudBoard.Uwp.Services
{
    public class BoardClientService : IDisposable
    {
        private BoardClientService(MessageWebSocket socket, Uri serverUri)
        {
            Socket = socket;
            ServerUri = serverUri;
            Logger = new Logger(nameof(BoardClientService));
        }

        private Logger Logger { get; }

        private MessageWebSocket Socket { get; }

        public Uri ServerUri { get; }
        
        public static async Task<BoardClientService> CreateServiceAsync(ImmutableBoardHost host)
        {
            var serverHttpUri = new Uri(host.IpAddress);
            var serverUriBuilder = new UriBuilder(serverHttpUri)
            {
                Scheme = "ws"
            };
            var serverUri = serverUriBuilder.Uri;
            var socket = new MessageWebSocket();

            // TODO actual protocol
            socket.Control.SupportedProtocols.Add("echo");
            socket.Control.MessageType = SocketMessageType.Utf8;
            var service = new BoardClientService(socket, serverUri);
            await service.ConnectToHostAsync();
            return service;
        }

        public async Task SendMessageAsync(string message)
        {
            var writer = new DataWriter(Socket.OutputStream);
            writer.WriteString(message);
            await writer.StoreAsync();
        }

        public void Dispose()
        {
            Socket.Dispose();
        }

        private async Task<MessageWebSocket> ConnectToHostAsync()
        {
            Socket.MessageReceived += OnMessageReceived;
            Socket.Closed += OnClosed;
            await Socket.ConnectAsync(ServerUri);
            Logger.Info?.Msg($"Opened connection with '{ServerUri}'");
            return Socket;
        }

        private void OnClosed(IWebSocket sender, WebSocketClosedEventArgs args)
        {
            Logger.Info?.Msg($"Closed connection with '{ServerUri}'");
        }

        private void OnMessageReceived(MessageWebSocket sender, MessageWebSocketMessageReceivedEventArgs args)
        {
            var reader = args.GetDataReader();
            reader.UnicodeEncoding = UnicodeEncoding.Utf8;
            var msg = reader.ReadString(reader.UnconsumedBufferLength);
            Logger.Info?.Msg($"Received message from server '{ServerUri}': {msg}");
        }
    }
}
