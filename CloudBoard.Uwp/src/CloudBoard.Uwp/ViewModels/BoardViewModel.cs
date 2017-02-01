using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using CloudBoard.Uwp.Models;
using CloudBoard.Uwp.Services;

namespace CloudBoard.Uwp.ViewModels
{
    public class BoardViewModel : ViewModelBase
    {
        private ImmutableBoardHost _boardHost;
        private bool _isHostedLocally;

        public BoardViewModel()
        {
            PropertyChanged += OnPropertyChanged;
        }

        private void OnPropertyChanged(object sender, PropertyChangedEventArgs e)
        {
            switch (e.PropertyName)
            {
                case nameof(IsHostedLocally):
                {
                    StartServerListener();
                }
                    break;
                default:
                    break;
            }
        }

        private void StartServerListener()
        {
            // TODO
        }

        public ImmutableBoardHost BoardHost
        {
            get { return _boardHost; }
            set { Set(ref _boardHost, value); }
        }

        public bool IsHostedLocally
        {
            get { return _isHostedLocally; }
            set { Set(ref _isHostedLocally, value); }
        }

        public async Task OnLoaded(LoadArgs args)
        {
            BoardHost = args.Host;
            IsHostedLocally = args.IsHostedLocally;
            await ConnectToHost();
        }

        public async Task ConnectToHost()
        {
            using (var service = await BoardClientService.CreateServiceAsync(BoardHost))
            {
                await service.SendMessageAsync("Hello world!");
            }
        }

        public class LoadArgs
        {
            public ImmutableBoardHost Host { get; set; }

            public bool IsHostedLocally { get; set; }
        }
    }
}
