package com.coworkhub.negocio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String documentNumber;

    @Column(nullable = false, length = 120)
    private String fullName;

    /** Se guarda en minusculas para que la unicidad no distinga mayusculas (RN-11). */
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status = MemberStatus.ACTIVE;

    /** Lado duenio de MembershipPlan 1 -> N Member. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan plan;

    /**
     * Lado duenio del 1 <-> 1 con UserAccount: FK user_account_id unica y nullable (un miembro puede no tener cuenta).
     * La cuenta pertenece al miembro: se crea y se borra con el (cascade PERSIST/REMOVE + orphanRemoval).
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "user_account_id", unique = true)
    private UserAccount userAccount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public MemberStatus getStatus() { return status; }
    public void setStatus(MemberStatus status) { this.status = status; }
    public MembershipPlan getPlan() { return plan; }
    public void setPlan(MembershipPlan plan) { this.plan = plan; }
    public UserAccount getUserAccount() { return userAccount; }

    /** Mantiene sincronizados los dos lados de la relacion 1 <-> 1. */
    public void vincularCuenta(UserAccount cuenta) {
        this.userAccount = cuenta;
        if (cuenta != null) {
            cuenta.setMember(this);
        }
    }
}
