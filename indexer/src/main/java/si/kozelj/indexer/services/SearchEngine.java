package si.kozelj.indexer.services;

import one.util.streamex.StreamEx;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import si.kozelj.indexer.models.Posting;
import si.kozelj.indexer.models.rest.ResultWrapper;
import si.kozelj.indexer.repositories.PostingRepository;

import java.io.IOException;
import java.util.*;

@Service
public class SearchEngine {

    @Autowired
    private PostingRepository postingRepository;

    @Autowired
    private FileImporter fileImporter;

    public List<ResultWrapper> getQueryResults(String query) throws IOException {
        Set<String> searchTerms = StreamEx.split(query, " ").map(String::toLowerCase).toSet();
        if (searchTerms.isEmpty()) {
            return new ArrayList<>();
        }

        // fetch target postings
        List<Posting> targetPostings = postingRepository.findTopFiveMatchingDocuments(searchTerms);
        Map<String, List<Posting>> postingsByDocumentNames = StreamEx.of(targetPostings).groupingBy(Posting::getDocumentName);


        // load web content from selected documents
        Map<String, String> contentByDocumentName = fileImporter.getContent(postingsByDocumentNames.keySet());

        List<ResultWrapper> resultWrappers = new ArrayList<>();
        for (String documentName : postingsByDocumentNames.keySet()) {
            // retrieve all indexes that index the word in the result of the tokenizer
            List<Integer> indices = StreamEx.of(postingsByDocumentNames.get(documentName))
                                            .map(Posting::getIndexes)
                                            .map(o -> o.split(","))
                                            .flatMap(Arrays::stream)
                                            .map(Integer::valueOf)
                                            .sorted()
                                            .toList();

            // group indexes (and respect, that each index wants to extend itself with 3 to the left and 3 to the right
            Deque<IndexSpan> spanStack = new ArrayDeque<>();
            for (Integer index : indices) {
                // if the first span, just put it on the stack
                if (spanStack.isEmpty()) {
                    spanStack.push(new IndexSpan(index));
                    continue;
                }

                // check if the new index span would overlap with the previous index span; if yes, merge them
                IndexSpan previous = spanStack.remove();
                if (previous.shouldMerge(index)) {
                    spanStack.push(previous.merge(index));
                } else {
                    spanStack.push(previous);
                    spanStack.push(new IndexSpan(index));
                }
            }

            String documentText = contentByDocumentName.get(documentName);

            // tokenize the text, but this time retrieve the substring index spans
            List<Span> tokenizedSpans = StreamEx.of(SimpleTokenizer.INSTANCE.tokenizePos(documentText)).toList();
            List<String> snippets = new ArrayList<>();
            for (IndexSpan indexSpan : StreamEx.of(spanStack.descendingIterator()).toList()) {
                // extract start of snippet
                Span firstSpan = tokenizedSpans.get(indexSpan.getStart());

                // extract end of snippet
                int finalSpanIndex = Math.min(tokenizedSpans.size() - 1, indexSpan.getEnd());
                Span lastSpan = tokenizedSpans.get(finalSpanIndex);

                // extract snippet
                snippets.add(documentText.substring(firstSpan.getStart(), lastSpan.getEnd()));
            }

            // join snippets with triple dots
            String documentSnippet = StreamEx.of(snippets).joining(" ... ");

            // if we don't display first word of text, prepend with dots
            if (spanStack.peekLast().getStart() == 0) {
                documentSnippet = "... " + documentSnippet;
            }

            // if we don' display last word of text, add dots at end
            if (spanStack.peekFirst().getEnd() < tokenizedSpans.size() - 1) {
                documentSnippet = documentSnippet + " ...";
            }

            // add to results
            resultWrappers.add(new ResultWrapper(documentName, indices.size(), documentSnippet));
        }

        return resultWrappers;
    }


    private class IndexSpan {
        private final Integer start;
        private final Integer end;

        IndexSpan(Integer start, Integer end) {
            this.start = start;
            this.end = end;
        }

        IndexSpan(Integer start) {
            this.start = Math.max(0, start - 3);
            this.end =  start + 3;
        }

        boolean shouldMerge(Integer nextIndex) {
            return end >= nextIndex - 3;
        }

        IndexSpan merge(Integer nextIndex) {
            return new IndexSpan(start, nextIndex + 3);
        }

        Integer getStart() {
            return start;
        }

        Integer getEnd() {
            return end;
        }

        @Override
        public String toString() {
            return "(" + start + "," + end + ")";
        }
    }
}
