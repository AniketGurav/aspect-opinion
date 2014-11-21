/**
 * Copyright (C) 2014 Pengfei Liu <pfliu@se.cuhk.edu.hk>
 * The Chinese University of Hong Kong.
 *
 * This file is part of aspect-opinion.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cuhk.hccl.util;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * Utility class to extract noun phrases, compute words similarity and word sentiment
 *
 */
public class NLPUtil {
    public static final Set<String> NN_TAGS = new HashSet<String>(Arrays.asList(new String[] { "NN", "NNPS", "NNS" }));
    public static final Set<String> JJ_TAGS = new HashSet<String>(Arrays.asList(new String[] { "JJ", "JJR", "JJS" }));
    
    public static final Set<String> NEGATIONS = new HashSet<String>(Arrays.asList(new String[] { "no", "not", "never", "neither", "none" }));
    public static final String NOT_PREFIX = "NOT_";
    
    public static int MAX_STEPS = 2;

    public static StanfordCoreNLP createCoreNLP() {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        props.put("pos.model", "taggers/english-left3words-distsim.tagger");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        System.out.println("Creating an instance of StanfordCoreNLP to tag reviews...");
        return pipeline;
    }

    private static void addPair(ArrayList<String[]> pairs, String adj, String noun) {
        int lastIdx = pairs.size() - 1;
        if (lastIdx < 0) {
            pairs.add(new String[] { adj, noun });
        } else {
            String[] lastPair = pairs.get(lastIdx);
            // combine two adjacent adjs or nouns
            if (lastPair[0].equals(adj)) {
                lastPair[1] = lastPair[1] + " " + noun;
            } else if (lastPair[1].equals(noun)) {
                lastPair[0] = lastPair[0] + " " + adj;
            } else {
                pairs.add(new String[] { adj, noun });
            }
        }
    }

    public static ArrayList<String[]> extractNounPhrases(StanfordCoreNLP pipeline, String text, int searchRange) {
        ArrayList<String[]> wordPairs = new ArrayList<String[]>();
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        
        MAX_STEPS = searchRange;
        
        for (CoreMap sentence : sentences) {
            List<CoreLabel> labels = sentence.get(TokensAnnotation.class);
            
            // Check negation
            boolean hasNegation = false;
            for (CoreLabel label : labels){
            	if (NEGATIONS.contains(label.lemma().toLowerCase())){
            		hasNegation = true;
            	}
            }
            
            for (int idx = 0; idx < labels.size(); idx++) {
                CoreLabel label = labels.get(idx);
                if (NN_TAGS.contains(label.get(PartOfSpeechAnnotation.class))) {
                    for (int step = 1; step <= MAX_STEPS; step++) {
                        CoreLabel leftLabel = labels.get(Math.max(0, idx - step));
                        if (JJ_TAGS.contains(leftLabel.tag())) {
                        	if (hasNegation)
                        		addPair(wordPairs, NOT_PREFIX + leftLabel.get(LemmaAnnotation.class), label.get(LemmaAnnotation.class));
                        	else
                        		addPair(wordPairs, leftLabel.get(LemmaAnnotation.class), label.get(LemmaAnnotation.class));
                            break;
                        }
                        CoreLabel rightLabel = labels.get(Math.min(idx + step, labels.size() - 1));
                        if (JJ_TAGS.contains(rightLabel.tag())) {
                        	if (hasNegation)
                        		addPair(wordPairs, NOT_PREFIX + rightLabel.get(LemmaAnnotation.class), label.get(LemmaAnnotation.class));
                        	else
                        		addPair(wordPairs, rightLabel.get(LemmaAnnotation.class), label.get(LemmaAnnotation.class));
                            
                        	break;
                        }
                    }
                }
            }
        }
        return wordPairs;
    }

    public static RelatednessCalculator createCalculator() {
        ILexicalDatabase db = new NictWordNet();
        WS4JConfiguration.getInstance().setMFS(true);
        System.out.println("Creating an instance of WuPalmer to calculate similarity between words...");
        return new WuPalmer(db);
    }
    
    public static String StreamTokenizer(StringReader reader)throws IOException {
		
		StringBuilder buffer = new StringBuilder();

		StreamTokenizer tokenizer = new StreamTokenizer(reader);
		tokenizer.lowerCaseMode(true);
		tokenizer.eolIsSignificant(false);
		tokenizer.whitespaceChars('.', '.');

		while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
			switch (tokenizer.ttype) {
			case StreamTokenizer.TT_WORD:
				buffer.append(tokenizer.sval + " ");
				break;
			case StreamTokenizer.TT_NUMBER:
				buffer.append(tokenizer.nval + " ");
				break;
			case StreamTokenizer.TT_EOL:
				break;
			default:
				break;
			}
		}

		return buffer.toString();
	}
}
