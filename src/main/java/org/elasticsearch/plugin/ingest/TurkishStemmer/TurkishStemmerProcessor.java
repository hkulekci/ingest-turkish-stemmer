/*
 * Copyright [2020] [Haydar KULEKCI]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.elasticsearch.plugin.ingest.TurkishStemmer;

import org.elasticsearch.SpecialPermission;
import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
import zemberek.morphology.TurkishMorphology;
import zemberek.morphology.analysis.SingleAnalysis;
import zemberek.morphology.analysis.WordAnalysis;
import zemberek.morphology.generator.WordGenerator;
import zemberek.morphology.lexicon.DictionaryItem;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.ingest.ConfigurationUtils.readBooleanProperty;
import static org.elasticsearch.ingest.ConfigurationUtils.readStringProperty;

public class TurkishStemmerProcessor extends AbstractProcessor {

    public static final String TYPE = "turkish_stemmer";

    private final String field;
    private final String targetField;
    private final Boolean storeOriginal;
    private String delimiter = " ";
    private Boolean wordGeneration = false;

    public TurkishStemmerProcessor(String tag, String description, String field,
                 String targetField, Boolean storeOriginal) throws IOException {
        super(tag, description);
        this.field = field;
        this.targetField = targetField;
        this.storeOriginal = storeOriginal;
    }

    public void setDelimiter(String delimiter)
    {
        this.delimiter = delimiter;
    }

    public void setWordGeneration(Boolean wordGeneration)
    {
        this.wordGeneration = wordGeneration;
    }

    private List<String> getLemmas(List<String> terms) {
        SecurityManager sm = System.getSecurityManager();
        Boolean storeOriginal = this.storeOriginal;
        Boolean wordGeneration = this.wordGeneration;
        if (sm != null) {
            // unprivileged code such as scripts do not have SpecialPermission
            sm.checkPermission(new SpecialPermission());
        }
        return AccessController.doPrivileged(new PrivilegedAction<List<String>>() {
            public List<String> run() {
                List<String> resultContent = new java.util.ArrayList<>(Collections.emptyList());
                TurkishMorphology morphology = TurkishMorphology.createWithDefaults();
                for (String term: terms ) {
                    if (!resultContent.contains(term) && storeOriginal) {
                        resultContent.add(term);
                    }
                    WordAnalysis results = morphology.analyze(term);
                    for (SingleAnalysis result : results) {
                        String lemma = result.getLemmas().get(0);
                        if (!resultContent.contains(lemma)) {
                            resultContent.add(lemma);
                        }
                    }

                    if (wordGeneration) {
                        String[] number = {"A3sg", "A3pl"};
                        String[] possessives = {"P1sg", "P2sg", "P3sg"};
                        String[] cases = {"Dat", "Loc", "Abl"};


                        TurkishMorphology wordGenerationMorphology =
                                TurkishMorphology.builder().setLexicon(term).disableCache().build();

                        DictionaryItem item = wordGenerationMorphology.getLexicon().getMatchingItems(term).get(0);
                        for (String numberM : number) {
                            for (String possessiveM : possessives) {
                                for (String caseM : cases) {
                                    List<WordGenerator.Result> wordGenerationResults =
                                            wordGenerationMorphology.getWordGenerator().generate(
                                                    item, numberM, possessiveM, caseM
                                            );
                                    wordGenerationResults.forEach(s -> {
                                        if (!resultContent.contains(s.surface)) {
                                            resultContent.add(s.surface);
                                        }
                                    });
                                }
                            }
                        }
                    }
                }

                return resultContent;
            }
        });
    }

    @Override
    public IngestDocument execute(IngestDocument ingestDocument) throws Exception {
        String content = ingestDocument.getFieldValue(field, String.class);
        List<String> terms = Arrays.asList(content.split(this.delimiter));

        ingestDocument.setFieldValue(targetField, String.join(" ", this.getLemmas(terms)));
        return ingestDocument;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory implements Processor.Factory {

        @Override
        public TurkishStemmerProcessor create(Map<String, Processor.Factory> factories, String tag,
               String description, Map<String, Object> config) throws Exception {
            String field = readStringProperty(TYPE, tag, config, "field");
            String targetField = readStringProperty(TYPE, tag, config, "target_field", "default_field_name");
            Boolean storeOriginal = readBooleanProperty(TYPE, tag, config, "store_original", true);
            String delimiter = readStringProperty(TYPE, tag, config, "delimiter", " ");
            Boolean wordGeneration = readBooleanProperty(TYPE, tag, config, "word_generation", false);

            TurkishStemmerProcessor tsp = new TurkishStemmerProcessor(tag, description, field, targetField, storeOriginal);
            tsp.setDelimiter(delimiter);
            tsp.setWordGeneration(wordGeneration);

            return tsp;
        }
    }
}
