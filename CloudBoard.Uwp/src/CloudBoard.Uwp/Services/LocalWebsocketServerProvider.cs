using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Networking;
using Windows.Networking.Connectivity;
using IotWeb.Server;

namespace CloudBoard.Uwp.Services
{
    public static class LocalWebsocketServerProvider
    {
        public const int ServerPort = 8234;

        public const string ServerProtocol = "drawClick";

        public static HttpServer CreateServer()
        {
            var server = new HttpServer(ServerPort);
            server.AddWebSocketRequestHandler("/", new WebsocketServerRequestHandler());
            return server;
        }

        public static Uri GetLocalServerUri()
        {
            var hostNames = NetworkInformation.GetHostNames();
            var hostName = hostNames.FirstOrDefault(x => x.Type == HostNameType.Ipv4);
            var uri = new Uri($"http://{hostName}:{ServerPort}/");
            return uri;
        }
    }
}
