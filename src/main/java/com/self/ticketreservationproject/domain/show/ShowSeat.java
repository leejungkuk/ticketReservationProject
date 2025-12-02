package com.self.ticketreservationproject.domain.show;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "show_seat")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShowSeat {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "show_schedule_id", nullable = false)
  private ShowSchedule showSchedule;

  @Column(name = "seat_num", nullable = false)
  private String seatNumber;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private SeatStatus status;

  private int price;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  public void assignToSchedule(ShowSchedule schedule) {
    this.showSchedule = schedule;
  }

  public ShowSeat (String seatNumber, int price, SeatStatus status) {
    this.seatNumber = seatNumber;
    this.status = status;
    this.price = price;
  }

  public static ShowSeat create(String seatNumber, int price) {
    return new ShowSeat(seatNumber, price, SeatStatus.AVAILABLE);
  }

}
