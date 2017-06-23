import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

/**
*
* Aho-Corasick algorithm is used for performing the search in linear time. 
* http://ahocorasick.org/
* 
*/

public class AdUrlChecker {

    final Trie adBlockerMatchTrie;

    public AdUrlChecker(String adBlockKeywordListFile) {
        List<String> adBlockerKeyWordList = new ArrayList<String>();
        adBlockerMatchTrie = new Trie().removeOverlaps();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(adBlockKeywordListFile));
            String adKeyword;
            while((adKeyword = reader.readLine()) != null) {
                adBlockerKeyWordList.add(adKeyword);
            }
            reader.close();
        } catch(Exception e) {
            System.out.println("Error !! ");
        }
        for (String adKeyword : adBlockerKeyWordList) {
            adBlockerMatchTrie.addKeyword(adKeyword);
        }
    }

    public AdUrlChecker(List<String> keywords) {
        adBlockerMatchTrie = new Trie().removeOverlaps();
        for (String adKeyword : keywords) {
            adBlockerMatchTrie.addKeyword(adKeyword);
        }
    }

    public boolean isAdUrl(String data) {
        Collection<Emit> matches = adBlockerMatchTrie.parseText(data);
        return matches.size() > 0;
    }

    public static void main(String[] args) {
      // AdUrlChecker adUrlChecker = new AdUrlChecker("C:/Users/annie/AdBlockerKeywordList.txt");
      // System.out.println(adUrlChecker.isAdUrl("https://adclick.g.doubleclick.net/pcs/click?xai=AKAOjsvla7PZovES0BXNreM3zAZgC6fEdkQ1uId3BynS4Wllp1fPz4u8FdcQ5U631D4J2cFZKVJuiRofKHWJTZejSdLPtppzsHogrdVwZZtc-pE_nR7_4fCB7ZvDJDayIw&sig=Cg0ArKJSzIFaGCTEr9kN&urlfix=1&adurl=https://adclick.g.doubleclick.net/aclk%3Fsa%3DL%26ai%3DCTseZTlcUV4jDKoOmnATewbHwBs2u0MkI3bGD344BwI23ARABIABgyeb-hsijoBmCARdjYS1wdWItNjc2ODIwOTEyMzE5NzU3MMgBCagDAaoElAFP0M44fTBV8fmHCwWbcWdmd4TlQ7fFHiosaVSCTdPOQ27fFnaEqbbyH9xFbz2_ua5sBJiGXcBwRYIIIg_jrVTUUtIjGht6YsI2c3tzpU-SnJ8s9H-ojkwPISR9v83N5faQtxUUdyvqlEi1OfWRLZebH8ReIUGkePNWWAA8pGtapHasnt7U40yML7iiQxR-BWfQr159gAbxq9iCkLOcmDKgBiGoB6a-G9gHAA%26num%3D1%26sig%3DAOD64_0ZJ41sc3Zl56CvYzoNLgDqwZtO1w%26client%3Dca-pub-6768209123197570%26adurl%3Dhttp://www.mccormick.com/PureTastesBetter%253Futm_medium%253Dbanner-ads%2526utm_source%253Dinvite%2526utm_term%253DMKC_Cinnamon_160x600.html%2526utm_content%253Dcategory%2526utm_campaign%253Dpurity-2016"));
    }
}
