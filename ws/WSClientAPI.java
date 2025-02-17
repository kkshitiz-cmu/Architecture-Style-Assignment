/******************************************************************************************************************
* File:RESTClientAPI.java
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2018 Carnegie Mellon University
* Versions:
*	1.0 February 2018 - Initial write of assignment 3 (ajl).
*
* Description: This class is used to provide access to a Node.js server. The server for this application is 
* Server.js which provides RESTful services to access a MySQL database.
*
* Parameters: None
*
* Internal Methods: None
*  String retrieveOrders() - gets and returns all the orders in the orderinfo database
*  String retrieveOrders(String id) - gets and returns the order associated with the order id
*  String sendPost(String Date, String FirstName, String LastName, String Address, String Phone) - creates a new 
*  order in the orderinfo database
*
* External Dependencies: None
******************************************************************************************************************/

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class WSClientAPI
{
	/********************************************************************************
	* Description: Logs in a user after authenticating them
	* Parameters: None
	* Returns: String of the user id which can be used as an authentication token
	********************************************************************************/

   	public String login(String userId, String password) throws Exception
	{
		String url = "http://ws_server:3000/api/login";

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		//Form the request header and instantiate the response code
		con.setRequestMethod("POST");

		// The POST parameters
		String input = "userId="+userId+"&password="+password;

		con.setRequestProperty("Accept-Language", "en-GB,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("Content-length", Integer.toString(input.length()));
        con.setRequestProperty("Content-Language", "en-GB");
        con.setRequestProperty("charset", "utf-8");
        con.setUseCaches(false);
        con.setDoOutput(true);

		// Set up a stream and write the parameters to the server
		OutputStream os = con.getOutputStream();
		os.write(input.getBytes());
		os.flush();

		int responseCode = con.getResponseCode();

		//Set up a buffer to read the response from the server
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) 
		{
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}

	/********************************************************************************
	* Description: Logs the user out of the system when they exit
	* Parameters: None
	* Returns: String of the logout response
	********************************************************************************/

	public String logout(String token) throws Exception
	{
		// Set up the URL and connect to the node server

		String url = "http://ws_server:3000/api/logout";

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		//Form the request header and instantiate the response code
		con.setRequestMethod("POST");
		con.setRequestProperty("Authorization", token);
		int responseCode = con.getResponseCode();


		//Set up a buffer to read the response from the server
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		//Loop through the input and build the response string.
		//When done, close the stream.
		while ((inputLine = in.readLine()) != null) 
		{
			response.append(inputLine);
		}
		in.close();

		return(response.toString());

	}

	/********************************************************************************
	* Description: Gets and returns all the orders in the orderinfo database
	* Parameters: None
	* Returns: String of all the current orders in the orderinfo database
	********************************************************************************/

	public String retrieveOrders(String token) throws Exception
	{
		// Set up the URL and connect to the node server

		String url = "http://ws_server:3000/api/orders";

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		//Form the request header and instantiate the response code
		con.setRequestMethod("GET");
		con.setRequestProperty("Authorization", token);
		int responseCode = con.getResponseCode();


		//Set up a buffer to read the response from the server
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		//Loop through the input and build the response string.
		//When done, close the stream.
		while ((inputLine = in.readLine()) != null) 
		{
			response.append(inputLine);
		}
		in.close();

		return(response.toString());

	}
	
	/********************************************************************************
	* Description: Gets and returns the order based on the provided id from the
	*              orderinfo database.
	* Parameters: None
	* Returns: String of all the order corresponding to the id argument in the 
	*		   orderinfo database.
	********************************************************************************/

	public String retrieveOrders(String id, String token) throws Exception
	{
		// Set up the URL and connect to the node server
		String url = "http://ws_server:3000/api/orders/"+id;
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		//Form the request header and instantiate the response code
		con.setRequestMethod("GET");
		con.setRequestProperty("Authorization", token);
		int responseCode = con.getResponseCode();
		System.out.println("here");

		//Set up a buffer to read the response from the server
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		//Loop through the input and build the response string.
		//When done, close the stream.		

		while ((inputLine = in.readLine()) != null) 
		{
			response.append(inputLine);
		}
		in.close();

		return(response.toString());

	}

	/********************************************************************************
	* Description: Posts the new order to the orderinfo database
	* Parameters: None
	* Returns: String that contains the status of the POST operation
	********************************************************************************/

   	public String newOrder(String Date, String FirstName, String LastName, String Address, String Phone, String token) throws Exception
	{
		// Set up the URL and connect to the node server		
		URL url = new URL("http://ws_server:3000/api/orders");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		// The POST parameters
		String input = "order_date="+Date+"&first_name="+FirstName+"&last_name="+LastName+"&address="+Address+"&phone="+Phone;

		//Configure the POST connection for the parameters
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Authorization", token);
        conn.setRequestProperty("Accept-Language", "en-GB,en;q=0.5");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-length", Integer.toString(input.length()));
        conn.setRequestProperty("Content-Language", "en-GB");
        conn.setRequestProperty("charset", "utf-8");
        conn.setUseCaches(false);
        conn.setDoOutput(true);

        // Set up a stream and write the parameters to the server
		OutputStream os = conn.getOutputStream();
		System.out.println("here");
		os.write(input.getBytes());
		os.flush();

		//Loop through the input and build the response string.
		//When done, close the stream.	
		BufferedReader in = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		String inputLine;		
		StringBuffer response = new StringBuffer();

		//Loop through the input and build the response string.
		//When done, close the stream.		

		while ((inputLine = in.readLine()) != null) 
		{
			response.append(inputLine);
		}
		
		in.close();
		conn.disconnect();

		return(response.toString());
		
    } // newOrder

	/********************************************************************************
	* Description: Deletes the order from the orderinfo database
	* Parameters: orderId
	* Returns: String that contains the status of the delete operation
	********************************************************************************/

	public String deleteOrder(String id) throws Exception
	{
		String url = "http://ws_server:3000/api/orders/"+id;
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("DELETE");
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) 
		{
			response.append(inputLine);
		}
		in.close();

		return(response.toString());
	} //deleteOrder
	
} // WSClientAPI