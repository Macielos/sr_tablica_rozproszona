using System;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using CloudBoard.Uwp.Models;
using IotWeb.Common.Http;
using Newtonsoft.Json;

namespace CloudBoard.Uwp.Services
{
    public class WebsocketServerRequestHandler : IWebSocketRequestHandler
    {
        private ImmutableArray<WebSocket> _clients = ImmutableArray<WebSocket>.Empty;
        private readonly object _listLock = new object();
        private const string Protocol = "";

        public WebsocketServerRequestHandler()
        {
            Logger = new Logger(nameof(WebsocketServerRequestHandler));
        }

        private Logger Logger { get; }
        
        public bool WillAcceptRequest(string uri, string protocol)
        {
            return true;
        }

        public void Connected(WebSocket socket)
        {
            lock (_listLock)
            {
                _clients = _clients.Add(socket);
            }
            Logger.Info?.Msg($"Opened socket ({_clients.Length} now open)");
            socket.DataReceived += OnDataReceived;
            socket.ConnectionClosed += OnConnectionClosed;
        }

        private void OnConnectionClosed(WebSocket webSocket)
        {
            Logger.Info?.Msg($"Closed socket ({_clients.Length} now open)");
            lock (_listLock)
            {
                _clients = _clients.Remove(webSocket);
            }
        }

        private void OnDataReceived(WebSocket socket, string frame)
        {
            if (string.IsNullOrEmpty(frame))
            {
                return;
            }
            Logger.Debug?.Msg($"Received msg: {frame}");
            // ReSharper disable once InconsistentlySynchronizedField
            // reason: not critical
            foreach (var client in _clients)
            {
                try
                {
                    //echo
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
