package quix.web

import quix.core.utils.JsonOps.Implicits.global
import quix.core.utils.StringJsonHelpersSupport
import scalaj.http.Http

trait E2EContext extends StringJsonHelpersSupport {

  def get[T: Manifest](url: String): T = {
    Http("http://localhost:8888/" + url).asString.body.as[T]
  }

  def getResponse(url: String) = {
    Http("http://localhost:8888/" + url).asString
  }
}