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

import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.RandomDocumentPicks;
import org.elasticsearch.test.ESTestCase;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;

@ThreadLeakScope(ThreadLeakScope.Scope.NONE)
public class TurkishStemmerProcessorTests extends ESTestCase {

    public void testThatProcessorWorks() throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put("source_field", "kutucuk");
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random(), document);

        TurkishStemmerProcessor processor = new TurkishStemmerProcessor(
                randomAlphaOfLength(10), "description", "source_field", "target_field", true
        );
        Map<String, Object> data = processor.execute(ingestDocument).getSourceAndMetadata();

        assertThat(data, hasKey("target_field"));
        assertThat(data.get("target_field"), is("kutucuk kutu"));
    }

    public void testThatProcessorWorksWithoutOriginalValue() throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put("source_field", "kutucuk");
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random(), document);

        TurkishStemmerProcessor processor = new TurkishStemmerProcessor(
                randomAlphaOfLength(10), "description", "source_field", "target_field", false
        );
        Map<String, Object> data = processor.execute(ingestDocument).getSourceAndMetadata();

        assertThat(data, hasKey("target_field"));
        assertThat(data.get("target_field"), is("kutu"));
    }

    public void testThatProcessorWorksWithMultipleKeyword() throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put("source_field", "kutucuk güzel bir objedir");
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random(), document);

        TurkishStemmerProcessor processor = new TurkishStemmerProcessor(
                randomAlphaOfLength(10), "description", "source_field", "target_field", true
        );
        Map<String, Object> data = processor.execute(ingestDocument).getSourceAndMetadata();

        assertThat(data, hasKey("target_field"));
        assertThat(data.get("target_field"), is("kutucuk kutu güzel bir objedir obje"));
    }

    public void testThatProcessorWorksWithDifferentDelimiter() throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put("source_field", "kutucuk-niğdeli");
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random(), document);

        TurkishStemmerProcessor processor = new TurkishStemmerProcessor(
                randomAlphaOfLength(10), "description", "source_field", "target_field", false
        );
        processor.setDelimiter("-");
        Map<String, Object> data = processor.execute(ingestDocument).getSourceAndMetadata();

        assertThat(data, hasKey("target_field"));
        assertThat(data.get("target_field"), is("kutu niğde"));
    }

    public void testThatProcessorWorksWithWordGeneration() throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put("source_field", "kutucuk");
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random(), document);

        TurkishStemmerProcessor processor = new TurkishStemmerProcessor(
                randomAlphaOfLength(10), "description", "source_field", "target_field", false
        );
        processor.setWordGeneration(true);
        Map<String, Object> data = processor.execute(ingestDocument).getSourceAndMetadata();

        assertThat(data, hasKey("target_field"));
        assertThat(data.get("target_field"), is("kutu kutucuğuma kutucuğumda kutucuğumdan kutucuğuna kutucuğunda " +
                "kutucuğundan kutucuklarıma kutucuklarımda kutucuklarımdan kutucuklarına kutucuklarında kutucuklarından"));
    }
}

