package com.coworkhub.persistence.repository;

import com.coworkhub.negocio.model.Member;
import com.coworkhub.negocio.model.MemberStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @EntityGraph(attributePaths = {"plan", "userAccount"})
    List<Member> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = {"plan", "userAccount"})
    List<Member> findByStatusOrderByIdAsc(MemberStatus status);

    @EntityGraph(attributePaths = {"plan", "userAccount"})
    List<Member> findByFullNameContainingIgnoreCaseOrderByIdAsc(String fullName);

    @EntityGraph(attributePaths = {"plan", "userAccount"})
    List<Member> findByFullNameContainingIgnoreCaseAndStatusOrderByIdAsc(String fullName, MemberStatus status);

    @EntityGraph(attributePaths = {"plan", "userAccount"})
    Optional<Member> findWithDetailsById(Long id);

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByPlanId(Long planId);
}
