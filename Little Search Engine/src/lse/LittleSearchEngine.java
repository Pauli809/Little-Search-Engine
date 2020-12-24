package lse;

import java.io.*;

import java.util.*;

/**
 * This class builds an index of keywords. Each keyword maps to a set of pages in
 * which it occurs, with frequency of occurrence in each page.
 *
 */
public class LittleSearchEngine {

	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in 
	 * DESCENDING order of frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;

	/**
	 * The hash set of all noise words.
	 */
	HashSet<String> noiseWords;

	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashSet<String>(100,2.0f);
	}

	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeywordsFromDocument(String docFile) 
			throws FileNotFoundException {
		HashMap<String,Occurrence> toReturn = new HashMap<String,Occurrence>();
		Scanner scanner = new Scanner(new File(docFile));
		while(scanner.hasNext()) { 
			String key = getKeyword(scanner.next());
			if(key != null) { 
				if(!toReturn.containsKey(key)) {
					toReturn.put(key, new Occurrence(docFile, 1));
				}
				else { 
					toReturn.get(key).frequency++;
				}
			}
		}
		return toReturn;
	}

	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeywords(HashMap<String,Occurrence> kws) {
		ArrayList<Occurrence> chains;
		Map<String,Occurrence> map = kws;
		for (Map.Entry<String,Occurrence> entry : map.entrySet()) {
			if(keywordsIndex.containsKey(entry.getKey())) {
				keywordsIndex.get(entry.getKey()).add(entry.getValue());
				insertLastOccurrence(keywordsIndex.get(entry.getKey())); 
			}
			else {
				chains = new ArrayList<>();
				chains.add(entry.getValue());
				keywordsIndex.put(entry.getKey(), chains);
			}
		} 		
	}

	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * trailing punctuation(s), consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * NO OTHER CHARACTER SHOULD COUNT AS PUNCTUATION
	 * 
	 * If a word has multiple trailing punctuation characters, they must all be stripped
	 * So "word!!" will become "word", and "word?!?!" will also become "word"
	 * 
	 * See assignment description for examples
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyword(String word) {
		String wordtoTest = word;
		String result = wordtoTest.toLowerCase();
		char[] resultArray = result.toCharArray();

		int finalcharIndex = -1;
		int firstpIndex = -1;
		for(int i = 0; i < resultArray.length; i++) { 
			if(resultArray[i] >= 'a' && resultArray[i] <= 'z') {
				finalcharIndex = i;
			}
			else { 
				if(firstpIndex == -1) { 
					firstpIndex = i;
				}
			}
		}
		if(finalcharIndex == -1) { 
			return null;
		}
		if(firstpIndex < finalcharIndex && firstpIndex != -1) { 
			return null;
		}
		else { 
			if(result.indexOf('.') != -1) { 
				result = result.replace(".", "");
			}
			if(result.indexOf(',') != -1) { 
				result = result.replace(",", "");
			}
			if(result.indexOf('?') != -1) { 
				result = result.replace("?", "");
			}
			if(result.indexOf(':') != -1) { 
				result = result.replace(":", "");
			}
			if(result.indexOf(';') != -1) { 
				result = result.replace(";", "");
			}
			if(result.indexOf('!') != -1) { 
				result = result.replace("!", "");
			}
		}

		if(result.length() != finalcharIndex+1) {
			return null;
		}

		if(noiseWords.contains(result)) { 
			return null;
		}

		return result;

	}

	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion is done by
	 * first finding the correct spot using binary search, then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		/** COMPLETE THIS METHOD **/
		if(occs.size() == 1) { 
			return null;
		}
		ArrayList<Integer> mids = new ArrayList<>();
		Occurrence hold = occs.get(occs.size()-1);
		int hi= occs.size()-2;
		int lo = 0;
		int valuetoInsert = occs.get(occs.size()-1).frequency;
		while(lo < hi) { 
			int mid = (hi+lo)/2;
			mids.add(mid);
			if(valuetoInsert == occs.get(mid).frequency) {
				if(mid ==  occs.size()-2) {
					return mids;
				}
				int n = mid;
				while(occs.get(n).frequency == valuetoInsert) { 
					if(n == occs.size()-2) {
						return mids;
					}
					n++;
				}
				occs.remove(occs.size()-1);
				occs.add(n, hold);
				return mids;
			}
			if(valuetoInsert < occs.get(mid).frequency) {
				lo = mid+1;
			}
			else { 
				hi = mid-1;
			}
		}

		int mid = (lo+hi)/2;
		if(mid != 0) {
			mids.add(mid);
		}
		if(occs.get(mid).frequency < valuetoInsert) {
			occs.remove(occs.size()-1);
			occs.add(mid, hold);
		}
		else { 
			occs.remove(occs.size()-1);
			occs.add(mid+1, hold);
		}

		return mids;
	}

	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
			throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.add(word);
		}

		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeywordsFromDocument(docFile);
			mergeKeywords(kws);
		}
		sc.close();
	}

	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of document frequencies. 
	 * 
	 * Note that a matching document will only appear once in the result. 
	 * 
	 * Ties in frequency values are broken in favor of the first keyword. 
	 * That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2 also with the same 
	 * frequency f1, then doc1 will take precedence over doc2 in the result. 
	 * 
	 * The result set is limited to 5 entries. If there are no matches at all, result is null.
	 * 
	 * See assignment description for examples
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matches, 
	 *         returns null or empty array list.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		/** COMPLETE THIS METHOD **/
		ArrayList<String> orderDocs = new ArrayList<>();

		if(!keywordsIndex.containsKey(kw1) && !keywordsIndex.containsKey(kw2)) {
			return orderDocs;
		}


		if(!keywordsIndex.containsKey(kw1) || !keywordsIndex.containsKey(kw2)) {
			ArrayList<Occurrence> temp = keywordsIndex.containsKey(kw1) && !keywordsIndex.containsKey(kw2) ?  
					keywordsIndex.get(kw1) : keywordsIndex.get(kw2);

					int topSearch = 0;
					int ptr = 0;
					while(topSearch < 5) { 
						if(ptr < temp.size()) {
							if(orderDocs.isEmpty()) {
								orderDocs.add(keywordsIndex.get(kw1).get(ptr).document);
								ptr++;
								topSearch++;
								continue; 
							}
							boolean goodtoAdd = true;
							for(String q: orderDocs) {
								if(q.equals(keywordsIndex.get(kw1).get(ptr).document)) {
									goodtoAdd = false;
									break;
								}
							}
							if(goodtoAdd) { 
								orderDocs.add(keywordsIndex.get(kw1).get(ptr).document);
								ptr++;
								topSearch++;
								continue; 
							}
							else { 
								ptr++;
								continue;
							}
						}
					}
					return orderDocs;
		}



		int top5 = 0;
		int kw1Pointer = 0;
		int kw2Pointer = 0;

		while(top5 < 5) { 
			if(orderDocs.isEmpty()) { 
				char[] var = {'>' , '<', '='};
				char temp = (keywordsIndex.get(kw1).get(kw1Pointer).frequency > keywordsIndex.get(kw2).get(kw2Pointer).frequency) ? var[0] :
					(keywordsIndex.get(kw1).get(kw1Pointer).frequency < keywordsIndex.get(kw2).get(kw2Pointer).frequency ? var[1] : var[2]);
				switch(temp) { 
				case '>': 
					orderDocs.add(keywordsIndex.get(kw1).get(kw1Pointer).document);
					kw1Pointer++;
					top5++;
					break;
				case '<': 
					orderDocs.add(keywordsIndex.get(kw2).get(kw2Pointer).document);
					kw2Pointer++;
					top5++;
					break;
				default: 
					orderDocs.add(keywordsIndex.get(kw1).get(kw1Pointer).document);
					kw1Pointer++;
					kw2Pointer++;
					top5++;
					break;
				}
			}
			else { 
				if(kw1Pointer < keywordsIndex.get(kw1).size() || kw2Pointer < keywordsIndex.get(kw2).size()) {
					if(kw2Pointer >= keywordsIndex.get(kw2).size()) {
						boolean goodtoAdd = true;
						for(String q: orderDocs) {
							if(q.equals(keywordsIndex.get(kw1).get(kw1Pointer).document)) {
								goodtoAdd = false;
								break;
							}
						}
						if(goodtoAdd) { 
							orderDocs.add(keywordsIndex.get(kw1).get(kw1Pointer).document);
							kw1Pointer++;
							top5++;
							continue; 
						}
						else { 
							kw1Pointer++;
							continue;
						}
					}
					if(kw1Pointer >= keywordsIndex.get(kw1).size()) {
						boolean goodtoAdd = true;
						for(String q: orderDocs) {
							if(q.equals(keywordsIndex.get(kw2).get(kw2Pointer).document)) {
								goodtoAdd = false;
								break;
							}
						}
						if(goodtoAdd) { 
							orderDocs.add(keywordsIndex.get(kw2).get(kw2Pointer).document);
							kw2Pointer++;
							top5++;
							continue; 
						}
						else { 
							kw2Pointer++;
							continue;
						}
					}
					Occurrence currentkw1 = keywordsIndex.get(kw1).get(kw1Pointer);
					Occurrence currentkw2 = keywordsIndex.get(kw2).get(kw2Pointer);
					char[] var = {'>' , '<', '='};
					char temp = (currentkw1.frequency > currentkw2.frequency) ? var[0] :
						(currentkw1.frequency < currentkw2.frequency ? var[1] : var[2]);
					boolean goodtoAdd = true;
					switch(temp) { 
					case '>': 
						goodtoAdd = true;
						for(String q: orderDocs) {
							if(q.equals(keywordsIndex.get(kw1).get(kw1Pointer).document)) {
								goodtoAdd = false;
								break;
							}
						}
						if(goodtoAdd) { 
							orderDocs.add(keywordsIndex.get(kw1).get(kw1Pointer).document);
							kw1Pointer++;
							top5++;
							break; 
						}
						kw1Pointer++;
						break;
					case '<': 
						goodtoAdd = true;
						for(String q: orderDocs) {
							if(q.equals(keywordsIndex.get(kw2).get(kw2Pointer).document)) {
								goodtoAdd = false;
								break;
							}
						}
						if(goodtoAdd) { 
							orderDocs.add(keywordsIndex.get(kw2).get(kw2Pointer).document);
							kw2Pointer++;
							top5++;
							break; 
						}
						kw2Pointer++;
						break;
					default: 
						goodtoAdd = true;
						for(String q: orderDocs) {
							if(q.equals(keywordsIndex.get(kw1).get(kw1Pointer).document)) {
								goodtoAdd = false;
								break;
							}
						}
						if(goodtoAdd) { 
							orderDocs.add(keywordsIndex.get(kw1).get(kw1Pointer).document);
							kw1Pointer++;
							kw2Pointer++;
							top5++;
							break; 
						}
						kw1Pointer++;
						kw2Pointer++;
						break;
					}
				}
				else { 
					break;
				}
			}

		}

		return orderDocs;
	}
}





















