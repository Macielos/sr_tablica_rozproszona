using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CloudBoard.Uwp.Models
{
    public class DrawMessage
    {
        [Newtonsoft.Json.JsonProperty("oldX")]
        public int OldX { get; set; }
        [Newtonsoft.Json.JsonProperty("oldY")]
        public int OldY { get; set; }
        [Newtonsoft.Json.JsonProperty("x")]
        public int NewX { get; set; }
        [Newtonsoft.Json.JsonProperty("y")]
        public int NewY { get; set; }
        [Newtonsoft.Json.JsonProperty("type")]
        public string Operation { get; set; }
    }
}
