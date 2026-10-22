package com.cricketpulse.repo;

import com.cricketpulse.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByMatchIdOrderByIdAsc(Long matchId);
    List<Delivery> findByMatchIdAndInningsNumberOrderByIdAsc(Long matchId, int inningsNumber);
}
