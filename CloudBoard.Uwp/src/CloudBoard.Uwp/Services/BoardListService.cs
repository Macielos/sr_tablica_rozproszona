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
            Logger = new Logger(nameof(BoardListService));
        }

        private Logger Logger { get; }

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
            Logger.Debug?.Msg("Sending keep-alive");
            var currentUri = LocalWebsocketServerProvider.GetLocalServerUri();
            var currentHost = host.ToMutable();
            currentHost.IpAddress = currentUri.ToString();
            try
            {
                await Api.UpdateAsync(currentHost);
            }
            catch (Exception e)
            {
                Logger.Error?.Ex(e, "Failed to send keep-alive");
            }
        }

        public async Task<ImmutableBoardHost> CreateBoardAsync(string name)
        {
            var uri = LocalWebsocketServerProvider.GetLocalServerUri();
            try
            {
                var host = await Api.CreateAsync(new BoardHostCreate
                {
                    BoardName = name,
                    IpAddress = uri.ToString()
                });
                return host.ToImmutable();
            }
            catch (Exception e)
            {
                Logger.Error?.Ex(e, "Failed to create board.");
                throw;
            }
        }
    }
}
