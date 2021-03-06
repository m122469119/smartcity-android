package sjtu.dclab.smartcity.community.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 *2015年5月23日 下午9:14:48
 *@author changyi yuan
 */
public class NetUtilWithHttpClient {
	public static String sendGet(String addr, String content){
		if(content != null && content.trim().length() != 0)
			addr += "?" + content;
		
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(addr);
		
		BufferedReader br = null;
		try {
			HttpResponse response = client.execute(request);
			br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer();
			String line = "";
			while((line = br.readLine()) != null){
				sb.append(line);
			}
			br.close();
			
			return sb.toString();
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public static String sendPost(String addr, String params){
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(addr);
		
		BufferedReader br = null;
		
		try {
			StringEntity entity = new StringEntity(params, "UTF-8");
			entity.setContentEncoding("UTF-8");
			entity.setContentType("application/json");
			request.setEntity(entity);
			HttpResponse response = client.execute(request);
			br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer();
			String line = "";
			while((line = br.readLine()) != null){
				sb.append(line );
			}
			br.close();
			
			return sb.toString();
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
}
