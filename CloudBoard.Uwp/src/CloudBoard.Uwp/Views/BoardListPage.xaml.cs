﻿using System;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using CloudBoard.Uwp.ViewModels;

// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=402352&clcid=0x409

namespace CloudBoard.Uwp.Views
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class BoardListPage : Page
    {
        public BoardListPage()
        {
            ViewModel = new BoardListViewModel(this);
            this.InitializeComponent();
        }

        public BoardListViewModel ViewModel { get; }

        public Visibility ToInverseVisibility(bool value) => value ? Visibility.Collapsed : Visibility.Visible;

        public bool Not(bool value) => !value;

        public bool IsNotNullOrWhiteSpace(string s) => !string.IsNullOrWhiteSpace(s);
    }
}
