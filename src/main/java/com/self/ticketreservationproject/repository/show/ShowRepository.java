package com.self.ticketreservationproject.repository.show;

import com.self.ticketreservationproject.domain.show.ShowInfo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowRepository extends JpaRepository<ShowInfo, Integer> {
  boolean existsByTitle(String title);

  Optional<ShowInfo> findById(Long id);

  List<ShowInfo> findShowInfoByTitle(String title);
}
