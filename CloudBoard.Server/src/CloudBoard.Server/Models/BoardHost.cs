using System.ComponentModel.DataAnnotations;
using Microsoft.AspNetCore.Mvc.ModelBinding;

namespace CloudBoard.Server.Models
{
    /// <summary>
    /// Contains information required for creation and registration of new board on the server.
    /// </summary>
    [BindRequired]
    public class BoardHostCreate
    {
        /// <summary>
        /// Board host's WebSocket server URI in format '(http|https|ws|wss)://{hostaddress}:{port}'.
        /// </summary>
        [Required]
        [RegularExpression(@"^((http|ws)s?\://).+\:\d{1,5}/?$")]
        public string IpAddress { get; set; }

        /// <summary>
        /// User-friendly name for the board.
        /// </summary>
        [Required]
        public string BoardName { get; set; }
    }

    /// <summary>
    /// Describes board host, including board details.
    /// </summary>
    [BindRequired]
    public class BoardHost
    {
        /// <summary>
        /// Board details.
        /// </summary>
        [Required]
        public Board Board { get; set; }

        /// <summary>
        /// Board host's WebSocket server URI in format '(http|https|ws|wss)://{hostaddress}:{port}'.
        /// </summary>
        [Required]
        [RegularExpression(@"^((http|ws)s?\://).+\:\d{1,5}/?$")]
        public string IpAddress { get; set; }
    }

    /// <summary>
    /// Describes board.
    /// </summary>
    [BindRequired]
    public class Board
    {
        /// <summary>
        /// Board ID, unique, assigned by server during board creation.
        /// </summary>
        [Required]
        public string Id { get; set; }

        /// <summary>
        /// User-friendly name for the board.
        /// </summary>
        [Required]
        public string Name { get; set; }
    }
}