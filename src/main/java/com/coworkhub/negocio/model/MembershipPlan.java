package com.coworkhub.negocio.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "membership_plans")
public class MembershipPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(nullable = false)
    private int includedHoursPerMonth;

    /** Porcentaje (0-100) de descuento sobre la tarifa de las horas excedentes. */
    @Column(nullable = false)
    private int overageDiscount;

    @Column(nullable = false)
    private int maxActiveBookings;

    /** Lado inverso: el duenio es Member.plan. */
    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY)
    private List<Member> members = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getIncludedHoursPerMonth() { return includedHoursPerMonth; }
    public void setIncludedHoursPerMonth(int includedHoursPerMonth) { this.includedHoursPerMonth = includedHoursPerMonth; }
    public int getOverageDiscount() { return overageDiscount; }
    public void setOverageDiscount(int overageDiscount) { this.overageDiscount = overageDiscount; }
    public int getMaxActiveBookings() { return maxActiveBookings; }
    public void setMaxActiveBookings(int maxActiveBookings) { this.maxActiveBookings = maxActiveBookings; }
    public List<Member> getMembers() { return members; }
}
