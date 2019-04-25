package quix.core.results

import monix.eval.Task
import monix.execution.Scheduler
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope
import quix.api.execute._
import quix.api.users.User

import scala.collection.mutable.ListBuffer

class MultiBuilderTest extends SpecWithJUnit {

  class ctx extends Scope {
    val consumer = new TestConsumer[ExecutionEvent]
    val builder = new MultiBuilder(consumer)
    val query = ActiveQuery("id", "text", 1, User("test"), isCancelled = false, Map.empty)
  }

  "MultiBuilder.start" should {
    "send Start event on start(query)" in new ctx {
      builder.start(query).runToFuture(Scheduler.global)

      eventually {
        consumer.payloads must contain(Start(query.id, query.numOfQueries))
      }
    }
  }

  "MultiBuilder.end" should {

    "send End event on end(query)" in new ctx {
      builder.end(query).runToFuture(Scheduler.global)

      eventually {
        consumer.payloads must contain(End(query.id))
      }
    }
  }

  "MultiBuilder.startSubQuery" should {

    "send StartQuery & SubQueryDetails event on every startSubQuery(queryId, code, results)" in new ctx {
      builder.startSubQuery("query-id", "code", Batch(List.empty)).runToFuture(Scheduler.global)

      eventually {
        consumer.payloads must contain(SubQueryStart("query-id"))
        consumer.payloads must contain(SubQueryDetails("query-id", "code"))
      }
    }

    "send SubQueryFields if columns are present in results during startSubQuery(queryId, code, results)" in new ctx {
      val batch = Batch(List.empty, Option(List(BatchColumn("column"))))
      builder.startSubQuery("query-id", "code", batch).runToFuture(Scheduler.global)

      eventually {
        consumer.payloads must contain(SubQueryFields("query-id", List("column")))
      }
    }

    "send SubQueryFields only once if columns are present in results during startSubQuery(queryId, code, results)" in new ctx {
      val batch = Batch(List.empty, Option(List(BatchColumn("column"))))
      builder.startSubQuery("query-id", "code", batch).runToFuture(Scheduler.global)
      builder.startSubQuery("query-id", "code", batch).runToFuture(Scheduler.global)

      eventually {
        consumer.payloads.filter(_ == SubQueryFields("query-id", List("column"))) must haveSize(1)
      }
    }

    "send Progress if present in results during startSubQuery(queryId, code, results)" in new ctx {
      val batch = Batch(List.empty, stats = Option(BatchStats("state", 100)))
      builder.startSubQuery("query-id", "code", batch).runToFuture(Scheduler.global)

      eventually {
        consumer.payloads must contain(Progress("query-id", 100))
      }
    }

    "send Progress on every batch during startSubQuery(queryId, code, results)" in new ctx {
      val batch = Batch(List.empty, stats = Option(BatchStats("state", 100)))
      builder.startSubQuery("query-id", "code", batch).runToFuture(Scheduler.global)
      builder.startSubQuery("query-id", "code", batch).runToFuture(Scheduler.global)

      eventually {
        consumer.payloads.filter(_ == Progress("query-id", 100)) must haveSize(2)
      }
    }

    "send Error if present in results during startSubQuery(queryId, code, results)" in new ctx {
      val batch = Batch(List.empty, error = Option(BatchError("boom!")))
      builder.startSubQuery("query-id", "code", batch).runToFuture(Scheduler.global)

      eventually {
        consumer.payloads must contain(Error("query-id", "boom!"))
      }
    }

    "send Row events if data is present in results during startSubQuery(queryId, code, results)" in new ctx {
      val batch = Batch(List(List("a", "b", "c"), List("d", "e", "f")))
      builder.startSubQuery("query-id", "code", batch).runToFuture(Scheduler.global)

      eventually {
        consumer.payloads must contain(Row("query-id", List("a", "b", "c")))
        consumer.payloads must contain(Row("query-id", List("d", "e", "f")))
      }
    }
  }

  "MultiBuilder.endSubQuery" should {

    "send SubQueryEnd event on endSubQuery(query)" in new ctx {
      builder.endSubQuery(query.id).runToFuture(Scheduler.global)

      eventually {
        consumer.payloads must contain(SubQueryEnd(query.id))
      }
    }
  }

}

class TestConsumer[T] extends Consumer[T] {
  val payloads = ListBuffer.empty[T]

  override def id: String = "id"

  override def user: User = User("test")

  override def write(payload: T): Task[Unit] = Task(payloads += payload)

  override def close(): Task[Unit] = Task.unit
}