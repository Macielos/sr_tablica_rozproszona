using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using IotWeb.Common.Http;

namespace CloudBoard.Uwp.Services
{
    public class WebsocketServerRequestHandler : IWebSocketRequestHandler
    {
        private const string Protocol = "echo";

        public WebsocketServerRequestHandler()
        {
            Logger = new Logger(nameof(WebsocketServerRequestHandler));
        }

        private Logger Logger { get; }

        public List<WebSocket> Clients { get; } = new List<WebSocket>();

        public bool WillAcceptRequest(string uri, string protocol)
        {
            return protocol == Protocol;
        }

        public void Connected(WebSocket socket)
        {
            Clients.Add(socket);
            socket.DataReceived += SocketOnDataReceived;
            socket.ConnectionClosed += webSocket =>
            {
                Logger.Info?.Msg("Closed socket");
                Clients.Remove(webSocket);
            };
        }

        private void SocketOnDataReceived(WebSocket socket, string frame)
        {
            if (string.IsNullOrEmpty(frame))
            {
                return;
            }
            Logger.Info?.Msg($"Received msg: {frame}");
            // TODO deserialize, check protocol
            foreach (var client in Clients)
            {
                //echo
                try
                {
                    client.Send(frame);
                }
                catch (Exception e)
                {
                    Logger.Debug?.Ex(e, "Error sending to client.");
                }
            }
        }
    }
}
