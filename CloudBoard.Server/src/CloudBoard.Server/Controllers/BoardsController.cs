using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using CloudBoard.Server.Models;
using Microsoft.AspNetCore.Mvc;

namespace CloudBoard.Server.Controllers
{
    /// <summary>
    /// Access and manage all boards known to server.
    /// </summary>
    [Route("api/[controller]")]
    public class BoardsController : Controller
    {
        private static List<BoardHost> BoardHosts { get; } = new List<BoardHost>();
        private static Dictionary<string, DateTime> LastUpdateTimestamps { get; } = new Dictionary<string, DateTime>();

        /// <summary>
        /// Gets all board hosts registered on server.
        /// </summary>
        /// <returns>List of board hosts.</returns>
        [HttpGet]
        public IEnumerable<BoardHost> Get()
        {
            return BoardHosts;
        }
        
        /// <summary>
        /// Gets board host with given id.
        /// </summary>
        /// <param name="id">Board host id.</param>
        /// <returns>Board host data.</returns>
        [HttpGet("{id}", Name = "GetBoard")]
        public IActionResult Get(string id)
        {
            var foundHost = BoardHosts.Find(host => host.Board?.Id == id);
            return foundHost != null ? (IActionResult) new ObjectResult(foundHost) : NotFound();
        }

        // POST api/boards
        /// <summary>
        /// Creates new board and declares its host.
        /// </summary>
        /// <param name="newHost">Host and board data</param>
        /// <returns>Newly created board data.</returns>
        [HttpPost]
        [ProducesResponseType(typeof(BoardHost), 201)]
        public IActionResult Create([FromBody]BoardHostCreate newHost)
        {
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
            AddHost(host);
            return CreatedAtRoute("GetBoard", new {id}, host);
        }

        /// <summary>
        /// Updates board host data by board id.
        /// </summary>
        /// <param name="host">Updated data.</param>
        [HttpPut]
        [ProducesResponseType(204)]
        [ProducesResponseType(404)]
        public IActionResult Update([FromBody]BoardHost host)
        {
            if (!Exists(host.Board.Id))
            {
                return NotFound();
            }
            UpdateHost(host);
            return NoContent();
        }

        private static bool Exists(string id)
        {
            return BoardHosts.Any(x => x.Board.Id == id);
        }

        private static void AddHost(BoardHost host)
        {
            BoardHosts.Add(host);
            LastUpdateTimestamps[host.Board.Id] = DateTime.Now;
            CheckTimestamps();
        }

        private static void CheckTimestamps()
        {
            var threshold = DateTime.Now - TimeSpan.FromMinutes(5);
            var inactiveHosts = LastUpdateTimestamps.Where(pair => pair.Value < threshold).Select(pair => pair.Key).ToList();
            foreach (var inactiveId in inactiveHosts)
            {
                LastUpdateTimestamps.Remove(inactiveId);
                BoardHosts.RemoveAll(x => x.Board.Id == inactiveId);
            }
        }

        private static void UpdateHost(BoardHost host)
        {
            BoardHosts.RemoveAll(x => x.Board.Id == host.Board.Id);
            AddHost(host);
        }
    }
}
