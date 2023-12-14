package com.thingspire.thingspire.user.repository;

import com.thingspire.thingspire.user.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional <Member> findByEmail(String email);
    Optional<Member> findByName(String name);
    Optional<Member> findByLoginId(String loginId);
    List<Member> findAllByOrderByCreatedTimeDesc();

    Optional<Member> findMemberIdByLoginId(String loginId);

    @Query("SELECT m FROM Member m ORDER BY m.createdTime DESC, m.modifiedTime DESC")
    List<Member> findAllByOrderByCreatedAtAndModifiedTime();
}
