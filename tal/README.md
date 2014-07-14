Twitter-analysis-language
=========================

# Infrastructure

A conceptual analysis language for Twitter

The comments from Fabian on Jan. 27th

this.nSematics := enrich.count(this.semantics)
this.hasHashtags := this.nHashtags > 0
this.query.relevance := ml.classification(this, RELEVANCE.model, ...)
item.newAttribute := function(arg1, arg2, ...)
filteredItems := filter(twitter.sample, filterFunction(item) := { item.lang == "en"})
tweetPairs := createPairs(twitter.source)
tweetPairs := [{"tweetA":{...}, "tweetB": {}},   {"tweetA":{...}, "tweetB": {}},   {"tweetA":{...}, "tweetB": {}}, ...]
tweetPairs := [{"tweetA":{"id": 23, ..}, "tweetB": {"id": 24}},   {"tweetA":{"id": 23}, "tweetB": {"id": 25}},   {"tweetA":{...}, "tweetB": {}}, ...]
tweet.duplicateCandidates := [{"id": 24, ..}, {"id": 25, ...}, ...]
item.newAttribute := function(arg1, arg2, ...)
item.newAttribute = arg1 function arg2

Todo list in Section 4:
- Basic Data Model: Given the requirements from exsiting use cases, what are the essential elements in the basic data model? (Also depends on Twitter streaming API)
- 4.2 Acquiring data from source, e.g. this := source.filter(...) / source.search(keywords...)
- 4.3 Filter, e.g. this := this.filter(CONDITION)
- 4.4 Construction, e.g. this.[NEW ATTRIBUTE] := [EXPRESSION] - 1) Enrichment 2) Transformation 3) Machine Learning
