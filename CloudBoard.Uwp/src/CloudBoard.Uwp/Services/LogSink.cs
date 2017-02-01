using System;

namespace CloudBoard.Uwp.Services
{
    public class LogSink
    {
        public event EventHandler<Log> LogSaved;

        public void SaveLog(Logger logger, Logger.LogLevel level, string message)
        {
            RaiseMessageLogSaved(logger, level, message);
        }

        public void SaveLog(Logger logger, Logger.LogLevel level, Exception exception, string message = null)
        {
            RaiseExceptionLogSaved(logger, level, exception, message);
        }

        protected virtual void RaiseMessageLogSaved(Logger logger, Logger.LogLevel level, string message)
        {
            LogSaved?.Invoke(this, new Log(logger.Name, level, message, false, null));
        }
        protected virtual void RaiseExceptionLogSaved(Logger logger, Logger.LogLevel level, Exception exception, string message)
        {
            LogSaved?.Invoke(this, new Log(logger.Name, level, message, true, exception));
        }
    }

    public class Log
    {
        public Log(string loggerName, Logger.LogLevel level, string message, bool isException, Exception exception)
        {
            LoggerName = loggerName;
            Level = level;
            Message = message;
            Exception = exception;
            IsException = isException;
        }

        public string LoggerName { get; }

        public Logger.LogLevel Level { get; }

        public string Message { get; }

        public Exception Exception { get; }

        public bool IsException { get; }
    }
}
