package com.sloyardms.stashboxapi.domain.tag.cron;

import com.sloyardms.stashboxapi.domain.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Safety net for the tag_usage table.
 *
 * tag_usage.item_count is maintained incrementally by DB triggers (see
 * {@code V3__add_tag_usage_triggers.sql}). This job periodically recomputes it from the
 * source of truth so any drift, from a trigger gap, a bulk operation, or a manual DB
 * change self-heals within a day instead of accumulating silently.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TagUsageReconciliationJob {

    private final TagRepository tagRepository;

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void reconcileTagUsage() {
        log.info("Starting tag_usage reconciliation job");

        int backfilled = tagRepository.backfillMissingTagUsageRows();
        int corrected = tagRepository.reconcileTagUsageCounts();

        if (backfilled > 0 || corrected > 0) {
            log.warn("tag_usage reconciliation complete - {} missing row(s) created, {} count(s) corrected",
                    backfilled, corrected);
        } else {
            log.info("tag_usage reconciliation complete - no drift detected");
        }
    }
}
