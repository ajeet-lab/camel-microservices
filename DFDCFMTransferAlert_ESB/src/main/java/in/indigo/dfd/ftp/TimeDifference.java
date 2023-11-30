package in.indigo.dfd.ftp;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;

public class TimeDifference {
	public void timeDifference(String timeDiff, Exchange ex) {

		Long fileLastModified = (Long) ex.getMessage().getHeader("camelFileLastModified");
		long modifiedTime = new Date(fileLastModified).getTime();
		long currTime = new Date().getTime();
		long diff = currTime - modifiedTime;
		long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diff);

		ex.getMessage().setHeader("diffMinutes", diffMinutes);
		if (diffMinutes >= Long.parseLong(timeDiff)) {
			ex.getMessage().setHeader("MailSent", "Yes");
		}
	}
}
