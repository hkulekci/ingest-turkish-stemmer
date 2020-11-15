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

import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
// ES permission you should check before doPrivileged() blocks
import org.elasticsearch.SpecialPermission;
import zemberek.morphology.TurkishMorphology;
import zemberek.morphology.analysis.SingleAnalysis;
import zemberek.morphology.analysis.WordAnalysis;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.ingest.ConfigurationUtils.readStringProperty;

public class TurkishStemmerProcessor extends AbstractProcessor {

    public static final String TYPE = "turkish_stemmer";

    private final String field;
    private final String targetField;

    public TurkishStemmerProcessor(String tag, String description, String field,
                 String targetField) throws IOException {
        super(tag, description);
        this.field = field;
        this.targetField = targetField;
    }

    private List<String> getLemmas(String content) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            // unprivileged code such as scripts do not have SpecialPermission
            sm.checkPermission(new SpecialPermission());
        }
        return AccessController.doPrivileged(new PrivilegedAction<List<String>>() {
            public List<String> run() {

                List<String> resultContent = new java.util.ArrayList<>(Collections.emptyList());
                TurkishMorphology morphology = TurkishMorphology.createWithDefaults();

                WordAnalysis results = morphology.analyze(content);
                for (SingleAnalysis result : results) {
                    String lemma = result.getLemmas().get(0);
                    if (!resultContent.contains(lemma)) {
                        resultContent.add(lemma);
                    }
                }

                return resultContent;
            }
        });
    }

    @Override
    public IngestDocument execute(IngestDocument ingestDocument) throws Exception {
        String content = ingestDocument.getFieldValue(field, String.class);

        ingestDocument.setFieldValue(targetField, String.join(" ", this.getLemmas(content)));
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

            return new TurkishStemmerProcessor(tag, description, field, targetField);
        }
    }
}
