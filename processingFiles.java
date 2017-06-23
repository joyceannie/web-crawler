/* PART 1 : WEB CRAWLER 
 * Prerequisite :  
 * Path of specification.csv : /Users/pooja/specification.csv
 * Need to create an empty folder called crawler : /Users/pooja/crawler
 * To run this program, the paths in the code need to be changed accordingly
 * */
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.net.*;
import java.util.*;
import java.io.*;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler {

	static int docCount = 0;
	static int pagesToCrawl = 0;
	static int maxPagesToCrawl = 0;
	static BufferedWriter bw = null;
	static String previousServer = "";
	
	public static void main(String []v)
	{
		//HashSet to include unique visited urls
		Set<String> visitedUrls = new HashSet<>(); 
		String url="";
		String domainRestriction = "";
		try
		{
			String line="";
			BufferedReader fileReader = new BufferedReader(new FileReader("/Users/pooja/specification.csv"));
			//Read specification.csv 
			while ((line = fileReader.readLine()) != null) 
			{   		
				String[] tokens = line.split(",");
				
				//Fetch the seed url
            	url = tokens[0]; 
            	//Max pages to crawl
            	maxPagesToCrawl = Integer.parseInt(tokens[1]); 
            	
				if(tokens.length == 3)
				{
	            	//Domain restriction if any
	            	domainRestriction = tokens[2]; 
				}
				else
				{
					domainRestriction = "NO RESTRICTION";
				}
			
                pagesToCrawl = maxPagesToCrawl;
                
                //Create report.html
                bw = new BufferedWriter(new FileWriter("/Users/pooja/crawler/report.html"));
                
                //Keeping track of previous web server visited to maintain politeness policy
                previousServer = new URL(url).getHost();
			}
			fileReader.close();
        } 
        catch (Exception e)
		{
            e.printStackTrace();
        } 
		
		//Process the seed url
		processSeed(url, pagesToCrawl, domainRestriction, visitedUrls);	
		
	}
	
	/*
	 *	Method : robotSafe 
	 *	Input parameter : URL url
	 *	Return value : boolean
	 *	Given an URL, this method checks the robots.txt and returns appropriate boolean value true/false if the page can/cannot be crawled 
	 * 
	 */
	public static boolean robotSafe(URL url)
	{
	    String DISALLOW = "Disallow:";
	    String strHost = url.getHost();

		//Form URL of the robots.txt file
	    String strRobot = "http://" + strHost + "/robots.txt";
	    URL urlRobot;
	    try
	    {
	    	urlRobot = new URL(strRobot);
		}
	    catch (MalformedURLException e)
	    {  
		    return false;
		}

	    String strCommands;
	    try
	    {
	       InputStream urlRobotStream = urlRobot.openStream();

		   //Read in entire file
	       byte b[] = new byte[1000];
	       int numRead = urlRobotStream.read(b);
	       strCommands = new String(b, 0, numRead);
	       while (numRead != -1)
	       {
	          numRead = urlRobotStream.read(b);
	          if (numRead != -1)
	          {
	             String newCommands = new String(b, 0, numRead);
		         strCommands += newCommands;
	          }
		    }
	       urlRobotStream.close();
		}
	    catch (IOException e)
	    {
		    //If there is no robots.txt file, it is OK to crawl
		    return true;
		}
	    catch (StringIndexOutOfBoundsException stre)
	    {
		    //If there is no robots.txt file, it is OK to crawl
		    return true;
		}
	    catch(IllegalArgumentException a)
	    {
	    	return true;
	    }

	    //If there are no "disallow" values, then they are not blocking anything
	    if (strCommands.contains(DISALLOW)) 
	    {
	        String[] split = strCommands.split("\n");
	        String mostRecentUserAgent = null;
	        for (int i = 0; i < split.length; i++) 
	        {
	            String line = split[i].trim();
	            
	            //Check for the user-agent
	            if (line.toLowerCase().startsWith("user-agent")) 
	            {
	                int start = line.indexOf(":") + 1;
	                int end   = line.length();
	                mostRecentUserAgent = line.substring(start, end).trim();
	            }
	            else if (line.startsWith(DISALLOW))
	            {
	                    int start = line.indexOf(":") + 1;
	                    int end   = line.length();
	                    String rule = line.substring(start, end).trim();
	                    if(mostRecentUserAgent.equals("*"))
	                    {
	                    	String path = url.getPath();
	                    	
	                    	//Allows everything if BLANK
	        	            if (rule.length() == 0) return true;
	        	            
	        	            //Allows nothing if /
	        	            if (rule == "/") return false;      

	        	            //Check the path in the rule 
	        	            if (rule.length() <= path.length())
	        	            { 
	        	                String pathCompare = path.substring(0, rule.length());
	        	                if (pathCompare.equals(rule)) return false;
	        	            }    	       
	                    }
	             }
	           }
	        }    
		return true;
	}
	
	/*
	 *	Method : processSeed 
	 *	Input parameter : String URL, int remainingPages, String domainRestriction,Set<String> visited
	 *	Return value : void
	 *	Crawl the URL if it is not crawled within the domain restriction
	 * 
	 */
	
	public static void processSeed(String URL, int remainingPages, String domainRestriction,Set<String> visited)
	{
		FileWriter fWriter = null;
		BufferedWriter writer = null;
		String localPath = "";
		int httpStatus;	
		
		String reportData = "<!DOCTYPE html><html><head><style>table, th, td {border: 1px solid black;border-collapse: collapse;}</style><title>Web Crawler Statistics</title></head><body><h1>Web Crawler Statistics</h1><table style=\"width:100%\"><th>Live URL</th><th>Repository link</th><th>HTTP Status Code</th><th>No of outlinks</th><th>No of images</th>";
		
		
		//Do not process the URL if it is empty or has been already visited
		if(URL.isEmpty() || visited.contains(URL))
			return;
		
		try
		{
			//Check if there are still pages to be crawled
			if(pagesToCrawl >=1)
			{
				//Check if the page can be crawled
				if(robotSafe(new URL(URL)))
				{
					//Add the url to the visited set
					visited.add(URL);
					pagesToCrawl--;		
		
					try
					{		
						//Retrieve the page content, status code
						Connection.Response response = Jsoup.connect(URL).execute();	
						Document doc = response.parse();
						httpStatus = response.statusCode();
						
						System.out.println("URL :" + URL + " STATUS : "+httpStatus);		
						
						//Download the pages into crawler folder
						try 
						{
							File f = new File("/Users/pooja/crawler");
							if(!f.exists())
								f.mkdirs();
							String filename = "/Users/pooja/crawler/doc" + ++docCount + ".html";
							localPath = "file://"+ filename;
						    fWriter = new FileWriter(filename);
						    writer = new BufferedWriter(fWriter);
						    writer.write(doc.html()); 
						    writer.close();
						}
						catch (Exception e)
						{
							System.out.println("Error in writing to html file "+e);
						}
						
						//Calculate number of images in the page
						Elements images = doc.getElementsByTag("img");
						int noOfImages = images.size();
						
						//Calculate the number of outlinks in the page
						Elements questions = doc.select("a[href]");
						int noOfOutlinks = questions.size();
						
						for(Element link: questions)
						{					
							String checkLink = link.attr("abs:href");	
							//Handling # in <a href> to prevent pointing to the same page and decrementing the outlink count
							if(checkLink.indexOf("#") != -1)
							{
								noOfOutlinks--;
							}		
						}	
						
						//report.html
						try
						{
							//For the first page crawled, write the initial content of report.html
							if(pagesToCrawl == maxPagesToCrawl - 1)
							{
								bw.write(reportData);		
							}
							
							//Add the page statistics -> live URL, local repository path, status, number of images and outlinks
							bw.write("<tr><td><a href=\""+URL+"\">"+ URL +"</a></td><td><a href=\""+localPath+"\">"+localPath+"</a></td><td>"+httpStatus+"</td><td>"+noOfOutlinks+"</td><td>"+noOfImages+"</td></tr>");
							
							//Close the report.html after the last page is crawled
							if(pagesToCrawl == 0)
							{
								bw.write("</table></body></html>");
								bw.close();
							}
						}
						catch(Exception e1)
						{
							System.out.println("Exception e1 : " +e1);	
						}
						
						for(Element link: questions)
						{	
							String outlink = link.attr("abs:href");
							java.net.URL outlinkURL = new URL(outlink);
							
							//Handling # in <a href> to prevent pointing to the same page 
							if(outlink.indexOf("#") != -1)
							{
								//System.out.println(outlink);
								String[] getActualUrl = outlink.split("#"); 
								outlink = getActualUrl[0];
							}
							
							if(domainRestriction.equals("NO RESTRICTION"))
							{
								processSeed(outlink, pagesToCrawl, domainRestriction, visited);
							}
							else
							{
								//Check the domain restriction
								if(domainRestriction.indexOf(outlinkURL.getHost()) != -1)
								{
									//If the web server was the immediate previous one visited, then crawl the page after a second delay
									if(previousServer.equals(new URL(URL).getHost()))
									{
										try
										{
											//Politeness policy - 1second delay
											Thread.sleep(1000);
										}
										catch(InterruptedException t)
										{}
									}
									else
									{
										previousServer = new URL(URL).getHost();
									}
									
									processSeed(outlink, pagesToCrawl, domainRestriction, visited);
								}
							}	
						}			
					}
					catch(MalformedURLException ex)
					{
						//do nothing -> invalid starting URL
					}
					catch(IOException ex)
					{
						//System.out.println("Error in http request"+ex+" url:" + URL);
					}
				}
		}
			else
			{
				//url cannot be crawled as it is disallowed by robots.txt
				//System.out.println("[NOT SAFE ROBOTS.TXT]" + URL);
			}
		}
		catch(MalformedURLException malEx)
		{
			//do nothing -> invalid starting URL
		}
	}

}
