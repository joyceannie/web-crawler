import java.io.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.util.concurrent.ConcurrentHashMap;

public class processingFiles {

	/**
	 *Aho-Corasick algorithm is used for linear time search.
	 adKeywordFilePath is present in the path "C:/Users/annie/AdBlockerKeywordList.txt".
	 To run the program, the path needs to be changed properly.
	 Path of inputDirectory and outputDirectory should be changed properly before running the code.
	 */
	private static ConcurrentHashMap<String,Integer> hmap=new ConcurrentHashMap<String,Integer>(); 
	private static int fileCount;
	

	private static void removeAd(Document doc1) throws IOException
	{
		//Removes ad by performing string matching using easylist commonly used by adblockers
		String adKeywordFilePath="C:/Users/annie/AdBlockerKeywordList.txt";
		AdUrlChecker adUrlChecker = new AdUrlChecker(adKeywordFilePath);
		 for (Element element :doc1.select("script")){                
			 String refLink = element.attr("src");
			 //String matching is done in linear time using Aho-Corasick algorithm
		     if(adUrlChecker.isAdUrl(refLink)){
		    	 element.remove();
		     }
		                
		  }
	}

	
	private static void mappingData(Document doc1)
	{
		//Maps text from all crawled pages to a concurrent hashmap
			for(Element div:doc1.select("div"))
			{
				if(hmap.containsKey(div.text())){
				hmap.put(div.text(), hmap.get(div.text())+1);	
				}
				else{
				hmap.put(div.text(), 1);
				}
			}	
			
			for (Element link:doc1.select("a[href]")){
				if(hmap.containsKey(link.text())){
				hmap.put(link.text(), hmap.get(link.text())+1);	
				}
				else{
				hmap.put(link.text(), 1);
				}	
			}
			
			for(Element ulTag:doc1.select("ul")){
				if(hmap.containsKey(ulTag.text())){
					hmap.put(ulTag.text(), hmap.get(ulTag.text())+1);	
				}
				else{
					hmap.put(ulTag.text(), 1);
				}
		}	
			
			
	}
	
	private static void updateHashmap(){
		//Removing text from hashmap if frequency is below a threshold
		for (String key:hmap.keySet()){
			if(hmap.containsKey(key) && hmap.get(key)<=fileCount/2){
				hmap.remove(key);
	
			}
		}
	
	}
	
	
	
	private static void removeNoise(Document doc1){
		//If the text in a particular tag is present in the hashmap, it is recognized as noise.
		for(Element div:doc1.select("div"))
		{
			if(hmap.containsKey(div.text())){
				div.remove();
			}
		}
		
		for(Element ulTag:doc1.select("ul")){
			if(hmap.containsKey(ulTag.text())){
				ulTag.remove();
			}
		}

		for(Element link:doc1.select("a"))
		{
			if(hmap.containsKey(link.text())){
				link.remove();
			}
		}
		
	}
	

	private static void createFile(Document doc1,int docCount){
		//Updated document is written to the output directory
		FileWriter fWriter = null;
		BufferedWriter writer =null;
		String outputDirectory="H:/SCU/Courses/web search/webCrawler/finalData/";
		File f = new File(outputDirectory);
		if(!f.exists())
			f.mkdirs();
		String filename = outputDirectory+"doc" + docCount + ".html";
		
		
	    try {
	    	fWriter = new FileWriter(filename);
	    	writer = new BufferedWriter(fWriter);
			writer.write(doc1.html());
			writer.close();
	    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
		
	
	public static void main(String[] args) throws IOException {

		 String inputDirectory="H:/SCU/Courses/web search/webCrawler/output/";
		 fileCount=new File(inputDirectory).list().length;
		//fileCount=3;
		 Document doc1;
	
		 //Reading all input files and mapping the text
		for (int i=1;i<=fileCount;++i){
			doc1 = Jsoup.parse(new File(inputDirectory+"doc"+i+".html"),"ISO-8859-1");
			mappingData(doc1);					
	
		}
		
		//Removing less frequent text from hashmap as it is main content
		updateHashmap();

		//Reading all the input files
		for (int i=1;i<=fileCount;i++){
			doc1 = Jsoup.parse(new File(inputDirectory+"doc"+i+".html"),"ISO-8859-1");
			//Hashmap contains noise part which is removed from the files
			removeNoise(doc1);
			
			//Removing all nav tags
			for(Element element : doc1.select("nav")){
				element.remove();		 
			}
			
			//Ads are removed
			removeAd(doc1);
			
			//Writing the modified document to disc
			createFile(doc1,i);	

		}
		
	}

}

