using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CloudBoard.Uwp.Services
{
    public class DebugLogPrinter
    {
        [Conditional("DEBUG")]
        public static void SubscribeTo(LogSink sink)
        {
            sink.LogSaved += SinkOnLogSaved;
        }

        private static void SinkOnLogSaved(object sender, Log log)
        {
            if (!Debugger.IsAttached)
            {
                return;
            }
            if (log.IsException)
            {
                Debug.WriteLineIf(!string.IsNullOrEmpty(log.Message),
                    $"{log.Level}@{log.LoggerName} (exception below): {log.Message}");
                Debug.WriteLine($"{log.Level}@{log.LoggerName}: {log.Exception}");
            }
            else
            {
                Debug.WriteLine($"{log.Level}@{log.LoggerName}: {log.Message}");
            }
        }
    }
}
