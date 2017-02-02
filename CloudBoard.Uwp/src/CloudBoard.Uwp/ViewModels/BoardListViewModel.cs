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
        private string _newBoardName;

        public BoardListViewModel(Page page)
        {
            ParentPage = page;
            BoardListService.SubscribeBoards(UpdateHosts).Start();
        }

        private Page ParentPage { get; }

        private BoardListService BoardListService { get; } = new BoardListService();

        public string NewBoardName
        {
            get { return _newBoardName; }
            set { Set(ref _newBoardName, value); }
        }

        public ImmutableArray<ImmutableBoardHost> Hosts
        {
            get { return _hosts; }
            private set { Set(ref _hosts, value); }
        }

        public bool IsInitialized
        {
            get { return _isInitialized; }
            private set { Set(ref _isInitialized, value); }
        }

        public void HostClicked(object sender, ItemClickEventArgs e)
        {
            var host = (ImmutableBoardHost)e.ClickedItem;
            OpenBoard(host);
        }

        public async void UpdateHostList()
        {
            await BoardListService.UpdateBoardsAsync(UpdateHosts);
        }

        public void OpenBoard(ImmutableBoardHost host)
        {
            ParentPage.Frame.Navigate(typeof(BoardPage), new BoardViewModel.LoadArgs
            {
                Host = host
            });
        }

        public async Task CreateBoardAsync()
        {
            if (string.IsNullOrWhiteSpace(NewBoardName))
            {
                return;
            }
            var host = await BoardListService.CreateBoardAsync(NewBoardName);
            // TODO host locally, navigate etc.
            OpenBoard(host);
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
