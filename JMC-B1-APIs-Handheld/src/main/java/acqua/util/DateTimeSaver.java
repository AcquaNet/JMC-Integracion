package acqua.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class DateTimeSaver {

	// private static final Logger LOG = Logger.getLogger("dragon_java.log");
	private static final long securityTime = 94670856000000L;
	public static long timestamp = 0;

	public static long getUpdateTime(String request, String newest) throws Exception {
		// LOG.info("JAVA.CurrentTimeSaver.24: Retriving update time for " + request);
		if (timestamp != 0) {
			// LOG.info("JAVA.CurrentTimeSaver.27: Update date exists on memory, returning
			// it");
			// Insurances
			// LOG.info("JAVA.CurrentTimeSaver.44: Check if dates safe");
			long maxDate;
			if (newest.equals("now")) {
				maxDate = new Date().getTime();
			} else {
				maxDate = Long.valueOf(newest);
			}
			if ((maxDate - timestamp) > securityTime) {
				throw new Exception("Date cant be older than " + TimeUnit.MILLISECONDS.toDays(securityTime)
						+ "days for safety reasons. Time difference: "
						+ TimeUnit.MILLISECONDS.toDays(maxDate - timestamp) + " days.");
			}
			return timestamp;
		} else {
			// LOG.info("JAVA.CurrentTimeSaver.29: Update date is not on memory, look up
			// file");
			File file = new File("DateTimeSaver_" + request + ".properties");
			if (!file.exists()) {
				// LOG.info("JAVA.CurrentTimeSaver.32: File doesnt exist, creating it");
				file.createNewFile();
			}
			Properties prop = new Properties();
			// LOG.info("JAVA.CurrentTimeSaver.36: Initiating FileInputStream to " +
			// "DateTimeSaver_" + request
			// + ".properties");
			FileInputStream input = new FileInputStream("DateTimeSaver_" + request + ".properties");
			// LOG.info("JAVA.CurrentTimeSaver.39: Loading properties from
			// fileInputStream");
			prop.load(input);
			if (prop.getProperty("timestamp") != null)
				timestamp = Long.valueOf(prop.getProperty("timestamp"));

			// Insurances
			// LOG.info("JAVA.CurrentTimeSaver.44: Check if dates safe");
			long maxDate;
			if (newest.equals("now")) {
				maxDate = new Date().getTime();
			} else {
				maxDate = Long.valueOf(newest);
			}
			if ((maxDate - timestamp) > securityTime) {
				throw new Exception("Date cant be older than " + TimeUnit.MILLISECONDS.toDays(securityTime)
						+ "days for safety reasons. Time difference: "
						+ TimeUnit.MILLISECONDS.toDays(maxDate - timestamp) + " days.");
			}

			input.close();
			// System.out.println(prop.getProperty("UpdateTime."+request));
			// LOG.info("JAVA.CurrentTimeSaver.52: Return Date timestamp");
			return timestamp;
		}
	}

	public static Boolean setUpdateTime(String request, long timestamp) throws IOException {
		// LOG.info("JAVA.CurrentTimeSaver.58: setUpdateTime Called for " + request);
		DateTimeSaver.timestamp = timestamp;

		Properties prop = new Properties();
		// LOG.info("JAVA.CurrentTimeSaver.62: Create File OutputStream to " +
		// "DateTimeSaver_" + request + ".properties");
		FileOutputStream output = new FileOutputStream("DateTimeSaver_" + request + ".properties");
		prop.setProperty("timestamp." + request, "" + timestamp);
		// LOG.info("JAVA.CurrentTimeSaver.65: Store properties to file");
		prop.store(output, "");
		output.close();
		return true;
	}

	@SuppressWarnings("unused")
	public static ArrayList<String> getDatesBetweenThenAndNow(HashMap<String, Object> dateMap) throws ParseException {
		ArrayList<String> dates = new ArrayList<String>();
		String date = (String) dateMap.get("UpdateDate");
		String time = (String) dateMap.get("UpdateTime");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
		Date date1 = sdf.parse(date + ' ' + time);
		return dates;
	}

	public static List<Date> getDaysBetweenDates(Date startdate, Date enddate) {
		List<Date> dates = new ArrayList<Date>();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(startdate);

		while (calendar.getTime().before(enddate)) {
			Date result = calendar.getTime();
			dates.add(result);
			calendar.add(Calendar.DATE, 1);
		}
		return dates;
	}

	public static ArrayList<HashMap<String, Object>> orderByDate(ArrayList<HashMap<String, Object>> list) {
		// LOG.info("JAVA.CompareUtils.59: Sort List by Date");
		Collections.sort(list, new CalendarComparator());
		// LOG.info("JAVA.CompareUtils.59: Return Sorted List");
		return list;
	}
}

class CalendarComparator implements Comparator<HashMap<String, Object>> {
	@Override
	public int compare(HashMap<String, Object> obj1, HashMap<String, Object> obj2) {

		if (obj1.containsKey("calendar") && (obj2.containsKey("calendar"))) {
			if (obj1.get("calendar").getClass().equals(Calendar.class)
					&& obj2.get("calendar").getClass().equals(Calendar.class)) {
				if (((Calendar) obj1.get("calendar")).after((Calendar) obj2.get("calendar"))) {
					return -1;
				} else if (((Calendar) obj1.get("calendar")).before((Calendar) obj2.get("calendar"))) {
					return 1;
				} else {
					return 0;
				}
			}
			return 0;
		} else
			return 0;
	}
}