package hello.jdbc.service;

import static hello.jdbc.connection.ConnectionConst.PASSWORD;
import static hello.jdbc.connection.ConnectionConst.URL;
import static hello.jdbc.connection.ConnectionConst.USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@Slf4j
class MemberServiceV3_1Test {

  public static final String MEMBER_A = "memberA";
  public static final String MEMBER_B = "memberB";
  public static final String MEMBER_EX = "ex";

  private MemberRepositoryV3 memberRepository;
  private MemberServiceV3_1 memberService;

  @BeforeEach
  void before() {
    DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
    memberRepository = new MemberRepositoryV3(dataSource);
    PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
    memberService = new MemberServiceV3_1(transactionManager, memberRepository);
  }

  @AfterEach
  void after() throws SQLException {
    memberRepository.delete(MEMBER_A);
    memberRepository.delete(MEMBER_B);
    memberRepository.delete(MEMBER_EX);
  }

  @DisplayName("정상 이체")
  @Test
  void accountTransfer() throws SQLException {
    Member memberA = new Member(MEMBER_A, 10000);
    Member memberB = new Member(MEMBER_B, 10000);
    memberRepository.save(memberA);
    memberRepository.save(memberB);

    memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

    Member findMemberA = memberRepository.findById(memberA.getMemberId());
    Member findMemberB = memberRepository.findById(memberB.getMemberId());

    assertThat(findMemberA.getMoney()).isEqualTo(8000);
    assertThat(findMemberB.getMoney()).isEqualTo(12000);
  }

  @DisplayName("이체중 예외 발생")
  @Test
  void accountTransferEx() throws SQLException {
    Member memberA = new Member(MEMBER_A, 10000);
    Member memberEx = new Member(MEMBER_EX, 10000);
    memberRepository.save(memberA);
    memberRepository.save(memberEx);

    assertThatThrownBy(
        () -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
        .isInstanceOf(IllegalStateException.class);

    Member findMemberA = memberRepository.findById(memberA.getMemberId());
    Member findMemberB = memberRepository.findById(memberEx.getMemberId());

    // 롤백되어 처음 상태로 돌아간다.
    assertThat(findMemberA.getMoney()).isEqualTo(10000);
    assertThat(findMemberB.getMoney()).isEqualTo(10000);
  }

}