using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using CloudBoard.Uwp.Models;
using Refit;

namespace CloudBoard.Uwp.Services
{
    public interface IBoardHostApi
    {
        [Get("/boards")]
        Task<List<BoardHost>> GetBoardsAsync();

        [Get("/boards/{id}")]
        Task<BoardHost> GetBoardAsync(string id);

        [Post("/boards")]
        Task<BoardHost> CreateAsync([Body] BoardHostCreate newHost);

        [Put("/boards")]
        Task UpdateAsync([Body] BoardHost host);
    }
}
