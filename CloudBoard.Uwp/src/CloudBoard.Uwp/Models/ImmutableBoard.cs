namespace CloudBoard.Uwp.Models
{
    public sealed class ImmutableBoard
    {
        public ImmutableBoard(Board board)
        {
            Id = board.Id;
            Name = board.Name;
        }

        public string Id { get; }

        public string Name { get; }

        public Board ToMutable()
        {
            return new Board
            {
                Id = Id,
                Name = Name
            };
        }

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
}