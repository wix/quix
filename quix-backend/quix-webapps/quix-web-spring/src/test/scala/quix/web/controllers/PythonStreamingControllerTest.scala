package quix.web.controllers

import com.typesafe.scalalogging.LazyLogging
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{TestContextManager, TestPropertySource}
import quix.web.controllers.MyMatchers._
import quix.web.{E2EContext, SpringConfigWithTestExecutor}

@RunWith(classOf[SpringRunner])
@DirtiesContext
@SpringBootTest(webEnvironment = DEFINED_PORT, classes = Array(classOf[SpringConfigWithTestExecutor]))
@TestPropertySource(locations = Array("classpath:test.properties"))
class PythonStreamingControllerTest extends E2EContext with LazyLogging {

  new TestContextManager(this.getClass).prepareTestInstance(this)

  @Test
  def passSanity(): Unit = {
    val listener = execute("print(123)", module = "snake")

    assertThat(listener.messagesJ, hasEvent("""{"event":"start","data":{"id":"query-id","numOfQueries":1}}"""))
    assertThat(listener.messagesJ, hasEvent("""{"event":"query-start","data":{"id":"query-id"}}"""))
    assertThat(listener.messagesJ, hasEvent("""{"event":"log","data":{"id":"query-id","line":"done installing modules ['py4j']","level":"INFO"}}"""))
    assertThat(listener.messagesJ, hasEvent("""{"event":"log","data":{"id":"query-id","line":"123","level":"INFO"}}"""))
    assertThat(listener.messagesJ, hasEvent("""{"event":"query-end","data":{"id":"query-id"}}"""))
    assertThat(listener.messagesJ, hasEvent("""{"event":"end","data":{"id":"query-id"}}"""))
  }

  @Test
  def installAndUseCustomModule(): Unit = {
    val listener = execute(sql =
      """import numpy as np
        |a = np.arange(6)
        |print(a)
        |""".stripMargin, session = Map("packages" -> "numpy"), module = "snake")

    assertThat(listener.messagesJ, hasEvent("""{"event":"start","data":{"id":"query-id","numOfQueries":1}}"""))
    assertThat(listener.messagesJ, hasEvent("""{"event":"query-start","data":{"id":"query-id"}}"""))
    assertThat(listener.messagesJ, hasEvent("""{"event":"log","data":{"id":"query-id","line":"done installing modules ['py4j', 'numpy']","level":"INFO"}}"""))
    assertThat(listener.messagesJ, hasEvent("""{"event":"log","data":{"id":"query-id","line":"[0 1 2 3 4 5]","level":"INFO"}}"""))
    assertThat(listener.messagesJ, hasEvent("""{"event":"query-end","data":{"id":"query-id"}}"""))
    assertThat(listener.messagesJ, hasEvent("""{"event":"end","data":{"id":"query-id"}}"""))
  }


}
