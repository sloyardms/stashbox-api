package com.sloyardms.stashboxapi.domain.stash.service;

import com.sloyardms.stashboxapi.domain.stash.dto.response.StashItemDetailResponse;
import com.sloyardms.stashboxapi.domain.stash.dto.response.StashItemSummaryResponse;
import com.sloyardms.stashboxapi.domain.stash.mapper.StashItemMapper;
import com.sloyardms.stashboxapi.domain.stash.model.StashItem;
import com.sloyardms.stashboxapi.domain.stash.repository.StashItemRepository;
import com.sloyardms.stashboxapi.domain.tag.dto.response.TagCountResponse;
import com.sloyardms.stashboxapi.domain.tag.mapper.TagMapper;
import com.sloyardms.stashboxapi.domain.tag.repository.TagRepository;
import com.sloyardms.stashboxapi.shared.exception.types.ResourceNotFoundException;
import com.sloyardms.stashboxapi.shared.utils.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class StashItemSearchService {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^\\p{L}\\p{N}]+");
    private static final int MAX_QUERY_LENGTH = 100;
    private static final int MAX_TAG_COUNT = 20;

    private static final Map<String, String> BASE_SORT_MAPPINGS = Map.of(
            "title", "title",
            "url", "url",
            "description", "description",
            "createdAt", "created_at",
            "updatedAt", "updated_at"
    );
    private static final Map<String, String> SEARCH_SORT_MAPPINGS;
    static {
        Map<String, String> m = new HashMap<>(BASE_SORT_MAPPINGS);
        m.put("relevance", "search_rank");
        SEARCH_SORT_MAPPINGS = Map.copyOf(m);
    }

    private final StashItemRepository stashItemRepository;
    private final StashItemMapper stashItemMapper;
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Transactional(readOnly = true)
    public StashItemDetailResponse findById(UUID userId, UUID stashItemId){
        StashItem stashItem = stashItemRepository.findByIdAndUserId(stashItemId, userId)
                .orElseThrow(()-> new ResourceNotFoundException("StashItem", "id",  stashItemId));
        StashItemDetailResponse response =
                stashItemMapper.toDetailResponse(stashItem);

        List<TagCountResponse> tags =
                tagRepository.findTagsWithCountForStashItem(stashItem.getId())
                        .stream()
                        .map(tagMapper::toCountResponse)
                        .toList();
        response.setTags(tags);
        return response;
    }

    @Transactional(readOnly = true)
    public Page<StashItemSummaryResponse> list(UUID userId, String groupSlug, String rawTags, Pageable pageable){
        List<String> tagSlugs = parseTagSlugs(rawTags);
        String tagSlugsCsv = tagSlugs.isEmpty()?null: String.join(",",tagSlugs);

        Pageable mappedPageable = PageableUtils.remapSort(pageable, BASE_SORT_MAPPINGS);
        return stashItemRepository.listInGroup(userId, groupSlug, tagSlugsCsv, mappedPageable)
                .map(stashItemMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<StashItemSummaryResponse> search(UUID userId, String groupSlug, String rawQuery, String rawTags, Pageable pageable){
        List<String> tokens = tokenize(rawQuery);
        if(tokens.isEmpty()){
            throw new IllegalArgumentException("search requrires a non-blank query");
        }

        List<String> tagSlugs = parseTagSlugs(rawTags);
        String tagSlugsCsv = tagSlugs.isEmpty() ? null : String.join(",", tagSlugs);

        Pageable mapped = PageableUtils.remapSort(pageable, SEARCH_SORT_MAPPINGS);
        String tsQuery = toPrefixTsQuery(tokens);
        return stashItemRepository.searchInGroup(userId, groupSlug, tsQuery, tagSlugsCsv, mapped)
                .map(stashItemMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<StashItemSummaryResponse> searchDeleted(UUID userId, String rawQuery,Pageable pageable){
        List<String> tokens = tokenize(rawQuery);
        if(tokens.isEmpty()){
            throw new IllegalArgumentException("search requrires a non-blank query");
        }
        Pageable mapped = PageableUtils.remapSort(pageable, SEARCH_SORT_MAPPINGS);
        String tsQuery = toPrefixTsQuery(tokens);
        return stashItemRepository.searchInDeleted(userId, tsQuery, mapped)
                .map(stashItemMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<StashItemSummaryResponse> listDeleted(UUID userId, Pageable pageable){
        Pageable mappedPageable = PageableUtils.remapSort(pageable, BASE_SORT_MAPPINGS);
        return stashItemRepository.listInDeleted(userId, mappedPageable)
                .map(stashItemMapper::toSummaryResponse);
    }

    // HELPER METHODS

    private List<String> parseTagSlugs(String rawTags) {
        if (rawTags == null || rawTags.isBlank()) return List.of();
        return Arrays.stream(rawTags.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isBlank() && s.matches("[a-z0-9-]+"))
                .distinct()
                .limit(MAX_TAG_COUNT)
                .toList();
    }

    private List<String> tokenize(String rawQuery) {
        if (rawQuery == null) return List.of();
        String trimmed = rawQuery.strip();
        if (trimmed.length() > MAX_QUERY_LENGTH) {
            trimmed = trimmed.substring(0, MAX_QUERY_LENGTH);
        }
        return Arrays.stream(trimmed.split("\\s+"))
                .map(t -> NON_ALPHANUMERIC.matcher(t).replaceAll(""))
                .filter(t -> !t.isBlank())
                .toList();
    }

    private String toPrefixTsQuery(List<String> tokens) {
        List<String> copy = new ArrayList<>(tokens);
        int last = copy.size() - 1;
        copy.set(last, copy.get(last) + ":*");
        return String.join(" & ", copy);
    }
}
