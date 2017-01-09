using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CloudBoard.Uwp.Models
{
    public class BoardHostCreate
    {
        public string IpAddress { get; set; }

        public string BoardName { get; set; }
    }

    public class BoardHost
    {
        public Board Board { get; set; }

        public string IpAddress { get; set; }
    }

    public sealed class ImmutableBoardHost
    {
        public ImmutableBoardHost(BoardHost host)
        {
            IpAddress = host.IpAddress;
            Board = host.Board.ToImmutable();
        }

        public string IpAddress { get; }

        public ImmutableBoard Board { get; }

        public override bool Equals(object obj)
        {
            var other = obj as ImmutableBoardHost;
            return other != null && Equals(other);
        }

        private bool Equals(ImmutableBoardHost other)
        {
            return string.Equals(IpAddress, other.IpAddress) && Equals(Board, other.Board);
        }

        public override int GetHashCode()
        {
            unchecked
            {
                return ((IpAddress?.GetHashCode() ?? 0) * 397) ^ (Board?.GetHashCode() ?? 0);
            }
        }
    }

    public class Board
    {
        public string Id { get; set; }

        public string Name { get; set; }
    }

    public sealed class ImmutableBoard
    {
        public ImmutableBoard(Board board)
        {
            Id = board.Id;
            Name = board.Name;
        }

        public string Id { get; }

        public string Name { get; }

        public override bool Equals(object obj)
        {
            var other = obj as ImmutableBoard;
            return other != null && Equals(other);
        }

        private bool Equals(ImmutableBoard other)
        {
            return string.Equals(Id, other.Id) && string.Equals(Name, other.Name);
        }

        public override int GetHashCode()
        {
            unchecked
            {
                return ((Id?.GetHashCode() ?? 0) * 397) ^ (Name?.GetHashCode() ?? 0);
            }
        }
    }

    public static class ImmutableExtensions
    {
        public static ImmutableBoard ToImmutable(this Board board)
        {
            return new ImmutableBoard(board);
        }

        public static ImmutableBoardHost ToImmutable(this BoardHost host)
        {
            return new ImmutableBoardHost(host);
        }
    }
}
