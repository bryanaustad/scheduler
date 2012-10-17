package org.lds.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegExpress {
	public void testRegExpress(String reg, String test) {
		Pattern pat = Pattern.compile(reg,Pattern.CASE_INSENSITIVE);
		Matcher m = pat.matcher(test);
		System.out.println("like is " + m.lookingAt());
		System.out.println("exact match is " + m.matches());
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestRegExpress tg = new TestRegExpress();
		String path = "\\conf\\";
		tg.testRegExpress("(.[a-z]+.main)|(.[a-z]+.[a-z]+(.json)|(.[a-z]+.[a-z]+..+((.css)|(.js)|(.png))))", path);
	}
}
