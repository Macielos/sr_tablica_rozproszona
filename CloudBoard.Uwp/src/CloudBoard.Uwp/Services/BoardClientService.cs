using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.WebSockets;
using System.Text;
using System.Threading.Tasks;
using Windows.Networking.Sockets;
using Windows.Storage.Streams;
using CloudBoard.Uwp.Models;
using UnicodeEncoding = Windows.Storage.Streams.UnicodeEncoding;

namespace CloudBoard.Uwp.Services
{
    public class BoardClientService
    {
        public static async Task<MessageWebSocket> ConnectToHostAsync(ImmutableBoardHost host)
        {
            var serverHttpUri = new Uri(host.IpAddress);
            var serverUriBuilder = new UriBuilder(serverHttpUri)
            {
                Scheme = "ws"
            };
            var serverUri = serverUriBuilder.Uri;
            var socket = new MessageWebSocket();
            socket.Control.MessageType = SocketMessageType.Utf8;
            socket.MessageReceived += (sender, args) =>
            {
                var reader = args.GetDataReader();
                reader.UnicodeEncoding = UnicodeEncoding.Utf8;
                var msg = reader.ReadString(reader.UnconsumedBufferLength);
                ;
            };
            socket.Closed += (sender, args) =>
            {
                ;
            };
            await socket.ConnectAsync(serverUri);
            await SendMessageAsync(socket, "Hello World!");
            return socket;
        }

        public static async Task SendMessageAsync(MessageWebSocket socket, string message)
        {
            var writer = new DataWriter(socket.OutputStream);
            writer.WriteString(message);
            await writer.StoreAsync();
        }
    }
}
