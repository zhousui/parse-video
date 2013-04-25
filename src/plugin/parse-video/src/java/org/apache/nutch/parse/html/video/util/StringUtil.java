package org.apache.nutch.parse.html.video.util;

import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.MatchResult;

public class StringUtil {
	public static String[] splitStringPattern(String input, String pattern) throws java.lang.IllegalStateException {
		Scanner s = new Scanner(input);
		s.findInLine(pattern);
		MatchResult result=null;
		try{
			result = s.match();
		}catch(IllegalStateException e){
			return null;
		}
		String[] ret = new String[result.groupCount()];
	     for (int i=1; i<=result.groupCount(); i++)
	         ret[i-1] = (result.group(i));
	     s.close(); 
	     return ret;
	}
	public static Integer[] getDoubleInt(String input, String pattern) {
			Integer[] reti = new Integer[2];
		try{
		String[] ret = splitStringPattern(input, pattern);
		reti[0] = Integer.parseInt(ret[0]);
		reti[1] = Integer.parseInt(ret[1]);
		}catch(Exception e){
			reti[0] = null;
			reti[1] = null;
		}
		return reti;
	}
	public static Integer getSingleInt(String input, String pattern) {
		Integer v1;
		try{
		String[] ret = splitStringPattern(input, pattern);
		v1 = Integer.parseInt(ret[0]);
		//System.out.println("v1:"+v1);
		}catch(Exception e){
			v1 = null;
		}
		return v1;
	}
	public static Set<Integer> getNumsFromStr(String str) {

		  String[] ary = str.replaceAll("[^\\d]", " ").split("\\s+");
		  
		  Set<Integer> set = new TreeSet<Integer>();
		  
		  for(String num: ary){
		   if(!num.trim().equals("")){
		    set.add(new Integer(num.trim()));
		   }
		  }
		  
		  return set;
		 }
}
