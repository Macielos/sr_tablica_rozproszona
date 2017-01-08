using System.ComponentModel.DataAnnotations;

namespace CloudBoard.Server.Models
{
    public class BoardHostCreate
    {
        [Required]
        public string IpAddress { get; set; }

        [Required]
        public string BoardName { get; set; }
    }

    public class BoardHost
    {
        [Required]
        public Board Board { get; set; }

        [Required]
        public string IpAddress { get; set; }
    }

    public class Board
    {
        [Required]
        public string Id { get; set; }

        [Required]
        public string Name { get; set; }
    }
}