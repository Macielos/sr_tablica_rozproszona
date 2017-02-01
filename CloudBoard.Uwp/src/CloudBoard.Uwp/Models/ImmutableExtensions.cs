namespace CloudBoard.Uwp.Models
{
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