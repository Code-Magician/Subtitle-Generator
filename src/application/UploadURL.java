package application;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UploadURL {
	@SerializedName("upload_url") @Expose private String upload_url;
	
	public String getUploadUrl()
	{
		return upload_url;
	}
}
