package util;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;

public class S3Sign {
	public static void main(String[] args) {
		BasicAWSCredentials creds = new BasicAWSCredentials("AKIAI66VK3KSIQEBBEAA", "0/d40XgqJcvyJ4q93Dx3nQddKCmvbrzlOGVoTd+p"); 
		AmazonS3Client S3 = new AmazonS3Client(creds);
		Calendar cal = Calendar.getInstance();
		cal.add(1, Calendar.DAY_OF_MONTH);
		Date date = cal.getTime();
		URL url = S3.generatePresignedUrl("share.faermanj", "gru10-iot-tour.mp4",date , HttpMethod.GET );
		System.out.println(url.toString());
	}
}
