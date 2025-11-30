package engine;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Implements a simple logging format.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 */
public class MinimalFormatter extends Formatter {
  /** Format for the date. */
  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

  /** System line separator. */
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  public MinimalFormatter() { // NOPMD
    super();
  }

  @Override
  public final String format(final LogRecord logRecord) {

    final StringBuilder output =
        new StringBuilder()
            .append('[')
            .append(logRecord.getLevel())
            .append('|')
            .append(DATE_FORMAT.format(Instant.ofEpochMilli(logRecord.getMillis())))
            .append("]: ")
            .append(logRecord.getMessage())
            .append(' ')
            .append(LINE_SEPARATOR);

    return output.toString();
  }
}
