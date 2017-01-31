using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using CloudBoard.Uwp.Models;
using CloudBoard.Uwp.ViewModels;

// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=234238

namespace CloudBoard.Uwp.Views
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class BoardPage : Page
    {
        public BoardPage()
        {
            this.InitializeComponent();
            BoardCanvas.InkPresenter.InputDeviceTypes |= CoreInputDeviceTypes.Mouse | CoreInputDeviceTypes.Touch;
            //BoardCanvas.InkPresenter.StrokeContainer.GetStrokes()[0].
        }

        public BoardViewModel ViewModel { get; } = new BoardViewModel();

        protected override async void OnNavigatedTo(NavigationEventArgs e)
        {
            base.OnNavigatedTo(e);
            switch (e.NavigationMode)
            {
                case NavigationMode.New:
                    await ViewModel.OnLoaded((BoardViewModel.LoadArgs)e.Parameter);
                    break;
                case NavigationMode.Back:
                    break;
                case NavigationMode.Forward:
                    break;
                case NavigationMode.Refresh:
                    break;
                default:
                    break;
            }
        }
    }
}
