namespace CloudBoard.Uwp.Models
{
    public sealed class ImmutableBoardHost
    {
        public ImmutableBoardHost(BoardHost host)
        {
            IpAddress = host.IpAddress;
            Board = host.Board.ToImmutable();
        }

        public string IpAddress { get; }

        public ImmutableBoard Board { get; }

        public BoardHost ToMutable()
        {
            return new BoardHost
            {
                IpAddress = IpAddress,
                Board = Board.ToMutable()
            };
        }

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
}