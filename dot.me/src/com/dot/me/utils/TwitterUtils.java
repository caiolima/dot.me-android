package com.dot.me.utils;

import java.util.Date;
import java.util.Vector;

import com.dot.me.app.R;
import com.dot.me.model.Mensagem;
import com.dot.me.model.User;
import com.dot.me.model.bd.Facade;

import android.content.Context;
import android.text.GetChars;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.widget.TextView;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterUtils {

	private static Twitter twitter;

	public static Twitter getTwitter() {
		if (twitter == null) {
			twitter = new TwitterFactory().getInstance();

			twitter.setOAuthConsumer(Constants.CONSUMER_KEY,
					Constants.CONSUMER_SECRET);

			return twitter;
		}
		return twitter;
	}

	public static Twitter getTwitter(AccessToken token) {
		if (twitter == null) {
			twitter = new TwitterFactory().getInstance();

			twitter.setOAuthConsumer(Constants.CONSUMER_KEY,
					Constants.CONSUMER_SECRET);
		}
		twitter.setOAuthAccessToken(token);
		return twitter;
	}

	public static String friendlyFormat(Date created, Context ctx) {

		// today
		Date today = new Date();

		// how much time since (ms)
		Long duration = today.getTime() - created.getTime();

		int second = 1000;
		int minute = second * 60;
		int hour = minute * 60;
		int day = hour * 24;

		if (duration < second * 7) {
			return ctx.getString(R.string.right_now);
		}

		if (duration < minute) {
			int n = (int) Math.floor(duration / second);
			return n + " "+ctx.getString(R.string.seconds_ago);
		}

		if (duration < minute * 2) {
			return ctx.getString(R.string.about_one_minute_ago);
		}

		if (duration < hour) {
			int n = (int) Math.floor(duration / minute);
			return n + " "+ctx.getString(R.string.minutes_ago);
		}

		if (duration < hour * 2) {
			return ctx.getString(R.string.about_one_hour);
		}

		if (duration < day) {
			int n = (int) Math.floor(duration / hour);
			return n + " "+ctx.getString(R.string.hours_ago);
		}
		if (duration > day && duration < day * 2) {
			return ctx.getString(R.string.yesterday);
		}

		if (duration < day * 365) {
			int n = (int) Math.floor(duration / day);
			return n + " "+ctx.getString(R.string.days_ago);
		} else {
			return ctx.getString(R.string.over_a_year);
		}
	}

	public static Spanned createMessage(String content) {
		String out = "";
		String[] words = content.split(" ");
		//Vector<String> allWords = new Vector<String>();

		/*for (String word : words) {
			String[] gets = word.split("\\n");
			
			for (String get : gets) {
				allWords.add(get);
			}
		}*/
		
		
		
		for (String word : words) {
			String complete="";
			if(word.contains("\n")){
				String aux=word;
				int pos=word.indexOf("\n");
				word=word.substring(0, word.indexOf("\n"));
				complete=aux.substring(pos-1);
			}
			
			if (word.contains("http://") || (word.contains("https://"))) {
				int initPos=word.indexOf("http");
				String link=word.substring(initPos);
				String preText=word.substring(0, initPos);
				word = preText+"<a href=\"" + link + "\">" + link + "</a>";
			}

			out += word+complete + " ";
		}
		out = out.substring(0, out.length() - 1);
		out = out.replace("\n", "<br/>");
		
		return Html.fromHtml(out);
	}

	private static String createMentionLink(String word) {
		try {
			char c = word.charAt(word.length() - 1);
			String complement = "";
			while (!(Character.isLetter(c) || Character.isDigit(c))) {
				complement = c + complement;
				word = word.substring(0, word.length() - 1);
				c = word.charAt(word.length() - 1);
			}
			word = "<a href=\"twitter_search_user://find_user?username="
					+ word.substring(1) + "\">" + word + "</a>" + complement;

			return word;
		} catch (Exception e) {
			return word;
		}

	}

	public static void stripUnderlines(TextView textView) {
		Spannable s = (Spannable) textView.getText();
		URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
		for (URLSpan span : spans) {
			int start = s.getSpanStart(span);
			int end = s.getSpanEnd(span);
			s.removeSpan(span);
			span = new URLSpanNoUnderline(span.getURL());
			s.setSpan(span, start, end, 0);

		}
		textView.setText(s);
	}
	
	public static ResponseUpdate updateTweets(Context ctx,ResponseList<twitter4j.Status> list,int type) {
		// ImageUtils.loadImages(list);
		Mensagem lastMessage = null;
		Vector<Mensagem> mensagens = new Vector<Mensagem>();
		for (twitter4j.Status status : list) {
			Mensagem m = Mensagem.creteFromTwitterStatus(status);
			Facade facade = Facade.getInstance(ctx);
			User u = User.createFromTwitterUser(status.getUser());
			facade.insert(u);
			m.setTipo(type);

			if (!facade.exsistsStatus(m.getIdMensagem(), m.getTipo())) {

				facade.insert(m);

			}

			mensagens.add(m);
			lastMessage = m;
		}
		
		ResponseUpdate response = new ResponseUpdate();
		response.lastMessage = lastMessage;
		response.mensagens = mensagens;
		return response;
	}
	
	public static class ResponseUpdate {
		public Vector<Mensagem> mensagens;
		public Mensagem lastMessage;
	}
	
	public static void logoutTwitter(){
		twitter=null;
	}

}
