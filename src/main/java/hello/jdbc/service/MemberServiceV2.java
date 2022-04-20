package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 트랜잭션 - 파라미터 연동, pool을 고려한 종료
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

  private final DataSource dataSource;
  private final MemberRepositoryV2 memberRepository;

  public void accountTransfer(String fromId, String toId, int money) throws SQLException {
    Connection con = dataSource.getConnection();

    try {
      con.setAutoCommit(false); // 트랜잭션 시작

      // 트랜잭션이 시작된 커넥션을 전달하면서 비즈니스 로직을 수행
      bizLogic(con, fromId, toId, money);
      con.commit(); // 성공시 커밋
    } catch (Exception e) {
      con.rollback(); // 실패시 롤백
      throw new IllegalStateException(e);
    } finally {
      release(con);
    }

  }

  private void bizLogic(Connection con, String fromId, String toId, int money
  ) throws SQLException {
    Member fromMember = memberRepository.findById(con, fromId);
    Member toMember = memberRepository.findById(con, toId);

    memberRepository.update(con, fromId, fromMember.getMoney() - money);
    validation(toMember); // 예외 발생
    memberRepository.update(con, toId, toMember.getMoney() + money);
  }

  private void release(Connection con) {
    if (con != null) {
      try {
        con.setAutoCommit(true); // AutoCommit false 였기 때문에 true로 재 설정 후 풀에 반납
        con.commit();
      } catch (Exception e) {
        log.info("error", e);
      }
    }
  }

  private void validation(Member toMember) {
    if (Objects.equals(toMember.getMemberId(), "ex")) {
      throw new IllegalStateException("이체중 예외 발생");
    }
  }

}
