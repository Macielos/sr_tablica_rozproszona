using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using CloudBoard.Server.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.ModelBinding;

namespace CloudBoard.Server.Controllers
{
    /// <summary>
    /// Access and manage all boards known to server.
    /// </summary>
    [Route("api/[controller]")]
    public class BoardsController : Controller
    {
        private static ConcurrentDictionary<string, HostEntry> BoardHosts { get; } = new ConcurrentDictionary<string, HostEntry>();

        private static BoardHost StaticServerHost { get; } = new BoardHost
        {
            IpAddress = "http://sr-94933.onmodulus.net:80/",
            Board = new Board
            {
                Id = Guid.NewGuid().ToString(),
                Name = "Default board"
            }
        };

        /// <summary>
        /// Gets all board hosts registered on server.
        /// </summary>
        /// <returns>List of board hosts.</returns>
        [HttpGet]
        public IEnumerable<BoardHost> Get()
        {
            CheckTimestamps();
            var list = BoardHosts.ToArray().Select(x => x.Value.Host).OrderBy(x => x.Board.Name).ToList();
            list.Insert(0, StaticServerHost);
            return list;
        }

        /// <summary>
        /// Gets board host with given id.
        /// </summary>
        /// <param name="id">Board host id.</param>
        /// <returns>Board host data.</returns>
        [HttpGet("{id}", Name = "GetBoard")]
        [ProducesResponseType(typeof(BoardHost), 200)]
        [ProducesResponseType(typeof(ErrorMessage), 404)]
        public IActionResult Get(string id)
        {
            CheckTimestamps();
            HostEntry foundHost;
            return BoardHosts.TryGetValue(id, out foundHost)
                ? new ObjectResult(foundHost.Host)
                : NotFound(new ErrorMessage { Error = "No host with given id." });
        }

        // POST api/boards
        /// <summary>
        /// Creates new board and declares its host.
        /// </summary>
        /// <param name="newHost">Host and board data</param>
        /// <returns>Newly created board data.</returns>
        [HttpPost]
        [ProducesResponseType(typeof(BoardHost), 201)]
        [ProducesResponseType(typeof(ModelStateDictionary), 400)]
        public IActionResult Create([FromBody]BoardHostCreate newHost)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }
            var id = Guid.NewGuid().ToString();
            var host = new BoardHost
            {
                IpAddress = newHost.IpAddress,
                Board = new Board
                {
                    Id = id,
                    Name = newHost.BoardName
                }
            };
            UpdateHost(host);
            return CreatedAtRoute("GetBoard", new {id}, host);
        }

        /// <summary>
        /// Updates board host data by board id.
        /// </summary>
        /// <param name="host">Updated data.</param>
        [HttpPut]
        [ProducesResponseType(204)]
        [ProducesResponseType(typeof(ModelStateDictionary), 400)]
        [ProducesResponseType(typeof(ErrorMessage), 404)]
        public IActionResult Update([FromBody]BoardHost host)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }
            if (!Exists(host.Board.Id))
            {
                return NotFound(new ErrorMessage {Error = "No host with given id."});
            }
            UpdateHost(host);
            return NoContent();
        }

        private static bool Exists(string id)
        {
            return BoardHosts.ContainsKey(id);
        }
        
        private static void CheckTimestamps()
        {
            var threshold = DateTime.Now - TimeSpan.FromMinutes(5);
            var inactiveHosts = BoardHosts.Values
                .Where(entry => entry.LastUpdateTimestamp < threshold)
                .Select(entry => entry.Host.Board.Id)
                .ToList();
            foreach (var inactiveId in inactiveHosts)
            {
                HostEntry entry;
                BoardHosts.TryRemove(inactiveId, out entry);
            }
        }

        private static void UpdateHost(BoardHost host)
        {
            BoardHosts[host.Board.Id] = new HostEntry {Host = host, LastUpdateTimestamp = DateTime.Now};
        }

        /// <summary>
        /// Contains some information as to whether request failed. Not user-friendly, use for debugging and development only.
        /// </summary>
        public class ErrorMessage
        {
            /// <summary>
            /// Debug/development error message
            /// </summary>
            public string Error { get; set; }
        }

        private struct HostEntry
        {
            public DateTime LastUpdateTimestamp { get; set; }

            public BoardHost Host { get; set; }
        }
    }
}
