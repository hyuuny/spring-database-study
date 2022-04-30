package hello.jdbc.exception.basic;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class UnCheckedTest {

  @Test
  void unchecked_catch() {
    Service service = new Service();
    service.callCatch();
  }

  @Test
  void unchecked_throw() {
    Service service = new Service();
    assertThatThrownBy(() -> service.callThrow())
        .isInstanceOf(MyUncheckedException.class);
  }

  /**
   * RunTimeException을 상속받은 예외는 언체크 예외가 된다.
   */
  static class MyUncheckedException extends RuntimeException {

    public MyUncheckedException(String message) {
      super(message);
    }
  }

  static class Service {

    Repository repository = new Repository();

    /**
     * 필요한 경우 예외를 잡아서 처리
     */
    public void callCatch() {
      try {
        repository.call();
      } catch (MyUncheckedException e) {
        log.info("예외 처리, message={}", e.getMessage());
      }
    }

    /**
     * 예외를 잡지 않아도 자연스럽게 상위로 넘어간다.
     */
    public void callThrow() {
      repository.call();
    }
  }

  static class Repository {

    public void call() {
      throw new MyUncheckedException("ex");
    }
  }


}
