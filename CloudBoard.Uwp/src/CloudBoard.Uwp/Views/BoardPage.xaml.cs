using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using System.Threading.Tasks;
using Windows.Devices.Input;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI;
using Windows.UI.Core;
using Windows.UI.Input;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using Windows.UI.Xaml.Shapes;
using CloudBoard.Uwp.Models;
using CloudBoard.Uwp.Services;
using CloudBoard.Uwp.ViewModels;
using Microsoft.Toolkit.Uwp;

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
            Logger = new Logger(nameof(BoardPage));
            this.InitializeComponent();
            //BoardCanvas.InkPresenter.InputDeviceTypes |= CoreInputDeviceTypes.Mouse | CoreInputDeviceTypes.Touch;
            //BoardCanvas.InkPresenter.StrokeContainer.GetStrokes()[0].
            ProgressRing.RegisterPropertyChangedCallback(ProgressRing.IsActiveProperty, (sender, dp) =>
            {
                if (ProgressRing.IsActive)
                {
                    var noawait = ConnectingDialog.ShowAsync();
                }
                else
                {
                    ConnectingDialog.Hide();
                }
            });
            ScrollViewer.SizeChanged += ScrollViewer_SizeChanged;
            IsTouch = new TouchCapabilities().TouchPresent != 0;
            EditToggleButton.IsChecked = true;
        }

        private bool IsTouch { get; }

        private Logger Logger { get; }

        private void ScrollViewer_SizeChanged(object sender, SizeChangedEventArgs e)
        {
            var zoomFactor = (float) Math.Min(e.NewSize.Height / 400, e.NewSize.Width / 600);
            ScrollViewer.ChangeView(null, null, zoomFactor);
        }

        private Point LastPoint { get; set; }

        private bool IsDrawing { get; set; }
        
        public static readonly DependencyProperty ViewModelProperty = DependencyProperty.Register(
            "ViewModel", typeof(BoardViewModel), typeof(BoardPage), new PropertyMetadata(default(BoardViewModel)));

        public BoardViewModel ViewModel
        {
            get { return (BoardViewModel) GetValue(ViewModelProperty); }
            set { SetValue(ViewModelProperty, value); }
        }
        
        protected override async void OnNavigatedTo(NavigationEventArgs e)
        {
            base.OnNavigatedTo(e);
            switch (e.NavigationMode)
            {
                case NavigationMode.New:
                    ViewModel = new BoardViewModel(this);
                    SubscribeToViewModel();
                    await Task.Delay(TimeSpan.FromMilliseconds(100));
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

        protected override void OnNavigatedFrom(NavigationEventArgs e)
        {
            base.OnNavigatedFrom(e);
            ViewModel?.Dispose();
        }

        private void ToggleButton_OnChecked(object sender, RoutedEventArgs e)
        {
            IsDrawing = true;
            if (BoardTouchOverlay != null && IsTouch) BoardTouchOverlay.Visibility = Visibility.Visible;
        }

        private void ToggleButton_OnUnchecked(object sender, RoutedEventArgs e)
        {
            IsDrawing = false;
            if (BoardTouchOverlay != null) BoardTouchOverlay.Visibility = Visibility.Collapsed;
        }
        
        private void BoardCanvas_OnPointerPressed(object sender, PointerRoutedEventArgs e)
        {
            Logger.Debug?.Msg("pressed");
            if (!IsDrawing)
            {
                return;
            }
            e.Handled = true;
            BoardCanvas.CapturePointer(e.Pointer);
            var currentPoint = e.GetCurrentPoint(BoardCanvas).Position;
            if (currentPoint.X < 0 || currentPoint.Y < 0)
            {
                return;
            }
            LastPoint = currentPoint;
            DrawLineBetweenPoints(currentPoint, LastPoint);
        }

        private void BoardCanvas_OnPointerMoved(object sender, PointerRoutedEventArgs e)
        {
            Logger.Debug?.Msg("moved");
            if (!e.Pointer.IsInContact)
            {
                return;
            }
            e.Handled = true;
            var currentPoint = e.GetCurrentPoint(BoardCanvas).Position;
            if (currentPoint.X < 0 || currentPoint.Y < 0)
            {
                return;
            }
            DrawLineBetweenPoints(currentPoint, LastPoint);
            LastPoint = currentPoint;
        }

        private void BoardCanvas_OnPointerExited(object sender, PointerRoutedEventArgs e)
        {
            Logger.Debug?.Msg("exited");
            ReleasePointerCapture(e.Pointer);
        }

        private void BoardTouchOverlay_OnPointerPressed(object sender, PointerRoutedEventArgs e)
        {
            Logger.Debug?.Msg("ovrl pressed");
            if (!IsDrawing)
            {
                return;
            }
            e.Handled = true;
            BoardTouchOverlay.CapturePointer(e.Pointer);
            var currentPoint = e.GetCurrentPoint(BoardCanvas).Position;
            if (currentPoint.X < 0 || currentPoint.Y < 0)
            {
                return;
            }
            LastPoint = currentPoint;
            DrawLineBetweenPoints(currentPoint, LastPoint);
        }

        private void BoardTouchOverlay_OnPointerMoved(object sender, PointerRoutedEventArgs e)
        {
            Logger.Debug?.Msg("ovrl moved");
            if (!e.Pointer.IsInContact)
            {
                return;
            }
            e.Handled = true;
            var currentPoint = e.GetCurrentPoint(BoardCanvas).Position;
            if (currentPoint.X < 0 || currentPoint.Y < 0)
            {
                return;
            }
            DrawLineBetweenPoints(currentPoint, LastPoint);
            LastPoint = currentPoint;
        }

        private void BoardTouchOverlay_OnPointerExited(object sender, PointerRoutedEventArgs e)
        {
            Logger.Debug?.Msg("ovrl exited");
            ReleasePointerCapture(e.Pointer);
        }

        private void DrawLineBetweenPoints(Point p1, Point p2)
        {
            if (IsDrawing)
            {
                DrawLineAndSend(p1.X, p1.Y, p2.X, p2.Y);
            }
        }

        private const string DrawOperation = "drag";

        private void DrawLineAndSend(double x1, double y1, double x2, double y2)
        {
            ViewModel?.SendDraw(new DrawMessage
            {
                OldX = (int) x1,
                OldY = (int) y1,
                NewX = (int) x2,
                NewY = (int) y2,
                Operation = DrawOperation
            });
            DrawLine(x1, y1, x2, y2);
        }

        private void DrawLine(double x1, double y1, double x2, double y2)
        {
            var line = new Line
            {
                StrokeStartLineCap = PenLineCap.Round,
                StrokeEndLineCap = PenLineCap.Round,
                Stroke = new SolidColorBrush(Colors.YellowGreen),
                StrokeThickness = 5,
                X1 = x1,
                Y1 = y1,
                X2 = x2,
                Y2 = y2
            };
            BoardCanvas.Children.Add(line);
        }

        private void ClearButton_OnClick(object sender, RoutedEventArgs e)
        {
            BoardCanvas.Children.Clear();
        }


        private void SubscribeToViewModel()
        {
            var listener = new WeakEventListener<BoardViewModel, object, DrawMessage>(ViewModel)
            {
                OnEventAction = OnDrawMessage
            };
            ViewModel.DrawMessageReceived += listener.OnEvent;
        }

        private void OnDrawMessage(BoardViewModel viewModel, object o, DrawMessage e)
        {
            if (e.Operation != DrawOperation)
            {
                return;
            }
            var noawait = Dispatcher.RunAsync(CoreDispatcherPriority.Normal, () =>
            {
                DrawLine(e.OldX, e.OldY, e.NewX, e.NewY);
            });
        }
    }
}
