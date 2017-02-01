using System;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using CloudBoard.Uwp.Models;
using Refit;

namespace CloudBoard.Uwp.Services
{
    /// <summary>
    /// Wraps operations performed on Board list server (room server).
    /// </summary>
    public class BoardListService
    {
        public const string BoardsApiUri = "http://srcloudboardserver.azurewebsites.net/api";

        public BoardListService()
        {
            Api = RestService.For<IBoardHostApi>(BoardsApiUri);
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

        public async Task UpdateHostAsync(ImmutableBoardHost host)
        {
            var currentUri = LocalWebsocketServerProvider.GetLocalServerUri();
            var currentHost = host.ToMutable();
            currentHost.IpAddress = currentUri.ToString();
            await Api.UpdateAsync(currentHost);
        }

        public async Task<ImmutableBoardHost> CreateBoardAsync(string name)
        {
            var uri = LocalWebsocketServerProvider.GetLocalServerUri();
            var host = await Api.CreateAsync(new BoardHostCreate
            {
                BoardName = name,
                IpAddress = uri.ToString()
            });
            return host.ToImmutable();
        }
    }
}
