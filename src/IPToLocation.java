import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.sql.*;

public class IPToLocation {

	public static void main(String[] args) throws IOException, SQLException{
		String filePath = "C:/Users/Optimistic/Desktop/IPDB11.csv";
		String ipAddress = "197.220.163.60";
		
//		List<List<String>> ipList = readCSV("C:/Users/Optimistic/Desktop/myCopy.csv");
//		writeToFile(ipList);
//		System.out.println("Done");
		
		System.out.println("Reading from File");
		fromFile(ipAddress, filePath);
		
		System.out.println("\nReading from Database");
		fromDB(ipAddress);	
		
		System.out.println("\nReading from API");
		fromAPI(ipAddress);	
	}
	
	public static void fromAPI(String ipAddress) throws MalformedURLException{
		URL url = new URL("http://api.ipinfodb.com/v3/ip-country/?key=86bb4dc147c72eff3e3c8e8eb61d79ab9da1ba7f1f1dce360ccb15f4cc35bec8&ip="
					+ ipAddress + "&format=json");
		
		Long startTime, endTime;
		startTime = System.currentTimeMillis();
		
		HttpURLConnection connection = null;
		try{
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			
			java.io.DataOutputStream dos = new java.io.DataOutputStream(connection.getOutputStream());
			dos.flush();
			dos.close();
			
			InputStream is = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuffer response = new StringBuffer();
			String line; 
			while((line=br.readLine())!=null){
				response.append(line + "\n");
			}
			
			System.out.println(response.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
		
		endTime = System.currentTimeMillis();
		System.out.println("Total Execution Time is: " + (endTime-startTime)/1000 + "s");
	}
	
	public static void fromFile(String ipAddress,String filePath) throws IOException, SQLException{
		Long startTime,endTime;
		startTime = System.currentTimeMillis();
		
		List<List<String>> ipList = readCSV(filePath);
		System.out.println("Country: " + getCountry(ipAddress, ipList));
		
		endTime = System.currentTimeMillis();
		System.out.println("Total Execution time: " + (endTime-startTime)/1000 + "s");
	}
	
	public static void fromDB(String ipAddress) throws SQLException{
		Long startTime,endTime;
		startTime = System.currentTimeMillis();
		
		System.out.println("Country: " + getCountry(ipAddress));
		
		endTime = System.currentTimeMillis();
		System.out.println("Total Execution time: " + (endTime-startTime)/1000 + "s");
	}
	
	public static void writeToFile(List<List<String>> ipList) throws FileNotFoundException{
		File outputfile = new File("C:/Users/Optimistic/Desktop/IPDB11.csv");
		PrintWriter pw = new PrintWriter(outputfile);
		
		Long i = 0L;		
		for(List<String> d:ipList){
			pw.println(d.get(0) + "," + d.get(1) + ","  + d.get(2) + ","  + d.get(3) + ","  + d.get(4) + ","  + d.get(5) + ","  + d.get(6) + ","  + d.get(7) + ","  + d.get(8) + ","  + d.get(9));
			i++;
		}
		System.out.println("Files written: " + i);
		pw.close();
	}
		
	public static List<List<String>> readCSV(String filePath) throws IOException{
		File file = new File(filePath);
		FileReader fileReader = new FileReader(file);
		BufferedReader bf = new BufferedReader(fileReader);
		
		String lineContent;
		List<List<String>> contentList = new ArrayList<>();
		
		while ((lineContent=bf.readLine())!=null){
			contentList.add(makeList(lineContent.replace("\"","").split(",")));
		}
		
		bf.close();		
		return contentList;
	}
	
	public static List<String> makeList(String[] contentArray){
		 List<String> list = Arrays.asList(contentArray);
		 return list;
	}
	
	public static int[] ipToArray(String IPAddress){
		IPAddress = IPAddress.replace(".", "_");
		String[] stringOctet = IPAddress.split("_");
		
		int[] intOctet = new int[4];
		
		for(int i=0;i<stringOctet.length;i++){
			intOctet[i] = Integer.parseInt(stringOctet[i]);
		}
		
		return intOctet;
	}
	
	public static long convertIPAddressToIPNumber(String ipAddress){
		int[] ipArray = ipToArray(ipAddress);
		Long ipNumber = 0L;
		int i = 3;
		for(int octet:ipArray){
			ipNumber += (long) (octet * Math.pow(256, i));
			i--; 
		}
		
		return ipNumber;
	}
	
	public static String getCountry(String ipAddress,List<List<String>> ipList){
		Long ipNumber = convertIPAddressToIPNumber(ipAddress);
		
		String country ="N/A";
		
		for(List<String> list:ipList){
			if (ipNumber >= Long.parseLong(list.get(0)) && ipNumber <=  Long.parseLong(list.get(1))){
				country = list.get(3);
			}
		}
		
		return country.replace("-","N/A");
	}
	
	public static String getCountry(String IPAddress) throws SQLException{
		String DB = "jdbc:mysql://localhost/ipdb";
		String sql = "SELECT ipfrom,ipto,country_name FROM iptable";
		String country = "N/A";
		Long ipNumber = convertIPAddressToIPNumber(IPAddress);
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs=null;
		
		try {
			conn = DriverManager.getConnection(DB,"root","iprosoft1");
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
	
			while(rs.next()){
				Long ipfrom = rs.getLong("ipfrom");
				Long ipto = rs.getLong("ipto");
				
				if(ipNumber>=ipfrom && ipNumber<=ipto){
					country = rs.getString("country_name");
					break;
				}
			}
		} catch (SQLException e) {			
			e.printStackTrace();
		}
	    
	    conn.close();
	    stmt.close();
	    rs.close();
	    return country.replace("-","N/A");	
	}
}
