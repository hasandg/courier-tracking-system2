package com.hasandag.courier.tracking.system.store.repository;

import com.hasandag.courier.tracking.system.store.model.Store;
import com.hasandag.courier.tracking.system.store.model.StoreEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreEntryRepository extends JpaRepository<StoreEntry, Long> {

    List<StoreEntry> findByCourierIdOrderByEntryTimeDesc(String courierId);

    Page<StoreEntry> findByCourierIdOrderByEntryTimeDesc(String courierId, Pageable pageable);

    Page<StoreEntry> findByStoreOrderByEntryTimeDesc(Store store, Pageable pageable);
}
