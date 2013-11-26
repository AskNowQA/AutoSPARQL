package org.aksw.autosparql.commons.nlp.wordnet;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

public class WordNet {

	public IDictionary dict;	

	public WordNet()
	{		
		URL url = this.getClass().getClassLoader().getResource("models/en/wordnet/dict");
		dict = new RAMDictionary(url, ILoadPolicy.NO_LOAD);
		try{dict.open();} catch(IOException e) {throw new RuntimeException("couldn't open dictionary",e);}
	}

	public List<String> getBestSynonyms(POS pos, String s)
	{		
		List<String> synonyms = new ArrayList<String>();
		IIndexWord iw = dict.getIndexWord(s,pos);//dict.getMorphologicalProcessor().lookupBaseForm(pos, s)
		//			IndexWord iw = dict.getMorphologicalProcessor().lookupBaseForm(pos, s);
		if(iw != null)
		{
			IWordID wordID = iw.getWordIDs().get(0); 
			IWord word = dict.getWord(wordID);
			ISynset synset = word.getSynset () ;
			// iterate over words associated with the syns
			for ( IWord w : synset.getWords () )
			{
				String c = w.getLemma();
				if (!c.equals(s) && !c.contains(" ") && synonyms.size() < 4) {synonyms.add(c);}
			}
		}
		return synonyms;
	}

	/** is this correct?*/
	public List<String> getAttributes(String s)
	{
		List<String> result = new ArrayList<String>();
		return getBestSynonyms(POS.ADJECTIVE, s);
	}

	/**
	 * Funktion returns a List of Hypo and Hypernyms of a given string 
	 * @param s Word for which you want to get Hypo and Hypersyms
	 * @return List of Hypo and Hypernyms
	 * @throws JWNLException
	 */
	public List<String> getRelatedNouns(String s)
	{
		List<String> result = new ArrayList<String>();
		IIndexWord iw = null;
		Synset sense=null;	
		iw=dict.getIndexWord(s,POS.NOUN);
		if(iw!=null){

			IWordID wordID = iw.getWordIDs().get(0); 
			IWord word = dict.getWord(wordID);
			ISynset synset = word.getSynset();

			List<ISynsetID> relatedSynsets = synset.getRelatedSynsets(Pointer.HYPERNYM);
			relatedSynsets.addAll(synset.getRelatedSynsets(Pointer.HYPONYM));

			List <IWord> words;
			for (ISynsetID sid : relatedSynsets) {
				words = dict.getSynset(sid ).getWords () ;
				for(Iterator < IWord > i = words.iterator () ; i.hasNext () ;)
				{
					result.add(i.next().getLemma());				      
				}				      
			}
		}
		return result;
	}

}