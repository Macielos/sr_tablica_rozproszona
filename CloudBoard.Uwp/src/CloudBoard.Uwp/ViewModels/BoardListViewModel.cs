using System;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml.Controls;
using CloudBoard.Uwp.Models;
using CloudBoard.Uwp.Services;
using CloudBoard.Uwp.Views;

namespace CloudBoard.Uwp.ViewModels
{
    public class BoardListViewModel : ViewModelBase
    {
        private ImmutableArray<ImmutableBoardHost> _hosts = ImmutableArray<ImmutableBoardHost>.Empty;
        private bool _isInitialized;

        public BoardListViewModel(Page page)
        {
            ParentPage = page;
            ServerService.SubscribeBoards(UpdateHosts).Start();
        }

        private BoardServerService ServerService { get; } = new BoardServerService();

        public ImmutableArray<ImmutableBoardHost> Hosts
        {
            get { return _hosts; }
            set { Set(ref _hosts, value); }
        }

        public bool IsInitialized
        {
            get { return _isInitialized; }
            private set { Set(ref _isInitialized, value); }
        }

        private Page ParentPage { get; }

        public void HostClicked(object sender, ItemClickEventArgs e)
        {
            var host = (ImmutableBoardHost)e.ClickedItem;
            OpenBoard(host);
        }

        public void OpenBoard(ImmutableBoardHost host, bool isHostedLocally = false)
        {
            ParentPage.Frame.Navigate(typeof(BoardPage), new BoardViewModel.LoadArgs()
            {
                Host = host,
                IsHostedLocally = isHostedLocally
            });
        }

        public async Task<ImmutableBoardHost> CreateBoardAsync(string name)
        {
            var host = await ServerService.CreateBoardAsync(name);
            return host;
        }
        private void UpdateHosts(ImmutableArray<ImmutableBoardHost> hosts)
        {
            IsInitialized = true;
            // check if lists differ
            if (hosts.Length != Hosts.Length || hosts.Except(Hosts).Any())
            {
                Hosts = hosts;
            }
        }
    }
}
