# Elasticsearch Turkish Stemmer Ingest Processor

Explain the use case of this processor in a TLDR fashion.

## Usage


```
PUT _ingest/pipeline/turkish-stemmer-pipeline
{
  "description": "A pipeline to do whatever",
  "processors": [
    {
      "turkish_stemmer" : {
        "field" : "my_field"
      }
    }
  ]
}

PUT /my-index/my-type/1?pipeline=turkish-stemmer-pipeline
{
  "my_field" : "Some content"
}

GET /my-index/my-type/1
{
  "my_field" : "Some content"
  "potentially_enriched_field": "potentially_enriched_value"
}
```

### Simulate on Kibana

```
GET _ingest/pipeline


PUT _ingest/pipeline/zemberek_test
{
  "description" : "Zemberek test",
  "processors" : [
    {
      "turkish_stemmer" : {
        "field": "name",
        "target_field": "name_stem",
        "store_original": false,
        "word_generation": false
      }
    }
  ]
}



GET _ingest/pipeline/zemberek_test/_simulate
{
  "docs": [
    {
      "_index": "index",
      "_id": "id",
      "_source": {
        "name": "domatesçiler"
      }
    }
  ]
}
```

## Configuration

| Parameter | Use |
| --- | --- |
| some.setting   | Configure x |
| other.setting  | Configure y |

## Setup

In order to install this plugin, you need to create a zip distribution first by running

```bash
gradle clean check
```

This will produce a zip file in `build/distributions`.

After building the zip file, you can install it like this

```bash
bin/elasticsearch-plugin install file:///path/to/ingest-turkish-stemmer/build/distribution/ingest-turkish-stemmer-0.0.1-SNAPSHOT.zip
```

## Build

```
./gradlew idea
./gradlew clean
./gradlew --stop
./gradlew build
```

## Bugs & TODO

* [x] add a tokenizer for the pipeline processor (TurkishTokenizer)
* [ ] put the normalization (normalizer.normalize)
* [x] put the morphological analysis for the words
* [x] we can use morphology.getWordGenerator() to generate similar words


