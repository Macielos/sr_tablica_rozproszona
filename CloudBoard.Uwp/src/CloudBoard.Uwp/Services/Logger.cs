using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CloudBoard.Uwp.Services
{
    public class Logger
    {
        public Logger(string name = null)
        {
            Sink = App.Instance.LogSink;
            Name = name;
            Debug = new LevelLogger(LogLevel.Debug, this);
            Info = new LevelLogger(LogLevel.Info, this);
            Error = new LevelLogger(LogLevel.Error, this);
        }

        public LevelLogger Debug { get; }

        public LevelLogger Info { get; }

        public LevelLogger Error { get; }

        public LogSink Sink { get; }

        public string Name { get; }

        public class LevelLogger
        {
            public LevelLogger(LogLevel level, Logger logger)
            {
                Level = level;
                Logger = logger;
            }

            public LogLevel Level { get; }

            public Logger Logger { get; }

            public void Msg(string message)
            {
                Logger.Sink.SaveLog(Logger, Level, message);
            }

            public void Ex(Exception e, string message = null)
            {
                Logger.Sink.SaveLog(Logger, Level, e, message);
            }
        }

        public enum LogLevel
        {
            Debug,
            Info,
            Error
        }
    }
}
