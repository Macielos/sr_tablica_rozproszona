using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using IotWeb.Common.Http;

namespace CloudBoard.Uwp.Services
{
    public class WebsocketHandler : IWebSocketRequestHandler
    {
        public List<WebSocket> Clients { get; } = new List<WebSocket>();

        public bool WillAcceptRequest(string uri, string protocol)
        {
            return true;
        }

        public void Connected(WebSocket socket)
        {
            Clients.Add(socket);
            socket.DataReceived += SocketOnDataReceived;
            socket.ConnectionClosed += webSocket =>
            {
                Clients.Remove(webSocket);
            };
        }

        private void SocketOnDataReceived(WebSocket socket, string frame)
        {
            if (string.IsNullOrEmpty(frame))
            {
                return;
            }
            // TODO deserialize, check protocol
            foreach (var client in Clients)
            {
                //echo
                client.Send(frame);
            }
        }
    }
}
