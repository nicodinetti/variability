package uy.edu.fing.modeler.variability.log;

public class LogUtils {

	private static int step = 0;

	public static void log(String prefix, String msg) {
		String res = "";
		for (int i = 0; i < step; i++) {
			res = res + "\t";
		}
		System.out.println(res + prefix + ": " + msg);
	}

	public static void logNext(String prefix, String msg) {
		next();
		log(prefix, msg);
	}

	public static void logBack(String prefix, String msg) {
		log(prefix, msg);
		back();
	}

	public static void next() {
		step++;
	}

	public static void back() {
		step--;
	}

}
