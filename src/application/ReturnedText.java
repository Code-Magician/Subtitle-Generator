package application;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


class Word{
	@SerializedName("text") 	@Expose 	private String text;
	@SerializedName("start") 	@Expose 	private Integer start;
	@SerializedName("end") 		@Expose 	private Integer end;
	
	
	public String getText() {
		return text;
	}
	
	public Integer getStart() {
		return start;
	}
	
	public Integer getEnd() {
		return end;
	}
	
	
	@Override
	public String toString()
	{
		return 
				'[' +
				text + ", " + 
				start + ", " + 
				end + ']' + '\n'; 
	}
}

public class ReturnedText {
	@SerializedName("status") 		@Expose 	private String status;
	@SerializedName("id") 	  		@Expose 	private String id;
	@SerializedName("audio_url") 	@Expose 	private String audio_url;
	@SerializedName("text") 		@Expose 	private String text;
	@SerializedName("words") 		@Expose 	private ArrayList<Word> words;
	@SerializedName("error") 		@Expose 	private String	error;
	
	
	
	public String getAudio_url() {
		return audio_url;
	}
	
	public void setAudio_url(String audio_url) {
		this.audio_url = audio_url;
	}
	
	public String getStatus() {
		return status;
	}
	
	public String getId() {
		return id;
	}
	
	public String getText() {
		return text;
	}
	
	public ArrayList<Word> getWords() {
		return words;
	}
	
	public String getError()
	{
		return error;
	}
	
	
	@Override
	public String toString() {
		
		return 
				audio_url + '\n' + 
				status 	+ '\n' + 
				id + '\n' + 
				text + '\n' + 
				words.toString() + '\n';
	}
	
	
	public String SubtitleFormatString()
	{
		String result = "";
		int seqNum = 1;
		for(Integer i=0; i<words.size();)
		{
			String caption = words.get(i).getText();
			int st = words.get(i).getStart();
			int duration = words.get(i).getEnd() - words.get(i).getStart();
			i++;
			
			boolean seperated = false;
			while(i < words.size() && duration < 2000)
			{
				if(!seperated && caption.length() >= 16) {
					seperated = true;
					caption += '\n';
				}
				else caption += ' ';
				
				caption +=  words.get(i).getText();
				
				duration += words.get(i).getEnd() - words.get(i).getStart();
				i++;
			}
			int en;
			if(i != words.size())
				en = words.get(i).getStart();
			else
				en = st + duration;
			
//			int st = x.getStart();
			int stMil = st%1000;			
			int stSec = (((st/1000)%3600)%60);	
			int stMin = (((st/1000)%3600)/60);
			int stHrs = stSec/3600;
			
//			int en = x.getEnd();
			int enMil = en%1000;			
			int enSec = (((en/1000)%3600)%60);	
			int enMin = (((en/1000)%3600)/60);
			int enHrs = enSec/3600;
			
			String seq 		= ((Integer)(seqNum)).toString();		seqNum++;
			String time 	= String.format("%02d:%02d:%02d,%03d", stHrs, stMin, stSec, stMil) 
								+ " --> " +
								String.format("%02d:%02d:%02d,%03d", enHrs, enMin, enSec, enMil);
			
			String frame	= seq + '\n' + time + '\n' + caption + "\n\n";
			
			result += frame;
		}
		
		
		return result;
	}
}



















