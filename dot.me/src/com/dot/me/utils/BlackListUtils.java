package com.dot.me.utils;

import java.util.Arrays;
import java.util.Vector;

import com.dot.me.model.Mensagem;

public class BlackListUtils {

	public static boolean validateMessage(Vector<String> blacklist, Mensagem m) {

		Vector<String> parts = new Vector<String>(Arrays.asList(m.getMensagem()
				.toLowerCase().split(" ")));

		for (String blackword : blacklist) {
			int count_equals = 0;
			String[] parts_blackword = blackword.split("\\+");
			for (String part_blackword : parts_blackword) {
				for (String part : parts) {
					if (part.toLowerCase().contains(part_blackword.toLowerCase())){
						count_equals++;
						break;
					}
				}
			}
			if(count_equals==parts_blackword.length)
				return false;
		}

		return true;
	}
}
