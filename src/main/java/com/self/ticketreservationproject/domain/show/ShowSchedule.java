package com.self.ticketreservationproject.domain.show;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "show_schedule")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShowSchedule {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "show_id")
  private ShowInfo showInfo;

  @Column(nullable = false)
  private LocalDateTime startTime;

  @Column(name = "created_at", updatable = false)
  @CreatedDate
  private LocalDateTime createdAt;

  @OneToMany(mappedBy = "showSchedule", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ShowSeat> seats = new ArrayList<>();

  @Builder
  public ShowSchedule(LocalDateTime startTime) {
    this.startTime = startTime;
  }

  public void assignToInfo(ShowInfo showInfo) {
    this.showInfo = showInfo;
  }

  public void addSeat(ShowSeat seat) {
    seats.add(seat);
    seat.assignToSchedule(this);
  }

  public void generateSeats(List<String> seatNumbers, int defaultPrice) {
    for(String seatNumber : seatNumbers) {
      ShowSeat seat = ShowSeat.create(seatNumber, defaultPrice);
      addSeat(seat);
    }
  }

}
