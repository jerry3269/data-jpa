package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Rollback(value = false)
@Transactional
class MemberJpaRepositoryTest {

    @Autowired private MemberJpaRepository memberJpaRepository;

    @Test
    public void testMember(){
        //given
        Member member = new Member("memberA");
        Member savedMember = memberJpaRepository.save(member);

        //when
        Member findMember = memberJpaRepository.findById(member.getId());

        //then
        assertThat(savedMember.getId()).isEqualTo(findMember.getId());
        assertThat(savedMember).isEqualTo(findMember);
    }

}