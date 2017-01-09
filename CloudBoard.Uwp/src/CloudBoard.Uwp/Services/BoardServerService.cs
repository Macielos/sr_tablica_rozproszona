using System;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Networking;
using Windows.UI.Xaml;
using CloudBoard.Uwp.Models;
using Refit;
using Windows.Networking.Connectivity;

namespace CloudBoard.Uwp.Services
{
    public class BoardServerService
    {
        public BoardServerService()
        {
            Api = RestService.For<IBoardHostApi>("http://srcloudboardserver.azurewebsites.net/api");
        }

        private IBoardHostApi Api { get; }

        public DispatcherTimer SubscribeBoards(Action<ImmutableArray<ImmutableBoardHost>> callback)
        {
            var timer = new DispatcherTimer();
            timer.Tick += async (sender, o) =>
            {
                var t = (DispatcherTimer) sender;
                t.Interval = TimeSpan.FromSeconds(5);
                await UpdateBoardsAsync(callback);
            };
            return timer;
        }

        public async Task UpdateBoardsAsync(Action<ImmutableArray<ImmutableBoardHost>> callback)
        {
            var boards = await Api.GetBoardsAsync();
            callback(boards.Select(x => x.ToImmutable()).ToImmutableArray());
        }

        public async Task<ImmutableBoardHost> CreateBoardAsync(string name)
        {
            var hostNames = NetworkInformation.GetHostNames();
            var hostName = hostNames.FirstOrDefault(x => x.Type == HostNameType.Ipv4);
            var host = await Api.CreateAsync(new BoardHostCreate
            {
                BoardName = name,
                IpAddress = $"http://{hostName}:{App.ServerPort}/"
            });
            return host.ToImmutable();
        }
    }
}
