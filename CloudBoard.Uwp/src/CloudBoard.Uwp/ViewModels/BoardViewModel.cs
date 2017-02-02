using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Linq.Expressions;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml.Controls;
using CloudBoard.Uwp.Models;
using CloudBoard.Uwp.Services;
using CloudBoard.Uwp.Views;
using Microsoft.Toolkit.Uwp;

namespace CloudBoard.Uwp.ViewModels
{
    public class BoardViewModel : ViewModelBase, IDisposable
    {
        private ImmutableBoardHost _boardHost;
        private bool _isHostedLocally;
        private bool _isConnecting = true;
        private bool _isConnectionError;

        public BoardViewModel(Page parentPage)
        {
            ParentPage = parentPage;
            Logger = new Logger(nameof(BoardViewModel));
            BoardListService = new BoardListService();
        }

        private BoardListService BoardListService { get; }

        public event EventHandler<DrawMessage> DrawMessageReceived;

        private Page ParentPage { get; }

        private BoardClientService ClientService { get; set; }

        private Logger Logger { get; }

        public bool IsConnecting
        {
            get { return _isConnecting; }
            private set { Set(ref _isConnecting, value); }
        }

        public bool IsConnectionError
        {
            get { return _isConnectionError; }
            private set { Set(ref _isConnectionError, value); }
        }
        
        public ImmutableBoardHost BoardHost
        {
            get { return _boardHost; }
            private set { Set(ref _boardHost, value); }
        }

        public bool IsHostedLocally
        {
            get { return _isHostedLocally; }
            private set { Set(ref _isHostedLocally, value); }
        }

        public async Task OnLoaded(LoadArgs args)
        {
            BoardHost = args.Host;
            await ConnectToHost();
        }

        public async void SendDraw(DrawMessage message)
        {
            if (IsConnecting || IsConnectionError || ClientService == null)
            {
                return;
            }
            try
            {
                await ClientService.SendMessageAsync(message);
            }
            catch (Exception e)
            {
                Logger.Error?.Ex(e, "Error sending draw message.");
            }
        }

        private async Task ConnectToHost()
        {
            IsConnecting = true;
            IsConnectionError = false;
            try
            {
                var hostUri = new Uri(BoardHost.IpAddress);
                if (LocalWebsocketServerProvider.GetLocalServerUri() == hostUri)
                {
                    IsHostedLocally = true;
                    App.Instance.KeepAliveAsyncAction = KeepAliveAsync;
                    App.Instance.StartServer();
                }
                ClientService = await BoardClientService.CreateServiceAsync(BoardHost);
            }
            catch (Exception e)
            {
                Logger.Error?.Ex(e, "failed to connect");
                IsConnectionError = true;
                return;
            }
            finally
            {
                IsConnecting = false;
            }
            var listener = new WeakEventListener<BoardClientService, object, DrawMessage>(ClientService)
            {
                OnEventAction = OnDrawMessage
            };
            ClientService.DrawMessageReceived += listener.OnEvent;
        }

        private async Task KeepAliveAsync()
        {
            await BoardListService.UpdateHostAsync(BoardHost);
        }

        private void OnDrawMessage(BoardClientService boardClientService, object o, DrawMessage e)
        {
            DrawMessageReceived?.Invoke(this, e);
        }

        public void Dispose()
        {
            ClientService?.Dispose();
            App.Instance?.StopServer();
        }

        public class LoadArgs
        {
            public ImmutableBoardHost Host { get; set; }
        }
    }
}
